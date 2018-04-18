package com.wxt.library.tree.listener;

import com.wxt.library.tree.pagetree.PageTreeNode;

import java.util.List;

/**
 * Created by Administrator on 2018/3/6.
 */

public interface PageTreeUpdateListener<T extends PageTreeNode> {
    void onUpdateFinish(List<T> nodeList);
}
