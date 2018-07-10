package com.wxt.library.base.adapter.dropdownmenuadapter;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.listener.DropDownMutilConfirmListener;
import com.wxt.library.listener.DropDownMutilSelectChangedListener;
import com.wxt.library.model.DropDownBean;
import com.wxt.library.util.Util;
import com.wxt.library.view.AlwaysMarqueeTextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MutilSelectDropDownAdapter extends BaseDropDownAdapter {

    // 列数
    private int spanCount;
    // 多个点击的监听
    private DropDownMutilConfirmListener dropDownItemClickListener;
    // 多个切换的监听
    private DropDownMutilSelectChangedListener dropDownSelectChangedListener;
    // 当前选中的index
    private String checkItemPosition = "";
    // 可选择的最大个数
    private int maxCount = 0;

    private Map<Integer, AlwaysMarqueeTextView> viewMap = new HashMap<>();

    public void setMaxSelectCount(int count) {
        if (count < 0) {
            count = 0;
        }
        this.maxCount = count;
    }

    public final void setDropDownSelectChangedListener(DropDownMutilSelectChangedListener listener) {
        this.dropDownSelectChangedListener = listener;
    }

    public final void setDropDownItemClickListener(DropDownMutilConfirmListener listener) {
        this.dropDownItemClickListener = listener;
    }

    public <T extends DropDownBean> MutilSelectDropDownAdapter(Context context, List<T> list, int spanCount) {
        super(context, list);
        this.spanCount = spanCount;
    }

    @Override
    public final int getItemCount() {
        return 1;
    }

    @Override
    public final int onCreateViewLayoutID(int viewType) {
        return R.layout.item_mutilselect_drop_down;
    }

    private AlwaysMarqueeTextView getTextView() {
        AlwaysMarqueeTextView textView = new AlwaysMarqueeTextView(context);
        textView.setRepeat(true);
        textView.setTextColor(context.getResources().getColor(R.color.drop_down_unselected));
        textView.setBackgroundResource(R.drawable.dropdown_noselect_frame);
        textView.setSingleLine();
        textView.setGravity(Gravity.CENTER);
        textView.setPadding(0, Util.dp2px(context, 8), 0, Util.dp2px(context, 8));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;
        params.gravity = Gravity.CENTER;
        textView.setLayoutParams(params);
        return textView;
    }

    public String getTabText() {
        StringBuffer sb = new StringBuffer();
        String[] positions = checkItemPosition.split(",");
        for (int i = 0; i < positions.length; i++) {
            try {
                int index = Integer.valueOf(positions[i]);
                if (getObject(index) != null) {
                    sb.append(((DropDownBean) getObject(index)).getShowString());
                    sb.append(",");
                }
            } catch (Exception e) {

            }
        }
        if (sb.length() > 0 && ",".equals(sb.toString().substring(sb.length() - 1))) {
            return sb.toString().substring(0, sb.length() - 1).toString();
        }
        return tabText;
    }

    @Override
    public void onBindViewHolder(int viewType, View view, final int position) {
        Button btn = view.findViewById(R.id.button);
        LinearLayout linearLayout = view.findViewById(R.id.layout);

        List<DropDownBean> valueList = (List<DropDownBean>) getList();
        int size = valueList.size();
        if (linearLayout.getChildCount() == 0 && size > 0) {
            int rowCount = (size % spanCount > 0 ? 1 : 0) + size / spanCount;
            for (int i = 0; i < rowCount - 1; i++) {
                LinearLayout innerLinearLayout = new LinearLayout(context);
                innerLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams innerLinearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                innerLinearLayoutParams.gravity = Gravity.CENTER_VERTICAL;

                if (i > 0) {
                    innerLinearLayoutParams.topMargin = Util.dp2px(context, 8);
                }
                for (int j = 0; j < spanCount; j++) {
                    AlwaysMarqueeTextView textView = getTextView();
                    textView.setText(valueList.get(i * spanCount + j).getShowString());

                    if (j > 0) {
                        View view1 = new View(context);
                        view1.setLayoutParams(new LinearLayout.LayoutParams(Util.dp2px(context, 8), 1));
                        innerLinearLayout.addView(view1);
                    }
                    viewMap.put(i * spanCount + j, textView);
                    innerLinearLayout.addView(textView);
                }
                linearLayout.addView(innerLinearLayout, innerLinearLayoutParams);
            }

            int count = size % spanCount;
            if (count == 0) count = spanCount;
            LinearLayout innerLinearLayout = new LinearLayout(context);
            LinearLayout.LayoutParams innerLinearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            innerLinearLayoutParams.gravity = Gravity.CENTER_VERTICAL;
            innerLinearLayoutParams.topMargin = Util.dp2px(context, 8);
            for (int j = 0; j < count; j++) {
                if (j > 0) {
                    View view1 = new View(context);
                    view1.setLayoutParams(new LinearLayout.LayoutParams(Util.dp2px(context, 8), 1));
                    innerLinearLayout.addView(view1);
                }
                AlwaysMarqueeTextView textView = getTextView();
                textView.setText(valueList.get((rowCount - 1) * spanCount + j).getShowString());
                viewMap.put((rowCount - 1) * spanCount + j, textView);
                innerLinearLayout.addView(textView);
            }
            linearLayout.addView(innerLinearLayout, innerLinearLayoutParams);
        }

        for (int i = 0; i < size; i++) {
            if (isSelectedItem(i)) {
                viewMap.get(i).setTextColor(context.getResources().getColor(R.color.drop_down_selected));
                viewMap.get(i).setBackgroundResource(R.drawable.dropdown_select_frame);
            } else {
                viewMap.get(i).setTextColor(context.getResources().getColor(R.color.drop_down_unselected));
                viewMap.get(i).setBackgroundResource(R.drawable.dropdown_noselect_frame);
            }
        }

        for (final int index : viewMap.keySet()) {
            final TextView textView = viewMap.get(index);
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String oldPosition = checkItemPosition;
                    if (checkItemPosition.contains("," + index + ",")) {
                        // 已经选中的
                        checkItemPosition = checkItemPosition.replace("," + index + ",", "");
                    } else {
                        if (maxCount > 0) {
                            int selectCount = checkItemPosition.split(",,").length;
                            if (selectCount >= maxCount && !TextUtils.isEmpty(checkItemPosition)) {
                                // 移除第一个
                                int dotIndex = checkItemPosition.substring(1).indexOf(",") + 1;
                                int removeIndex = Integer.valueOf(checkItemPosition.substring(0, dotIndex).replace(",", ""));
                                viewMap.get(removeIndex).setTextColor(context.getResources().getColor(R.color.drop_down_unselected));
                                viewMap.get(removeIndex).setBackgroundResource(R.drawable.dropdown_noselect_frame);

                                checkItemPosition = checkItemPosition.substring(dotIndex + 1);
                                oldPosition = checkItemPosition;
                            }
                        }
                        checkItemPosition += ("," + index + ",");
                    }

                    if (isSelectedItem(index)) {
                        textView.setTextColor(context.getResources().getColor(R.color.drop_down_selected));
                        textView.setBackgroundResource(R.drawable.dropdown_select_frame);
                    } else {
                        textView.setTextColor(context.getResources().getColor(R.color.drop_down_unselected));
                        textView.setBackgroundResource(R.drawable.dropdown_noselect_frame);
                    }

                    List<Integer> list = new ArrayList<>();
                    String[] positions = oldPosition.replace(",,", "@").replace(",", "").split("@");
                    for (int i = 0; i < positions.length; i++) {
                        try {
                            list.add(Integer.valueOf(positions[i]));
                        } catch (Exception e) {

                        }
                    }
                    if (dropDownSelectChangedListener != null) {
                        dropDownSelectChangedListener.onSelectChanged(MutilSelectDropDownAdapter.this, list, index);
                    } else if (context instanceof DropDownMutilSelectChangedListener) {
                        DropDownMutilSelectChangedListener listener = (DropDownMutilSelectChangedListener) context;
                        listener.onSelectChanged(MutilSelectDropDownAdapter.this, list, index);
                    }
                }
            });
        }

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (menu != null) {
                    if (menu.getSelectPosition() != 1 && menu.getSelectPosition() / 2 == viewIndex) {
                        notifyDataSetChanged();
                        // 始终保持顶部tab与选中的一致
                        String s = getTabText();
                        menu.setTabText(TextUtils.isEmpty(s) ? tabText : s);
                        menu.closeMenu();
                    }
                    List<Integer> list = new ArrayList<>();
                    String[] positions = checkItemPosition.split(",");
                    for (int i = 0; i < positions.length; i++) {
                        try {
                            list.add(Integer.valueOf(positions[i]));
                        } catch (Exception e) {

                        }
                    }
                    if (dropDownItemClickListener != null) {
                        dropDownItemClickListener.onSelectConfirm(MutilSelectDropDownAdapter.this, list);
                    } else if (context instanceof DropDownMutilConfirmListener) {
                        DropDownMutilConfirmListener listener = (DropDownMutilConfirmListener) context;
                        listener.onSelectConfirm(MutilSelectDropDownAdapter.this, list);
                    }
                }
            }
        });

    }

    public final String getCheckItemPosition() {
        return checkItemPosition;
    }

    public final void setCheckItemPosition(String checkItemPosition) {
        if (checkItemPosition != null && !checkItemPosition.equals(this.checkItemPosition)) {
            this.checkItemPosition = checkItemPosition;
        }
        if (menu != null) {
            // 始终保持顶部tab与选中的一致
            String s = getTabText();
//            Util.print("getTabText=" + getTabText() + " viewIndex=" + viewIndex + " tabText=" + tabText + " checkItemPosition=" + checkItemPosition);
            menu.setTabText(TextUtils.isEmpty(s) ? tabText : s, viewIndex);
        }
        notifyDataSetChanged();
    }

    public final void updateData(List<?> list) {
        if (list == null) {
            return;
        }
        checkItemPosition = "";
        super.updateData(list);
    }

    protected final boolean isSelectedItem(int position) {
        if (checkItemPosition.contains("," + position + ",")) {
            return true;
        }
        return false;
    }

    @Override
    public void clearSelect() {
        if (checkItemPosition == "") {
            return;
        }
        notifyDataSetChanged();
        checkItemPosition = "";
        if (menu != null) {
            // 始终保持顶部tab与选中的一致
            menu.setTabText(tabText, viewIndex);
            menu.closeMenu();
        }
    }
}
