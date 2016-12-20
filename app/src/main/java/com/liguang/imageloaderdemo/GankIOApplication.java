package com.liguang.imageloaderdemo;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.frogermcs.androiddevmetrics.AndroidDevMetrics;
import com.github.moduth.blockcanary.BlockCanary;
import com.github.moduth.blockcanary.BlockCanaryContext;
import com.liguang.imageloaderdemo.album.Utils;
import com.liguang.imageloaderdemo.data.ItemsRepository;
import com.liguang.imageloaderdemo.util.Injection;
import com.squareup.leakcanary.LeakCanary;

public class GankIOApplication extends Application {
    private static final String TAG = "GankIOApplication";
    private static Context sAppContext;
    static BlockCanaryContext sBlockCanaryContext;

    @Override
    public void onCreate() {
        super.onCreate();
        setupDebugMode();
        sAppContext = getApplicationContext();
        setupFresco();
        setupBlockCanary();
        setupLowLevel();
    }

    private void setupLowLevel() {
        ItemsRepository repository = Injection.provideItemsRepository(getApplicationContext());
        repository.setup();
    }

    private void setupBlockCanary() {
        sBlockCanaryContext = new AppBlockCanaryContext();
        BlockCanary.install(this, sBlockCanaryContext).start();
    }

    private void setupFresco() {
        ImagePipelineConfig imagePipelineConfig = ImagePipelineConfig.newBuilder(getApplicationContext())
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();
        Fresco.initialize(this, imagePipelineConfig, null);
        FLog.setMinimumLoggingLevel(FLog.VERBOSE);
    }

    private void setupDebugMode() {
        Utils.enableStrictMode();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);
        //Use it only in debug builds
        if (BuildConfig.DEBUG) {
            AndroidDevMetrics.initWith(this);
        }
    }

    public static Context getAppContext() {
        return sAppContext;
    }
}
