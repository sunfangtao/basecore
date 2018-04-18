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
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.wxt.library.R;
import com.wxt.library.retention.NotProguard;

/**
 * Created by Administrator on 2017/4/26.
 */

public class CustomInstrumentPanelView extends View {

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
    // 最底层渐变背景颜色（环形）
    private int[] mBottomSweepColor;
    // 最底层渐变背景宽度（环形）
    private float mBottomSweepWidth;
    // 最底层渐变背景宽度（内环形）
    private float mBottomInnerSweepWidth;
    // 顶层渐变背景宽度（环形）
    private float mTopSweepWidth;
    // 顶层渐变背景（环形）
    private int[] mTopSweepColor;
    // 顶层渐变背景（指针）
    private int[] mTopNeeldeSweepColor;
    // 顶层渐变位置（指针）
    private float[] mTopNeeldeSweepPosition;
    // 顶层渐变背景（扫描遮罩）
    private int[] mTopShandowColor;
    // 顶层渐变位置（扫描遮罩）
    private float[] mTopShandowPosition;
    // 顶层渐变背景（填充）
    private int[] mTopFillColor;
    // 顶层渐变位置（填充）
    private float[] mTopFillPosition;
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

    // 内圆大小
    private float topSweepSize = 3 / 7f;
    // 内圆上指针阴影角度
    private float topSweepNeedleShandowDegree = 30f;

    public CustomInstrumentPanelView(Context context) {
        this(context, null);
    }

