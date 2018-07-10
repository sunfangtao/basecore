package com.wxt.library.selectcity.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.base.adapter.BaseAdapter;
import com.wxt.library.selectcity.model.City;
import com.wxt.library.selectcity.model.HotCity;

import java.util.List;

public class GridListAdapter extends BaseAdapter {

    public GridListAdapter(Context context, List<HotCity> data) {
        super(context, data);
    }

    @Override
    public int onCreateViewLayoutID(int viewType) {
        return R.layout.cp_grid_item_layout;
    }

    @Override
    public void onBindViewHolder(int viewType, View view, int position) {
        final City city = (City) getObject(position);

        ((TextView) view).setText(city.getName());
    }

}
