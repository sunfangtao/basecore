package com.wxt.library.view;

import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.wxt.library.R;
import com.wxt.library.util.Util;

/**
 * Created by Administrator on 2018/1/29.
 */

public class NestedStickScrollerView extends LinearLayout implements NestedScrollingParent, NestedScrollingChild {

    private NestedScrollingParentHelper nestedScrollingParentHelper;
    private NestedScrollingChildHelper nestedScrollingChildHelper;

    private int topHeight;
    private View topView;
    private View stickView;
    private View scrollChildView;

    private int mLastTouchX;
    private int mLastTouchY;
    private final int[] mNestedOffsets = new int[2];
    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];

    private GestureDetector mGestureDetector;

    private OverScroller mScroller;


    public NestedStickScrollerView(Context context) {
        super(context);
        init();
    }

    public NestedStickScrollerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NestedStickScrollerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        nestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        mScroller = new OverScroller(getContext());
        mGestureDetector = new GestureDetector(getContext(), new MyGestureListener());
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    public void fling(float velocityY) {
        if (velocityY < 0) {
            // 向上滑动
            mScroller.fling(0, getScrollY(), 0, -(int) velocityY, 0, 0, 0, topHeight);
        } else if (velocityY > 0) {
            mScroller.fling(0, getScrollY(), 0, -(int) velocityY, 0, 0, 0, topHeight);
        }
        invalidate();
    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        public boolean onDown(MotionEvent e) {
            mScroller.forceFinished(true);
            mLastTouchX = (int) (e.getX() + 0.5f);
            mLastTouchY = (int) (e.getY() + 0.5f);
            startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
            return true;
        }

        public boolean onSingleTapUp(MotionEvent e) {
            stopNestedScroll();
            return true;
        }

        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            final int x = (int) e2.getX();
            final int y = (int) e2.getY();
            int dx = mLastTouchX - x;
            int dy = mLastTouchY - y;
            if (dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset)) {
                dx -= mScrollConsumed[0];
                dy -= mScrollConsumed[1];
                mNestedOffsets[0] += mScrollOffset[0];
                mNestedOffsets[1] += mScrollOffset[1];
            }
            if (getScrollY() + dy > 0) {
                scrollTo(0, getScrollY() + dy);
                dispatchNestedScroll(0, dy, dx, 0, mNestedOffsets);
            } else {
                if (getScrollY() > 0) {
                    scrollBy(0, -getScrollY());
                    dispatchNestedScroll(0, getScrollY(), dx, dy - getScrollY(), mNestedOffsets);
                }
                scrollTo(0, 0);
            }
            mLastTouchX = x - mScrollOffset[0];
            mLastTouchY = y - mScrollOffset[1];
            return true;
        }

        // 用户按下触屏、快速移动后松开：
        //   按下并快速滑动一小段距离（多个move），up时触发，e1为down(仅一次)时的MotionEvent，
        //   e2为up(仅一次)时的MotionEvent:
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (!dispatchNestedPreFling(velocityX, velocityY)) {
                fling(velocityY);
                boolean consumed = false;
                if (velocityY < 0 && getScrollY() < topHeight) {
                    consumed = true;
                }
                if (velocityY > 0 && getScrollY() == 0) {
                    consumed = true;
                }

                dispatchNestedFling(velocityX, velocityY, consumed);
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    //获取子view
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        topView = findViewById(R.id.nested_top);
        stickView = findViewById(R.id.nested_stick);
        scrollChildView = findViewById(R.id.nested_list);

        if (topView == null || stickView == null || scrollChildView == null) {
            throw new IllegalArgumentException("null");
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        switch (topView.getLayoutParams().height) {
            case ViewGroup.LayoutParams.WRAP_CONTENT:
            case ViewGroup.LayoutParams.MATCH_PARENT:
                topView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                break;
        }

        switch (stickView.getLayoutParams().height) {
            case ViewGroup.LayoutParams.WRAP_CONTENT:
                stickView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                break;
            case ViewGroup.LayoutParams.MATCH_PARENT:
                stickView.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                break;
        }

        ViewGroup.LayoutParams params = scrollChildView.getLayoutParams();
        params.height = getMeasuredHeight() - stickView.getMeasuredHeight();
        setMeasuredDimension(getMeasuredWidth(), topView.getMeasuredHeight() + stickView.getMeasuredHeight() + scrollChildView.getMeasuredHeight());
        if (topHeight <= 0) {
            topHeight = topView.getMeasuredHeight();
        }
    }

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        // 监听垂直滚动,内部view垂直滑动可以传递给本类，onTouch中Down的时候触发
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        // 由本类消耗指定的垂直滚动，将剩余的交给内部view
        // 上滑 dy>0 ,下滑 dy<0
        if (showTop(dy) || hideTop(dy)) {
            // 本类消耗所有滚动，不交给内部view
            scrollBy(0, dy);
            consumed[1] = dy;
        }
    }

    public boolean showTop(int dy) {
        if (dy < 0) {
            if (getScrollY() > 0 && scrollChildView.getScrollY() == 0 && !scrollChildView.canScrollVertically(-1)) {
                return true;
            }
        }

        return false;
    }

    public boolean hideTop(int dy) {
        if (dy > 0) {
            if (getScrollY() < topHeight) {
                return true;
            }
        }
        return false;
    }

    //scrollBy内部会调用scrollTo
    //限制滚动范围
    @Override
    public void scrollTo(int x, int y) {
        if (y < 0) {
            y = 0;
        }
        if (y > topHeight) {
            y = topHeight;
        }

        super.scrollTo(x, y);
    }

    // 后于child滚动
    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    // 返回值：是否消费了fling
    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {

        mScroller.forceFinished(true);
        if (velocityY > 0 && getScrollY() < topHeight) {
            // TODO 向上滑
            fling(-velocityY);
            return true;
        }
        return false;
    }

    //返回值：是否消费了fling
    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (target instanceof RecyclerView && velocityY < 0) {
            final RecyclerView recyclerView = (RecyclerView) target;
            if (recyclerView.computeVerticalScrollOffset() == 0) {
                fling(-velocityY);
                return true;
            }
        } else {
            if (!consumed) {
                if (velocityY > 0) {
                    //向上滑
                    fling(-velocityY);
                    return true;
                } else if (velocityY < 0 && getScrollY() < topHeight) {

                }
            }
        }
        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return nestedScrollingParentHelper.getNestedScrollAxes();
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        nestedScrollingParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(View target) {
        nestedScrollingParentHelper.onStopNestedScroll(target);
    }

    public void setNestedScrollingEnabled(boolean enabled) {
        nestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    public boolean isNestedScrollingEnabled() {
        return nestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    public boolean startNestedScroll(int axes) {
        return nestedScrollingChildHelper.startNestedScroll(axes);
    }

    public void stopNestedScroll() {
        nestedScrollingChildHelper.stopNestedScroll();
    }

    public boolean hasNestedScrollingParent() {
        return nestedScrollingChildHelper.hasNestedScrollingParent();
    }

    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed,
                                        int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return nestedScrollingChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return nestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return nestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}
