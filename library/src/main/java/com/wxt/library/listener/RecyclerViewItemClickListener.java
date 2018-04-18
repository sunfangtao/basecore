package com.wxt.library.listener;

import android.view.View;

import com.wxt.library.base.adapter.BaseAdapter;

public interface RecyclerViewItemClickListener {
    void onRecyclerViewItemClick(BaseAdapter adapter, View v, int position);
}
