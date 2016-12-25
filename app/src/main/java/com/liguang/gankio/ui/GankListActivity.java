package com.liguang.gankio.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;

import com.android.debug.hv.ViewServer;
import com.liguang.gankio.R;

import hugo.weaving.DebugLog;

@DebugLog
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
    protected void onRestart() {
        super.onRestart();
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

//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        //intercept event
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            //按返回键时不销毁当前activity，类似手Q
//            boolean result = moveTaskToBack(true);
////            overridePendingTransition(R.anim.grow_from_middle, R.anim.shrink_to_middle);
//            Log.d(TAG, "onKeyDown: moveTaskToBack result=" + result);
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ViewServer.get(this).removeWindow(this);
    }
}
