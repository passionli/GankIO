package com.liguang.imageloaderdemo;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.github.moduth.blockcanary.BlockCanary;
import com.github.moduth.blockcanary.BlockCanaryContext;
import com.liguang.imageloaderdemo.album.LightWeightWorker;
import com.liguang.imageloaderdemo.util.Utils;

public class MyApplication extends Application {
    private static Context sAppContext;
    static BlockCanaryContext sBlockCanaryContext;

    @Override
    public void onCreate() {
        super.onCreate();
        com.liguang.imageloaderdemo.album.Utils.enableStrictMode();
        Utils.copyDB2SDCard(this);

        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(getApplicationContext())
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();
//        DraweeConfig draweeConfig = DraweeConfig.newBuilder()
//
//                .build();
        Fresco.initialize(this);
        FLog.setMinimumLoggingLevel(FLog.VERBOSE);
        sAppContext = getApplicationContext();
        sBlockCanaryContext = new AppBlockCanaryContext();
        BlockCanary.install(this, sBlockCanaryContext).start();
        LightWeightWorker.init();
    }

    public static Context getAppContext() {
        return sAppContext;
    }
}
