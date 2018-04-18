package com.wxt.library.selectimage.adapter;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wxt.library.R;
import com.wxt.library.base.adapter.BaseAdapter;
import com.wxt.library.selectimage.models.Album;

import java.util.ArrayList;

public class CustomAlbumSelectAdapter extends BaseAdapter {

    private int width;

    public CustomAlbumSelectAdapter(Context context, ArrayList<Album> albums, int column) {
        super(context, albums);
        width = (int) (context.getResources().getDisplayMetrics().widthPixels - (column + 1) * context.getResources().getDisplayMetrics().density) / column;
    }

    @Override
    public int onCreateViewLayoutID(int viewType) {
        return R.layout.item_album_select;
    }

    @Override
    public void onBindViewHolder(int viewType, View view, int position) {
        ImageView imageView = view.findViewById(R.id.image_view_album_image);
        TextView textView = view.findViewById(R.id.text_view_album_name);

        view.getLayoutParams().width = width;
        view.getLayoutParams().height = width;

        Album album = (Album) getObjcet(position);

        textView.setText(album.name);

        Glide.with(context).load(album.cover).into(imageView);
    }
}
