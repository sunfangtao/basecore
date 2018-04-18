package com.wxt.library.selectimage.adapter;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wxt.library.R;
import com.wxt.library.base.adapter.BaseAdapter;
import com.wxt.library.selectimage.models.Image;

import java.util.ArrayList;

/**
 * Created by Darshan on 4/18/2015.
 */
public class CustomImageSelectAdapter extends BaseAdapter {

    private int width;

    public CustomImageSelectAdapter(Context context, ArrayList<Image> images, int column) {
        super(context, images);
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        float destiny = context.getResources().getDisplayMetrics().density;
        width = (int) ((screenWidth - (column + 1) * destiny) / column);
    }

    @Override
    public int onCreateViewLayoutID(int viewType) {
        return R.layout.item_image_select;
    }

    @Override
    public void onBindViewHolder(int viewType, View view, int position) {
        ImageView imageView = view.findViewById(R.id.image_view_image_select);
        View viewAlpha = view.findViewById(R.id.view_alpha);
        CheckBox ck = view.findViewById(R.id.image_view_ck);

        view.getLayoutParams().width = width;
        view.getLayoutParams().height = width;

        final Image image = (Image) getObjcet(position);
        if (image.isSelected) {
            viewAlpha.setAlpha(0.35f);
            ck.setChecked(true);
        } else {
            viewAlpha.setAlpha(0.0f);
            ck.setChecked(false);
        }
        Glide.with(context).load(image.path).into(imageView);
    }
}
