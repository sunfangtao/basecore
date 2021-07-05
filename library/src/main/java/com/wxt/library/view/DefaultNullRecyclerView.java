package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.listener.LoadMoreListener;
import com.wxt.library.model.CustomSavedState;
import com.wxt.library.util.Util;

/**
 * Created by Administrator on 2017/11/21.
 */

public class DefaultNullRecyclerView extends FrameLayout {

    private int layoutId = -1;
    private int icon = -1;
    private int padding = 0;
    private int width = 100;
    private int height = 100;
    private String text = "";
    private RecyclerView mRecyclerView;
    private int yPosition = 0;
    private RecyclerView.AdapterDataObserver adapterDataObserver;
    // 用于恢复用户滚动的位置
    private RecyclerView.OnScrollListener scrollListener;
    // 滑动到底部自动加载更多
    private LoadMoreListener loadMoreListener;

    private MyOnScrollerListener myOnScrollerListener = new MyOnScrollerListener();

    public DefaultNullRecyclerView(@NonNull Context context) {
        this(context, null);
        setId(Util.getViewAutoId());
    }

    public DefaultNullRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DefaultNullRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NullRecyclerView);
        layoutId = a.getResourceId(R.styleable.NullRecyclerView_nullLayout, -1);
        icon = a.getResourceId(R.styleable.NullRecyclerView_nullIcon, R.drawable.nullicon);
        padding = a.getDimensionPixelOffset(R.styleable.NullRecyclerView_nullPadding, Util.dp2px(getContext(), 25));
        width = a.getDimensionPixelOffset(R.styleable.NullRecyclerView_nullIconWidth, Util.dp2px(getContext(), 100));
        height = a.getDimensionPixelOffset(R.styleable.NullRecyclerView_nullIconHeight, Util.dp2px(getContext(), 100));
        text = a.getString(R.styleable.NullRecyclerView_nullText);
        if (text == null) {
            text = "空空如也";
        }
        a.recycle();

        init();

        adapterDataObserver = new RecyclerView.AdapterDataObserver() {

            @Override
            public void onChanged() {
                showNullData(mRecyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                showNullData(mRecyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
                super.onItemRangeChanged(positionStart, itemCount, payload);
                showNullData(mRecyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                showNullData(mRecyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                super.onItemRangeRemoved(positionStart, itemCount);
                showNullData(mRecyclerView.getAdapter().getItemCount());
            }

            @Override
            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                showNullData(mRecyclerView.getAdapter().getItemCount());
            }
        };
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    public RecyclerView.LayoutManager getLayoutManager() {
        return mRecyclerView.getLayoutManager();
    }

    public void setLoadMoreListener(LoadMoreListener loadMoreListener) {
        this.loadMoreListener = loadMoreListener;
    }

    public void addOnScrollListener(RecyclerView.OnScrollListener listener) {
        mRecyclerView.addOnScrollListener(listener);
    }

    public void clearOnScrollListeners() {
        mRecyclerView.clearOnScrollListeners();
        mRecyclerView.addOnScrollListener(myOnScrollerListener);
    }

    private void init() {
        if (layoutId > 0) {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            View nullView = LayoutInflater.from(getContext()).inflate(layoutId, null);
            nullView.setLayoutParams(params);
            addView(nullView);
        } else {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            TextView textView = new TextView(getContext());

            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(text);
            textView.setTextSize(14);
            textView.setPadding(0, padding, 0, 0);
            textView.setTextColor(Color.parseColor("#5F5F5F"));
            Drawable iconDrawable = getResources().getDrawable(icon);
            iconDrawable.setBounds(0, 0, width, height);
            textView.setCompoundDrawables(null, iconDrawable, null, null);
            textView.setCompoundDrawablePadding(Util.dp2px(getContext(), 15));
            textView.setLayoutParams(params);
            addView(textView);
        }
        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mRecyclerView);
    }

    private void setYPosition(int yPosition) {
        mRecyclerView.smoothScrollBy(0, yPosition, null);
    }

    public void setLayoutId(int resourceId) {
        if (resourceId > 0) {
            this.layoutId = resourceId;
            View nullView = LayoutInflater.from(getContext()).inflate(this.layoutId, null);
            if (nullView != null) {
                removeViewAt(0);
                addView(nullView, 0);
            }
        }
    }

    public void setNullIcon(int resourceId) {
        setNullIcon(resourceId, width, height);
    }

    public void setNullIcon(int resourceId, int width, int height) {
        if (layoutId <= 0) {
            this.icon = resourceId;
            if (this.icon <= 0) {
                ((TextView) getChildAt(0)).setCompoundDrawables(null, null, null, null);
                return;
            }
            Drawable iconDrawable = getResources().getDrawable(this.icon);
            iconDrawable.setBounds(0, 0, width, height);
            ((TextView) getChildAt(0)).setCompoundDrawables(null, iconDrawable, null, null);
        }
    }

    public void setNullText(String text) {
        if (layoutId <= 0) {
            this.text = text;
            ((TextView) getChildAt(0)).setText(text);
        }
    }

    private void setRecyclerViewScrollerListener() {
        if (scrollListener != null) {
            this.mRecyclerView.removeOnScrollListener(scrollListener);
        }
        scrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                yPosition += dy;
            }
        };
        this.mRecyclerView.addOnScrollListener(scrollListener);

        this.mRecyclerView.removeOnScrollListener(myOnScrollerListener);
        this.mRecyclerView.addOnScrollListener(myOnScrollerListener);
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        setRecyclerViewScrollerListener();
        setObserver(adapter);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration decor) {
        mRecyclerView.addItemDecoration(decor);
    }

    public void updateRecyclerView(RecyclerView recyclerView) {
        if (recyclerView != null) {
            if (recyclerView.getAdapter() == null) {
                throw new IllegalArgumentException("recyclerview 的 adapter 不能为空!");
            }
            removeViewAt(1);
            this.mRecyclerView = recyclerView;
            addView(this.mRecyclerView);
            setRecyclerViewScrollerListener();
            setObserver(recyclerView.getAdapter());
        }
    }

    public RecyclerView.Adapter getAdapter() {
        return this.mRecyclerView.getAdapter();
    }

    private void setObserver(RecyclerView.Adapter adapter) {
        try {
            if (adapter.hasObservers()) {
                adapter.unregisterAdapterDataObserver(adapterDataObserver);
            }
        } catch (IllegalStateException e) {

        } finally {
            adapter.registerAdapterDataObserver(adapterDataObserver);
        }
    }

    private void showNullData(int count) {
        if (count == 0) {
            getChildAt(0).setVisibility(View.VISIBLE);
            getChildAt(1).setVisibility(View.GONE);
        } else {
            getChildAt(0).setVisibility(View.GONE);
            getChildAt(1).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if (mRecyclerView.getAdapter().hasObservers()) {
                mRecyclerView.getAdapter().unregisterAdapterDataObserver(adapterDataObserver);
            }
        } catch (IllegalStateException e) {

        } catch (NullPointerException e) {
        }
    }

    private class MyOnScrollerListener extends RecyclerView.OnScrollListener {

        //用来标记是否正在向最后一个滑动，既是否向下滑动
        boolean isSlidingToLast = false;

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();

                //当前屏幕所看到的子项个数
                int visibleItemCount = layoutManager.getChildCount();
                //当前RecyclerView的所有子项个数
                int totalItemCount = layoutManager.getItemCount();
                //屏幕中最后一个可见子项的position
                int lastVisiblePosition = 0;

                if (layoutManager instanceof GridLayoutManager) {
                    GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                    lastVisiblePosition = gridLayoutManager.findLastVisibleItemPosition();
                } else if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                    lastVisiblePosition = linearLayoutManager.findLastVisibleItemPosition();
                } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                    StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) recyclerView.getLayoutManager();
                    int[] lastVisiblePositions = staggeredGridLayoutManager.findLastVisibleItemPositions(new int[staggeredGridLayoutManager.getSpanCount()]);
                    lastVisiblePosition = getMaxElem(lastVisiblePositions);
                }

                if (visibleItemCount > 0 && lastVisiblePosition > 0 && lastVisiblePosition == totalItemCount - 1 && isSlidingToLast) {
                    loadMore();
                }
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            //dx用来判断横向滑动方向，dy用来判断纵向滑动方向
            if (dy > 0 || dx > 0) {
                //大于0表示，正在向下(右)滚动
                isSlidingToLast = true;
            } else {
                isSlidingToLast = false;
            }
        }
    }

    private void loadMore() {
        if (loadMoreListener != null) {
            loadMoreListener.loadMore();
        } else if (getContext() instanceof LoadMoreListener) {
            ((LoadMoreListener) getContext()).loadMore();
        }
    }

    private int getMaxElem(int[] arr) {
        int size = arr.length;
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            if (arr[i] > maxVal)
                maxVal = arr[i];
        }
        return maxVal;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        CustomSavedState ss = (CustomSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int index = 1;
        setLayoutId((int) ss.childrenStates.get(index++));
        setNullIcon((int) ss.childrenStates.get(index++));
        setNullText((String) ss.childrenStates.get(index++));
        setYPosition((int) ss.childrenStates.get(index++));
        if (mRecyclerView.getAdapter() != null) {
            showNullData((int) ss.childrenStates.get(index++));
        }
        setRecyclerViewScrollerListener();
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        CustomSavedState ss = new CustomSavedState(superState);
        ss.childrenStates = new SparseArray<Integer>();

        int index = 1;
        ss.childrenStates.put(index++, layoutId);
        ss.childrenStates.put(index++, icon);
        ss.childrenStates.put(index++, text);
        ss.childrenStates.put(index++, yPosition);
        if (mRecyclerView.getAdapter() != null) {
            ss.childrenStates.put(index++, mRecyclerView.getAdapter().getItemCount());
        }

        return ss;
    }

}
