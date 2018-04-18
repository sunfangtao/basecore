package com.wxt.library.selectcity.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.base.adapter.BaseAdapter;
import com.wxt.library.selectcity.SelectCityActivity;
import com.wxt.library.selectcity.adapter.decoration.GridItemDecoration;
import com.wxt.library.selectcity.model.City;
import com.wxt.library.selectcity.model.HotCity;

import java.util.List;

/**
 * @Author: Bro0cL
 * @Date: 2018/2/5 12:06
 */
public class CityListAdapter extends BaseAdapter {
    private static final int VIEW_TYPE_CURRENT = 10;
    private static final int VIEW_TYPE_HOT = 11;

    private List<HotCity> mHotData;

    public CityListAdapter(Context context, List<City> data, List<HotCity> hotData) {
        super(context, data);
        this.mHotData = hotData;
    }

    @Override
    public int getItemViewType(int position) {
        City city = (City) getObjcet(position);
        if (position == 0 && city.getPinyin().substring(0, 1).equals("定"))
            return VIEW_TYPE_CURRENT;
        if (position == 1 && city.getPinyin().substring(0, 1).equals("热"))
            return VIEW_TYPE_HOT;
        return super.getItemViewType(position);
    }

    @Override
    public int onCreateViewLayoutID(int viewType) {
        switch (viewType) {
            case VIEW_TYPE_CURRENT:
                return R.layout.cp_list_item_location_layout;
            case VIEW_TYPE_HOT:
                return R.layout.cp_list_item_hot_layout;
            default:
                return R.layout.cp_list_item_default_layout;
        }
    }

    @Override
    public void onBindViewHolder(int viewType, View view, int position) {

        final City city = (City) getObjcet(position);
        switch (viewType) {
            case VIEW_TYPE_CURRENT:
                ((TextView) view.findViewById(R.id.cp_list_item_location)).setText(city.getName());
                view.findViewById(R.id.cp_list_item_location).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(SelectCityActivity.class.getName()).putExtra(SelectCityActivity.SELECT_KEY, city));
                    }
                });
                break;
            case VIEW_TYPE_HOT:
                RecyclerView recyclerView = ((RecyclerView) view);
                GridListAdapter mAdapter = new GridListAdapter(context, mHotData);
                recyclerView.setLayoutManager(new GridLayoutManager(context, 3));
                if (recyclerView.getItemDecorationCount() == 0) {
                    recyclerView.addItemDecoration(new GridItemDecoration(3, 10));
                }
                recyclerView.setAdapter(mAdapter);
                break;
            default:
                ((TextView) view).setText(city.getName());
        }
    }

}
