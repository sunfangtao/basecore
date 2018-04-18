package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wxt.library.R;
import com.wxt.library.model.CustomSavedState;
import com.wxt.library.util.Util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/4.
 */

public class UPloadVerifyPicView extends RelativeLayout {

    public static final int NO_VERIFY = 1;
    public static final int VERIFYING = 2;
    public static final int VERIFY_FAIL = 3;
    public static final int VERIFY_SUCCESS = 4;
    public static final int VERIFY_EDIT = 5;

    @IntDef({NO_VERIFY, VERIFYING, VERIFY_FAIL, VERIFY_SUCCESS, VERIFY_EDIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface VERIFY {
    }

    private int picWidth;
    private int picHeight;
    private int textSize;
    private int textColor;
    private int drawablePadding;

    private ImageView backgroudImg;
    private TextView textView;
    private ImageView imageView;

    private ArrayList<Integer> textColorList;
    private ArrayList<Integer> photoPicResource;
    private ArrayList<String> text;
    private Object backgroundResource;
    private int curStatus;

    public UPloadVerifyPicView(Context context) {
        this(context, null);
    }

    public UPloadVerifyPicView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UPloadVerifyPicView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.UPloadVerifyPicView);
        curStatus = a.getResourceId(R.styleable.UPloadVerifyPicView_curStatus, 0);
        backgroundResource = a.getResourceId(R.styleable.UPloadVerifyPicView_backgroundResource, R.drawable.nullicon);
        picWidth = a.getDimensionPixelSize(R.styleable.UPloadVerifyPicView_picWidth, Util.dp2px(getContext(), 30));
        picHeight = a.getDimensionPixelSize(R.styleable.UPloadVerifyPicView_picHeight, Util.dp2px(getContext(), 20));
        textSize = a.getDimensionPixelSize(R.styleable.UPloadVerifyPicView_textSize, Util.sp2px(getContext(), 16));
        textColor = a.getColor(R.styleable.UPloadVerifyPicView_textColor, Color.parseColor("#000000"));
        drawablePadding = a.getDimensionPixelSize(R.styleable.UPloadVerifyPicView_drawablePadding, Util.dp2px(getContext(), 5));
        CharSequence[] values = a.getTextArray(R.styleable.UPloadVerifyPicView_text);
        a.recycle();

        textColorList = new ArrayList<>();
        textColorList.add(textColor);
        textColorList.add(textColor);
        textColorList.add(textColor);
        textColorList.add(textColor);
        textColorList.add(textColor);

        photoPicResource = new ArrayList<>();
        text = new ArrayList<>();

        photoPicResource.add(R.drawable.no_verify_pic);
        photoPicResource.add(R.drawable.verifing_pic);
        photoPicResource.add(R.drawable.verify_fail_pic);
        photoPicResource.add(R.drawable.verify_success_icon);
        photoPicResource.add(0);

        if (values != null) {
            if (values.length == 5) {
                text.add(values[0].toString());
                text.add(values[1].toString());
                text.add(values[2].toString());
                text.add(values[3].toString());
                text.add(values[4].toString());
            } else {
                throw new IllegalArgumentException("text 大小必须为5");
            }
        } else {
            text.add("1");
            text.add("2");
            text.add("3");
            text.add("大师傅");
            text.add("");
        }

        // 添加背景图
        backgroudImg = new ImageView(getContext());
        backgroudImg.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        backgroudImg.setScaleType(ImageView.ScaleType.FIT_XY);
        if (backgroundResource != null)
            backgroudImg.setImageResource((int) backgroundResource);
        addView(backgroudImg);

        // 添加拍照小图片和文字
        LinearLayout layout = new LinearLayout(getContext());
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        imageView = new ImageView(getContext());
        imageView.setLayoutParams(new LinearLayout.LayoutParams(picWidth, picHeight));
        layout.addView(imageView);

        textView = new TextView(getContext());
        textView.setTextColor(textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, drawablePadding, 0, 0);
        textView.setLayoutParams(textParams);
        layout.addView(textView);

        addView(layout);

        updateStatus(NO_VERIFY);
    }

    public void updateNoVerifyText(String s) {
        this.text.set(0, s);
        textView.setText(s);
    }

    public void updateTextColor(ArrayList<Integer> textColor) {
        if (textColor != null && textColor.size() == 5) {
            this.textColorList = textColor;
        } else {
            throw new IllegalArgumentException("textColor 大小必须为5");
        }
        textView.setTextColor(this.textColorList.get(curStatus - 1));
    }

    public void updatePicResource(ArrayList<Integer> photoPicResource) {
        if (photoPicResource != null && photoPicResource.size() == 5) {
            this.photoPicResource = photoPicResource;
        } else {
            throw new IllegalArgumentException("photoPicResource 大小必须为5");
        }
        imageView.setImageResource(this.photoPicResource.get(curStatus - 1));
    }

    public void updateTextResource(ArrayList<String> text) {
        if (text != null && text.size() == 5) {
            this.text = text;
            this.text.add("");
        } else {
            throw new IllegalArgumentException("text 大小必须为5");
        }
        textView.setText(text.get(curStatus - 1));
    }

    public void setBackgroundImgResource(Object backgroundImg) {
        this.backgroundResource = backgroundImg;
        Glide.with(getContext()).load(backgroundImg).into(backgroudImg);
    }

    public void updateStatus(@VERIFY int status) {
        if (this.curStatus != status) {
            this.curStatus = status;
            Glide.with(getContext()).load(this.backgroundResource).into(backgroudImg);
            imageView.setImageResource(this.photoPicResource.get(curStatus - 1));
            textView.setText(text.get(curStatus - 1));
            textView.setTextColor(textColorList.get(curStatus - 1));
        }
    }

    public int getStatus() {
        return curStatus;
    }

    public ImageView getBackgroudImg() {
        return backgroudImg;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        CustomSavedState ss = (CustomSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int index = 1;
        picWidth = (int) ss.childrenStates.get(index++);
        picHeight = (int) ss.childrenStates.get(index++);
        textSize = (int) ss.childrenStates.get(index++);
        textColor = (int) ss.childrenStates.get(index++);
        drawablePadding = (int) ss.childrenStates.get(index++);
        photoPicResource = (ArrayList<Integer>) ss.childrenStates.get(index++);
        text = (ArrayList<String>) ss.childrenStates.get(index++);
        backgroundResource = ss.childrenStates.get(index++);
        curStatus = (int) ss.childrenStates.get(index++);
        textColorList = (ArrayList<Integer>) ss.childrenStates.get(index++);
        Glide.with(getContext()).load(this.backgroundResource).into(backgroudImg);
        imageView.setImageResource(this.photoPicResource.get(curStatus - 1));
        textView.setText(text.get(curStatus - 1));
        textView.setTextColor(textColorList.get(curStatus - 1));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

    @Nullable
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomSavedState ss = new CustomSavedState(superState);
        ss.childrenStates = new SparseArray<Integer>();

        int index = 1;
        ss.childrenStates.put(index++, picWidth);
        ss.childrenStates.put(index++, picHeight);
        ss.childrenStates.put(index++, textSize);
        ss.childrenStates.put(index++, textColor);
        ss.childrenStates.put(index++, drawablePadding);
        ss.childrenStates.put(index++, photoPicResource);
        ss.childrenStates.put(index++, text);
        ss.childrenStates.put(index++, backgroundResource);
        ss.childrenStates.put(index++, curStatus);
        ss.childrenStates.put(index++, textColorList);
        return ss;
    }
}
