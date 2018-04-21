package com.wxt.library.guideviewpager;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.model.CustomSavedState;
import com.wxt.library.util.Util;

import java.util.List;

/**
 * Created by Administrator on 2018/4/20.
 */

public class GuideBanner extends RelativeLayout implements ViewPager.OnPageChangeListener {

    private ViewPager viewPager;
    private View[] viewArray;
    private ShapeDrawable selectDrawable;
    private ShapeDrawable noSelectDrawable;

    private int textViewSize;

    public GuideBanner(Context context) {
        this(context, null);
    }

    public GuideBanner(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViewPager();
    }

    private void initViewPager() {
        textViewSize = Util.dp2px(getContext(), 15);
        viewPager = new ViewPager(getContext());
        viewPager.addOnPageChangeListener(this);
        addView(viewPager);
    }

    public void setData(List<View> viewList) {
        GuideAdapter adapter = new GuideAdapter(viewList);
        viewPager.setAdapter(adapter);

        viewArray = new View[viewList.size()];

        LinearLayout linearLayout = new LinearLayout(getContext());
        RelativeLayout.LayoutParams linearLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        linearLayoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        linearLayoutParams.setMargins(0, getResources().getDisplayMetrics().heightPixels - textViewSize - Util.dp2px(getContext(), 15) - Util.getStatusHeight(getContext()), 0, 0);
        addView(linearLayout, linearLayoutParams);

        for (int i = 0; i < viewList.size(); i++) {
            TextView textView = new TextView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(textViewSize, textViewSize);
            if (i < viewList.size() - 1)
                params.rightMargin = Util.dp2px(getContext(), 5);
            textView.setLayoutParams(params);

            if (i == 0) {
                textView.setBackgroundDrawable(getSelectBK());
            } else {
                textView.setBackgroundDrawable(getNoSelectBK());
            }

            ((LinearLayout) getChildAt(1)).addView(textView);
            viewArray[i] = textView;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < viewPager.getAdapter().getCount(); i++) {
            if (i == position) {
                viewArray[i].setBackgroundDrawable(getSelectBK());
            } else {
                viewArray[i].setBackgroundDrawable(getNoSelectBK());
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private ShapeDrawable getSelectBK() {
        if (selectDrawable == null) {
            selectDrawable = new ShapeDrawable();
            selectDrawable.setShape(new OvalShape());
            selectDrawable.getPaint().setColor(getResources().getColor(R.color.colorAccent));
            selectDrawable.getPaint().setAntiAlias(true);
            selectDrawable.getPaint().setStyle(Paint.Style.FILL);
        }
        return selectDrawable;
    }

    private ShapeDrawable getNoSelectBK() {
        if (noSelectDrawable == null) {
            noSelectDrawable = new ShapeDrawable();
            noSelectDrawable.setShape(new OvalShape());
            noSelectDrawable.getPaint().setColor(Color.GRAY);
            noSelectDrawable.getPaint().setAntiAlias(true);
            noSelectDrawable.getPaint().setStyle(Paint.Style.FILL);
        }
        return noSelectDrawable;
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomSavedState ss = new CustomSavedState(superState);
        try {
            ss.childrenStates = new SparseArray<Integer>();
            int index = 1;
            ss.childrenStates.put(index++, viewPager.getCurrentItem());

        } catch (Exception e) {

        }
        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        CustomSavedState ss = (CustomSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int index = 1;
        int currentItem = (int) ss.childrenStates.get(index++);
        viewPager.setCurrentItem(currentItem);
    }
}
