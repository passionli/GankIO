package com.liguang.gankio.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtil {
    private static final String TAG = "PreferenceUtil";
    public static final String DATA_BOOTSTRAP_DONE = "data_bootstrap_done";

    private final Context mContext;
    private static PreferenceUtil sInstance;
    private final SharedPreferences mPreferences;

    private PreferenceUtil(Context context) {
        mContext = context.getApplicationContext();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }

    public static PreferenceUtil getInstance(Context context) {
        if (sInstance == null) {
            synchronized (PreferenceUtil.class) {
                if (sInstance == null) {
                    sInstance = new PreferenceUtil(context);
                }
            }
        }

        return sInstance;
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        boolean value = mPreferences.getBoolean(key, false);
        return value;
    }

    public void putInt(String key, int value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    public int getInt(String key) {
        int value = mPreferences.getInt(key, 0);
        return value;
    }

    public void putLong(String key, long value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * @param key
     * @return default is 0
     */
    public long getLong(String key) {
        long value = mPreferences.getLong(key, 0);
        return value;
    }


    public String getString(String key) {
        String value = mPreferences.getString(key, null);
        return value;
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public boolean isDataBootstrapDone() {
        return getBoolean(DATA_BOOTSTRAP_DONE);
    }

    public void setDataBootstrapDone() {
        putBoolean(DATA_BOOTSTRAP_DONE, true);
    }
}
