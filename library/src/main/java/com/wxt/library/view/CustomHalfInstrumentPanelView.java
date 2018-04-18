package com.wxt.library.view;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.wxt.library.R;
import com.wxt.library.retention.NotProguard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/4/26.
 */

public class CustomHalfInstrumentPanelView extends View {

    // 最大刻度
    private float maxValue;
    // 最小刻度
    private float minValue;
    // 当前刻度
    private float value;
    // 刻度默认颜色
    private int scaleNeedleColor;
    // 刻度字体默认颜色
    private int scaleTextColor;
    // 刻度间隔（度数）
    private float majorTickStep;
    // 次刻度的个数
    private int minorTicks;
    // 仪表盘字体大小
    private float labelTextSize;
    // 仪表盘大小（相对于半圆，大为正，小为负）
    private int angle;
    // 指针颜色
    private int needleColor;
    // 可用的角度范围（居中）
    private float availableAngle;
    // 起始角度（x正向为0，顺时针为正）
    private float startDegree;
    // 最底层渐变背景宽度（环形）
    private float mBottomSweepWidth;
    // 最底层渐变背景宽度（内环形）
    private float mBottomInnerSweepWidth;
    // 顶层渐变背景（扫描遮罩）
    private int[] mTopShandowColor;
    // 顶层渐变位置（扫描遮罩）
    private float[] mTopShandowPosition;
    // 控件大小
    private int viewWidth;
    // 单位
    private String unit;
    // 数值字体颜色
    private int valueColor;
    // 数值字体大小
    private float valueTextSize;
    // 单位字体大小
    private float valueUnitTextSize;
    // 指针原点的x坐标
    private float needleCenterX;
    // 指针原点的y坐标
    private float needleCenterY;
    // 指针对应的圆半径
    private float needleRadis;
    // 指针上小圆半径
    private float needleArcRadis;
    // 圆弧对应的半径（view宽度的一半）
    private float arcRadis;
    // 圆弧对应的原点x坐标
    private float arcCenterX;
    // 圆弧对应的原点y坐标
    private float arcCenterY;
    // 刻度线画笔
    private Paint ticksAndTablePaint;
    // 刻度值画笔
    private Paint textPaint;
    //
    private TickInfo tickInfo;
    //
    private float yPosition;
    //
    private float yRadus;

    //
    private ArrayList<TickPoint> tickPointList;
    //
    private Map<Integer, ArrayList<TickPoint>> tickMinorPointMap;
    //
    private ArrayList<TickPoint> tickMajorMinorPointList;

    public class TickInfo {
        // 主刻度的长度
        public float majarTicksLength;
        // 次主刻度的长度
        public float minorMinorTicksLength;
        // 次刻度的长度
        public float minorTicksLength;
        // 主刻度之间的间隔(角度)
        public float majorStep;
        // 次刻度的间隔(角度)
        public float minorStep;
    }

    public class TickPoint {
        // 画刻度线时，刻度线外点x坐标
        public float x;
        // 画刻度线时，刻度线外点x坐标
        public float y;
        // 画刻度线时，刻度线与指针圆点的距离
        public float distance;
        // 与水平线的角度(弧度)
        public float degree;
        // 画刻度线时，刻度线内点x坐标
        public float innerX;
        // 画刻度线时，刻度线内点x坐标
        public float innerY;
    }

    public CustomHalfInstrumentPanelView(Context context) {
        this(context, null);
    }

    public CustomHalfInstrumentPanelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomHalfInstrumentPanelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InstrumentPanelView);

        maxValue = a.getFloat(R.styleable.InstrumentPanelView_maxValue, 16f);
        minValue = a.getFloat(R.styleable.InstrumentPanelView_minValue, 8f);
        value = a.getFloat(R.styleable.InstrumentPanelView_value, minValue);
        majorTickStep = a.getFloat(R.styleable.InstrumentPanelView_majorTickStep, 4f);
        minorTicks = a.getInt(R.styleable.InstrumentPanelView_minorTicks, 10);
        labelTextSize = a.getDimension(R.styleable.InstrumentPanelView_tickTextSize, 0);
        scaleNeedleColor = a.getColor(R.styleable.InstrumentPanelView_tickColor, Color.parseColor("#107BFF"));
        scaleTextColor = a.getColor(R.styleable.InstrumentPanelView_tickTextColor, Color.parseColor("#107BFF"));
        valueTextSize = a.getDimension(R.styleable.InstrumentPanelView_valueTextSize, 0);
        valueColor = a.getColor(R.styleable.InstrumentPanelView_valueTextColor, Color.parseColor("#FFFFFF"));
        valueUnitTextSize = a.getDimension(R.styleable.InstrumentPanelView_valueUnitTextSize, 0);
        angle = a.getInt(R.styleable.InstrumentPanelView_angle, -40);
        needleColor = a.getColor(R.styleable.InstrumentPanelView_needleColor, Color.parseColor("#FFFFFF"));
        mBottomInnerSweepWidth = a.getDimension(R.styleable.InstrumentPanelView_sweepWidth, 0);
        unit = a.getString(R.styleable.InstrumentPanelView_unit);
        yPosition = Math.min(Math.max(0, a.getFloat(R.styleable.InstrumentPanelView_centerY, 0.8f)), 1);
        yRadus = Math.min(a.getFloat(R.styleable.InstrumentPanelView_yRadus, 0.7f), yPosition);
        a.recycle();
    }

    private void setViewWidth(int width) {
        this.viewWidth = width;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public double getValue() {
        return value;
    }

    public void setValue(float value) {
        if (value < 0)
            throw new IllegalArgumentException(
                    "Non-positive value specified as a value.");
        if (value > maxValue)
            value = maxValue;
        this.value = value;
        invalidate();
    }

    @TargetApi(11)
    public ValueAnimator setValue(float value, boolean animate) {
        return setValue(value, 1000, 100);
    }

    @TargetApi(11)
    public ValueAnimator setValue(float value, long duration, long startDelay) {
        if (value < 0)
            throw new IllegalArgumentException(
                    "Non-positive value specified as a speed.");

        if (value > maxValue)
            value = maxValue;

        ValueAnimator va = ValueAnimator.ofObject(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return startValue + fraction * (endValue - startValue);
            }
        }, Double.valueOf(getValue()), Double.valueOf(value));

        va.setDuration(duration);
        va.setStartDelay(startDelay);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Double value = (Double) animation.getAnimatedValue();
                if (value != null)
                    setValue((float) (double) value);
            }
        });
        va.start();
        return va;
    }

    private RectF getOval(float factor) {
        int canvasWidth = viewWidth - getPaddingLeft() - getPaddingRight();
        RectF oval;
        oval = new RectF(0, 0, canvasWidth * factor, canvasWidth * factor);
        oval.offset(canvasWidth * (1 - factor) / 2, canvasWidth * (1 - factor) / 2);
        return oval;
    }

    public void setUint(String unit) {
        this.unit = unit;
    }

    public void setTopShandowColor(int[] colors) {
        if (colors == null || colors.length < 2) {
            return;
        }

        mTopShandowColor = new int[colors.length + 1];
        for (int i = 0; i < colors.length; i++) {
            mTopShandowColor[i] = colors[i];
        }
        mTopShandowColor[colors.length] = colors[colors.length - 1];

        mTopShandowPosition = new float[colors.length + 1];
        mTopShandowPosition[0] = 0f;
        mTopShandowPosition[colors.length] = 1f;
    }

    private void updateTopShandowPosition(float endValue) {
        if (mTopShandowPosition.length > 2)
            for (int i = 1; i < mTopShandowPosition.length - 1; i++) {
                mTopShandowPosition[i] = endValue * i / (mTopShandowPosition.length - 2);
            }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //　清空画板
        canvas.drawColor(Color.TRANSPARENT);
        drawTicksAndLables(canvas);
        drawShandow(canvas);
        drawNeedle(canvas);
        drawText(canvas);
    }

    private void init() {

        if ((maxValue - minValue) % majorTickStep != 0) {
            throw new RuntimeException("majorTickStep必须整除(maxValue-minValue)：majorTickStep=" + majorTickStep + " (maxValue-minValue)=" + (maxValue - minValue));
        }

        if (labelTextSize == 0) {
            labelTextSize = viewWidth / 22f;
        }
        if (valueTextSize == 0) {
            valueTextSize = viewWidth / 13f;
        }
        if (valueUnitTextSize == 0) {
            valueUnitTextSize = viewWidth / 10f;
        }
        if (mBottomSweepWidth == 0) {
            mBottomSweepWidth = viewWidth / 70f;
        }
        if (mBottomInnerSweepWidth == 0) {
            mBottomInnerSweepWidth = viewWidth / 100;
        }

        arcRadis = viewWidth / 2;
        arcCenterX = viewWidth / 2;
        arcCenterY = viewWidth / 2;

        needleRadis = arcRadis * yRadus;
        needleCenterX = arcCenterX;
        needleCenterY = arcCenterY * yPosition;


        needleArcRadis = viewWidth * 3 / 100f;

        availableAngle = 180 + 2 * angle;
        startDegree = 180 - angle;

        if (TextUtils.isEmpty(unit))
            unit = "KM/H";

        mTopShandowColor = new int[]{Color.parseColor("#aa38C4FD"), Color.parseColor("#aa107BFF"), Color.parseColor("#aa107BFF")};
        mTopShandowPosition = new float[]{0f, 0.5f, 1f};

        tickPointList = new ArrayList<>();
        tickMajorMinorPointList = new ArrayList<>();
        tickMinorPointMap = new HashMap<>();

        // 刻度线画笔
        ticksAndTablePaint = new Paint();
        ticksAndTablePaint.setAntiAlias(true);
        ticksAndTablePaint.setStrokeWidth(mBottomInnerSweepWidth);
        ticksAndTablePaint.setStyle(Paint.Style.STROKE);
        ticksAndTablePaint.setColor(scaleNeedleColor);

        // 刻度值画笔
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(scaleTextColor);
        textPaint.setTextSize(labelTextSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        RectF ticksAndTableRectF = getOval(1f - 2 * mBottomSweepWidth / viewWidth);
        tickInfo = new TickInfo();
        // 主刻度的长度
        tickInfo.majarTicksLength = ticksAndTableRectF.width() * 0.07f;
        // 次主刻度的长度
        tickInfo.minorMinorTicksLength = ticksAndTableRectF.width() * 0.05f;
        // 次刻度的长度
        tickInfo.minorTicksLength = ticksAndTableRectF.width() * 0.03f;
        // 主刻度之间的间隔(角度)
        tickInfo.majorStep = majorTickStep / (maxValue - minValue) * availableAngle;
        // 次刻度的间隔(角度)
        tickInfo.minorStep = tickInfo.majorStep / minorTicks;
    }

    /**
     * 大圆中的角度
     *
     * @param currentAngle
     * @return
     */
    private TickPoint getTransDegree(float currentAngle, float radis) {
        TickPoint tickPoint = new TickPoint();
        tickPoint.x = (float) (arcCenterX + Math.cos(currentAngle * Math.PI / 180) * arcRadis);
        tickPoint.y = (float) (arcCenterY + Math.sin(currentAngle * Math.PI / 180) * arcRadis);

        tickPoint.distance = (float) Math.sqrt(Math.pow(tickPoint.x - needleCenterX, 2) + Math.pow(tickPoint.y - needleCenterY, 2));

        float tickDegree = (float) (Math.asin((tickPoint.y - needleCenterY) / tickPoint.distance));

        if (tickPoint.x > needleCenterX) {
            tickDegree = (float) (Math.PI - tickDegree);
        }
        tickPoint.degree = tickDegree;

        tickPoint.innerX = (float) (needleCenterX + (Math.cos(Math.PI - tickPoint.degree) * ((tickPoint.distance - (mBottomSweepWidth / 2)) - radis)));
        tickPoint.innerY = (float) (needleCenterY + Math.sin(Math.PI - tickPoint.degree) * ((tickPoint.distance - mBottomSweepWidth / 2) - radis));

        tickPoint.x = (float) (needleCenterX + Math.cos(Math.PI - tickPoint.degree) * (tickPoint.distance - mBottomSweepWidth / 2));
        tickPoint.y = (float) (needleCenterY + Math.sin(Math.PI - tickPoint.degree) * (tickPoint.distance - mBottomSweepWidth / 2));

        return tickPoint;
    }

    protected void drawTicksAndLables(Canvas canvas) {

        RectF ticksAndTableArcRectF = getOval(1f - mBottomSweepWidth / viewWidth);

        // 主刻度宽度导致的额外角度(大约，可重新计算)
        float degree = (float) (ticksAndTableArcRectF.width() * 0.01f * 90 / Math.PI / ticksAndTableArcRectF.width());
        ticksAndTablePaint.setStrokeWidth(mBottomInnerSweepWidth);
        canvas.drawArc(ticksAndTableArcRectF, startDegree - degree, availableAngle + 2 * degree, false, ticksAndTablePaint);

        double curProgress = minValue;
        // +0.1保证int与float之间
        float endAngle = availableAngle + startDegree + 0.1f;

        int forCount = 0;
        // 主刻度所在的角度（x正向为0，顺时针为正）
        for (float currentAngle = startDegree; currentAngle <= endAngle; currentAngle += tickInfo.majorStep) {

            // 画主分割线
            ticksAndTablePaint.setStrokeWidth(ticksAndTableArcRectF.width() * 0.01f);
            if (tickPointList.size() <= forCount) {
                tickPointList.add(getTransDegree(currentAngle, tickInfo.majarTicksLength));
            }
            TickPoint tickPoint = tickPointList.get(forCount);
            canvas.drawLine(tickPoint.innerX, tickPoint.innerY, tickPoint.x, tickPoint.y, ticksAndTablePaint);

            // 画次分割线
            ticksAndTablePaint.setStrokeWidth(ticksAndTableArcRectF.width() * 0.003f);
            for (int i = 0; i < minorTicks; i++) {
                float angle = currentAngle + (i + 1) * tickInfo.minorStep;

                ArrayList<TickPoint> list = tickMinorPointMap.get(forCount);
                if (list == null) {
                    list = new ArrayList<>();
                    tickMinorPointMap.put(forCount, list);
                }
                if (list.size() <= i) {
                    list.add(getTransDegree(angle, tickInfo.minorTicksLength));
                }
                if (angle < endAngle) {
                    TickPoint tickMinorPoint = list.get(i);
                    canvas.drawLine(tickMinorPoint.innerX, tickMinorPoint.innerY, tickMinorPoint.x, tickMinorPoint.y, ticksAndTablePaint);
                }
            }

            // 画主次分割线
            ticksAndTablePaint.setStrokeWidth(ticksAndTableArcRectF.width() * 0.006f);
            float angle = currentAngle + tickInfo.majorStep / 2;
            if (tickMajorMinorPointList.size() <= forCount) {
                tickMajorMinorPointList.add(getTransDegree(angle, tickInfo.minorMinorTicksLength));
            }
            if (angle < endAngle) {
                // 最后一个主分割线画完后，不画次分割线
                TickPoint tickMajorMinorPoint = tickMajorMinorPointList.get(forCount);
                canvas.drawLine(tickMajorMinorPoint.innerX, tickMajorMinorPoint.innerY, tickMajorMinorPoint.x, tickMajorMinorPoint.y, ticksAndTablePaint);
            }

            // 画刻度盘上的文字
            // 文字高度
            float textHeight = textPaint.getFontMetrics().descent - textPaint.getFontMetrics().ascent;
            // 文字紧贴主刻度线
            float txtX = tickPoint.innerX;
            float txtY = tickPoint.innerY + textHeight;
            canvas.drawText((int) curProgress + "", txtX, txtY, textPaint);

            curProgress += majorTickStep;
            forCount++;
        }
    }

    protected void drawNeedle(Canvas canvas) {

        // value 对应的角度
        float valueDegree = (value - minValue) / (maxValue - minValue) * availableAngle + startDegree;

        float endX = (float) (arcCenterX + Math.cos(valueDegree * Math.PI / 180) * arcRadis);
        float endY = (float) (arcCenterY + Math.sin(valueDegree * Math.PI / 180) * arcRadis);

        // 终点与圆心的距离
        float distance = (float) Math.sqrt(Math.pow(endX - needleCenterX, 2) + Math.pow(endY - needleCenterY, 2));

        float needleStartX = needleRadis * (endX - needleCenterX) / distance + needleCenterX;
        float needleStartY = needleRadis * (endY - needleCenterY) / distance + needleCenterY;

        float k = -(needleStartX - needleCenterX) / (needleStartY - needleCenterY);
        float b = needleCenterY - k * needleCenterX;

        float detaX = (float) (Math.cos(Math.atan(k)) * needleArcRadis / 2);

        float needleBottomStartX = needleCenterX - detaX;
        float needleBottomStartY = k * needleBottomStartX + b;

        float needleBottomEndX = needleCenterX + detaX;
        float needleBottomEndY = k * needleBottomEndX + b;

        Paint needlePaint = new Paint();
        needlePaint.setAntiAlias(true);
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setColor(needleColor);

        Path path = new Path();
        path.moveTo(needleStartX, needleStartY);
        path.lineTo(needleBottomStartX, needleBottomStartY);
        path.lineTo(needleBottomEndX, needleBottomEndY);
        path.lineTo(needleStartX, needleStartY);
        canvas.drawPath(path, needlePaint);

        canvas.drawCircle(needleCenterX, needleCenterY, needleArcRadis, needlePaint);
    }

    protected void drawShandow(Canvas canvas) {

        float startX = (float) (arcCenterX + Math.cos(startDegree * Math.PI / 180) * arcRadis);
        float startY = (float) (arcCenterY + Math.sin(startDegree * Math.PI / 180) * arcRadis);

        // value 对应的角度
        float curDegree = (value - minValue) / (maxValue - minValue) * availableAngle;
        if (curDegree == 0) return;

        RectF ovalOuter = getOval(1f - 2 * mBottomInnerSweepWidth / viewWidth);

        updateTopShandowPosition(curDegree / 360);

        Shader shader = new SweepGradient(ovalOuter.centerX(), ovalOuter.centerY(), mTopShandowColor, mTopShandowPosition);

        Matrix matrix = new Matrix();
        matrix.setRotate(startDegree, ovalOuter.centerX(), ovalOuter.centerY());
        shader.setLocalMatrix(matrix);

        Path path = new Path();
        path.moveTo(startX, startY);
        // 角度顺时针为正
        path.arcTo(ovalOuter, startDegree, curDegree, false);
        path.lineTo(needleCenterX, needleCenterY);
        path.lineTo(startX, startY);

        Paint shandowPaint = new Paint();
        shandowPaint.setAntiAlias(true);
        shandowPaint.setShader(shader);
        canvas.drawPath(path, shandowPaint);
    }

    protected void drawText(Canvas canvas) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(valueColor);
        paint.setTextSize(valueTextSize);
        paint.setTextAlign(Paint.Align.CENTER);

        // 文字高度
        float textHeight = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
        float txtX = needleCenterX;
        float txtY = needleCenterY + 2 * textHeight;
        canvas.drawText(((int) (value * 10) / 10f) + "", txtX, txtY, paint);

        paint.setTextSize(valueUnitTextSize);
        // 文字高度
        textHeight = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
        txtY = txtY + 1.2f * textHeight;
        canvas.drawText(unit, txtX, txtY, paint);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 此控件宽高比为 1
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);

        int width;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            int desired = getPaddingLeft() + getPaddingRight();
            width = Math.max(100, desired += getMeasuredWidth());
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desired, widthSize);
            }
        }

        setViewWidth(width);
        init();

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int height;

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else {
            int desired = getPaddingTop() + getPaddingBottom();
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(valueColor);
            paint.setTextSize(valueTextSize);
            paint.setTextAlign(Paint.Align.CENTER);

            // 文字高度
            float textHeight = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
            paint.setTextSize(valueUnitTextSize);
            textHeight = 1.2f * (paint.getFontMetrics().descent - paint.getFontMetrics().ascent) + 2f * textHeight;

            height = (int) (textHeight + needleCenterY + needleArcRadis) + desired;
        }

        setMeasuredDimension(width, height);
    }
}
