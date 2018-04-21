package com.wxt.library.selectimage.activity;

import com.wxt.library.base.activity.BaseShowBigImageActivity;

/**
 * Created by Administrator on 2018/4/20.
 */

public class ShowImageActivity extends BaseShowBigImageActivity {

    @Override
    protected Object setImage() {
        if (getIntent() == null) {
            return null;
        }
        return getIntent().getStringExtra("SHOW_BIG_IMAGE");
    }
}
