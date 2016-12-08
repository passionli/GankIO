package com.liguang.imageloaderdemo.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OkHttpHL {
    private static final String TAG = "OkHttpHL";
    private static Handler sUIHandler = new Handler(Looper.getMainLooper());
    public static final MediaType JSON = MediaType.parse("application/json;charset=utf-8");
    OkHttpClient client = new OkHttpClient();

    public Call run(String url, final HttpManager.Callback callback) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: ");
                final String error = e.toString();
                sUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onFailure(error);
                    }
                });
            }

            @Override
            public void onResponse(final Call call, final Response response) throws IOException {
                Log.d(TAG, "onResponse: ");
                sUIHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callback.onResponse(com.alibaba.fastjson.JSON.parseObject(response.body().string(), callback.mType));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return call;
    }

    public String post(String url, String json) throws IOException {
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder().url(url).post(body).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
