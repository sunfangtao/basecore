package com.wxt.library.selectimage.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.wxt.library.R;
import com.wxt.library.base.activity.BaseActivity;
import com.wxt.library.base.adapter.BaseAdapter;
import com.wxt.library.base.adapter.decoration.RecyclerViewItemDecoration;
import com.wxt.library.listener.RecyclerViewItemClickListener;
import com.wxt.library.selectimage.Constants;
import com.wxt.library.selectimage.adapter.CustomAlbumSelectAdapter;
import com.wxt.library.selectimage.models.Album;
import com.wxt.library.util.MyHandler;
import com.wxt.library.util.PermissionRequestUtil;
import com.wxt.library.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class AlbumSelectActivity extends BaseActivity implements RecyclerViewItemClickListener {

    public static final String INTENT_EXTRA_IMAGES = "images";

    private final int DEFAULT_MAX_COUNT = 10;
    // 相册列表
    private ArrayList<Album> albums;
    //
    private CustomAlbumSelectAdapter adapter;

    private int maxCount = DEFAULT_MAX_COUNT;

    private final String[] projection = new String[]{
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_select_base);

        Intent intent = getIntent();
        if (intent != null) {
            maxCount = Math.max(15, intent.getIntExtra(Constants.INTENT_EXTRA_LIMIT, DEFAULT_MAX_COUNT));
            if (maxCount <= 0) {
                maxCount = DEFAULT_MAX_COUNT;
            }
        }

        RecyclerView recyclerView = findViewById(R.id.grid_view_album_select);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new RecyclerViewItemDecoration(Util.dp2px(this, 1)));
        adapter = new CustomAlbumSelectAdapter(this, albums = new ArrayList<>(), 2);
        recyclerView.setAdapter(adapter);

        initPermission();
    }

    private void initPermission() {
        if (PermissionRequestUtil.getInstance().requestPermission(this, PermissionRequestUtil.READ_EXTERNAL_STORAGE)) {
            loadAlbums();
        }
    }

    private void loadAlbums() {
        Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                null, null, MediaStore.Images.Media.DATE_ADDED);
        if (cursor == null) {
            return;
        }

        ArrayList<Album> temp = new ArrayList<>(cursor.getCount());
        HashSet<Long> albumSet = new HashSet<>();
        File file;
        if (cursor.moveToLast()) {
            do {
                long albumId = cursor.getLong(cursor.getColumnIndex(projection[0]));
                String album = cursor.getString(cursor.getColumnIndex(projection[1]));
                String image = cursor.getString(cursor.getColumnIndex(projection[2]));

                if (!albumSet.contains(albumId)) {
                        /*
                        It may happen that some image file paths are still present in cache,
                        though image file does not exist. These last as long as media
                        scanner is not run again. To avoid get such image file paths, check
                        if image file exists.
                         */
                    file = new File(image);
                    if (file.exists()) {
                        temp.add(new Album(album, image));
                        albumSet.add(albumId);
                    }
                }

            } while (cursor.moveToPrevious());
        }
        cursor.close();

        albums.clear();
        albums.addAll(temp);

        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionRequestUtil.READ_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadAlbums();
        } else {
            toast.setText("无法读取图片");
            new MyHandler(1000) {
                @Override
                public void run() {
                    finish();
                }
            };
        }
    }

    @Override
    public void onRecyclerViewItemClick(BaseAdapter adapter, View v, int position) {
        Intent intent = new Intent(this, ImageSelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_ALBUM, albums.get(position).name);
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, maxCount);
        startActivityForResult(intent, 1000);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1000 && resultCode == RESULT_OK && data != null) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

}
