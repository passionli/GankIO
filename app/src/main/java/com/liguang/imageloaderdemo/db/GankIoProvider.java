package com.liguang.imageloaderdemo.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Arrays;

/**
 * 可能被上层多个客户端使用，需要处理多线程问题。线程是Binder线程池中的线程。最大16个。
 **/
public class GankIoProvider extends ContentProvider {
    private static final String TAG = GankIoProvider.class
            .getSimpleName();

    //底层采用SQLite
    private SQLiteOpenHelper mOpenHelper;
    //匹配客户端发起的Uri
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int USER = 100;
    private static final int USER_ID = 101;
    private static final int USER_ID_DEVICE = 102;

    private static final int ITEM = 200;
    private static final int ITEM_TAG = 201;
    private static final int ITEM_TAG_ID = 202;
    //http://gank.io/api/data/Android/10/1
    private static final int ITEM_TAG_PAGECOUNT_PAGE = 203;

    @Override
    public boolean onCreate() {
        mOpenHelper = new GankIoDatabase(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM_TAG_PAGECOUNT_PAGE:
                return GankIoContract.Item.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * Returns a tuple of question marks. For example, if count is 3, returns
     * "(?,?,?)".
     */
    private String makeQuestionMarkTuple(int count) {
        if (count < 1) {
            return "()";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(?");
        for (int i = 1; i < count; i++) {
            stringBuilder.append(",?");
        }
        stringBuilder.append(")");

        return stringBuilder.toString();
    }

    /**
     * Build an advanced {@link SelectionBuilder} to match the requested
     * {@link Uri}. This is usually only used by {@link #query}, since it
     * performs table joins useful for {@link Cursor} data.
     */
    private SelectionBuilder buildExpandedSelection(Uri uri, int match) {
        final SelectionBuilder builder = new SelectionBuilder();
        switch (match) {
            case ITEM: {
                return builder.table(GankIoDatabase.Tables.ITEM);
            }
            case ITEM_TAG_ID: {
                final String item_server_id = GankIoContract.Item.getItemId(uri);
                return builder.table(GankIoDatabase.Tables.ITEM).where(GankIoContract.Item.SERVER_ID + "=?",
                        item_server_id);
            }
            case ITEM_TAG_PAGECOUNT_PAGE: {
                String tag = GankIoContract.Item.getItemTag(uri);
                int pageCount = GankIoContract.Item.getItemPageCount(uri);
                int page = GankIoContract.Item.getItemPage(uri);
                return builder.table(GankIoDatabase.Tables.ITEM).where(GankIoContract.Item.TYPE + "=?",
                        tag).limit(pageCount, page);
            }
            default: {
                throw new UnsupportedOperationException("Unknown uri: " + uri);
            }
        }
    }

    private SelectionBuilder buildSimpleSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM: {
                return builder.table(GankIoDatabase.Tables.ITEM);
            }
//            case DEVICE_ID: {
//                final String item_server_id = GankIoContract.Item.getItemId(uri);
//                return builder.table(GankIoDatabase.Tables.ITEM).where(GankIoContract.Item.SERVER_ID + "=?",
//                        item_server_id);
//            }
            default: {
                throw new UnsupportedOperationException("Unknown uri for " + match
                        + ": " + uri);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        Log.d(TAG,
                "uri=" + uri + " match=" + match + " proj="
                        + Arrays.toString(projection) + " selection="
                        + selection + " args=" + Arrays.toString(selectionArgs)
                        + ")");
        final SelectionBuilder builder = buildExpandedSelection(uri, match);
        boolean distinct = !TextUtils
                .isEmpty(uri
                        .getQueryParameter(GankIoContract.QUERY_PARAMETER_DISTINCT));
        //构造SQL语句，交给下层SQL数据库处理
        Cursor cursor = builder.where(selection, selectionArgs).query(db,
                distinct, projection, sortOrder, null);
        Context context = getContext();
        if (null != context) {
            cursor.setNotificationUri(context.getContentResolver(), uri);
        }

        return cursor;
    }

    //Uri发生改变时，通知上层UI，以便刷新UI的数据源
    void notifyChange(Uri uri) {
        Context context = getContext();
        if (null != context) {
            context.getContentResolver().notifyChange(uri, null);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.d(TAG, "insert(uri=" + uri + ", values=" + values.toString());
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM: {
                db.insertOrThrow(GankIoDatabase.Tables.ITEM, null, values);
                notifyChange(uri);
                return GankIoContract.Item.CONTENT_URI;
            }
            case ITEM_TAG_ID: {
                db.insertOrThrow(GankIoDatabase.Tables.ITEM, null, values);
                notifyChange(uri);
                return GankIoContract.Item.buildItemUri(values.getAsString(GankIoContract.Item.SERVER_ID));
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: "
                        + uri);
            }
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).delete(db);
        notifyChange(uri);
        return retVal;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "update(uri=" + uri + ", values=" + values.toString());
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final SelectionBuilder builder = buildSimpleSelection(uri);
        int retVal = builder.where(selection, selectionArgs).update(db, values);
        notifyChange(uri);
        return retVal;
    }

    /**
     * Build and return a {@link UriMatcher} that catches all {@link Uri}
     * variations supported by this {@link ContentProvider}.
     */
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = GankIoContract.CONTENT_AUTHORITY;

        //http://gank.io/api/data/
        matcher.addURI(authority, "item", ITEM);
        //http://gank.io/api/data/TAG/
        matcher.addURI(authority, "item/*", ITEM_TAG);
        matcher.addURI(authority, "item/*/*", ITEM_TAG_ID);
        //http://gank.io/api/data/Android/10/1
        matcher.addURI(authority, "item/*/*/*", ITEM_TAG_PAGECOUNT_PAGE);

        return matcher;
    }
}
