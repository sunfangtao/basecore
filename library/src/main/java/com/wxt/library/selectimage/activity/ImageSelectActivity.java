package com.wxt.library.selectimage.activity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.wxt.library.R;
import com.wxt.library.base.activity.BaseActivity;
import com.wxt.library.base.adapter.BaseAdapter;
import com.wxt.library.base.adapter.decoration.RecyclerViewItemDecoration;
import com.wxt.library.listener.RecyclerViewItemClickListener;
import com.wxt.library.listener.RecyclerViewItemLongClickListener;
import com.wxt.library.selectimage.Constants;
import com.wxt.library.selectimage.adapter.CustomImageSelectAdapter;
import com.wxt.library.selectimage.models.Image;
import com.wxt.library.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class ImageSelectActivity extends BaseActivity implements RecyclerViewItemClickListener, RecyclerViewItemLongClickListener {

    private ArrayList<Image> images;
    // 图集
    private String album;
    // 图集ID
    private long albumId;
    // 最大数目
    private int maxCount;

    private CustomImageSelectAdapter adapter;

    private int countSelected;

    private final String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, MediaStore.Images.Media.DATA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_select_base);

        album = getIntent().getStringExtra(Constants.INTENT_EXTRA_ALBUM);
        albumId = getIntent().getLongExtra(Constants.INTENT_EXTRA_ALBUM_ID, 1l);
        maxCount = getIntent().getIntExtra(Constants.INTENT_EXTRA_LIMIT, 1);

        setTitle(album);

        RecyclerView recyclerView = findViewById(R.id.grid_view_image_select);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.addItemDecoration(new RecyclerViewItemDecoration(Util.dp2px(this, 1)));
        adapter = new CustomImageSelectAdapter(this, images = new ArrayList<>(), 4);
        recyclerView.setAdapter(adapter);

        loadImages();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setTitle(countSelected + "/" + maxCount + "已选择");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.select_image_menu, menu);//加载menu文件到布局
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        sendIntent();
        return true;
    }

    @Override
    public void onRecyclerViewLongItemClick(BaseAdapter adapter, View v, int position) {
        // TODO 长按查看大图
        Intent intent = new Intent(this, ShowImageActivity.class);
        intent.putExtra("SHOW_BIG_IMAGE", images.get(position).path);
        startActivity(intent);
    }

    @Override
    public void onRecyclerViewItemClick(BaseAdapter adapter, View v, int position) {
        toggleSelection(position);
    }

    @Override
    public void forReceiverResult(Intent intent) {
        if (intent != null) {
            toggleSelection(intent.getIntExtra("ImageSelectActivity_position", 0));
        }
    }

    private void toggleSelection(int position) {
        if (!images.get(position).isSelected && countSelected >= maxCount) {
            toast.setText("最多选择" + maxCount + "张");
            return;
        }

        if (images.get(position).isSelected) {
            countSelected--;
            images.get(position).isSelected = false;
        } else if (countSelected < maxCount) {
            countSelected++;
            images.get(position).isSelected = true;
        }
        invalidateOptionsMenu();
        adapter.notifyItemChanged(position);
    }

    private void deselectAll() {
        for (int i = 0, l = images.size(); i < l; i++) {
            images.get(i).isSelected = false;
        }
        countSelected = 0;
        adapter.notifyDataSetChanged();
    }

    private ArrayList<Image> getSelected() {
        ArrayList<Image> selectedImages = new ArrayList<>();
        for (int i = 0, l = images.size(); i < l; i++) {
            if (images.get(i).isSelected) {
                selectedImages.add(images.get(i));
            }
        }
        return selectedImages;
    }

    private void sendIntent() {
        Intent intent = new Intent();
        intent.putExtra(AlbumSelectActivity.INTENT_EXTRA_IMAGES, getSelected());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void loadImages() {
        File file;
        HashSet<Long> selectedImages = new HashSet<>();
        if (images != null) {
            Image image;
            for (int i = 0, l = images.size(); i < l; i++) {
                image = images.get(i);
                file = new File(image.path);
                if (file.exists() && image.isSelected) {
                    selectedImages.add(image.id);
                }
            }
        }

        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                MediaStore.Images.Media.BUCKET_ID + " =?", new String[]{albumId + ""}, MediaStore.Images.Media.DATE_ADDED);
        if (cursor == null) {
            return;
        }

            /*
            In case this runnable is executed to onChange calling loadImages,
            using countSelected variable can result in a race condition. To avoid that,
            tempCountSelected keeps track of number of selected images. On handling
            FETCH_COMPLETED message, countSelected is assigned value of tempCountSelected.
             */
        int tempCountSelected = 0;
        ArrayList<Image> temp = new ArrayList<>(cursor.getCount());
        if (cursor.moveToLast()) {
            do {
                long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
                String name = cursor.getString(cursor.getColumnIndex(projection[1]));
                String path = cursor.getString(cursor.getColumnIndex(projection[2]));
                boolean isSelected = selectedImages.contains(id);
                if (isSelected) {
                    tempCountSelected++;
                }

                file = new File(path);
                if (file.exists()) {
                    temp.add(new Image(id, name, path, isSelected));
                }

            } while (cursor.moveToPrevious());
        }
        cursor.close();

        images.clear();
        images.addAll(temp);

        adapter.notifyDataSetChanged();
    }

}
