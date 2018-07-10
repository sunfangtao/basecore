package com.wxt.library.listener;

import com.wxt.library.base.adapter.dropdownmenuadapter.BaseDropDownAdapter;

import java.util.List;

public interface DropDownMutilConfirmListener {
        void onSelectConfirm(BaseDropDownAdapter adapter, List<Integer> newPositionList);
    }