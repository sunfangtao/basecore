package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.model.MUtilItemData;
import com.wxt.library.priva.util.ForbidFastClick;
import com.wxt.library.retention.NotProguard;
import com.wxt.library.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2018/4/21.
 */

public class MutilItemView extends LinearLayout {

    private LayoutInflater inflater;

    private int topBottomLineColor;
    private int centerLineColor;
    private int leftPadding;
    private int rightPadding;

    private final int resourceId = R.layout.mutilitem_item;
    private final Map<Integer, Integer> resourceMap = new HashMap<>();

    private List<MUtilItemData> dataList;

    private ItemClickListener itemClickListener;

    public MutilItemView(Context context) {
        this(context, null);
    }

    public MutilItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initShader();
        init(attrs);
    }

    private void initShader() {
        // 去掉边界光晕效果
        setOverScrollMode(View.OVER_SCROLL_NEVER);

        setOrientation(LinearLayout.VERTICAL);

        inflater = LayoutInflater.from(getContext());
    }

    private void init(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MutilItemView);

        topBottomLineColor = a.getColor(R.styleable.MutilItemView_topBottomLineColor, Color.parseColor("#33000000"));
        centerLineColor = a.getColor(R.styleable.MutilItemView_centerLineColor, Color.parseColor("#26000000"));
        leftPadding = Math.max(0, getPaddingLeft() == 0 ? Util.dp2px(getContext(), 16) : getPaddingLeft());
        rightPadding = Math.max(0, getPaddingRight() == 0 ? Util.dp2px(getContext(), 16) : getPaddingRight());
        setPadding(0, 0, 0, 0);

        a.recycle();
    }

    private void addView() {
        if (dataList != null) {
            int length = dataList.size();
            ImageView topLine = new ImageView(getContext());
            topLine.setBackgroundColor(topBottomLineColor);
            topLine.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1));
            addView(topLine);
            for (int i = 0; i < length; i++) {
                View view = inflater.inflate(resourceMap.get(i) == null ? resourceId : resourceMap.get(i), this, false);
                view.setPadding(leftPadding, view.getPaddingTop(), rightPadding, view.getPaddingBottom());
                addView(view);
                view.setBackgroundResource(R.drawable.mutilitem_item_bg);
                view.setOnClickListener(new ClickListener(i));

                if (resourceMap.get(i) == null) {
                    if (dataList.get(i).getLeftDrawableId() > 0) {
                        view.findViewById(R.id.mutilitem_left_im).setBackgroundResource(dataList.get(i).getLeftDrawableId());
                    } else {
                        view.findViewById(R.id.mutilitem_left_im).setVisibility(View.GONE);
                    }
                    if (dataList.get(i).getRightDrawableId() > 0) {
                        view.findViewById(R.id.mutilitem_right_im).setBackgroundResource(dataList.get(i).getRightDrawableId());
                    } else {
                        view.findViewById(R.id.mutilitem_right_im).setVisibility(View.GONE);
                    }
                    if (dataList.get(i).getDescricption() != null) {
                        ((TextView) view.findViewById(R.id.mutilitem_description_tv)).setText(dataList.get(i).getDescricption());
                    } else {
                        view.findViewById(R.id.mutilitem_description_tv).setVisibility(View.GONE);
                    }
                    if (dataList.get(i).getTitle() != null) {
                        ((TextView) view.findViewById(R.id.mutilitem_title_tv)).setText(dataList.get(i).getTitle());
                    } else {
                        view.findViewById(R.id.mutilitem_title_tv).setVisibility(View.GONE);
                    }
                    if (dataList.get(i).getSubTitle() != null) {
                        ((TextView) view.findViewById(R.id.mutilitem_subtitle_tv)).setText(dataList.get(i).getSubTitle());
                    } else {
                        view.findViewById(R.id.mutilitem_subtitle_tv).setVisibility(View.GONE);
                    }
                }

                if (getContext() instanceof AddViewListener) {
                    ((AddViewListener) getContext()).onAddView(i, view);
                }

                if (i < length - 1) {
                    ImageView centerLine = new ImageView(getContext());
                    centerLine.setBackgroundColor(centerLineColor);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1);
                    params.setMargins(leftPadding, 0, 0, 0);
//                    centerLine.setPadding(leftPadding, 0, 0, 0);
                    centerLine.setLayoutParams(params);
                    addView(centerLine);
                }
            }
            ImageView bottomLine = new ImageView(getContext());
            bottomLine.setBackgroundColor(topBottomLineColor);
            bottomLine.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 1));
            bottomLine.setPadding(leftPadding, 0, 0, 0);
            addView(bottomLine);
        }
    }

    @NotProguard
    public void setDataLayout(int position, int resourceId) {
        if (position >= 0 && resourceId > 0) {
            resourceMap.put(position, resourceId);
        }
    }

    @NotProguard
    public void setDatas(List<MUtilItemData> dataList) {
        this.dataList = dataList;
        addView();
    }

    @NotProguard
    public void setItemClickListener(ItemClickListener listener) {
        this.itemClickListener = listener;
    }

    private class ClickListener implements View.OnClickListener {

        private int position;

        public ClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            if (ForbidFastClick.isFastDoubleClick()) {
                return;
            }
            if (itemClickListener != null) {
                itemClickListener.onItemView(position);
            }
        }
    }

    @NotProguard
    public interface AddViewListener {
        @NotProguard
        void onAddView(int position, View view);
    }

    @NotProguard
    public interface ItemClickListener {
        @NotProguard
        void onItemView(int position);
    }
}
