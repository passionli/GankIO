package com.liguang.imageloaderdemo.album;

import android.util.Log;

/**
 * 用于在主线程显示Bitmap
 */
public class Display implements Runnable {
    private static final String TAG = "Display";
    ImageLoader.LoaderResult result;

    public Display(ImageLoader.LoaderResult result) {
        this.result = result;
    }

    @Override
    public void run() {
        //这个ImageView对象的里面记的是主线程用户最后需要的URI
        String currentUri = (String) result.imageView.getTag(ImageLoader.TAG_KEY_URI);
        //ImageView对象的当前请求Uri可能已经由于复用原因被上层修改
        if (result.lastUri.equals(currentUri)) {
            //这行代码需要在主线程中执行
            result.imageView.setImageDrawable(result.bitmap);
            result.listener.onLoadComplete(result.imageView);
        } else {
            Log.w(TAG, "handleMessage: 下层异步结果回来后，上层ImageView的URI已经改变");
        }
    }
}
