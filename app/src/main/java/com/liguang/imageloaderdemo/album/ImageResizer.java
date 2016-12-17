package com.liguang.imageloaderdemo.album;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

public class ImageResizer {
    private static final String TAG = ImageResizer.class.getSimpleName();
    private ImageLoader mImageLoader;

    public ImageResizer(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        //默认开启最小内存颜色模式
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        if (Utils.hasHoneycomb()) {
            addBitmapOptions(options);
        }

        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        Log.d(TAG, "calculateInSampleSize: reqWidth=" + reqWidth + " reqHeight=" + reqHeight);
        int inSampleSize = 1;

        //图片原始宽高
        final int height = options.outHeight;
        final int width = options.outWidth;

        //原始宽高其中一个比要求的宽高大时，需要进行缩放
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize <<= 1;
            }
        }

        return inSampleSize;
    }

    public Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        //默认开启最小内存颜色模式
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        if (Utils.hasHoneycomb()) {
            addBitmapOptions(options);
        }

        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    private void addBitmapOptions(BitmapFactory.Options options) {
        options.inMutable = true;
        options.inBitmap = mImageLoader.getBitmapFromReusableSet(options);
    }

    public static Bitmap decodeBitmapFromAssets(Context context, String fileName, int reqWidth, int reqHeight) throws IOException {
        InputStream is = context.getAssets().open(fileName);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(is, null, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        //默认开启565颜色模式
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(is, null, options);
    }
}
