package com.wxt.library.selectcity.listener;

import com.wxt.library.selectcity.model.City;

public interface OnPickListener {
    void onPick(int position, City data);

    void onLocate();
}
