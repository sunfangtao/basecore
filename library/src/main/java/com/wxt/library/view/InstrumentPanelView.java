package com.wxt.library.view;

import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import com.wxt.library.R;
import com.wxt.library.retention.NotProguard;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义仪表盘
 *
 * @author SunFangTao
 * @Date 2014-8-13
 */
public class InstrumentPanelView extends View {

    private int viewWidth;
    /**
     * 默认最大刻度
     */
    private static final float DEFAULT_MAX_SPEED = 220.0f;
    /**
     * 默认的刻度间隔
     */
    private static final float DEFAULT_MAJOR_TICK_STEP = 20.0f;
    /**
     * 默认的次刻度分隔数
     */
    private static final int DEFAULT_MINOR_TICKS = 5;
    /**
     * 默认字体大小
     */
    private static final int DEFAULT_LABEL_TEXT_SIZE_SP = 12;
    /**
     * 最大刻度
     */
    private double maxSpeed;
    /**
     * 当前刻度
     */
    private double speed;
    /**
     * 刻度默认颜色
     */
    private int defaultColor;
    /**
     * 刻度间隔（度数）
     */
    private double majorTickStep;
    /**
     * 次刻度的个数
     */
    private int minorTicks;
    /**
     * 仪表盘背景画笔
     */
    private Paint backgroundPaint;
    /**
     * 仪表盘背景最外层颜色
     */
    private int outerBackgroundColor = Color.WHITE;
    /**
     * 仪表盘背景内层颜色
     */
    private int centerBackgroundColor = Color.TRANSPARENT;
    /**
     * 仪表盘背景最内层颜色
     */
    private int innerBackgroundColor = Color.parseColor("#6abcfb");
    /**
     * 指针画笔
     */
    private Paint needlePaint;
    /**
     * 指针颜色
     */
    private int needleColor = Color.parseColor("#23e23e");
    /**
     * 刻度画笔
     */
    private Paint ticksPaint;
    /**
     * 仪表盘名称画笔
     */
    private Paint txtPaint;
    /**
     * 刻度值颜色画笔
     */
    private Paint colorLinePaint;
    private List<ColoredRange> ranges = new ArrayList<ColoredRange>();
    /**
     * 仪表盘字体大小
     */
    private float labelTextSize;
    /**
     * 仪表盘大小（相对于半圆，大为正，小为负）
     */
    private int angle = 40;
    /**
     * 可用的角度范围（居中）
     */
    private float availableAngle = 180 + 2 * angle;
    /**
     * 刻度所在位置
     */
    private final float scale = 0.35f;

    private String unit;
    private String style;

    public InstrumentPanelView(Context context) {
        this(context, null);
        init();
    }

