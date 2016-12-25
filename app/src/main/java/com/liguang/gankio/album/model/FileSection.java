package com.liguang.gankio.album.model;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.liguang.gankio.ImageDetailActivity;
import com.liguang.gankio.LGViewUtils;
import com.liguang.gankio.R;
import com.liguang.gankio.album.AlbumViewHolder;
import com.liguang.gankio.album.FrescoUtils;
import com.liguang.gankio.album.HeaderViewHolder;

import java.io.Serializable;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class FileSection extends StatelessSection {
    private static final String TAG = "FileSection";
    public String title;
    List<FileItem> items;
    private int mImageWidth;

    public FileSection(String title, List<FileItem> items) {
        super(R.layout.section_header, R.layout.section_item);
        this.title = title;
        this.items = items;
    }

    @Override
    public int getContentItemsTotal() {
        return items.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(View view) {
        mImageWidth = (LGViewUtils.getScreenWidth(view.getContext()) - LGViewUtils.dp2px(view.getContext(), 20)) / 4;
        AlbumViewHolder holder = new AlbumViewHolder(view);
        holder.setDraweeViewWidth(mImageWidth);
        return holder;
    }

    @Override
    public void onBindItemViewHolder(RecyclerView.ViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: Element " + position);
        final FileItem lowLevelUserInfo = items.get(position);
        AlbumViewHolder albumViewHolder = (AlbumViewHolder) holder;
        //text是同步设置上去的，所以不会出现错位问题
        albumViewHolder.textView.setText(lowLevelUserInfo.text);
        FrescoUtils.load(lowLevelUserInfo.uri, albumViewHolder.draweeView, mImageWidth, mImageWidth);
        albumViewHolder.draweeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) v.getContext();
                Intent intent = new Intent(activity, ImageDetailActivity.class);
                //这里应该传数组
                intent.putExtra(ImageDetailActivity.EXTRA_IMAGE_ARRAY, (Serializable) items);
                intent.putExtra(ImageDetailActivity.EXTRA_IMAGE, position);
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
        HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

        headerHolder.title.setText(title);
        headerHolder.select.setText(title);

        headerHolder.select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), String.format("Clicked on more button from the header of Section %s",
                        title),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
