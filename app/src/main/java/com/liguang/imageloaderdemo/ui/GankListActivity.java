package com.liguang.imageloaderdemo.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.android.debug.hv.ViewServer;
import com.liguang.imageloaderdemo.R;

import hugo.weaving.DebugLog;

public class GankListActivity extends AppCompatActivity {
    private static final String TAG = "GankListActivity";

    @DebugLog
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_gank_list);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(GankListFragment.TAG);
        if (fragment == null) {
            fragment = new GankListFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container,
                    fragment, GankListFragment.TAG).commit();
        }
        ViewServer.get(this).addWindow(this);
    }

    @DebugLog
    @Override
    protected void onStart() {
        super.onStart();
    }

    @DebugLog
    @Override
    protected void onResume() {
        super.onResume();
        ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //intercept event
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //按返回键时不销毁当前activity，类似手Q
            boolean result = moveTaskToBack(true);
            Log.d(TAG, "onKeyDown: moveTaskToBack result=" + result);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }
}
