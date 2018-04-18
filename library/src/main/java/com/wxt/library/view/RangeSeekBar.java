package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;

import com.wxt.library.R;
import com.wxt.library.model.CustomSavedState;
import com.wxt.library.util.Util;

import java.util.ArrayList;

import static java.lang.Float.NaN;

/**
 * Created by Administrator on 2017/7/31.
 */

public class RangeSeekBar extends View {

    public interface RangeSeekBarChangedListener {
        void rangeSeekBarValueChanged(float leftValue, float rightValue);
    }

    public class BackgroundLine {
        public int lineColor;
        public float lineWidth;
        public Paint paint;
    }

    public class Trick {
        public ArrayList<String> trickValues = new ArrayList<>();
        public ArrayList<Float> trickPositon = new ArrayList<>();
        public float interval;
        public int textColor;
        public float textSize;
        public int trickWidth;
        public int trickHeight;
        public int trickColor;
        public int trickInterval;
        public Paint trickPaint;
        public Paint textPaint;
        public float textMaxHeight;
    }

    public class Thumb {
        public int startColor;
        public int endColor;
        public float lineWidth;
        public Drawable startDrawable;
        public Drawable endDrawable;
        public int drawableWidth;
        public int drawableHeight;
        public Shader shader;
        public Paint drawablePaint;
        public Paint linePaint;
        public float startPostion;
        public float endPosition;
    }

    public class Indicator {
        public int startColor;
        public int endColor;
        public int arrowHeight;
        public int aroundCorner;
        public int height;
        public float interval;
        public float textSize;
        public int textColor;
        public Paint backgroundPaint;
        public Paint textPaint;
        public int textPadding;
    }

    private Context context;

    private int VIEW_WIDTH = 0;
    private int VIEW_HEIGHT = 0;
    private float START_X = 0;
    private float END_X = 0;
    private float START_Y = 0;
    private float END_Y = 0;

    private BackgroundLine backgroundLine;
    private Thumb thumb;
    private Trick trick;
    private Indicator indicator;

    private float leftValue = 200;
    private float rightValue = 1000;

    private boolean isMoveLeft = false;
    private boolean isMoveRight = false;

    // 最后一个是否变成不限
    private boolean isAutoInfinite = true;

    private RangeSeekBarChangedListener listener;

    public void setListener(RangeSeekBarChangedListener listener) {
        this.listener = listener;
    }

