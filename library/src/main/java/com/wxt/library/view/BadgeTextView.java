package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.util.Util;

/**
 * Created by Administrator on 2017/12/22.
 */

public class BadgeTextView extends RelativeLayout {

    private TextView textView;
    private ImageView imageView;
    private TextView badgeTV;

    private CharSequence text;
    private int textSize;
    private int textColor;
    private Drawable background;
    private int imWidth;
    private int imHeight;
    private boolean showBadge;
    private String badge;
    private int badgeTextColor;
    private int badgeTextSize;
    private int badgeColor;
    private int drawablePadding;

    public BadgeTextView(Context context) {
        super(context);
        init();
    }

    public BadgeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BadgeTextView);
        text = a.getText(R.styleable.BadgeTextView_badge_text);
        textSize = a.getDimensionPixelOffset(R.styleable.BadgeTextView_badge_textSize, Util.sp2px(context, 16));
        textColor = a.getColor(R.styleable.BadgeTextView_badge_textColor, Color.parseColor("#FFFFFF"));
        background = a.getDrawable(R.styleable.BadgeTextView_badge_background);
        imWidth = a.getDimensionPixelOffset(R.styleable.BadgeTextView_badge_imgWidth, Util.dp2px(context, 40));
        imHeight = a.getDimensionPixelOffset(R.styleable.BadgeTextView_badge_imgHeight, Util.dp2px(context, 40));
        showBadge = a.getBoolean(R.styleable.BadgeTextView_badge_showBadge, false);
        badge = a.getString(R.styleable.BadgeTextView_badge_badge);
        badgeTextColor = a.getColor(R.styleable.BadgeTextView_badge_badgeTextColor, Color.parseColor("#FFFFFF"));
        badgeTextSize = a.getDimensionPixelOffset(R.styleable.BadgeTextView_badge_badgeTextSize, Util.sp2px(context, 12));
        badgeColor = a.getColor(R.styleable.BadgeTextView_badge_badgeColor, Color.parseColor("#FF0000"));
        drawablePadding = a.getDimensionPixelSize(R.styleable.BadgeTextView_badge_imPadding, Util.dp2px(context, 10));
        a.recycle();

        showBadge(showBadge);
        if (badge == null || badge.length() == 0)
            badge = "0";
        setBadge(badge);
        setBadgeTextColor(badgeTextColor);
        setBadgeTextSize(TypedValue.COMPLEX_UNIT_PX, badgeTextSize);
        setDrawable(background);
        setDrawableSize(imWidth, imHeight);
        setText(text);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        setTextColor(textColor);
        setDrawablePadding(drawablePadding);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(badgeColor);
        drawable.setCornerRadius(50);
        drawable.setShape(GradientDrawable.RECTANGLE);
        badgeTV.setBackgroundDrawable(drawable);
    }

    public BadgeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void showBadge(boolean isShow) {
        badgeTV.setVisibility(isShow ? View.VISIBLE : View.GONE);
    }

    public void setBadge(String badge) {
        badgeTV.setText(badge);
    }

    public void setBadgeTextColor(int color) {
        badgeTV.setTextColor(color);
    }

    public void setBadgeTextSize(int unit, int size) {
        badgeTV.setTextSize(unit, size);
        badgeTV.getLayoutParams().height = size + Util.dp2px(getContext(), 8);
        badgeTV.setMinWidth(badgeTV.getLayoutParams().height);
    }

    public void setDrawable(Drawable top) {
        imageView.setImageDrawable(top);
    }

    public void setDrawableSize(int width, int height) {
        imageView.getLayoutParams().width = width;
        imageView.getLayoutParams().height = height;
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public void setTextColor(int color) {
        textView.setTextColor(color);
    }

    public void setTextSize(int unit, int size) {
        textView.setTextSize(unit, size);
    }

    public void setDrawablePadding(int padding) {
        textView.setPadding(getPaddingLeft(), padding + getPaddingTop(), getPaddingRight(), getPaddingBottom());
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.badgetextview, null);
        imageView = view.findViewById(R.id.badge_textview_im);
        textView = view.findViewById(R.id.badge_textview_tv);
        badgeTV = view.findViewById(R.id.badge_textview_badge);

        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(view, params);
    }
}
