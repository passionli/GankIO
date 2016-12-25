package com.liguang.gankio.network;

import hugo.weaving.DebugLog;

public class URLHelper {
    public static final String PREFIX = "http://gank.io/api/data/";
    public static final char SEPERATOR = '/';
    private static final String IMAGE_URL_MIDDLE = "?imageView2/0/w/";

    @DebugLog
    public static String createImageUrlWithWidth(String url, int width) {
        return url + IMAGE_URL_MIDDLE + width;
    }
}
