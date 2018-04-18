package com.wxt.library.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.wxt.library.R;
import com.wxt.library.retention.NotProguard;

/**
 * Created by Administrator on 2017/6/14.
 */
public class BottomMenu {

    private Activity context;
    private LayoutInflater inflater;
    private View mPopupWindow;
    private float alpha = 0.5f;
    private int backgroundColor = 0x44000000;
    private PopupWindow popupWindow;

    public BottomMenu(Activity context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public BottomMenu setShowBackgroundAlpha(float alpha) {
        this.alpha = alpha;
        return this;
    }

    private void setBackgroundAlpha(float startAlpha, float bgAlpha) {

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(startAlpha, bgAlpha);
        valueAnimator.setDuration(200);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            // ValueAnimator需要自己在监听处理中设置对象参数
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                WindowManager.LayoutParams lp = context.getWindow().getAttributes();
                lp.alpha = (Float) animation.getAnimatedValue();
                context.getWindow().setAttributes(lp);
            }
        });
        valueAnimator.start();
    }

    
    public BottomMenu setBackgroundColor(int color) {
        this.backgroundColor = color;
        return this;
    }

    public void afterShow() {

    }

    public View findViewById(int id) {
        if (id < 0) {
            return null;
        }
        return mPopupWindow.findViewById(id);
    }

    public BottomMenu showPopupWindow(int layoutId) {
        try {
            mPopupWindow = inflater.inflate(layoutId, null);

            popupWindow = new PopupWindow(null, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

            popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    // popupWindow隐藏时恢复屏幕正常透明度
                    setBackgroundAlpha(alpha, 1.0f);
                }
            });

            // 设置SelectPicPopupWindow的View
            popupWindow.setContentView(mPopupWindow);
            // 设置SelectPicPopupWindow弹出窗体可点击
            popupWindow.setFocusable(true);
            // 设置SelectPicPopupWindow弹出窗体动画效果
            popupWindow.setAnimationStyle(R.style.PopupAnimation);
            // 实例化一个ColorDrawable颜色为半透明
            ColorDrawable dw = new ColorDrawable(backgroundColor);
            // 设置SelectPicPopupWindow弹出窗体的背景
            popupWindow.setBackgroundDrawable(dw);

            View rootView = ((ViewGroup) context.findViewById(android.R.id.content)).getChildAt(0);
            popupWindow.showAtLocation(rootView, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);

            setBackgroundAlpha(1.0f, alpha);//设置屏幕透明度
            afterShow();
        } catch (Exception e) {

        }
        return this;
    }

    public void dismiss() {
        if (popupWindow != null)
            popupWindow.dismiss();
    }

}
