package com.wxt.library.priva.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.wxt.library.util.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2018/4/11.
 */

public class ReturnBtnView extends View {

    private int width;
    private int height;

    private float widthOffsetRate = 0.38f;
    private float heightOffsetRate = 0.25f;

    private float lineWidth;

    private double angle;

    private float sinAngle, cosAngle, tanAngle;

    List<Integer> list = new ArrayList<>();

    public ReturnBtnView(Context context) {
        super(context);
        initAngle();
    }

    public ReturnBtnView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAngle();
    }

    public ReturnBtnView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAngle();
    }

    private void initAngle() {
        float value = height * (1 - heightOffsetRate * 2) / 2 / (width * (1 - widthOffsetRate * 2));
        angle = Math.atan(value);
        sinAngle = (float) Math.sin(angle);
        cosAngle = (float) Math.cos(angle);
        tanAngle = value;

        list.add(32);
        list.add(36);
        list.add(48);
        list.add(96);
        list.add(108);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 实例化路径
        Path mPath = new Path();

        Point point0 = new Point(width - width * widthOffsetRate, height * heightOffsetRate);
//        Point point1 = new Point(point0.x, point0.y + lineWidth / sinAngle);
        Point point1 = new Point(point0.x + lineWidth * sinAngle, point0.y + lineWidth * cosAngle);
        Point point2 = new Point(width - width * widthOffsetRate - (height / 2 - point0.y - lineWidth / sinAngle) / tanAngle, height / 2);
        Point point3 = new Point(point1.x, height - point1.y);
        Point point4 = new Point(point0.x, height - height * heightOffsetRate);
        Point point5 = new Point(width * widthOffsetRate, height / 2);
        mPath.moveTo(point0.x, point0.y);
        // 连接路径到点
        mPath.lineTo(point1.x, point1.y);
        mPath.lineTo(point2.x, point2.y);
        mPath.lineTo(point3.x, point3.y);
        mPath.lineTo(point4.x, point4.y);
        mPath.lineTo(point5.x, point5.y);
        // 闭合曲线
        mPath.close();
        // 绘制路径
        canvas.drawPath(mPath, new Paint(Paint.ANTI_ALIAS_FLAG));

    }

    private class Point {
        public float x;
        public float y;

        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        switch (list.get(0)) {
            case 108:
                lineWidth = 6.7f;
                break;
            case 96:
                lineWidth = 6;
                break;
            case 64:
                lineWidth = 4f;
                break;
            case 48:
                lineWidth = 3f;
                break;
            case 36:
                lineWidth = 2.25f;
                break;
            default:
                lineWidth = 2f;
        }
        width = list.get(0);
        height = list.get(0);
        setMeasuredDimension(width, height);
    }
}
