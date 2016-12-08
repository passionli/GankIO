package com.liguang.imageloaderdemo.album;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;
import com.liguang.imageloaderdemo.ImageDetailActivity;
import com.liguang.imageloaderdemo.R;

public class AlbumViewHolder extends RecyclerView.ViewHolder {
    private static final String TAG = "AlbumViewHolder";
    public TextView textView;
    public SimpleDraweeView draweeView;

    public AlbumViewHolder(View itemView) {
        super(itemView);
        textView = (TextView) itemView.findViewById(R.id.text);
        draweeView = (SimpleDraweeView) itemView.findViewById(R.id.my_image_view);
    }

    public void setDraweeViewWidth(int width){
//        ViewGroup.LayoutParams lp = draweeView.getLayoutParams();
//        lp.width = width;
//        lp.height = width;
//        draweeView.setLayoutParams(lp);
    }
}