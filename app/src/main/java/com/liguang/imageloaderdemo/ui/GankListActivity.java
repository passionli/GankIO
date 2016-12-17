package com.liguang.imageloaderdemo.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.android.debug.hv.ViewServer;
import com.liguang.imageloaderdemo.R;

public class GankListActivity extends AppCompatActivity {
    private static final String TAG = "GankListActivity";

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

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ViewServer.get(this).setFocusedWindow(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }
}
