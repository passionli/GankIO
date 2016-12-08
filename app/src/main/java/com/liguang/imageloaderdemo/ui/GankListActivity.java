package com.liguang.imageloaderdemo.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.liguang.imageloaderdemo.R;

public class GankListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gank_list);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(GankListFragment.TAG);
        if (fragment == null) {
            fragment = new GankListFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container,
                    fragment, GankListFragment.TAG).commit();
        }
    }
}