    public float getLeftValue() {
        if (thumb.startPostion == START_X) {
            leftValue = Float.valueOf(trick.trickValues.get(0));
        }
        int size = trick.trickPositon.size();
        for (int i = 1; i < size; i++) {
            if (trick.trickPositon.get(i) >= thumb.startPostion) {
                try {
                    float startValue = Float.valueOf(trick.trickValues.get(i - 1));
                    float endValue = Float.valueOf(trick.trickValues.get(i));
                    float per = (thumb.startPostion - trick.trickPositon.get(i - 1)) / (trick.trickPositon.get(i) - trick.trickPositon.get(i - 1));
                    leftValue = startValue + (endValue - startValue) * per;
                    break;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        return leftValue = (int) (leftValue * 10) / 10f;
    }

    public void setLeftValue(float leftValue) {
        this.leftValue = leftValue;
        thumb.startPostion = 0;
        thumb.endPosition = 0;
        invalidate();
    }

    public float getRightValue() {
        int size = trick.trickPositon.size();
        for (int i = 1; i < size; i++) {
            if (trick.trickPositon.get(i) >= thumb.endPosition) {
                try {
                    float startValue = Float.valueOf(trick.trickValues.get(i - 1));
                    float endValue = Float.valueOf(trick.trickValues.get(i));
                    float per = (thumb.endPosition - trick.trickPositon.get(i - 1)) / (trick.trickPositon.get(i) - trick.trickPositon.get(i - 1));
                    rightValue = startValue + (endValue - startValue) * per;
                    break;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
        if (isAutoInfinite && trick.trickPositon.get(size - 1) == thumb.endPosition) {
            return NaN;
        }
        return rightValue = (int) (rightValue * 10) / 10f;
    }

    public void setRightValue(float rightValue) {
        this.rightValue = rightValue;
        thumb.startPostion = 0;
        thumb.endPosition = 0;
        invalidate();
    }

    public RangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;

        backgroundLine = new BackgroundLine();
        trick = new Trick();
        thumb = new Thumb();
        indicator = new Indicator();

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RangeSeekBar);

        isAutoInfinite = a.getBoolean(R.styleable.RangeSeekBar_isAutoInfinite, true);

        backgroundLine.lineWidth = a.getDimensionPixelSize(R.styleable.RangeSeekBar_backgroundLineWidth, Util.dp2px(context, 5));
        backgroundLine.lineColor = a.getColor(R.styleable.RangeSeekBar_backgroundLineColor, Color.GRAY);

        trick.trickColor = a.getColor(R.styleable.RangeSeekBar_trickColor, Color.GRAY);
        trick.trickHeight = a.getDimensionPixelSize(R.styleable.RangeSeekBar_trickHeight, Util.dp2px(context, 5));
        trick.trickWidth = a.getDimensionPixelSize(R.styleable.RangeSeekBar_trickWidth, Util.dp2px(context, 1));
        trick.textColor = a.getColor(R.styleable.RangeSeekBar_trickTextColor, Color.GRAY);
        trick.textSize = a.getDimensionPixelSize(R.styleable.RangeSeekBar_trickTextSize, Util.sp2px(context, 13));
        trick.trickInterval = a.getDimensionPixelSize(R.styleable.RangeSeekBar_trickInterval, Util.dp2px(context, 3));

        thumb.lineWidth = a.getDimensionPixelSize(R.styleable.RangeSeekBar_thumbLineWidth, Util.dp2px(context, 10));
        thumb.startColor = a.getColor(R.styleable.RangeSeekBar_thumbStartColor, Color.YELLOW);
        thumb.endColor = a.getColor(R.styleable.RangeSeekBar_thumbEndColor, Color.RED);
        thumb.startDrawable = a.getDrawable(R.styleable.RangeSeekBar_thumbStartDrawable);
        if (thumb.startDrawable == null) {
            thumb.startDrawable = getResources().getDrawable(R.drawable.circle);
        }
        thumb.endDrawable = a.getDrawable(R.styleable.RangeSeekBar_thumbEndDrawable);
        if (thumb.endDrawable == null) {
            thumb.endDrawable = getResources().getDrawable(R.drawable.circle);
        }
        thumb.drawableWidth = a.getDimensionPixelSize(R.styleable.RangeSeekBar_thumbDrawableWidth, Util.dp2px(context, 25));
        thumb.drawableHeight = a.getDimensionPixelSize(R.styleable.RangeSeekBar_thumbDrawableHeight, Util.dp2px(context, 25));

        indicator.startColor = a.getColor(R.styleable.RangeSeekBar_indicatorStartColor, thumb.startColor);
        indicator.endColor = a.getColor(R.styleable.RangeSeekBar_indicatorEndColor, thumb.endColor);
        indicator.textPadding = a.getDimensionPixelSize(R.styleable.RangeSeekBar_indicatorTextPadding, Util.dp2px(context, 2));
        indicator.textSize = a.getDimensionPixelSize(R.styleable.RangeSeekBar_indicatorTextSize, Util.sp2px(context, 13));
        indicator.textColor = a.getColor(R.styleable.RangeSeekBar_indicatorTextColor, Color.GRAY);
        indicator.interval = a.getDimensionPixelSize(R.styleable.RangeSeekBar_indicatorInterval, Util.dp2px(context, 0));
        indicator.arrowHeight = a.getDimensionPixelSize(R.styleable.RangeSeekBar_indicatorArrowHeight, Util.dp2px(context, 4));
        indicator.aroundCorner = a.getDimensionPixelSize(R.styleable.RangeSeekBar_indicatorAroundCorner, Util.dp2px(context, 2));
        a.recycle();

        initInner();
    }

    private void initInner() {
        Paint backgroundLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundLinePaint.setColor(backgroundLine.lineColor);
        backgroundLinePaint.setStrokeWidth(backgroundLine.lineWidth);
        backgroundLine.paint = backgroundLinePaint;

        trick.trickValues.add("100");
        trick.trickValues.add("200");
        trick.trickValues.add("1000");
        trick.trickValues.add("5000");
        trick.textMaxHeight = getMaxTextHeight();
        Paint trickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        trickPaint.setColor(trick.trickColor);
        trickPaint.setStrokeWidth(trick.trickWidth);
        trick.trickPaint = trickPaint;
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(trick.textColor);
        textPaint.setTextSize(trick.textSize);
        trick.textPaint = textPaint;

        Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        thumb.drawablePaint = thumbPaint;
        Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(thumb.lineWidth);
        thumb.linePaint = linePaint;

        indicator.height = (int) (trick.textMaxHeight + indicator.interval + indicator.textPadding * 2 + indicator.arrowHeight) + 1;
        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.FILL);
        indicator.backgroundPaint = backgroundPaint;
        Paint indicatorTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        indicatorTextPaint.setTextSize(indicator.textSize);
        indicatorTextPaint.setColor(indicator.textColor);
        indicatorTextPaint.setTextAlign(Paint.Align.CENTER);
        indicator.textPaint = indicatorTextPaint;
    }

    private float getMaxTextHeight() {
        float height = 0;
        Paint paint = new Paint();
        paint.setTextSize(trick.textSize);
        for (int i = 0; i < trick.trickValues.size(); i++) {
            float curHeight = paint.getFontMetrics().descent - paint.getFontMetrics().ascent;
            if (curHeight > height) {
                height = curHeight;
            }
        }
        return height;
    }

    public void setTrickValues(ArrayList<String> values) {
        trick.trickValues = values;
        trick.textMaxHeight = getMaxTextHeight();
        requestLayout();
    }

    private void init() {
        START_X = thumb.drawableWidth;
        END_X = VIEW_WIDTH - thumb.drawableWidth;

        START_Y = VIEW_HEIGHT - 2 * trick.trickInterval - trick.trickHeight - trick.textMaxHeight - thumb.drawableHeight / 2;
        END_Y = START_Y;
    }

    private float downPosition;
    private float downLeftPostion;
    private float downRightPostion;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getActionMasked();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                isMoveLeft = false;
                isMoveRight = false;
                if (event.getY() > START_Y - thumb.drawableHeight / 2 && event.getY() < START_Y + thumb.drawableHeight / 2) {
                    if (event.getX() > thumb.startPostion - thumb.drawableWidth / 2 && event.getX() < thumb.startPostion + thumb.drawableWidth / 2) {
                        isMoveLeft = true;
                    }
                    if (event.getX() > thumb.endPosition - thumb.drawableWidth / 2 && event.getX() < thumb.endPosition + thumb.drawableWidth / 2) {
                        isMoveRight = true;
                    }
                }
                downPosition = event.getX();
                downLeftPostion = thumb.startPostion;
                downRightPostion = thumb.endPosition;
                break;
            case MotionEvent.ACTION_MOVE:

                if (isMoveLeft) {
                    float curPostion;
                    curPostion = event.getX() - downPosition + downLeftPostion;
                    if (curPostion <= START_X) {
                        curPostion = START_X;
                    }
                    if (curPostion >= downRightPostion - thumb.drawableWidth) {
                        curPostion = downRightPostion - thumb.drawableWidth;
                    }
                    if (thumb.startPostion != curPostion) {
                        thumb.startPostion = curPostion;
                        invalidate();
                        if (listener != null) {
                            listener.rangeSeekBarValueChanged(getLeftValue(), rightValue);
                        } else if (context instanceof RangeSeekBarChangedListener) {
                            ((RangeSeekBarChangedListener) context).rangeSeekBarValueChanged(getLeftValue(), rightValue);
                        }
                    }

                } else if (isMoveRight) {
                    float curPostion = event.getX() - downPosition + downRightPostion;
                    if (curPostion <= downLeftPostion + thumb.drawableWidth) {
                        curPostion = downLeftPostion + thumb.drawableWidth;
                    }
                    if (curPostion >= END_X) {
                        curPostion = END_X;
                    }
                    if (thumb.endPosition != curPostion) {
                        thumb.endPosition = curPostion;
                        invalidate();
                        if (listener != null) {
                            listener.rangeSeekBarValueChanged(leftValue, getRightValue());
                        } else if (context instanceof RangeSeekBarChangedListener) {
                            ((RangeSeekBarChangedListener) context).rangeSeekBarValueChanged(leftValue, getRightValue());
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 画背景
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, VIEW_WIDTH, VIEW_HEIGHT, paint);

        drawBackgroundLine(canvas);
        darwTrick(canvas);
        drawThumb(canvas);
        drawIndicator(canvas);
    }

    private void drawBackgroundLine(Canvas canvas) {
        canvas.drawLine(START_X, START_Y, END_X, END_Y, backgroundLine.paint);
    }

    private void darwTrick(Canvas canvas) {
        int length = trick.trickValues.size();
        if (length < 2) {
            new Exception("");
        }
        trick.interval = (VIEW_WIDTH - thumb.drawableWidth * 2) * 1f / (length - 1);
        float startY = START_Y + trick.trickInterval + thumb.drawableHeight / 2;
        float endY = startY + trick.trickHeight;
        for (int i = 0; i < length; i++) {
            float startX = START_X + i * trick.interval;
            trick.trickPositon.add(i, startX);
            canvas.drawLine(startX, startY, startX, endY, trick.trickPaint);
            String text = trick.trickValues.get(i);
            if (isAutoInfinite && i == length - 1) {
                text = "不限";
            }
            canvas.drawText(text, startX, endY + trick.trickInterval + trick.textMaxHeight - trick.textPaint.getFontMetrics().descent, trick.textPaint);
        }
    }

    private void drawThumb(Canvas canvas) {
        if (thumb.startPostion == 0 && thumb.endPosition == 0) {
            thumb.startPostion = getCenterX(leftValue);
            thumb.endPosition = getCenterX(rightValue);
        }
        thumb.shader = new LinearGradient(thumb.startPostion, START_Y, thumb.endPosition, END_Y, new int[]{thumb.startColor, thumb.endColor},
                new float[]{0, 1}, Shader.TileMode.CLAMP);
        thumb.linePaint.setShader(thumb.shader);
        canvas.drawLine(thumb.startPostion, START_Y, thumb.endPosition, END_Y, thumb.linePaint);
        canvas.drawBitmap(drawableToBitmap(thumb.startDrawable), thumb.startPostion - thumb.drawableWidth / 2, START_Y - thumb.drawableHeight / 2, thumb.drawablePaint);
        canvas.drawBitmap(drawableToBitmap(thumb.endDrawable), thumb.endPosition - thumb.drawableWidth / 2, START_Y - thumb.drawableHeight / 2, thumb.drawablePaint);
    }

    private void drawIndicator(Canvas canvas) {
        float textWidth = indicator.textPaint.measureText(leftValue + "");
        RectF leftRect = new RectF(thumb.startPostion - textWidth / 2 - indicator.textPadding, 0, thumb.startPostion + textWidth / 2 + indicator.textPadding, indicator.textPadding * 2 + trick.textMaxHeight);
        indicator.backgroundPaint.setColor(indicator.startColor);
        indicator.backgroundPaint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(leftRect, indicator.aroundCorner, indicator.aroundCorner, indicator.backgroundPaint);
        Path path = new Path();
        path.moveTo(leftRect.centerX(), leftRect.bottom);
        path.lineTo(leftRect.centerX() - indicator.arrowHeight, leftRect.bottom);
        path.lineTo(leftRect.centerX(), leftRect.bottom + indicator.arrowHeight);
        path.lineTo(leftRect.centerX() + indicator.arrowHeight, leftRect.bottom);
        path.close();
        canvas.drawPath(path, indicator.backgroundPaint);
        canvas.drawText(leftValue + "", leftRect.centerX(), leftRect.centerY() + indicator.textPaint.getFontMetrics().descent, indicator.textPaint);

        textWidth = indicator.textPaint.measureText(rightValue + "");
        RectF rightRect = new RectF(thumb.endPosition - textWidth / 2 - indicator.textPadding, 0, thumb.endPosition + textWidth / 2 + indicator.textPadding, indicator.textPadding * 2 + trick.textMaxHeight);
        indicator.backgroundPaint.setColor(indicator.endColor);
        canvas.drawRoundRect(rightRect, indicator.aroundCorner, indicator.aroundCorner, indicator.backgroundPaint);
        Path path2 = new Path();
        path2.moveTo(rightRect.centerX(), rightRect.bottom);
        path2.lineTo(rightRect.centerX() - indicator.arrowHeight, rightRect.bottom);
        path2.lineTo(rightRect.centerX(), rightRect.bottom + indicator.arrowHeight);
        path2.lineTo(rightRect.centerX() + indicator.arrowHeight, rightRect.bottom);
        path2.close();
        canvas.drawPath(path2, indicator.backgroundPaint);
        canvas.drawText(rightValue + "", rightRect.centerX(), rightRect.centerY() + indicator.textPaint.getFontMetrics().descent, indicator.textPaint);
    }

    private float getCenterX(float value) {
        int index = getIndex(value);
        float maxValue = Float.valueOf(trick.trickValues.get(index));
        float minValue = Float.valueOf(trick.trickValues.get(index - 1));
        return (value - minValue) * trick.interval / (maxValue - minValue) + trick.trickPositon.get(index - 1);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(),
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);

        // 获取这个图片的宽和高
        float width = bitmap.getWidth();
        float height = bitmap.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) thumb.drawableWidth) / width;
        float scaleHeight = ((float) thumb.drawableHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, (int) width, (int) height, matrix, true);
    }

    private int getIndex(float value) {
        for (int i = 0; i < trick.trickValues.size(); i++) {
            float curValue = Float.valueOf(trick.trickValues.get(i));
            if (curValue > value) {
                return i;
            }
        }
        return trick.trickValues.size() - 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        VIEW_WIDTH = measureWidth(widthMeasureSpec);
        VIEW_HEIGHT = measureHeight(heightMeasureSpec);
        init();
        setMeasuredDimension(VIEW_WIDTH, VIEW_HEIGHT);
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = specSize;
            }
        }

        return result;
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        return (int) (indicator.height + indicator.interval + thumb.drawableHeight + trick.textMaxHeight + trick.trickHeight + 2 * trick.trickInterval);
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        CustomSavedState ss = (CustomSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int index = 1;

    }

    @Nullable
    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomSavedState ss = new CustomSavedState(superState);
        ss.childrenStates = new SparseArray<Integer>();

        int index = 1;

        return ss;
    }
}
