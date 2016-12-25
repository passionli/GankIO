package com.liguang.gankio.album;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.liguang.gankio.R;
import com.liguang.gankio.album.model.FileItem;
import com.liguang.gankio.album.model.FileSection;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class ImageListFragment extends Fragment {
    private static final String EXTRA_TAG = "extra_tag";
    Unbinder mUnbinder;
    private List<ArrayList<FileItem>> mData;
    private String mTag;
    @BindView(R.id.progress)
    LinearLayout mProgress;
    protected RecyclerView mRecyclerView;
    private GridLayoutManager mLayoutManager;
    private SectionedRecyclerViewAdapter mAdapter;
    private FetchDataTask mFetchDataTask;

    public ImageListFragment() {
        // Required empty public constructor
    }

    public static ImageListFragment newInstance(String tag) {
        ImageListFragment fragment = new ImageListFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTag = getArguments().getString(EXTRA_TAG);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_image_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLayoutManager = new GridLayoutManager(getActivity(), 4);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (mAdapter.getSectionItemViewType(position)) {
                    case SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER:
                        return 4;
                    default:
                        return 1;
                }
            }
        });
        mAdapter = new SectionedRecyclerViewAdapter();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Fresco.getImagePipeline().resume();
                } else {
                    Fresco.getImagePipeline().pause();
                }
            }
        });
        if (Utils.hasMarshmallow()) {
            String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};

            int permsRequestCode = 200;

            requestPermissions(perms, permsRequestCode);
        } else {
//            mImageLoader = ImageLoader.getInstance(getActivity());
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Stop -> Start: new Task instance to fetch data
        mFetchDataTask = new FetchDataTask();
        mFetchDataTask.execute();
    }

    private void bindData(List<ArrayList<FileItem>> data) {
        mData = data;
        mAdapter.removeAllSections();
        for (int i = 0, size = data.size(); i < size; i++) {
            mAdapter.addSection(new FileSection("section " + String.valueOf(i), mData.get(i)));
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    private class FetchDataTask extends AsyncTask<Void, Void, List<ArrayList<FileItem>>> {

        @Override
        protected void onPreExecute() {
            if (mProgress.getVisibility() != View.VISIBLE) {
                mProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected List<ArrayList<FileItem>> doInBackground(Void... params) {
            return FileUtils.load("Camera/liguang/");
        }

        @Override
        protected void onPostExecute(List<ArrayList<FileItem>> arrayLists) {
            if (mProgress.getVisibility() != View.GONE) {
                mProgress.setVisibility(View.GONE);
            }
            mRecyclerView.setVisibility(View.VISIBLE);
            bindData(arrayLists);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200:
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted) {
                    //授权成功之后，调用系统相机进行拍照操作等
//                    mImageLoader = ImageLoader.getInstance(getContext());
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    //用户授权拒绝之后，友情提示一下就可以了
                }

                break;
        }
    }
}
