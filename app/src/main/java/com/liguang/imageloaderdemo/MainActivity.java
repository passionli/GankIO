package com.liguang.imageloaderdemo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.liguang.imageloaderdemo.album.AlbumUIFragment;
import com.liguang.imageloaderdemo.album.Utils;
import com.liguang.imageloaderdemo.bean.ItemListBean;
import com.liguang.imageloaderdemo.dao.ItemListDao;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Retrieve the Toolbar from our content view, and set it as the action bar
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Fragment fragment = getSupportFragmentManager().findFragmentByTag(AlbumUIFragment.TAG);
        if (fragment == null) {
            fragment = new AlbumUIFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container,
                    fragment, AlbumUIFragment.TAG).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
