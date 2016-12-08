package com.liguang.imageloaderdemo.network;

import java.util.Locale;

public class URLHelper {
    public static final String PREFIX = "http://gank.io/api/data/";
    public static final char SEPERATOR = '/';
    private static final String IMAGE_URL_FORMAT = "%s?imageView2/0/w/%d";

    public static String createImageUrlWithWidth(String url, int width) {
        return String.format(Locale.CHINESE, IMAGE_URL_FORMAT, url, width);
    }

}
