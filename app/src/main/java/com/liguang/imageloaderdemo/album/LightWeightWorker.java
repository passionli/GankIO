package com.liguang.imageloaderdemo.album;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;

public class LightWeightWorker {
    private static final String TAG = "LightWeightWorker";
    static Handler sHandler;
    static HandlerThread sHandlerThread;

    public static void init() {
        getLightWeightWorkerHandler();
    }

    public static final Handler getLightWeightWorkerHandler() {
        if (sHandler == null) {
            synchronized (LightWeightWorker.class) {
                if (sHandler == null) {
                    sHandlerThread = new HandlerThread(TAG);
                    sHandlerThread.start();
                    sHandler = new Handler(sHandlerThread.getLooper());
                }
            }
        }
        return sHandler;
    }

    public static void destroy() {
        if (sHandler != null) {
            sHandler.removeCallbacksAndMessages(null);
        }
        if (sHandlerThread != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                sHandlerThread.quitSafely();
            } else {
                sHandlerThread.quit();
            }
            sHandlerThread = null;
        }
    }
}
