package com.wxt.library.listener;

import com.wxt.library.base.adapter.dropdownmenuadapter.BaseDropDownAdapter;

import java.util.List;

public interface DropDownMutilSelectChangedListener {
        void onSelectChanged(BaseDropDownAdapter adapter, List<Integer> oldPositionList, int changePosition);
    }