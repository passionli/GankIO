package com.liguang.imageloaderdemo.network;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 同步接口
 */
public class HttpManager {
    private static volatile HttpManager sInstance;
    OkHttpClient mClient = new OkHttpClient();

    public String doGet(String url) {
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

    public static HttpManager getInstance() {
        if (sInstance == null) {
            synchronized (HttpManager.class) {
                if (sInstance == null) {
                    sInstance = new HttpManager();
                }
            }
        }

        return sInstance;
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
