package com.liguang.imageloaderdemo.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

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
    }
}
