package com.wxt.library.base.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.wxt.library.R;

import java.util.List;

public class ListDropDownAdapter extends BaseDropDownAdapter {

    public ListDropDownAdapter(Context context, List<String> list) {
        super(context, list);
    }

    @Override
    public final int onCreateViewLayoutID(int viewType) {
        return R.layout.item_default_drop_down;
    }

    @Override
    public void onBindViewHolder(int viewType, View view, final int position) {
        TextView textView = (TextView) view.findViewById(R.id.text);
        textView.setText((String) getObjcet(position));
        if (isSelectedItem(position)) {
            textView.setTextColor(context.getResources().getColor(R.color.drop_down_selected));
            textView.setBackgroundResource(R.color.check_bg);
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.drop_down_unselected));
            textView.setBackgroundResource(R.color.white);
        }
    }
}
