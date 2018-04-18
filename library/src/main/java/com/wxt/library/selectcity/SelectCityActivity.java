package com.wxt.library.selectcity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.base.activity.BaseActivity;
import com.wxt.library.base.adapter.BaseAdapter;
import com.wxt.library.listener.RecyclerViewItemClickListener;
import com.wxt.library.selectcity.adapter.CityListAdapter;
import com.wxt.library.selectcity.adapter.GridListAdapter;
import com.wxt.library.selectcity.adapter.decoration.DividerItemDecoration;
import com.wxt.library.selectcity.adapter.decoration.SectionItemDecoration;
import com.wxt.library.selectcity.db.DBManager;
import com.wxt.library.selectcity.model.City;
import com.wxt.library.selectcity.model.HotCity;
import com.wxt.library.selectcity.model.LocatedCity;
import com.wxt.library.view.SideIndexBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/17.
 */

public class SelectCityActivity extends BaseActivity implements SideIndexBar.OnIndexTouchedChangedListener, TextWatcher, RecyclerViewItemClickListener {

    private DBManager dbManager;

    private TextView mOverlayTextView;
    private SideIndexBar sideIndexBar;
    private TextView emptyTV;
    private EditText searchET;
    private RecyclerView mRecyclerView;
    private CityListAdapter cityListAdapter;
    private LinearLayoutManager mLayoutManager;

    private List<City> mAllCities = new ArrayList<>();
    private List<City> mResults = new ArrayList<>();
    private List<HotCity> mHotCities = new ArrayList<>();

    public static final String SELECT_KEY = "selectKey";
    public static final String LOCATION_CITY = "locationCity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_select_city);
        initView();
        dbManager = new DBManager(this);
        super.onCreate(savedInstanceState);

        String[] DEFAULT_INDEX_ITEMS = {"定位", "热门", "A", "B", "C", "D", "E", "F", "G", "H",
                "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};

        sideIndexBar.setOverlayTextView(mOverlayTextView).setOnIndexChangedListener(this);
        sideIndexBar.setTextArray(DEFAULT_INDEX_ITEMS);

        mRecyclerView.setLayoutManager(mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new SectionItemDecoration(this, mResults), 0);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this), 1);
        cityListAdapter = new CityListAdapter(this, mResults, mHotCities);
        mRecyclerView.setAdapter(cityListAdapter);
    }

    @Override
    protected void noSaveInstanceStateForCreate() {
        initData();
    }

    private void initView() {
        mOverlayTextView = findViewById(R.id.cp_overlay);
        sideIndexBar = findViewById(R.id.cp_side_index_bar);
        searchET = findViewById(R.id.cp_search_et);
        emptyTV = findViewById(R.id.cp_empty_tv);
        emptyTV.setVisibility(View.GONE);
        mRecyclerView = findViewById(R.id.cp_recyclerview);
    }

    private void initData() {
        initHotCities();
        mAllCities.addAll(dbManager.getAllCities());
        mAllCities.add(0, new LocatedCity(getIntent().getStringExtra(LOCATION_CITY), "未知", "0"));
        mAllCities.add(1, new HotCity("热门城市", "未知", "0"));
        mResults.addAll(mAllCities);
        searchET.addTextChangedListener(this);
    }

    private void initHotCities() {
        String[] hotCityNames = getResources().getStringArray(R.array.hot_city);
        if (hotCityNames == null || hotCityNames.length == 0) {
            mHotCities.add(new HotCity("北京", "未知", "0"));
            mHotCities.add(new HotCity("上海", "未知", "0"));
            mHotCities.add(new HotCity("广州", "未知", "0"));
            mHotCities.add(new HotCity("深圳", "未知", "0"));
        } else {
            for (int i = 0; i < hotCityNames.length; i++) {
                mHotCities.add(new HotCity(hotCityNames[i], "未知", "0"));
            }
        }
    }

    @Override
    public void onIndexChanged(String index, int position) {
        //滚动RecyclerView到索引位置
        if (mResults == null || mResults.isEmpty()) return;
        if (TextUtils.isEmpty(index)) return;
        int size = mResults.size();
        for (int i = 0; i < size; i++) {
            if (TextUtils.equals(index.substring(0, 1), mResults.get(i).getSection().substring(0, 1))) {
                if (mLayoutManager != null) {
                    mLayoutManager.scrollToPositionWithOffset(i, 0);
                    return;
                }
            }
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String keyword = s.toString();
        mResults.clear();
        if (TextUtils.isEmpty(keyword)) {
            emptyTV.setVisibility(View.GONE);
            sideIndexBar.setVisibility(View.VISIBLE);
            mResults.addAll(mAllCities);
            if (mRecyclerView.getItemDecorationCount() == 1) {
                mRecyclerView.addItemDecoration(new SectionItemDecoration(this, mResults), 0);
            }
            cityListAdapter.updateData(mResults);
        } else {
            // 开始数据库查找
            sideIndexBar.setVisibility(View.GONE);
            mResults.addAll(dbManager.searchCity(keyword));
            if (mRecyclerView.getItemDecorationCount() > 1) {
                mRecyclerView.removeItemDecoration(mRecyclerView.getItemDecorationAt(0));
            }
            if (mResults == null || mResults.isEmpty()) {
                emptyTV.setVisibility(View.VISIBLE);
            } else {
                emptyTV.setVisibility(View.GONE);
                cityListAdapter.updateData(mResults);
            }
        }
        mRecyclerView.scrollToPosition(0);
    }

    @Override
    protected void afterRestoreInstanceState(Bundle savedInstanceState) {
        searchET.addTextChangedListener(this);
        if (!TextUtils.isEmpty(searchET.getText().toString())) {
            sideIndexBar.setVisibility(View.GONE);
            if (mRecyclerView.getItemDecorationCount() > 1) {
                mRecyclerView.removeItemDecoration(mRecyclerView.getItemDecorationAt(0));
            }
            if (mResults.isEmpty()) {
                emptyTV.setVisibility(View.VISIBLE);
            } else {
                emptyTV.setVisibility(View.GONE);
            }
        } else {
            sideIndexBar.setVisibility(View.VISIBLE);
        }
        mRecyclerView.requestFocus();
    }

    @Override
    public void forReceiverResult(Intent intent) {
        if (intent != null) {
            City city = (City) intent.getSerializableExtra(SELECT_KEY);
            city = dbManager.searchSingleCity(city.getName());
            setResult(RESULT_OK, intent.putExtra(SELECT_KEY, city));
            finish();
        }
    }

    @Override
    public void onRecyclerViewItemClick(BaseAdapter adapter, View v, int position) {
        if (adapter instanceof CityListAdapter) {
            if (position == 0 && mResults.get(0).getPinyin().substring(0,1).equals("定")) {
                return;
            }
            if (position == 1 && mResults.get(1).getPinyin().substring(0,1).equals("热")) {
                return;
            }
            City city = mResults.get(position);
            city = dbManager.searchSingleCity(city.getName());
            setResult(RESULT_OK, new Intent().putExtra(SELECT_KEY, city));
            finish();
        } else if (adapter instanceof GridListAdapter) {
            City city = mHotCities.get(position);
            city = dbManager.searchSingleCity(city.getName());
            setResult(RESULT_OK, new Intent().putExtra(SELECT_KEY, city));
            finish();
        }
    }
}
