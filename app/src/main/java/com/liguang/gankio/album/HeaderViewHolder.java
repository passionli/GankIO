package com.liguang.gankio.album;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.liguang.gankio.R;

public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public final TextView title;
        public final TextView select;

        public HeaderViewHolder(View view) {
            super(view);

            title = (TextView) view.findViewById(R.id.title);
            select = (TextView) view.findViewById(R.id.select);
        }
    }
