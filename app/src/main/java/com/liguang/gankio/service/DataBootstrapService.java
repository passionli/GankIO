package com.liguang.gankio.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.liguang.gankio.R;

import java.io.InputStream;

/**
 * 数据初始化服务。负责把raw/json数据写入数据库，避免第一次打开App空白页面这种糟糕的用户体验。
 * <p>
 */
public class DataBootstrapService extends IntentService {
    private static final String ACTION_DATA_BOOTSTRAP = "com.liguang.gankio.service.action.DATA_BOOTSTRAP";

    public DataBootstrapService() {
        super("DataBootstrapService");
    }

    /**
     * Starts this service to perform action DATA_BOOTSTRAP. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startDataBootstrap(Context context) {
        Intent intent = new Intent(context, DataBootstrapService.class);
        intent.setAction(ACTION_DATA_BOOTSTRAP);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DATA_BOOTSTRAP.equals(action)) {
                handleActionDataBootstrap();
            }
        }
    }

    /**
     * Handle action DataBootstrap in the provided background thread
     */
    private void handleActionDataBootstrap() {
        //read done flag
        //doing? 由于是串行执行，故不可能出现running的情况
        //start DataBootstrap
        //load json to buffer
//        InputStream is = getResources().openRawResource(R.raw.bootstrap);
        //write buffer to db
        //write done flag
    }
}
