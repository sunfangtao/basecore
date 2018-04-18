package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;

import com.wxt.library.R;
import com.wxt.library.model.CustomSavedState;
import com.wxt.library.util.Util;

/**
 * Created by Administrator on 2016/5/7.
 */
public class DrawableCenterRadioButton extends android.support.v7.widget.AppCompatRadioButton {

    // drawable 的宽度
    private int drawableWidth;
    // drawable 的高度
    private int drawableHeight;
    // drawable 和 文字是否居中
    private boolean isCenter;

    private int padding = -1;

    private int grivate = 0;
    private int calculatePadding = 0;
    private int calculateDrawablePadding = 0;

    private int offset = 0;

    private Rect leftRect;
    private Rect topRect;
    private Rect rightRect;
    private Rect bottomRect;

    public void setDrawableWidth(int drawableWidth) {
        this.drawableWidth = drawableWidth;
        invalidate();
    }

    public void setDrawableHeight(int drawableHeight) {
        this.drawableHeight = drawableHeight;
        invalidate();
    }

    public void setCenter(boolean center) {
        isCenter = center;
        invalidate();
    }

    public DrawableCenterRadioButton(Context context) {
        this(context, null);
        setId(Util.getViewAutoId());
    }

    @Override
    public void setCompoundDrawables(@Nullable Drawable left, @Nullable Drawable top, @Nullable Drawable right, @Nullable Drawable bottom) {
        if (left != null && leftRect == null) {
            leftRect = left.copyBounds();
        }
        if (top != null && topRect == null) {
            topRect = top.copyBounds();
        }
        if (right != null && rightRect == null) {
            rightRect = right.copyBounds();
        }
        if (bottom != null && bottomRect == null) {
            bottomRect = bottom.copyBounds();
        }
        super.setCompoundDrawables(left, top, right, bottom);
    }

