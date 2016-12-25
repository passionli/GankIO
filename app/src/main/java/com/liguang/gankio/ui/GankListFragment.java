package com.liguang.gankio.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.liguang.gankio.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import hugo.weaving.DebugLog;

import static com.liguang.gankio.config.AppConfig.TAB_TAG;

@DebugLog
public class GankListFragment extends Fragment {
    public static final String TAG = "GankListFragment";
    private static final String KEY_TAB = "key_tab";
    @BindView(R.id.tabs)
    TabLayout mIndicator;
    @BindView(R.id.viewPager)
    ViewPager mViewPager;
    private MyViewPagerAdapter mAdapter;

    public GankListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAdapter = new MyViewPagerAdapter(getActivity().getSupportFragmentManager());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_gank_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        mViewPager.setAdapter(mAdapter);
        mIndicator.setupWithViewPager(mViewPager);
        mIndicator.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mViewPager.setOffscreenPageLimit(TAB_TAG.length);

        if (savedInstanceState != null) {
            mViewPager.setCurrentItem(savedInstanceState.getInt(KEY_TAB));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TAB, mViewPager.getCurrentItem());
    }

    @DebugLog
    private class MyViewPagerAdapter extends FragmentStatePagerAdapter {
        private static final String TAG = "MyViewPagerAdapter";

        public MyViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //复用,Lazy Fragment
            return ItemsFragment.newInstance(TAB_TAG[position]);
        }

        @Override
        public int getCount() {
            return TAB_TAG.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return TAB_TAG[position];
        }
    }
}
