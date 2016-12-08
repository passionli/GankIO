package com.liguang.imageloaderdemo.ui;

import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;

public class CustomDividerDrawable extends ColorDrawable {

    private int mIntrinsicHeight;

    public CustomDividerDrawable(@ColorInt int color) {
        super(color);
    }

    public void setIntrinsicHeight(int intrinsicHeight) {
        this.mIntrinsicHeight = intrinsicHeight;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }
}
