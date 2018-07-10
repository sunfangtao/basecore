package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;

import com.wxt.library.R;
import com.wxt.library.model.CustomSavedState;
import com.wxt.library.util.Util;

/**
 * 项目 BaseActivityApplication
 * Created by SunFangtao on 2018/07/10.
 * 图片和文字同时居中的话，只能设置一张图片
 */

public class AlwaysMarqueeTextView extends AppCompatTextView {

    // drawable 的宽度
    private int drawableWidth;
    // drawable 的高度
    private int drawableHeight;
    // drawable 的宽度
    private int leftDrawableWidth;
    // drawable 的高度
    private int leftDrawableHeight;
    // drawable 的宽度
    private int rightDrawableWidth;
    // drawable 的高度
    private int rightDrawableHeight;
    // drawable 的宽度
    private int topDrawableWidth;
    // drawable 的高度
    private int topDrawableHeight;
    // drawable 的宽度
    private int bottomDrawableWidth;
    // drawable 的高度
    private int bottomDrawableHeight;
    // drawable 和 文字是否居中
    private boolean center;

    private CharSequence charSequence;

    private float widthHeightRatio = 0;

    public void setHtmlText(CharSequence text) {
        setHtmlText(text, null);
    }

    public void setHtmlText(CharSequence text, Html.TagHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("handler不能为空");
        }
        setHtmlText(text, false, handler);
    }

    public void setHtmlText(CharSequence text, boolean isSingle, Html.TagHandler handler) {
        setSingleLine(isSingle);
        this.charSequence = Html.fromHtml("<default>" + text.toString() + "</default>", null, handler);
        super.setText(this.charSequence);
    }

    public void setHtmlTextLineSpace(int space) {
        if (space > 0) {
            setLineSpacing(space, 1.0f);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                setHeight(getHeight() - space);
                setPadding(getPaddingLeft(), getPaddingTop() + Util.sp2px(getContext(), space / 2), getPaddingRight(), getPaddingBottom());
            }
        }
    }

    public AlwaysMarqueeTextView(Context context) {
        this(context, null);
        setId(Util.getViewAutoId());
    }

    public void setRepeat(Boolean isRepeat) {
        if (isRepeat) {
            setSingleLine();
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
            setMarqueeRepeatLimit(-1);
        } else {
            setSingleLine();
            setEllipsize(TextUtils.TruncateAt.MIDDLE);
        }
    }

    public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, com.wxt.library.R.styleable.CustomDrawableTextView);
        drawableWidth = (int) a.getDimension(com.wxt.library.R.styleable.CustomDrawableTextView_drawableWidth, 0);
        drawableHeight = (int) a.getDimension(com.wxt.library.R.styleable.CustomDrawableTextView_drawableHeight, 0);

        leftDrawableWidth = (int) a.getDimension(R.styleable.CustomDrawableTextView_leftDrawableWidth, drawableWidth);
        leftDrawableHeight = (int) a.getDimension(R.styleable.CustomDrawableTextView_leftDrawableHeight, drawableHeight);

        rightDrawableWidth = (int) a.getDimension(R.styleable.CustomDrawableTextView_rightDrawableWidth, drawableWidth);
        rightDrawableHeight = (int) a.getDimension(R.styleable.CustomDrawableTextView_rightDrawableHeight, drawableHeight);

        topDrawableWidth = (int) a.getDimension(R.styleable.CustomDrawableTextView_topDrawableWidth, drawableWidth);
        topDrawableHeight = (int) a.getDimension(R.styleable.CustomDrawableTextView_topDrawableHeight, drawableHeight);

        bottomDrawableWidth = (int) a.getDimension(R.styleable.CustomDrawableTextView_bottomDrawableWidth, drawableWidth);
        bottomDrawableHeight = (int) a.getDimension(R.styleable.CustomDrawableTextView_bottomDrawableHeight, drawableHeight);

        center = a.getBoolean(com.wxt.library.R.styleable.CustomDrawableTextView_drawableTextCenter, false);
        widthHeightRatio = a.getFloat(com.wxt.library.R.styleable.CustomDrawableTextView_widthHeightRatio, 0f);
        boolean isRepeat = a.getBoolean(com.wxt.library.R.styleable.CustomDrawableTextView_drawableTextRepeat, false);
        a.recycle();

        setRepeat(isRepeat);

        int drawableCount = 0;
        Drawable[] drawables = getCompoundDrawables();
        if (null != drawables) {

            Drawable drawableLeft = drawables[0];
            Drawable drawableTop = drawables[1];
            Drawable drawableRight = drawables[2];
            Drawable drawableBottom = drawables[3];

            if (null != drawableLeft) {
                drawableCount++;
                drawableLeft.setBounds(0, 0, leftDrawableWidth, leftDrawableHeight);
            }
            if (null != drawableTop) {
                drawableCount++;
                drawableTop.setBounds(0, 0, topDrawableWidth, topDrawableHeight);
            }
            if (null != drawableRight) {
                drawableCount++;
                drawableRight.setBounds(0, 0, rightDrawableWidth, rightDrawableHeight);
            }
            if (null != drawableBottom) {
                drawableCount++;
                drawableBottom.setBounds(0, 0, bottomDrawableWidth, bottomDrawableHeight);
            }
            if (drawableCount > 0) {
                setCompoundDrawables(drawableLeft, drawableTop, drawableRight, drawableBottom);

                if (drawableCount == 1 && center) {
                    if (null != drawableLeft) {
                        setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                    } else if (null != drawableTop) {
                        setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
                    } else if (null != drawableRight) {
                        setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
                    } else if (null != drawableBottom) {
                        setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
                    }
                } else {
                    center = false;
                }
            }
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (center) {
            Drawable[] drawables = getCompoundDrawables();
            if (null != drawables) {

                Drawable drawableLeft = drawables[0];
                Drawable drawableRight = drawables[2];

                if (null != drawableLeft || null != drawableRight) {

                    float textWidth = getPaint().measureText(getText().toString());

                    if (null == drawableLeft) {
                        float contentWidth = textWidth + getCompoundDrawablePadding() + rightDrawableWidth;
                        if (getWidth() - contentWidth > 0) {
                            canvas.translate(-(getWidth() - contentWidth - getPaddingRight() - getPaddingLeft()) / 2, 0);
                        }
                    } else if (null == drawableRight) {
                        float contentWidth = textWidth + getCompoundDrawablePadding() + leftDrawableWidth;
                        if (getWidth() - contentWidth > 0) {
                            canvas.translate((getWidth() - contentWidth - getPaddingRight() - getPaddingLeft()) / 2, 0);
                        }
                    } else {
                        setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
                        float contentWidth = textWidth + getCompoundDrawablePadding() + leftDrawableWidth + rightDrawableWidth;
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
                        float contentHeight = textHeight + getCompoundDrawablePadding() + bottomDrawableHeight;
                        if (getHeight() - contentHeight > 0) {
                            canvas.translate(0, -(getHeight() - contentHeight - getPaddingBottom() - getPaddingTop()) / 2);
                        }
                    }
                    if (null == drawableBottom) {
                        Rect rect = new Rect();
                        getPaint().getTextBounds(getText().toString(), 0, getText().length(), rect);
                        float textHeight = getPaint().descent() - getPaint().ascent();
                        float contentHeight = textHeight + getCompoundDrawablePadding() + topDrawableHeight;
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widthHeightRatio > 0) {
            super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        CustomSavedState ss = (CustomSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int index = 1;
        drawableWidth = (int) ss.childrenStates.get(index++);
        drawableHeight = (int) ss.childrenStates.get(index++);
        center = (boolean) ss.childrenStates.get(index++);
        leftDrawableWidth = (int) ss.childrenStates.get(index++);
        leftDrawableHeight = (int) ss.childrenStates.get(index++);
        rightDrawableWidth = (int) ss.childrenStates.get(index++);
        rightDrawableHeight = (int) ss.childrenStates.get(index++);
        topDrawableWidth = (int) ss.childrenStates.get(index++);
        topDrawableHeight = (int) ss.childrenStates.get(index++);
        bottomDrawableWidth = (int) ss.childrenStates.get(index++);
        bottomDrawableHeight = (int) ss.childrenStates.get(index++);

        CharSequence charSequence = (CharSequence) ss.childrenStates.get(index++);
        if (charSequence.toString().contains("\n")) {
            setSingleLine(false);
        }
        setText(charSequence);
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
        ss.childrenStates.put(index++, center);
        ss.childrenStates.put(index++, leftDrawableWidth);
        ss.childrenStates.put(index++, leftDrawableHeight);
        ss.childrenStates.put(index++, rightDrawableWidth);
        ss.childrenStates.put(index++, rightDrawableHeight);
        ss.childrenStates.put(index++, topDrawableWidth);
        ss.childrenStates.put(index++, topDrawableHeight);
        ss.childrenStates.put(index++, bottomDrawableWidth);
        ss.childrenStates.put(index++, bottomDrawableHeight);
        ss.childrenStates.put(index++, charSequence == null ? getText() : charSequence);

        return ss;
    }

}
