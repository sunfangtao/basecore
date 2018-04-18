package com.wxt.library.base.adapter.decoration;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class RecyclerViewItemDecoration extends RecyclerView.ItemDecoration {
    private int space = 0;
    private int index;

    public RecyclerViewItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        //该View在整个RecyclerView中位置。
        index = parent.getChildAdapterPosition(view);

        int spanCount = ((GridLayoutManager) parent.getLayoutManager()).getSpanCount();

        if (spanCount > 1) {

            if (index < spanCount) {
                // 第一行
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
            outRect.bottom = space;

            if (index % spanCount == spanCount - 1) {
                // 最后一列
                outRect.right = space;
            } else {
                outRect.right = 0;
            }
            outRect.left = space;
        } else {
            if (index == 0) {
                outRect.top = space;
            } else {
                outRect.top = 0;
            }
            outRect.bottom = space;
            outRect.left = 0;
            outRect.right = 0;
        }
    }
}