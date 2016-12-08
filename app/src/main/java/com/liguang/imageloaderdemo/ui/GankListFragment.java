package com.liguang.imageloaderdemo.ui;

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

import com.liguang.imageloaderdemo.R;

public class GankListFragment extends Fragment {
    public static final String TAG = GankListFragment.class.getSimpleName();
    private static final String KEY_TAB = "key_tab";
    private TabLayout mIndicator;
    private ViewPager mViewPager;
    private String[] mTags = {"Android", "iOS", "前端"};

    public GankListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_gank_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mIndicator = (TabLayout) view.findViewById(R.id.tabs);
        mViewPager = (ViewPager) view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(new MyViewPager(getActivity().getSupportFragmentManager()));
        mIndicator.setupWithViewPager(mViewPager);
//        mViewPager.setOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mIndicator));
        mIndicator.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        mIndicator.setTabMode(TabLayout.MODE_FIXED);
        mViewPager.setOffscreenPageLimit(mTags.length);

        if (savedInstanceState != null) {
            mViewPager.setCurrentItem(savedInstanceState.getInt(KEY_TAB));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_TAB, mViewPager.getCurrentItem());
    }

    private class MyViewPager extends FragmentStatePagerAdapter {

        public MyViewPager(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            //复用,Lazy Fragment
            return ItemListFragment.newInstance(mTags[position]);
        }

        @Override
        public int getCount() {
            return mTags.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTags[position];
        }
    }
}