    public CustomInstrumentPanelView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomInstrumentPanelView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InstrumentPanelView);

        maxValue = a.getFloat(R.styleable.InstrumentPanelView_maxValue, 220f);
        minValue = a.getFloat(R.styleable.InstrumentPanelView_minValue, 0f);
        value = a.getFloat(R.styleable.InstrumentPanelView_value, minValue);
        majorTickStep = a.getFloat(R.styleable.InstrumentPanelView_majorTickStep, 20f);
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

        // 默认为控件宽度的1/70
        mBottomSweepWidth = a.getDimension(R.styleable.InstrumentPanelView_bottomSweepWidth, 0);
        mBottomInnerSweepWidth = a.getDimension(R.styleable.InstrumentPanelView_bottomInnerSweepWidth, 0);
        mTopSweepWidth = a.getDimension(R.styleable.InstrumentPanelView_topSweepWidth, 0);

        a.recycle();

        if ((maxValue - minValue) % majorTickStep != 0) {
            throw new RuntimeException("majorTickStep必须整除(maxValue-minValue)：majorTickStep=" + majorTickStep + " (maxValue-minValue)=" + (maxValue - minValue));
        }

        if (mBottomSweepWidth == 0) {
            mBottomSweepWidth = getMeasuredWidth() / 70f;
        }

        availableAngle = 180 + 2 * angle;
        startDegree = 180 - angle;

        if (TextUtils.isEmpty(unit))
            unit = "KM/H";

        init();
    }

    private void setViewWidth(int width) {
        this.viewWidth = width;
        if (mBottomSweepWidth == 0) {
            mBottomSweepWidth = viewWidth / 70f;
        }
        if (mBottomInnerSweepWidth == 0) {
            mBottomInnerSweepWidth = viewWidth / 100;
        }
        if (labelTextSize == 0) {
            labelTextSize = viewWidth / 22f;
        }
        if (mTopSweepWidth == 0) {
            mTopSweepWidth = viewWidth / 70f;
        }
        if (valueTextSize == 0) {
            valueTextSize = viewWidth * topSweepSize / 3;
        }
        if (valueUnitTextSize == 0) {
            valueUnitTextSize = viewWidth * topSweepSize / 11;
        }
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

    public void setBottomSweepColor(int[] colors) {
        if (colors == null || colors.length < 2) {
            return;
        }
        mBottomSweepColor = colors;
        invalidate();
    }

    public void setTopSweepColor(int[] colors) {
        if (colors == null || colors.length < 2) {
            return;
        }
        mTopSweepColor = colors;
        invalidate();
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

    public void setTopFillColor(int[] colors) {
        if (colors == null || colors.length < 2) {
            return;
        }

        mTopFillColor = new int[colors.length + 1];
        for (int i = 0; i < colors.length; i++) {
            mTopFillColor[i] = colors[i];
        }
        mTopFillColor[colors.length] = colors[colors.length - 1];

        mTopFillPosition = new float[colors.length + 1];
        mTopFillPosition[0] = 0f;
        mTopFillPosition[colors.length] = 1f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //　清空画板
        canvas.drawColor(Color.TRANSPARENT);
        drawBottomCircle(canvas);
        drawTicksAndLables(canvas);
        drawValueBkground(canvas);
        drawShandow(canvas);
        drawNeedle(canvas);
        drawText(canvas);
    }

    private void init() {
        mBottomSweepColor = new int[]{
                Color.parseColor("#001FB4F9"), Color.parseColor("#001FB4F9"), Color.parseColor("#111FB4F9"), Color.parseColor("#1FB4F9"),
                Color.parseColor("#107BFF"), Color.parseColor("#107BFF"), Color.parseColor("#107BFF"), Color.parseColor("#107BFF"),
                Color.parseColor("#107BFF"), Color.parseColor("#107BFF"), Color.parseColor("#107BFF"), Color.parseColor("#1FB4F9"),
                Color.parseColor("#111FB4F9"), Color.parseColor("#001FB4F9"), Color.parseColor("#001FB4F9")};

        mTopSweepColor = new int[]{Color.parseColor("#001FB4F9"), Color.parseColor("#001FB4F9"), Color.parseColor("#111FB4F9"), Color.parseColor("#1FB4F9"),
                Color.parseColor("#107BFF"), Color.parseColor("#107BFF"), Color.parseColor("#107BFF"), Color.parseColor("#107BFF"), Color.parseColor("#107BFF"),
                Color.parseColor("#107BFF"), Color.parseColor("#107BFF"), Color.parseColor("#1FB4F9"), Color.parseColor("#111FB4F9"), Color.parseColor("#001FB4F9"),
                Color.parseColor("#001FB4F9")};

        mTopNeeldeSweepColor = new int[]{
                Color.parseColor("#00107BFF"), Color.parseColor("#00107BFF"), Color.parseColor("#FFFFFF"), Color.parseColor("#FFFFFF"),
                Color.parseColor("#FFFFFF"), Color.parseColor("#00107BFF"), Color.parseColor("#00107BFF")};

        float offset = topSweepNeedleShandowDegree / 4 / 360;
        mTopNeeldeSweepPosition = new float[7];
        mTopNeeldeSweepPosition[0] = 0;
        mTopNeeldeSweepPosition[1] = (360 - topSweepNeedleShandowDegree) / 720f;
        mTopNeeldeSweepPosition[2] = mTopNeeldeSweepPosition[1] + offset;
        mTopNeeldeSweepPosition[3] = 0.5f;
        mTopNeeldeSweepPosition[5] = (360 + topSweepNeedleShandowDegree) / 720f;
        mTopNeeldeSweepPosition[4] = mTopNeeldeSweepPosition[5] - offset;
        mTopNeeldeSweepPosition[6] = 1;

        mTopShandowColor = new int[]{Color.parseColor("#0038C4FD"), Color.parseColor("#107BFF"), Color.parseColor("#107BFF")};
        mTopShandowPosition = new float[]{0f, 0.5f, 1f};

        mTopFillColor = new int[]{Color.parseColor("#63C1F8"), Color.parseColor("#63C1F8"), Color.parseColor("#77CBFA")};
        mTopFillPosition = new float[]{0f, 0.5f, 1f};
    }

    protected void drawBottomCircle(Canvas canvas) {
        Paint bottomCirclePaint = new Paint();
        bottomCirclePaint.setAntiAlias(true);
        bottomCirclePaint.setStrokeWidth(mBottomSweepWidth);
        bottomCirclePaint.setStyle(Paint.Style.STROKE);

        RectF bottomCircleRectF = getOval(1f - 2f * mBottomSweepWidth / viewWidth);

        SweepGradient mBottomSweepGradient = new SweepGradient(bottomCircleRectF.centerX(), bottomCircleRectF.centerY(), mBottomSweepColor, null);

        Matrix matrix = new Matrix();
        matrix.setRotate(90, bottomCircleRectF.centerX(), bottomCircleRectF.centerY());
        mBottomSweepGradient.setLocalMatrix(matrix);

        bottomCirclePaint.setShader(mBottomSweepGradient);
        canvas.drawArc(bottomCircleRectF, 0, 360, false, bottomCirclePaint);
    }

    protected void drawTicksAndLables(Canvas canvas) {

        Paint ticksAndTablePaint = new Paint();
        ticksAndTablePaint.setAntiAlias(true);
        ticksAndTablePaint.setStrokeWidth(mBottomInnerSweepWidth);
        ticksAndTablePaint.setStyle(Paint.Style.STROKE);
        ticksAndTablePaint.setColor(scaleNeedleColor);

        RectF ticksAndTableArcRectF = getOval(1f - 6f * mBottomSweepWidth / viewWidth);

        // 主刻度宽度导致的额外角度(大约，可重新计算)
        float degree = (float) (ticksAndTableArcRectF.width() * 0.01f * 90 / Math.PI / ticksAndTableArcRectF.width());
        canvas.drawArc(ticksAndTableArcRectF, startDegree - degree, availableAngle + 2 * degree, false, ticksAndTablePaint);

        RectF ticksAndTableRectF = getOval(1f - 7f * mBottomSweepWidth / viewWidth);

        // 次刻度的长度
        float minorTicksLength = ticksAndTableRectF.width() * 0.03f;
        // 次主刻度的长度
        float minorMinorTicksLength = ticksAndTableRectF.width() * 0.05f;
        // 主刻度的长度
        float majarTicksLength = ticksAndTableRectF.width() * 0.07f;
        // 主刻度之间的间隔(角度)
        float majorStep = majorTickStep / (maxValue - minValue) * availableAngle;
        // 次刻度的间隔(角度)
        float minorStep = majorStep / minorTicks;
        //
        float radius = ticksAndTableRectF.width() / 2;
        // 主刻度所在的角度（x正向为0，顺时针为正）
        float currentAngle = startDegree;
        double curProgress = minValue;

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(scaleTextColor);
        textPaint.setTextSize(labelTextSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // +0.1保证int与float之间
        float endAngle = availableAngle + startDegree + 0.1f;

        while (currentAngle <= endAngle) {
            // 没有到最后的刻度，画主分割线
            ticksAndTablePaint.setStrokeWidth(ticksAndTableRectF.width() * 0.01f);
            canvas.drawLine(
                    (float) (ticksAndTableRectF.centerX() + Math.cos(currentAngle / 180 * Math.PI) * (radius - majarTicksLength)),
                    (float) (ticksAndTableRectF.centerY() + Math.sin(currentAngle / 180 * Math.PI) * (radius - majarTicksLength)),
                    (float) (ticksAndTableRectF.centerX() + Math.cos(currentAngle / 180 * Math.PI) * radius),
                    (float) (ticksAndTableRectF.centerY() + Math.sin(currentAngle / 180 * Math.PI) * radius), ticksAndTablePaint);

            // 画次分割线
            ticksAndTablePaint.setStrokeWidth(ticksAndTableRectF.width() * 0.003f);
            for (int i = 1; i <= minorTicks; i++) {
                float angle = currentAngle + i * minorStep;
                if (angle < endAngle) {
                    canvas.drawLine(
                            (float) (ticksAndTableRectF.centerX() + Math.cos(angle / 180 * Math.PI) * (radius - minorTicksLength)),
                            (float) (ticksAndTableRectF.centerY() + Math.sin(angle / 180 * Math.PI) * (radius - minorTicksLength)),
                            (float) (ticksAndTableRectF.centerX() + Math.cos(angle / 180 * Math.PI) * radius),
                            (float) (ticksAndTableRectF.centerY() + Math.sin(angle / 180 * Math.PI) * radius), ticksAndTablePaint);
                }
            }

            // 画主次分割线
            ticksAndTablePaint.setStrokeWidth(ticksAndTableRectF.width() * 0.006f);
            float angle = currentAngle + majorStep / 2;
            if (angle < endAngle) {
                // 最后一个主分割线画完后，不画次分割线
                canvas.drawLine(
                        (float) (ticksAndTableRectF.centerX() + Math.cos(angle / 180 * Math.PI) * (radius - minorMinorTicksLength)),
                        (float) (ticksAndTableRectF.centerY() + Math.sin(angle / 180 * Math.PI) * (radius - minorMinorTicksLength)),
                        (float) (ticksAndTableRectF.centerX() + Math.cos(angle / 180 * Math.PI) * radius),
                        (float) (ticksAndTableRectF.centerY() + Math.sin(angle / 180 * Math.PI) * radius), ticksAndTablePaint);
            }

            // 画刻度盘上的文字
            canvas.save();
            canvas.rotate(currentAngle, ticksAndTableRectF.centerX(), ticksAndTableRectF.centerY());
            // 文字高度
            float textHeight = textPaint.getFontMetrics().descent - textPaint.getFontMetrics().ascent;
            // 文字紧贴主刻度线
            float txtX = ticksAndTableRectF.centerX() + radius - majarTicksLength - textHeight;
            float txtY = ticksAndTableRectF.centerY();
            canvas.rotate(90, txtX, txtY);
            canvas.drawText((int) curProgress + "", txtX, txtY, textPaint);
            canvas.restore();

            currentAngle += majorStep;
            curProgress += majorTickStep;
        }
    }

    protected void drawValueBkground(Canvas canvas) {
        Paint valueBkgroundPaint = new Paint();
        valueBkgroundPaint.setAntiAlias(true);
        valueBkgroundPaint.setStrokeWidth(mTopSweepWidth);
        valueBkgroundPaint.setStyle(Paint.Style.FILL);

        RectF valueBkgroundRectF = getOval(topSweepSize);

        RadialGradient mTopFillGradient = new RadialGradient(valueBkgroundRectF.centerX(), valueBkgroundRectF.centerY(), valueBkgroundRectF.width() / 2, mTopFillColor, mTopFillPosition, Shader.TileMode.REPEAT);
        valueBkgroundPaint.setShader(mTopFillGradient);
        canvas.drawCircle(valueBkgroundRectF.centerX(), valueBkgroundRectF.centerY(), valueBkgroundRectF.width() / 2, valueBkgroundPaint);

        valueBkgroundPaint.setStyle(Paint.Style.STROKE);
        SweepGradient mTopSweepGradient = new SweepGradient(valueBkgroundRectF.centerX(), valueBkgroundRectF.centerY(), mTopSweepColor, null);
        Matrix matrix = new Matrix();
        matrix.setRotate(90, valueBkgroundRectF.centerX(), valueBkgroundRectF.centerY());
        mTopSweepGradient.setLocalMatrix(matrix);
        valueBkgroundPaint.setShader(mTopSweepGradient);
        canvas.drawArc(valueBkgroundRectF, 0, 360, false, valueBkgroundPaint);
    }

    protected void drawNeedle(Canvas canvas) {
        Paint needlePaint = new Paint();
        needlePaint.setAntiAlias(true);
        needlePaint.setStrokeWidth(mTopSweepWidth);
        needlePaint.setStyle(Paint.Style.STROKE);

        RectF needleRectF = getOval(topSweepSize);

        SweepGradient mTopSweepGradient = new SweepGradient(needleRectF.centerX(), needleRectF.centerY(), mTopNeeldeSweepColor, mTopNeeldeSweepPosition);
        Matrix matrix = new Matrix();
        // 当前值对应的角度
        float degree = (value - minValue) / (maxValue - minValue) * availableAngle + startDegree;
        matrix.setRotate(degree - 180, needleRectF.centerX(), needleRectF.centerY());
        mTopSweepGradient.setLocalMatrix(matrix);
        needlePaint.setShader(mTopSweepGradient);
        canvas.drawArc(needleRectF, 0, 360, false, needlePaint);

        needlePaint.reset();
        needlePaint.setAntiAlias(true);
        needlePaint.setStyle(Paint.Style.FILL);
        needlePaint.setColor(needleColor);

        RectF oval = getOval(1f - 7f * mBottomSweepWidth / viewWidth);
        // 次刻度的长度
        float minorTicksLength = oval.width() * 0.03f;
        float radius = oval.width() / 2;

        float startX = (float) (needleRectF.centerX() + Math.cos(degree / 180 * Math.PI) * (radius - minorTicksLength));
        float startY = (float) (needleRectF.centerY() + Math.sin(degree / 180 * Math.PI) * (radius - minorTicksLength));

        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(needleRectF.centerX() + (float) (Math.cos((degree + 2) / 180f * Math.PI) * needleRectF.width() / 2), needleRectF.centerY() + (float) (Math.sin((degree + 2) / 180f * Math.PI) * needleRectF.width() / 2));
        path.lineTo(needleRectF.centerX() + (float) (Math.cos((degree - 2) / 180f * Math.PI) * needleRectF.width() / 2), needleRectF.centerY() + (float) (Math.sin((degree - 2) / 180f * Math.PI) * needleRectF.width() / 2));
        path.lineTo(startX, startY);
        canvas.drawPath(path, needlePaint);

        canvas.drawCircle(needleRectF.centerX() + (float) (Math.cos(degree / 180f * Math.PI) * needleRectF.width() / 2), needleRectF.centerY() + (float) (Math.sin(degree / 180f * Math.PI) * needleRectF.width() / 2), 1.5f * mTopSweepWidth, needlePaint);
    }

    protected void drawShandow(Canvas canvas) {

        RectF ovalOuter = getOval(1f - 6f * mBottomSweepWidth / viewWidth);
        RectF ovalInner = getOval(topSweepSize);

        float curAngle = (value - minValue) / (maxValue - minValue) * availableAngle;
        updateTopShandowPosition(curAngle / 360);

        Shader shader = new SweepGradient(ovalOuter.centerX(), ovalOuter.centerY(), mTopShandowColor, mTopShandowPosition);

        float startX = (float) (ovalOuter.centerX() + Math.cos(startDegree / 180f * Math.PI) * ovalOuter.width() / 2);
        float startY = (float) (ovalOuter.centerY() + Math.sin(startDegree / 180f * Math.PI) * ovalOuter.width() / 2);

        if (curAngle == 0) return;

        Path path = new Path();
        path.moveTo(startX, startY);
        // 角度顺时针为正
        path.arcTo(ovalOuter, startDegree, curAngle, false);
        path.lineTo(ovalInner.centerX() + (float) (Math.cos((curAngle + startDegree) / 180f * Math.PI) * ovalInner.width() / 2), ovalInner.centerY() + (float) (Math.sin((curAngle + startDegree) / 180f * Math.PI) * ovalInner.width() / 2));
        path.arcTo(ovalInner, curAngle + startDegree, -curAngle, false);
        path.lineTo(startX, startY);

        Matrix matrix = new Matrix();
        matrix.setRotate(startDegree, ovalInner.centerX(), ovalInner.centerY());
        shader.setLocalMatrix(matrix);

        Paint shandowPaint = new Paint();
        shandowPaint.setAntiAlias(true);
        shandowPaint.setShader(shader);
        canvas.drawPath(path, shandowPaint);
    }

    protected void drawText(Canvas canvas) {
        RectF textRectF = getOval(topSweepSize);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(valueColor);
        textPaint.setTextSize(valueTextSize);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // 文字高度
        float textHeight = textPaint.getFontMetrics().descent - textPaint.getFontMetrics().ascent;
        float txtX = textRectF.centerX();
        float txtY = textRectF.centerY() + textHeight / 6;
        canvas.drawText(((int) (value * 10) / 10f + ""), txtX, txtY, textPaint);

        textPaint.setTextSize(valueUnitTextSize);
        textHeight = textPaint.getFontMetrics().descent - textPaint.getFontMetrics().ascent;
        txtX = textRectF.centerX();
        txtY = txtY + textHeight * 3 / 2;
        canvas.drawText(unit, txtX, txtY, textPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 此控件宽高比为 1
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
            width = Math.max(50, desired += getWidth());
            if (widthMode == MeasureSpec.AT_MOST) {
                width = Math.min(desired, widthSize);
            }
        }
        setViewWidth(width);
        setMeasuredDimension(width, width);
    }
}
