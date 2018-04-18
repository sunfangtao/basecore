package com.wxt.library.base.adapter;

import android.content.Context;
import android.view.View;

import com.wxt.library.listener.DropDownItemClickListener;
import com.wxt.library.listener.DropDownSelectChangedListener;
import com.wxt.library.view.DropDownMenu;

import java.util.List;

public abstract class BaseDropDownAdapter extends BaseAdapter {

    private int viewIndex;
    private DropDownSelectChangedListener listener;
    private DropDownMenu menu;
    private String tabText = "";
    private int checkItemPosition = 0;

    public final void setCheckItemPosition(int position, boolean isCallBack) {
        if (checkItemPosition != position) {
            int oldPosition = checkItemPosition;
            checkItemPosition = position;

            if (isCallBack) {
                if (listener != null) {
                    listener.onSelectChanged(this, checkItemPosition, oldPosition);
                } else if (context instanceof DropDownSelectChangedListener) {
                    ((DropDownSelectChangedListener) context).onSelectChanged(this, position, oldPosition);
                }
            }
        }
    }

    public final void setCheckItemPosition(int position) {
        setCheckItemPosition(position, true);
    }

    public final void setViewIndex(int viewIndex) {
        this.viewIndex = viewIndex;
    }

    public final void setDropDownSelectChangedListener(DropDownSelectChangedListener listener) {
        this.listener = listener;
    }

    public final int getCheckItemPosition() {
        return checkItemPosition;
    }

    public final void setDropDownView(DropDownMenu menu) {
        this.menu = menu;
    }

    public final void updateDropDownData(List<String> list) {
        checkItemPosition = -1;
        updateData(list);
    }

    public final void setTabText(String tabText) {
        this.tabText = tabText;
    }

    public BaseDropDownAdapter(Context context, List<String> list) {
        super(context, list);
    }

    public final String getTableText(int position) {
        return position == 0 ? tabText : (String) getObjcet(position);
    }

    protected final boolean isSelectedItem(int position) {
        if (checkItemPosition == position) {
            return true;
        }
        return false;
    }

    @Override
    public void onBindViewHolder(final RVHolder holder, int var2) {
        this.onBindViewHolder(holder.getViewType(), holder.getViewHolder().getConvertView(), var2);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (menu.getSelectPosition() != 1 && menu.getSelectPosition() / 2 == viewIndex) {
                    // 防止多点点击出现问题,只响应对应的列表
                    int position = holder.getAdapterPosition();
                    if (menu != null) {
                        setCheckItemPosition(position);
                        notifyDataSetChanged();
                        // 始终保持顶部tab与选中的一致
                        menu.setTabText(getTableText(checkItemPosition));
                        menu.closeMenu();
                    }
                    if (context instanceof DropDownItemClickListener) {
                        ((DropDownItemClickListener) context).onDropDownItemClick(BaseDropDownAdapter.this, v, position);
                    }
                }
            }
        });
    }
}
