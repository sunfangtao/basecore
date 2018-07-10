package com.wxt.library.base.adapter.dropdownmenuadapter;

import android.content.Context;

import com.wxt.library.base.adapter.BaseAdapter;
import com.wxt.library.model.DropDownBean;
import com.wxt.library.util.Util;
import com.wxt.library.view.DropDownMenu;

import java.util.List;

public abstract class BaseDropDownAdapter extends BaseAdapter {

    // RecyclerView所在的Index
    protected int viewIndex;
    // 下拉控件
    protected DropDownMenu menu;
    // 标题
    protected String tabText = "";

    public final void setViewIndex(int viewIndex) {
        this.viewIndex = viewIndex;
    }

    public final void setDropDownView(DropDownMenu menu) {
        this.menu = menu;
    }

    public final void setTabText(String tabText) {
        this.tabText = tabText;
    }

    public <T extends DropDownBean> BaseDropDownAdapter(Context context, List<T> list) {
        super(context, list);
    }

    public String getTableText(int position) {
        return position <= 0 ? tabText : ((DropDownBean) getObject(position)).getShowString();
    }

    @Override
    public void onBindViewHolder(final RVHolder holder, int var2) {
        // 重写此方法，去掉默认的点击和长按事件
        this.onBindViewHolder(holder.getViewType(), holder.getViewHolder().getConvertView(), var2);
    }

    public abstract void clearSelect();
}
