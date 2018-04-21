package com.wxt.library.selectcity.adapter.decoration;

import android.content.Context;
import android.graphics.Color;

import com.wxt.library.base.adapter.decoration.GroupHeaderDecoration;
import com.wxt.library.selectcity.model.City;
import com.wxt.library.util.Util;

import java.util.List;

public class SectionItemDecoration extends GroupHeaderDecoration {

    private List<City> cityList;

    public SectionItemDecoration(Context context, List<City> cityList) {
        this.cityList = cityList;

        setHeaderHeight(Util.dp2px(context, 40));
        setTextPaddingLeft(Util.dp2px(context, 16));
        setTextSize(Util.sp2px(context, 16));
        setTextColor(Color.parseColor("#333333"));
        setHeaderContentColor(Color.parseColor("#dadada"));
    }

    @Override
    public String getHeaderName(int pos) {
        char c = cityList.get(pos).getPinyin().charAt(0);
        if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
            return (c + "").toUpperCase();
        }

        return cityList.get(pos).getPinyin();
    }
}
