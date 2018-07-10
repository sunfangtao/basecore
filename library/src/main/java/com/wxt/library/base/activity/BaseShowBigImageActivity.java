package com.wxt.library.base.activity;

import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.wxt.library.R;
import com.wxt.library.photoview.PhotoView;

/**
 * Created by Administrator on 2018/4/20.
 */

public abstract class BaseShowBigImageActivity extends BaseParseHelperActivity {

    private PhotoView photoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_bigimage_base);
        photoView = findViewById(R.id.photo_view);
        Glide.with(this).load(setImage()).into(photoView);
    }

    protected abstract Object setImage();
}
