package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.Nullable;
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

    public DrawableCenterRadioButton(Context context) {
        this(context, null);
        setId(Util.getViewAutoId());
    }

    public DrawableCenterRadioButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomDrawableTextView);
        drawableWidth = (int) a.getDimension(R.styleable.CustomDrawableTextView_drawableWidth, 0);
        drawableHeight = (int) a.getDimension(R.styleable.CustomDrawableTextView_drawableHeight, 0);
        isCenter = a.getBoolean(R.styleable.CustomDrawableTextView_drawableTextCenter, false);
        setSingleLine();
        a.recycle();

        initDrawable();
    }

    private void initDrawable(){
        int drawableCount = 0;
        Drawable[] drawables = getCompoundDrawables();
        if (null != drawables) {

            Drawable drawableLeft = drawables[0];
            Drawable drawableTop = drawables[1];
            Drawable drawableRight = drawables[2];
            Drawable drawableBottom = drawables[3];

            if (null != drawableLeft) {
                drawableCount++;
                drawableLeft.setBounds(0, 0, drawableWidth, drawableHeight);
            } else if (null != drawableTop) {
                drawableCount++;
                drawableTop.setBounds(0, 0, drawableWidth, drawableHeight);
            } else if (null != drawableRight) {
                drawableCount++;
                drawableRight.setBounds(0, 0, drawableWidth, drawableHeight);
            } else if (null != drawableBottom) {
                drawableCount++;
                drawableBottom.setBounds(0, 0, drawableWidth, drawableHeight);
            }
            if (drawableCount > 0) {
                setCompoundDrawables(drawableLeft, drawableTop, drawableRight, drawableBottom);

                if (drawableCount == 1 && isCenter) {
                    if (null != drawableLeft) {
                        setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                    } else if (null != drawableTop) {
                        setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                    } else if (null != drawableRight) {
                        setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                    } else if (null != drawableBottom) {
                        setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                    }
                } else {
                    isCenter = false;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (isCenter) {
            Drawable[] drawables = getCompoundDrawables();
            if (null != drawables) {

                Drawable drawableLeft = drawables[0];
                Drawable drawableRight = drawables[2];

                if (null != drawableLeft || null != drawableRight) {

                    float textWidth = getPaint().measureText(getText().toString());

                    if (null == drawableLeft) {
                        float contentWidth = textWidth + getCompoundDrawablePadding() + drawableWidth;
                        if (getWidth() - contentWidth > 0) {
                            canvas.translate(-(getWidth() - contentWidth - getPaddingRight() - getPaddingLeft()) / 2, 0);
                        }
                    } else if (null == drawableRight) {
                        float contentWidth = textWidth + getCompoundDrawablePadding() + drawableWidth;
                        if (getWidth() - contentWidth > 0) {
                            canvas.translate((getWidth() - contentWidth - getPaddingRight() - getPaddingLeft()) / 2, 0);
                        }
                    } else {
                        setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                        float contentWidth = textWidth + getCompoundDrawablePadding() + drawableWidth + drawableWidth;
                        if (getWidth() - contentWidth > 0) {
                            canvas.translate((getWidth() - contentWidth - getPaddingRight() - getPaddingLeft()) / 2, 0);
                        }
                    }

                }

                Drawable drawableTop = drawables[1];
                Drawable drawableBottom = drawables[3];

                if (null != drawableTop || null != drawableBottom) {

                    if (null == drawableTop) {
                        float textHeight = getPaint().descent() - getPaint().ascent();
                        float contentHeight = textHeight + getCompoundDrawablePadding() + drawableHeight;
                        if (getHeight() - contentHeight > 0) {
                            canvas.translate(0, -(getHeight() - contentHeight - getPaddingBottom() - getPaddingTop()) / 2);
                        }
                    }
                    if (null == drawableBottom) {
                        Rect rect = new Rect();
                        getPaint().getTextBounds(getText().toString(), 0, getText().length(), rect);
                        float textHeight = getPaint().descent() - getPaint().ascent();
                        float contentHeight = textHeight + getCompoundDrawablePadding() + drawableHeight;
                        if (getHeight() - contentHeight > 0) {
                            canvas.translate(0, (getHeight() - contentHeight - getPaddingBottom() - getPaddingTop()) / 2);
                        }
                    }
                }

            }
        }
        super.onDraw(canvas);
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
