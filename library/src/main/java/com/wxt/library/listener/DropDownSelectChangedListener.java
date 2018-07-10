package com.wxt.library.listener;

import com.wxt.library.base.adapter.dropdownmenuadapter.BaseDropDownAdapter;

public interface DropDownSelectChangedListener {
    void onSelectChanged(BaseDropDownAdapter adapter, int newPosition, int oldPosition);
}