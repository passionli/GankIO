package com.liguang.imageloaderdemo.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class GankIoDatabase extends SQLiteOpenHelper {
    private static final String TAG = GankIoDatabase.class
            .getSimpleName();
    private static final String DATABASE_NAME = "gankio_by_liguang.db";

    private static final int VER_2016_BETA_A = 100;
//    private static final int VER_2016_BETA_B = 101;

    // 每次数据库需要更新时，设置新的版本号
    private static final int CUR_DATABASE_VERSION = VER_2016_BETA_A;

    interface Tables {
        String ITEM = "item";
    }

    public GankIoDatabase(Context context) {
        super(context, DATABASE_NAME, null, CUR_DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate: ");
        db.execSQL("CREATE TABLE " + Tables.ITEM + " (" + BaseColumns._ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT, " + GankIoContract.Item.SERVER_ID
                + " TEXT NOT NULL UNIQUE ON CONFLICT REPLACE, "
                + GankIoContract.Item.CREATEDAT + " TEXT, " + GankIoContract.Item.DESC
                + " TEXT , " + GankIoContract.Item.IMAGES
                + " TEXT , " + GankIoContract.Item.PUBLISHEDAT
                + " TEXT , " + GankIoContract.Item.SOURCE + " TEXT, "
                + GankIoContract.Item.TYPE + " TEXT, "
                + GankIoContract.Item.URL + " TEXT, "
                + GankIoContract.Item.USED + " INTEGER, " + GankIoContract.Item.WHO
                + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i(TAG, "onUpgrade oldVersion = " + oldVersion + "#newVersion="
                + newVersion);
        // TODO 版本升级时数据迁移工作
        db.execSQL("DROP TABLE IF EXISTS " + Tables.ITEM);

        onCreate(db);
    }
}