    public InstrumentPanelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InstrumentPanelView);

        maxSpeed = a.getFloat(R.styleable.InstrumentPanelView_maxValue, DEFAULT_MAX_SPEED);
        speed = a.getFloat(R.styleable.InstrumentPanelView_value, 0f);
        defaultColor = a.getColor(R.styleable.InstrumentPanelView_tickColor, Color.parseColor("#23e23e"));
        majorTickStep = a.getFloat(R.styleable.InstrumentPanelView_majorTickStep, DEFAULT_MAJOR_TICK_STEP);
        minorTicks = a.getInt(R.styleable.InstrumentPanelView_minorTicks, DEFAULT_MINOR_TICKS);
        labelTextSize = a.getDimension(R.styleable.InstrumentPanelView_tickTextSize, DEFAULT_LABEL_TEXT_SIZE_SP);
        a.recycle();

        if (maxSpeed % majorTickStep != 0) {
            throw new RuntimeException("majorTickStep必须整除maxSpeed：majorTickStep=" + majorTickStep + " maxSpeed=" + maxSpeed);
        }
        init();
    }

    public double getSpeed() {
        return speed;
    }

    public void setSpeed(double speed) {
        if (speed < 0)
            throw new IllegalArgumentException(
                    "Non-positive value specified as a speed.");
        if (speed > maxSpeed)
            speed = maxSpeed;
        this.speed = speed;
        invalidate();
    }

    @TargetApi(11)
    public ValueAnimator setSpeed(double progress, long duration, long startDelay) {
        if (progress <= 0)
            throw new IllegalArgumentException(
                    "Non-positive value specified as a speed.");

        if (progress > maxSpeed)
            progress = maxSpeed;

        ValueAnimator va = ValueAnimator.ofObject(new TypeEvaluator<Double>() {
            @Override
            public Double evaluate(float fraction, Double startValue, Double endValue) {
                return startValue + fraction * (endValue - startValue);
            }
        }, Double.valueOf(getSpeed()), Double.valueOf(progress));

        va.setDuration(duration);
        va.setStartDelay(startDelay);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                Double value = (Double) animation.getAnimatedValue();
                if (value != null)
                    setSpeed(value);
            }
        });
        va.start();
        return va;
    }

    @TargetApi(11)
    public ValueAnimator setSpeed(double progress, boolean animate) {
        return setSpeed(progress, 1500, 200);
    }

    public int getDefaultColor() {
        return defaultColor;
    }

    public void setDefaultColor(int defaultColor) {
        this.defaultColor = defaultColor;
        invalidate();
    }

    public int getOuterBackgroundColor() {
        return outerBackgroundColor;
    }

    public void setOuterBackgroundColor(int outerBackgroundColor) {
        this.outerBackgroundColor = outerBackgroundColor;
        invalidate();
    }

    public int getCenterBackgroundColor() {
        return centerBackgroundColor;
    }

    public void setCenterBackgroundColor(int centerBackgroundColor) {
        this.centerBackgroundColor = centerBackgroundColor;
        invalidate();
    }

    public int getInnerBackgroundColor() {
        return innerBackgroundColor;
    }

    public void setInnerBackgroundColor(int innerBackgroundColor) {
        this.innerBackgroundColor = innerBackgroundColor;
        invalidate();
    }

    public int getAngle() {
        return angle;
    }

    public void setAngle(int angle) {
        this.angle = angle;
        this.availableAngle = 160 + 2 * angle;
        invalidate();
    }

    public int getNeedleColor() {
        return needleColor;
    }

    public void setNeedleColor(int needleColor) {
        this.needleColor = needleColor;
        invalidate();
    }

    public void clearColoredRanges() {
        ranges.clear();
        invalidate();
    }

    public void addColoredRange(double begin, double end, int color) {
        if (begin >= end)
            throw new IllegalArgumentException(
                    "Incorrect number range specified!");
        if (begin < -5.0 / 160 * maxSpeed)
            begin = -5.0 / 160 * maxSpeed;
        if (end > maxSpeed * (5.0 / 160 + 1))
            end = maxSpeed * (5.0 / 160 + 1);
        ranges.add(new ColoredRange(color, begin, end));
        invalidate();
    }

    public void setAvailableAngle(float availableAngle) {
        this.availableAngle = availableAngle;
        invalidate();
    }

    public void setUnit(String unit) {
        this.unit = unit;
        invalidate();
    }

    public void setStyle(String style) {
        this.style = style;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawColor(Color.TRANSPARENT);

        drawBackground(canvas);

        drawTicks(canvas);

        drawNeedle(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 此控件宽高比为1
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        // int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        // int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        // int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else {
            int desired = getPaddingLeft() + getPaddingRight();
            desired += getWidth();
            width = Math.max(50, desired);
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desired, widthSize);
            }
        }
        viewWidth = width;
        setMeasuredDimension(width, width);
    }

    private void drawNeedle(Canvas canvas) {
        needlePaint.setColor(needleColor);
        needlePaint.setStrokeWidth(1);
        RectF oval = getOval(canvas, 0.05f);
        float angle = (180 - availableAngle) / 2 + (float) (speed / maxSpeed * availableAngle);
        for (int i = 0; i < 11; i++) {
            int changeAngle = i - 5;
            canvas.drawLine(
                    (float) (oval.centerX() + Math
                            .cos((180 - angle + changeAngle * 2) / 180
                                    * Math.PI)
                            * oval.width()),
                    (float) (oval.centerY() - Math
                            .sin((angle - changeAngle * 2) / 180 * Math.PI)
                            * oval.width()),
                    (float) (oval.centerX() + Math.cos((180 - angle) / 180
                            * Math.PI)
                            * (oval.width() + canvas.getWidth() * 0.9f * scale)),
                    (float) (oval.centerY() - Math.sin(angle / 180 * Math.PI)
                            * (oval.width() + canvas.getWidth() * 0.9f * scale)),
                    needlePaint);
        }
        needlePaint.setStrokeWidth(3);
        canvas.drawCircle(oval.centerX(), oval.centerY(), oval.width(),
                needlePaint);

        Paint txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(needleColor);
        txtPaint.setTextSize(viewWidth * 16f / 225f);
        txtPaint.setTextAlign(Paint.Align.CENTER);
        if (unit == null) {
            unit = " km/h";
        }
        if (style == null) {
            style = "车速仪表盘";
        }
        canvas.drawText(style, canvas.getWidth() / 2,
                canvas.getHeight() * 0.77f, txtPaint);
        canvas.drawText(unit, canvas.getWidth() / 2,
                canvas.getHeight() * 0.37f, txtPaint);
    }

    private void drawTicks(Canvas canvas) {
        RectF oval = getOval(canvas, 1f);
        // 次刻度的长度
        float minorTicksLength = oval.width() * 0.04f;
        // 主刻度的间隔
        float majorStep = (float) (majorTickStep / maxSpeed * availableAngle);
        // 次刻度的间隔
        float minorStep = majorStep / (1 + minorTicks);
        //
        float radius = oval.width() * scale;
        // 正下为0，顺时针为正（0-180）
        float currentAngle = (180 - availableAngle) / 2;
        double curProgress = 0;
        ticksPaint.setColor(defaultColor);
        while (currentAngle <= availableAngle / 2 + 90) {
            // 没有到最后的刻度，画主分割线
            ticksPaint.setStrokeWidth(oval.width() * 0.008f);
            canvas.drawLine(
                    (float) (oval.centerX() + Math.cos((180 - currentAngle)
                            / 180 * Math.PI)
                            * (radius - minorTicksLength)),
                    (float) (oval.centerY() - Math.sin(currentAngle / 180
                            * Math.PI)
                            * (radius - minorTicksLength)),
                    (float) (oval.centerX() + Math.cos((180 - currentAngle)
                            / 180 * Math.PI)
                            * (radius + minorTicksLength)),
                    (float) (oval.centerY() - Math.sin(currentAngle / 180
                            * Math.PI)
                            * (radius + minorTicksLength)), ticksPaint);
            // 画次分割线
            ticksPaint.setStrokeWidth(oval.width() * 0.006f);
            for (int i = 1; i <= minorTicks; i++) {
                float angle = currentAngle + i * minorStep;
                if (angle >= availableAngle / 2 + 90) {
                    // 最后一个主分割线画完后，不画次分割线
                    break;
                }
                canvas.drawLine(
                        (float) (oval.centerX() + Math.cos((180 - angle) / 180
                                * Math.PI)
                                * radius),
                        (float) (oval.centerY() - Math.sin(angle / 180
                                * Math.PI)
                                * radius),
                        (float) (oval.centerX() + Math.cos((180 - angle) / 180
                                * Math.PI)
                                * (radius + minorTicksLength)),
                        (float) (oval.centerY() - Math.sin(angle / 180
                                * Math.PI)
                                * (radius + minorTicksLength)), ticksPaint);
            }

            // 画刻度盘上的文字
            canvas.save();
            canvas.rotate(180 + currentAngle, oval.centerX(), oval.centerY());
            float txtX = oval.centerX() + radius - minorTicksLength * 2.5f;
            float txtY = oval.centerY();
            canvas.rotate(+90, txtX, txtY);
            if ((int) curProgress >= 100) {
                txtPaint.setTextSize(labelTextSize - 2);
            } else {
                txtPaint.setTextSize(labelTextSize);
            }
            canvas.drawText((int) curProgress + "", txtX, txtY, txtPaint);
            canvas.restore();

            currentAngle += majorStep;
            curProgress += majorTickStep;
        }

        RectF smallOval = getOval(canvas, 0.8f);
        colorLinePaint.setColor(defaultColor);
        colorLinePaint.setStrokeWidth(oval.width() * 0.01f);
        canvas.drawArc(smallOval, availableAngle / 2 - 90, -availableAngle,
                false, colorLinePaint);

        for (ColoredRange range : ranges) {
            colorLinePaint.setColor(range.getColor());
            canvas.drawArc(
                    smallOval,
                    (float) (270 - availableAngle / 2 + range.getBegin()
                            / maxSpeed * availableAngle),
                    (float) ((range.getEnd() - range.getBegin()) / maxSpeed * availableAngle),
                    false, colorLinePaint);
        }
    }

    private RectF getOval(Canvas canvas, float factor) {
        RectF oval;
        final int canvasWidth = canvas.getWidth();
        oval = new RectF(0, 0, canvasWidth * factor, canvasWidth * factor);
        oval.offset(canvasWidth * (1 - factor) / 2, canvasWidth * (1 - factor) / 2);
        return oval;
    }

    private void drawBackground(Canvas canvas) {
        RectF ovel = getOval(canvas, 1);
        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // 外背景
        backgroundPaint.setColor(outerBackgroundColor);
        canvas.drawCircle(ovel.centerX(), ovel.centerY(), ovel.width() / 2,
                backgroundPaint);
        // 外内背景
        RectF ovel1 = getOval(canvas, 0.95f);
        // backgroundPaint.setColor(centerBackgroundColor);
        Shader shader = new LinearGradient(0, 0, 0, ovel1.width(),
                Color.parseColor("#184835"), Color.BLACK, TileMode.CLAMP);
        backgroundPaint.setShader(shader);
        canvas.drawCircle(ovel.centerX(), ovel.centerY(), ovel1.width() / 2,
                backgroundPaint);
        // 内背景
        // RectF ovel2 = getOval(canvas, 0.9f);
        // backgroundPaint.setColor(innerBackgroundColor);
        // backgroundPaint.setShader(null);
        // canvas.drawCircle(ovel.centerX(), ovel.centerY(), ovel2.width() / 2,
        // backgroundPaint);
    }

    @SuppressLint("NewApi")
    private void init() {
        if (Build.VERSION.SDK_INT >= 11 && !isInEditMode()) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);

        txtPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        txtPaint.setColor(Color.parseColor("#23e23e"));
        txtPaint.setTextSize(labelTextSize);
        txtPaint.setTextAlign(Paint.Align.CENTER);

        ticksPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ticksPaint.setStrokeWidth(3.0f);
        ticksPaint.setStyle(Paint.Style.STROKE);
        ticksPaint.setColor(defaultColor);

        colorLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        colorLinePaint.setStyle(Paint.Style.STROKE);
        colorLinePaint.setStrokeWidth(5);
        colorLinePaint.setColor(defaultColor);

        needlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        needlePaint.setStrokeWidth(3);
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setColor(needleColor);
    }

    private class ColoredRange {

        private int color;
        private double begin;
        private double end;

        public ColoredRange(int color, double begin, double end) {
            this.color = color;
            this.begin = begin;
            this.end = end;
        }

        public int getColor() {
            return color;
        }

        public double getBegin() {
            return begin;
        }

        public double getEnd() {
            return end;
        }
    }

}