    public DrawableCenterRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomDrawableTextView);
        drawableWidth = (int) a.getDimension(R.styleable.CustomDrawableTextView_drawableWidth, 0);
        drawableHeight = (int) a.getDimension(R.styleable.CustomDrawableTextView_drawableHeight, 0);
        isCenter = a.getBoolean(R.styleable.CustomDrawableTextView_drawableTextCenter, false);
        setSingleLine();
        a.recycle();

        padding = getCompoundDrawablePadding();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        Drawable[] drawables = getCompoundDrawables();
        Drawable drawableLeft = drawables[0];
        Drawable drawableTop = drawables[1];
        Drawable drawableRight = drawables[2];
        Drawable drawableBottom = drawables[3];
        if (drawableLeft != null) {
            if (isCenter) {
                if (grivate != (Gravity.LEFT | Gravity.CENTER_VERTICAL)) {
                    grivate = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                    setGravity(grivate);
                }
                if (calculatePadding != (int) (getWidth() - getBodyWidth(padding)) / 2) {
                    calculatePadding = (int) (getWidth() - getBodyWidth(padding)) / 2;
                    setPadding(calculatePadding, 0, 0, 0);
                }
            }
            if (drawableWidth > 0 && drawableHeight > 0 && leftRect != null) {
                // 用户指定宽度或者高度
                if (offset != (drawableHeight - leftRect.height()) / 2) {
                    offset = (drawableHeight - leftRect.height()) / 2;
                    drawableLeft.setBounds(0, -offset, drawableWidth, drawableHeight - offset);
                }
                if (calculateDrawablePadding != padding + drawableWidth - leftRect.width()) {
                    calculateDrawablePadding = padding + drawableWidth - leftRect.width();
                    setCompoundDrawablePadding(calculateDrawablePadding);
                }
            }
        } else if (drawableTop != null) {
            if (isCenter) {
                if (grivate != (Gravity.TOP | Gravity.CENTER_HORIZONTAL)) {
                    grivate = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
                    setGravity(grivate);
                }
                if (calculatePadding != (int) (getHeight() - getBodyHeight(padding)) / 2) {
                    calculatePadding = (int) (getHeight() - getBodyHeight(padding)) / 2;
                    setPadding(0, calculatePadding, 0, 0);
                }
            }
            if (drawableWidth > 0 && drawableHeight > 0 && topRect != null) {
                // 用户指定宽度或者高度
                if (offset != (drawableWidth - topRect.width()) / 2) {
                    offset = (drawableWidth - topRect.width()) / 2;
                    drawableTop.setBounds(-offset, 0, drawableWidth - offset, drawableHeight);
                }
                if (calculateDrawablePadding != padding + drawableHeight - topRect.height()) {
                    calculateDrawablePadding = padding + drawableHeight - topRect.height();
                    setCompoundDrawablePadding((int) (calculateDrawablePadding - getPaint().ascent() + getPaint().getFontMetrics().top));
                }
            }
        } else if (drawableRight != null) {
            if (isCenter) {
                if (isCenter && grivate != (Gravity.RIGHT | Gravity.CENTER_VERTICAL)) {
                    grivate = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                    setGravity(grivate);
                }
                if (calculatePadding != (int) (getWidth() - getBodyWidth(padding)) / 2) {
                    calculatePadding = (int) (getWidth() - getBodyWidth(padding)) / 2;
                    setPadding(0, 0, calculatePadding, 0);
                }
            }
            if (drawableWidth > 0 && drawableHeight > 0 && rightRect != null) {
                if (offset != (drawableHeight - rightRect.height()) / 2) {
                    offset = (drawableHeight - rightRect.height()) / 2;
                    drawableRight.setBounds(rightRect.width() - drawableWidth, -offset, rightRect.width(), drawableHeight - offset);
                }
                if (calculateDrawablePadding != padding + drawableWidth - rightRect.width()) {
                    calculateDrawablePadding = padding + drawableWidth - rightRect.width();
                    setCompoundDrawablePadding(calculateDrawablePadding);
                }
            }
        } else if (drawableBottom != null) {
            if (isCenter) {
                if (grivate != (Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL)) {
                    grivate = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    setGravity(grivate);
                }
                if (calculatePadding != (int) (getHeight() - getBodyHeight(padding)) / 2) {
                    calculatePadding = (int) (getHeight() - getBodyHeight(padding)) / 2;
                    setPadding(0, 0, 0, calculatePadding);
                }
            }
            if (drawableWidth > 0 && drawableHeight > 0 && bottomRect != null) {
                if (offset != (drawableWidth - bottomRect.width()) / 2) {
                    offset = (drawableWidth - bottomRect.width()) / 2;
                    drawableBottom.setBounds(-offset, bottomRect.height() - drawableHeight, drawableWidth - offset, bottomRect.height());
                }
                if (calculateDrawablePadding != padding + drawableHeight - bottomRect.height()) {
                    calculateDrawablePadding = padding + drawableHeight - bottomRect.height();
                    setCompoundDrawablePadding((int) (calculateDrawablePadding - getPaint().getFontMetrics().bottom));
                }
            }
        }
        super.onDraw(canvas);
    }

    private float getBodyWidth(int drawablePadding) {
        float textWidth = measureWidth();
        return textWidth + this.drawableWidth + drawablePadding;
    }

    private float getBodyHeight(int drawablePadding) {
        float textHeight = -getPaint().getFontMetrics().ascent;
        return textHeight + this.drawableHeight + drawablePadding;
    }

    private float measureWidth() {
        Paint paint = new Paint();
        paint.setTextSize(getTextSize());
        if (!TextUtils.isEmpty(getText())) {
            return paint.measureText(getText().toString());
        } else {
            return 0;
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        CustomSavedState ss = (CustomSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int index = 1;
        drawableWidth = (int) ss.childrenStates.get(index++);
        drawableHeight = (int) ss.childrenStates.get(index++);
        isCenter = (boolean) ss.childrenStates.get(index++);
        invalidate();
    }

    @Nullable
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomSavedState ss = new CustomSavedState(superState);
        ss.childrenStates = new SparseArray<Integer>();

        int index = 1;
        ss.childrenStates.put(index++, drawableWidth);
        ss.childrenStates.put(index++, drawableHeight);
        ss.childrenStates.put(index++, isCenter);

        return ss;
    }
}
