package com.wxt.library.tree.pagetree;

import android.content.Context;

import com.wxt.library.base.adapter.BaseAdapter;

import java.util.List;

/**
 * Created by Administrator on 2018/3/6.
 */

public abstract class BasePageTreeAdapter<T extends PageTreeNode> extends BaseAdapter {

    protected PageTreeHelper pageTreeHelper;

    public BasePageTreeAdapter(Context context, List<T> list, PageTreeHelper pageTreeHelper) {
        super(context, list);
        this.pageTreeHelper = pageTreeHelper;
    }

}
