package com.liguang.imageloaderdemo;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.liguang.imageloaderdemo.album.ImageDetailFragment;
import com.liguang.imageloaderdemo.album.Utils;
import com.liguang.imageloaderdemo.album.model.FileItem;

import java.util.List;

public class ImageDetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ImageDetailActivity.class.getSimpleName();
    private static final String IMAGE_CACHE_DIR = "images";
    public static final String EXTRA_IMAGE = "extra_image";
    public static final String EXTRA_IMAGE_ARRAY = "extra_image_array";
    //    private ImageFetcher mImageFetcher;
    private ViewPager mPager;
    private ImagePagerAdapter mAdapter;
    private List<FileItem> mItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.enableStrictMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_detail_pager);

        // Fetch screen height and width, to use as our max size when loading images as this
        // activity runs full screen
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // For this sample we'll use half of the longest width to resize our images. As the
        // image scaling ensures the image is larger than this, we should be left with a
        // resolution that is appropriate for both portrait and landscape. For best image quality
        // we shouldn't divide by 2, but this will use more memory and require a larger memory
        // cache.
        final int longest = (height > width ? height : width) / 2;

        // Set up ViewPager and backing adapter

        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setPageMargin((int) getResources().getDimension(R.dimen.activity_horizontal_margin));
        mPager.setOffscreenPageLimit(2);

        // Set up activity to go full screen
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Enable some additional newer visibility and ActionBar features to create a more
        // immersive photo viewing experience
        if (Utils.hasHoneycomb()) {
            //这里应该使用Toolbar
//            Toolbar toolbar = getSupportActionBar();
//            final ActionBar actionBar = getActionBar();
//
//            // Hide title text and set home as up
//            actionBar.setDisplayShowTitleEnabled(false);
//            actionBar.setDisplayHomeAsUpEnabled(true);
//
//            // Hide and show the ActionBar as the visibility changes
//            mPager.setOnSystemUiVisibilityChangeListener(
//                    new View.OnSystemUiVisibilityChangeListener() {
//                        @Override
//                        public void onSystemUiVisibilityChange(int vis) {
//                            if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
//                                actionBar.hide();
//                            } else {
//                                actionBar.show();
//                            }
//                        }
//                    });
//
//            // Start low profile mode and hide ActionBar
//            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
//            actionBar.hide();
        }

        //TODO 这里需要加上传递过来的数组的长度
        // Set the current item based on the extra passed in to this activity
        //当前点击的是一组图片中的那个数组下标,方便左右滑动
        mItems = (List<FileItem>) getIntent().getSerializableExtra(EXTRA_IMAGE_ARRAY);
        mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), mItems.size());
        mPager.setAdapter(mAdapter);
        final int extraCurrentItem = getIntent().getIntExtra(EXTRA_IMAGE, -1);
        if (extraCurrentItem != -1) {
            mPager.setCurrentItem(extraCurrentItem);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
//        mImageFetcher.setExitTasksEarly(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: ");
//        mImageFetcher.setExitTasksEarly(true);
//        mImageFetcher.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
//        mImageFetcher.closeCache();
    }

//    /**
//     * Called by the ViewPager child fragments to load images via the one ImageFetcher
//     */
//    public ImageFetcher getImageFetcher() {
//        return mImageFetcher;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.clear_cache:
//                mImageFetcher.clearCache();
                Toast.makeText(
                        this, R.string.clear_cache_complete_toast, Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
     * could be a large number of items in the ViewPager and we don't want to retain them all in
     * memory at once but create/destroy them on the fly.
     */
    private class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private final int mSize;

        public ImagePagerAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(TAG, "getItem: position=" + position);
            FileItem item = mItems.get(position);
            //根据底层数组下标，创建Fragment
            return ImageDetailFragment.newInstance(item.uri);
        }
    }


    /**
     * Set on the ImageView in the ViewPager children fragments, to enable/disable low profile mode
     * when the ImageView is touched.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public void onClick(View v) {
        final int vis = mPager.getSystemUiVisibility();
        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            showSystemUI();
        } else {
            hideSystemUI();
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
        v.invalidate();
    }

    // This snippet hides the system bars.
    private void hideSystemUI() {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    // This snippet shows the system bars. It does this by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
}
