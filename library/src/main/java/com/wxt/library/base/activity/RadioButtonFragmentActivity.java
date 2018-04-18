package com.wxt.library.base.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.wxt.library.base.fragment.BaseFragment;
import com.wxt.library.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/3/20.
 * 用户创建多个fragment和底部为RadioButton的界面
 */

public abstract class RadioButtonFragmentActivity extends BaseParseHelperActivity implements RadioGroup.OnCheckedChangeListener {

    // 切换
    private RadioGroup radioGroup;
    //------------------------------------------------------------
    private int selectId = 0;
    private int pageCount = 0;
    private List<BaseFragment> fragmentList;

    public abstract List<BaseFragment> getFragmentList();

    public abstract int getFragmentLayoutID();

    public abstract RadioGroup setRadioGroup();

    public abstract boolean isConsumeCheckChanged(RadioGroup group, int checkedId);

    public int getDefaultSelectIndex() {
        return 1;
    }

    private String getFragmentName(int index) {
        return getClass().getName() + "fragment_" + index;
    }

    @Override
    protected void noSaveInstanceStateForCreate() {
        fragmentList = getFragmentList();
        if (Util.isEmpty(fragmentList)) {
            throw new IllegalArgumentException("请设置相应个数的Fragment");
        }
        FragmentManager fm = getFragmentManager();

        pageCount = fragmentList.size();
        // 开启Fragment事务
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < pageCount; i++) {
            BaseFragment fragment = fragmentList.get(i);
            transaction.add(getFragmentLayoutID(), fragment, getFragmentName(i));
            transaction.hide(fragment);
        }

        transaction.commit();

        int index = Math.max(1, getDefaultSelectIndex());

        radioGroup = setRadioGroup();
        RadioButton lastRadioButton = null;

        int length = radioGroup.getChildCount();
        for (int i = 0; i < length; i++) {
            View view = radioGroup.getChildAt(i);
            if (view instanceof RadioButton) {
                lastRadioButton = (RadioButton) view;
                if (--index == 0) {
                    lastRadioButton = (RadioButton) view;
                    break;
                }
            }
        }

        if (lastRadioButton == null) {
            throw new IllegalArgumentException("没有找到RadioButton");
        }

        radioGroup.setOnCheckedChangeListener(this);

        selectId = lastRadioButton.getId();
        lastRadioButton.setChecked(true);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {

        if (isConsumeCheckChanged(group, checkedId)) {
            // TODO 变为之前的选中状态
            radioGroup.setOnCheckedChangeListener(null);
            ((RadioButton) findViewById(selectId)).setChecked(true);
            radioGroup.setOnCheckedChangeListener(this);
            return;
        }
        FragmentManager fm = getFragmentManager();
        // 开启Fragment事务
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < pageCount; i++) {
            transaction.hide(fragmentList.get(i));
        }

        int index = 0;

        int count = radioGroup.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = radioGroup.getChildAt(i);
            if (view instanceof RadioButton) {
                if (view.getId() == checkedId) {
                    transaction.show(fragmentList.get(index));
                    break;
                }
                index++;
            }
        }
        transaction.commit();

        selectId = checkedId;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("selectId", selectId);
        outState.putInt("pageCount", pageCount);
    }

    @Override
    protected void afterRestoreInstanceState(Bundle bundle) {
        selectId = bundle.getInt("selectId");
        pageCount = bundle.getInt("pageCount");
        // 回复FragmentList
        if (fragmentList == null) {
            fragmentList = new ArrayList<>();
        }
        for (int i = 0; i < pageCount; i++) {
            fragmentList.add((BaseFragment) getFragmentManager().findFragmentByTag(getFragmentName(i)));
        }
        if (radioGroup == null)
            radioGroup = setRadioGroup();

        radioGroup.setOnCheckedChangeListener(this);
        onCheckedChanged(null, radioGroup.getCheckedRadioButtonId() == -1 ? selectId : radioGroup.getCheckedRadioButtonId());
    }
}
