package com.liguang.gankio;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Looper;
import android.util.Printer;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.frogermcs.androiddevmetrics.AndroidDevMetrics;
import com.github.moduth.blockcanary.BlockCanary;
import com.github.moduth.blockcanary.BlockCanaryContext;
import com.liguang.gankio.album.Utils;
import com.liguang.gankio.data.ItemsRepository;
import com.liguang.gankio.service.DataBootstrapService;
import com.liguang.gankio.util.Injection;
import com.liguang.gankio.util.LGLog;
import com.squareup.leakcanary.LeakCanary;

import hugo.weaving.DebugLog;

public class GankIOApplication extends Application {
    private static final String TAG = "GankIOApplication";
    private static Context sAppContext;
    static BlockCanaryContext sBlockCanaryContext;

    @DebugLog
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
        DataBootstrapService.startDataBootstrap(getApplicationContext());
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
        LGLog.enableWriteLogFile(true);
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
