package com.liguang.imageloaderdemo.album.cache;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;

import java.util.Set;

public class MemoryCache {
    LruCache<String, Bitmap> mMemoryCache;
    Set<OOMSoftReference<Bitmap>> mReuseableBitmaps;
}
