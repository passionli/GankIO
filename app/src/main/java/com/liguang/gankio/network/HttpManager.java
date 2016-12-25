package com.liguang.gankio.network;

import android.util.Log;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 同步接口
 */
public class HttpManager {
    private static final String TAG = "HttpManager";
    private OkHttpClient mClient;

    private HttpManager() {
        mClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    public String doGet(String url) {
        Log.d(TAG, "doGet: " + url);
        Request request = new Request.Builder().url(url).build();
        Call call = mClient.newCall(request);
        try {
            Response response = call.execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void doPost() {

    }

    private static class HttpManagerHolder {
        private static final HttpManager sInstance = new HttpManager();
    }

    public static HttpManager getInstance() {
        return HttpManagerHolder.sInstance;
    }

    public static abstract class Callback<T> {
        Type mType;

        public Callback() {
            mType = getSuperclassTypeParameter(getClass());
        }

        static Type getSuperclassTypeParameter(Class<?> subclass) {
            Type superclass = subclass.getGenericSuperclass();
            if (superclass instanceof Class) {
                throw new RuntimeException("Missing type parameter.");
            }
            ParameterizedType parameterizedType = (ParameterizedType) superclass;
            return parameterizedType.getActualTypeArguments()[0];
        }

        public abstract void onFailure(String error);

        public abstract void onResponse(T result);
    }
}
