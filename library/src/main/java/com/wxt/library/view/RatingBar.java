package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.wxt.library.R;
import com.wxt.library.model.CustomSavedState;
import com.wxt.library.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/5/3.
 */

public class RatingBar extends LinearLayout {

    // 星星之间的间隔
    private int offset = 10;
    // 星星个数
    private int starCount;
    // 星星大小
    private int starImageSize;
    // 空的drawable
    private Drawable starEmptyDrawable;
    // 满的drawable
    private Drawable starFillDrawable;
    //
    private float value;
    //
    private boolean isFromUser;

    private ArrayList<RatingStar> imageList;

    public RatingBar(Context context) {
        this(context, null);
        setId(Util.getViewAutoId());
    }

    public RatingBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.RatingBar);

        starImageSize = (int) mTypedArray.getDimension(R.styleable.RatingBar_starImageSize, Util.dp2px(getContext(), 20));
        starCount = mTypedArray.getInteger(R.styleable.RatingBar_starCount, 5);
        starEmptyDrawable = mTypedArray.getDrawable(R.styleable.RatingBar_starEmpty);
        starFillDrawable = mTypedArray.getDrawable(R.styleable.RatingBar_starFill);
        isFromUser = mTypedArray.getBoolean(R.styleable.RatingBar_isFormUser, false);

        mTypedArray.recycle();

        if (starEmptyDrawable == null) {
            starEmptyDrawable = getResources().getDrawable(R.drawable.ratingbar_empty);
        }
        if (starFillDrawable == null) {
            starFillDrawable = getResources().getDrawable(R.drawable.ratingbar_fill);
        }

        initEmptyStar();
    }

    private void initEmptyStar() {
        imageList = new ArrayList<>();
        setOrientation(LinearLayout.HORIZONTAL);
        for (int i = 0; i < starCount; i++) {
            RatingStar imageView = new RatingStar(getContext());
            LayoutParams imageParams = new LayoutParams(starImageSize, starImageSize);
            imageParams.gravity = Gravity.CENTER_VERTICAL;
            if (i > 0) {
                //增加星星的间隔
                imageParams.leftMargin = offset;
            }
            imageView.setLayoutParams(imageParams);
            addView(imageView, i);
            imageList.add(imageView);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isFromUser) {
            final float x = event.getX();

            int i = 1;
            while ((x - getPaddingLeft()) > (starImageSize + offset) * i) {
                i++;
                if (i >= imageList.size()) {
                    break;
                }
            }
            float radus = (x - getPaddingLeft()) - (i - 1) * (starImageSize + offset);
            float value = i - 1 + (radus > starImageSize ? 1 : radus / starImageSize);
            setValue(value);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setValue(float value) {
        this.value = Math.min(Math.max(value, 0), starCount);
        updateValue();
    }

    private void updateValue() {
        if (value == 0) {
            for (RatingStar view : imageList) {
                view.setPercent(0f);
            }
        } else if (value == starCount) {
            for (RatingStar view : imageList) {
                view.setPercent(1f);
            }
        } else {
            // 浮点数的整数部分
            int fint = (int) value;
            BigDecimal b1 = new BigDecimal(Float.toString(value));
            BigDecimal b2 = new BigDecimal(Integer.toString(fint));
            for (int i = 0; i < fint; i++) {
                imageList.get(i).setPercent(1f);
            }
            // 浮点数的小数部分
            float fPoint = b1.subtract(b2).floatValue();
            if (fPoint > 0) {
                imageList.get(fint).setPercent(fPoint);
                for (int i = fint + 1; i < imageList.size(); i++) {
                    imageList.get(i).setPercent(0f);
                }
            } else {
                for (int i = fint; i < imageList.size(); i++) {
                    imageList.get(i).setPercent(0f);
                }
            }
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        CustomSavedState ss = (CustomSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int index = 1;
        setValue((Float) ss.childrenStates.get(index++));
    }

    @Nullable
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomSavedState ss = new CustomSavedState(superState);
        ss.childrenStates = new SparseArray<Integer>();

        int index = 1;
        ss.childrenStates.put(index++, value);

        return ss;
    }

    private class RatingStar extends FrameLayout {

        private ClipDrawable fillDrawable;

        public RatingStar(Context context) {
            this(context, null);
        }

        public RatingStar(Context context, @Nullable AttributeSet attrs) {
            this(context, attrs, 0);
        }

        public RatingStar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);

            ImageView emptyView = new ImageView(getContext());
            emptyView.setImageDrawable(starEmptyDrawable);

            fillDrawable = new ClipDrawable(starFillDrawable, Gravity.LEFT, ClipDrawable.HORIZONTAL);
            ImageView fillView = new ImageView(getContext());
            fillView.setImageDrawable(fillDrawable);

            addView(emptyView, 0);
            addView(fillView, 1);

            setPercent(0);
        }

        public void setPercent(float percent) {
            fillDrawable.setLevel((int) (10000 * percent));
        }

    }
}
