package com.liguang.imageloaderdemo.db;

import android.net.Uri;
import android.provider.BaseColumns;

public class GankIoContract {
    /**
     * Query parameter to create a distinct query.
     */
    public static final String QUERY_PARAMETER_DISTINCT = "distinct";

    public static final String CONTENT_AUTHORITY = "com.liguang.imageloaderdemo";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://"
            + CONTENT_AUTHORITY);

    //    private static final String PATH_USER = "user";
    private static final String PATH_ITEM = "item";

    public static final String[] TOP_LEVEL_PATHS = {PATH_ITEM};

//    interface UserColumns {
//        String USER_ID = "user_id";
//        String USER_NAME = "user_name";
//        /** Last time this entry was updated */
//        String UPDATED = "updated";
//    }

    interface ItemColumns {
        String SERVER_ID = "server_id";
        String CREATEDAT = "createdAt";
        String DESC = "desc";
        String IMAGES = "images";
        String PUBLISHEDAT = "publishedAt";
        String SOURCE = "source";
        String TYPE = "type";
        String URL = "url";
        String USED = "used";
        String WHO = "who";
    }

    public static class Item implements ItemColumns, BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_ITEM).build();

        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/item";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/item";

        public static Uri buildItemUri(String itemId) {
            return CONTENT_URI.buildUpon().appendPath(itemId).build();
        }

        public static Uri buildItemUri(String tag, int pageCount, int page) {
            return CONTENT_URI.buildUpon()
                    .appendPath(tag)
                    .appendPath(String.valueOf(pageCount))
                    .appendPath(String.valueOf(page))
                    .build();
        }

        public static String getItemId(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static String getItemTag(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static int getItemPageCount(Uri uri) {
            return Integer.valueOf(uri.getPathSegments().get(2));
        }

        public static int getItemPage(Uri uri) {
            return Integer.valueOf(uri.getPathSegments().get(3));
        }
    }

    private GankIoContract() {
    }
}
