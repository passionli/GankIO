package com.liguang.imageloaderdemo.album;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;

public class CountingBitmapDrawable extends BitmapDrawable {
    private static final String TAG = "CountingBitmapDrawable";
    private int mDisplayRefCount = 0;
    private int mCacheRefCount = 0;
    private boolean mHasBeenDisplayed;

    public CountingBitmapDrawable(Resources res, Bitmap bitmap) {
        super(res, bitmap);
    }

    public void setIsDisplayed(boolean isDisplayed){
        //主线程
        synchronized (this){
            if (isDisplayed){
                mDisplayRefCount++;
                mHasBeenDisplayed = true;
            }else {
                mDisplayRefCount--;
            }
        }

        checkState();
    }

    public void setIsCached(boolean isCached){
        //工作线程
        synchronized (this){
            if (isCached){
                mCacheRefCount++;
            }else {
                mCacheRefCount--;
            }
        }

        checkState();
    }

    private synchronized void checkState() {
        if (mDisplayRefCount <= 0 && mCacheRefCount<=0 && mHasBeenDisplayed && hasValidBitmap()){
            Log.d(TAG, "checkState: No longer being display or cached so recycling bitmap");
            getBitmap().recycle();
        }
    }

    private synchronized boolean hasValidBitmap() {
        Bitmap bitmap = getBitmap();
        return bitmap != null && !bitmap.isRecycled();
    }
}
