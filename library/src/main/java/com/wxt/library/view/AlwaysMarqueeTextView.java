package com.wxt.library.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.model.CustomSavedState;
import com.wxt.library.util.Util;

/**
 * 项目 BaseActivityApplication
 * Created by SunFangtao on 2016/12/14.
 */

@SuppressLint("AppCompatCustomView")
public class AlwaysMarqueeTextView extends TextView {

    // drawable 的宽度
    private int drawableWidth;
    // drawable 的高度
    private int drawableHeight;
    // drawable 和 文字是否居中
    private boolean center;

    // drawable 的宽度
    private int leftDrawableWidth;
    // drawable 的高度
    private int leftDrawableHeight;
    // drawable 的宽度
    private int rightDrawableWidth;
    // drawable 的高度
    private int rightDrawableHeight;
    // 文字图片的间距
    private int padding = -1;

    private int grivate = 0;
    private int calculatePadding = 0;
    private int calculateDrawablePadding = 0;

    private int offset = 0;
    private int leftOffset = 0;
    private int rightOffset = 0;

    private Rect leftRect;
    private Rect topRect;
    private Rect rightRect;
    private Rect bottomRect;

    private CharSequence charSequence;

    private float widthHeightRatio = 0;

    private int widthMeasureSpec = -1000, heightMeasureSpec = -1000;

    public void setDrawableWidth(int drawableWidth) {
        this.drawableWidth = drawableWidth;
        invalidate();
    }

    public void setDrawableHeight(int drawableHeight) {
        this.drawableHeight = drawableHeight;
        invalidate();
    }

    public void setHtmlText(CharSequence text) {
        setHtmlText(text, null);
    }

    public void setHtmlText(CharSequence text, Html.TagHandler handler) {
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

    public void setCenter(boolean center) {
        this.center = center;
        invalidate();
    }

    public AlwaysMarqueeTextView(Context context) {
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

    public AlwaysMarqueeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomDrawableTextView);
        drawableWidth = (int) a.getDimension(R.styleable.CustomDrawableTextView_drawableWidth, 0);
        drawableHeight = (int) a.getDimension(R.styleable.CustomDrawableTextView_drawableHeight, 0);
        leftDrawableWidth = (int) a.getDimension(R.styleable.CustomDrawableTextView_leftDrawableWidth, drawableWidth);
        leftDrawableHeight = (int) a.getDimension(R.styleable.CustomDrawableTextView_leftDrawableHeight, drawableHeight);
        rightDrawableWidth = (int) a.getDimension(R.styleable.CustomDrawableTextView_rightDrawableWidth, drawableWidth);
        rightDrawableHeight = (int) a.getDimension(R.styleable.CustomDrawableTextView_rightDrawableHeight, drawableHeight);
        center = a.getBoolean(R.styleable.CustomDrawableTextView_drawableTextCenter, false);
        widthHeightRatio = a.getFloat(R.styleable.CustomDrawableTextView_widthHeightRatio, 0f);
        boolean isRepeat = a.getBoolean(R.styleable.CustomDrawableTextView_drawableTextRepeat, false);
        a.recycle();

        padding = getCompoundDrawablePadding();

        if (isRepeat) {
            setSingleLine();
            setEllipsize(TextUtils.TruncateAt.MARQUEE);
            setMarqueeRepeatLimit(-1);
        } else {
            setSingleLine();
            setEllipsize(TextUtils.TruncateAt.MIDDLE);
        }
    }

    @Override
    public boolean isFocused() {
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
        Drawable drawableLeft = drawables[0];
        Drawable drawableTop = drawables[1];
        Drawable drawableRight = drawables[2];
        Drawable drawableBottom = drawables[3];
        if (drawableLeft != null && drawableRight != null) {
            // 居中失效,只修改图片大小
            if (leftDrawableWidth > 0 && leftDrawableHeight > 0 && leftRect != null) {
                if (leftOffset != (leftDrawableHeight - leftRect.height() + getPaddingTop()) / 2) {
                    leftOffset = (leftDrawableHeight - leftRect.height() + getPaddingTop()) / 2;
                    drawableLeft.setBounds(0, -leftOffset, leftDrawableWidth, leftDrawableHeight - leftOffset);
                }
                if ((getGravity() & Gravity.LEFT) == Gravity.LEFT && calculateDrawablePadding != padding + leftDrawableWidth - leftRect.width()) {
                    calculateDrawablePadding = padding + leftDrawableWidth - leftRect.width();
                    setCompoundDrawablePadding(calculateDrawablePadding);
                }
            }
            if (rightDrawableWidth > 0 && rightDrawableHeight > 0 && rightRect != null) {
                if (rightOffset != (rightDrawableHeight - rightRect.height() + getPaddingTop()) / 2) {
                    rightOffset = (rightDrawableHeight - rightRect.height() + getPaddingTop()) / 2;
                    drawableRight.setBounds(rightRect.width() - rightDrawableWidth, -rightOffset, rightRect.width(), rightDrawableHeight - rightOffset);
                }
                if ((getGravity() & Gravity.RIGHT) == Gravity.RIGHT && calculateDrawablePadding != padding + rightDrawableWidth - rightRect.width()) {
                    calculateDrawablePadding = padding + rightDrawableWidth - rightRect.width();
                    setCompoundDrawablePadding(calculateDrawablePadding);
                }
            }
        } else if (drawableLeft != null) {
            if (center) {
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
            if (center) {
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
            if (center) {
                if (center && grivate != (Gravity.RIGHT | Gravity.CENTER_VERTICAL)) {
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
            if (center) {
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
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (widthMeasureSpec == -1000 && heightMeasureSpec == -1000) {
            this.widthMeasureSpec = widthMeasureSpec;
            this.heightMeasureSpec = heightMeasureSpec;
        }
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
        widthMeasureSpec = (int) ss.childrenStates.get(index++);
        heightMeasureSpec = (int) ss.childrenStates.get(index++);
        drawableWidth = (int) ss.childrenStates.get(index++);
        drawableHeight = (int) ss.childrenStates.get(index++);
        center = (boolean) ss.childrenStates.get(index++);
        leftDrawableWidth = (int) ss.childrenStates.get(index++);
        leftDrawableHeight = (int) ss.childrenStates.get(index++);
        rightDrawableWidth = (int) ss.childrenStates.get(index++);
        rightDrawableHeight = (int) ss.childrenStates.get(index++);
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
        ss.childrenStates.put(index++, widthMeasureSpec);
        ss.childrenStates.put(index++, heightMeasureSpec);
        ss.childrenStates.put(index++, drawableWidth);
        ss.childrenStates.put(index++, drawableHeight);
        ss.childrenStates.put(index++, center);
        ss.childrenStates.put(index++, leftDrawableWidth);
        ss.childrenStates.put(index++, leftDrawableHeight);
        ss.childrenStates.put(index++, rightDrawableWidth);
        ss.childrenStates.put(index++, rightDrawableHeight);
        ss.childrenStates.put(index++, charSequence == null ? getText() : charSequence);

        return ss;
    }

}
