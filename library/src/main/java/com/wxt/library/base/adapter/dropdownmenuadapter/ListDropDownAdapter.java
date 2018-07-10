package com.wxt.library.base.adapter.dropdownmenuadapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.listener.DropDownItemClickListener;
import com.wxt.library.listener.DropDownSelectChangedListener;
import com.wxt.library.model.DropDownBean;
import com.wxt.library.util.Util;

import java.util.List;

public class ListDropDownAdapter extends BaseDropDownAdapter {

    // 单个点击的监听
    private DropDownItemClickListener dropDownItemClickListener;
    // 单个切换的监听
    private DropDownSelectChangedListener dropDownSelectChangedListener;
    //
    private int checkItemPosition = -1;

    public final void setDropDownSelectChangedListener(DropDownSelectChangedListener listener) {
        this.dropDownSelectChangedListener = listener;
    }

    public final void setDropDownItemClickListener(DropDownItemClickListener listener) {
        this.dropDownItemClickListener = listener;
    }

    public <T extends DropDownBean> ListDropDownAdapter(Context context, List<T> list) {
        super(context, list);
    }

    @Override
    public final void clearSelect() {
        if (checkItemPosition == -1) {
            return;
        }
        notifyDataSetChanged();
        checkItemPosition = -1;
        if (menu != null) {
            // 始终保持顶部tab与选中的一致
            menu.setTabText(getTableText(checkItemPosition), viewIndex);
            menu.closeMenu();
        }
    }

    @Override
    public final int onCreateViewLayoutID(int viewType) {
        return R.layout.item_default_drop_down;
    }

    @Override
    public void onBindViewHolder(int viewType, View view, final int position) {
        TextView textView = view.findViewById(R.id.text);
        textView.setText(((DropDownBean) getObject(position)).getShowString());
        if (isSelectedItem(position)) {
            textView.setTextColor(context.getResources().getColor(R.color.drop_down_selected));
            textView.setBackgroundResource(R.color.check_bg);
        } else {
            textView.setTextColor(context.getResources().getColor(R.color.drop_down_unselected));
            textView.setBackgroundResource(R.color.white);
        }

        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (menu != null) {
                    if (menu.getSelectPosition() != 1 && menu.getSelectPosition() / 2 == viewIndex) {
                        // 防止多点点击出现问题,只响应对应的列表
                        setCheckItemPosition(position);
                        notifyDataSetChanged();
                        // 始终保持顶部tab与选中的一致
                        menu.setTabText(getTableText(checkItemPosition));
                        menu.closeMenu();
                        if (dropDownItemClickListener != null) {
                            dropDownItemClickListener.onDropDownItemClick(ListDropDownAdapter.this, v, position);
                        } else if (context instanceof DropDownItemClickListener) {
                            ((DropDownItemClickListener) context).onDropDownItemClick(ListDropDownAdapter.this, v, position);
                        }
                    }
                }
            }
        });
    }

    public final void setCheckItemPosition(int position, boolean isCallBack) {
        if (checkItemPosition != position) {
            int oldPosition = checkItemPosition;
            checkItemPosition = position;

            if (isCallBack) {
                if (dropDownSelectChangedListener != null) {
                    dropDownSelectChangedListener.onSelectChanged(this, checkItemPosition, oldPosition);
                } else if (context instanceof DropDownSelectChangedListener) {
                    ((DropDownSelectChangedListener) context).onSelectChanged(this, position, oldPosition);
                }
            }
            if (menu != null) {
                menu.setTabText(getTableText(checkItemPosition), viewIndex);
            }
            notifyDataSetChanged();
        }
    }

    public final void setCheckItemPosition(int position) {
        setCheckItemPosition(position, true);
    }

    public final int getCheckItemPosition() {
        return checkItemPosition;
    }

    public final void updateData(List<?> list) {
        if (list == null) {
            return;
        }
        checkItemPosition = -1;
        super.updateData(list);
    }

    protected final boolean isSelectedItem(int position) {
        if (checkItemPosition == position) {
            return true;
        }
        return false;
    }

}
