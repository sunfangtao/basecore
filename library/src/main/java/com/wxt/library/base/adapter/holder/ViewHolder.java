package com.wxt.library.base.adapter.holder;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by Administrator on 2016/7/23.
 */
public class ViewHolder {

    private SparseArray<View> viewHolder;
    private View view;

    public static ViewHolder getViewHolder(View view) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if (viewHolder == null) {
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        }
        return viewHolder;
    }

    private ViewHolder(View view) {
        this.view = view;
        viewHolder = new SparseArray<View>();
        view.setTag(viewHolder);
    }

    public View get(int id) {
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return childView;
    }

    public View getConvertView() {
        return view;
    }

}
