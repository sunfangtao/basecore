package com.wxt.library.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.wxt.library.R;
import com.wxt.library.model.CustomSavedState;
import com.wxt.library.util.Util;

import java.util.List;

/**
 * Created by Administrator on 2017/11/21.
 */

public class DefaultNullRecyclerView extends FrameLayout {

    private int layoutId = -1;
    private int icon = -1;
    private String text = "";
    private RecyclerView mRecyclerView;
    private int yPosition = 0;
    private RecyclerView.AdapterDataObserver adapterDataObserver;
    private RecyclerView.OnScrollListener scrollListener;
    private List<RecyclerView.OnScrollListener> mScrollListeners;

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
        text = a.getString(R.styleable.NullRecyclerView_nullText);
        if (text == null || text.length() == 0) {
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

    public void addOnScrollListener(RecyclerView.OnScrollListener listener) {
        mRecyclerView.addOnScrollListener(listener);
    }

    public void clearOnScrollListeners() {
        mRecyclerView.clearOnScrollListeners();
    }

    private void init() {
        if (layoutId > 0) {
            View nullView = LayoutInflater.from(getContext()).inflate(layoutId, null);
            addView(nullView);
        } else {
            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            TextView textView = new TextView(getContext());

            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setText(text);
            textView.setPadding(0, Util.dp2px(getContext(), 30), 0, 0);
            textView.setTextColor(Color.parseColor("#5F5F5F"));
            Drawable iconDrawable = getResources().getDrawable(icon);
            iconDrawable.setBounds(0, 0, Util.dp2px(getContext(), 100), Util.dp2px(getContext(), 100));
            textView.setCompoundDrawables(null, iconDrawable, null, null);
            textView.setCompoundDrawablePadding(Util.dp2px(getContext(), 15));
            textView.setLayoutParams(params);
            addView(textView);
        }
        mRecyclerView = new RecyclerView(getContext());
        mRecyclerView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        addView(mRecyclerView);
    }

    private void setyPosition(int yPosition) {
        mRecyclerView.smoothScrollBy(0, yPosition, null);
    }

    public void setLayoutId(int resouceId) {
        if (resouceId > 0) {
            this.layoutId = resouceId;
            View nullView = LayoutInflater.from(getContext()).inflate(this.layoutId, null);
            if (nullView != null) {
                removeViewAt(0);
                addView(nullView, 0);
            }
        }
    }

    public void setNullIcon(int resouceId) {
        setNullIcon(resouceId, 100, 100);
    }

    public void setNullIcon(int resouceId, int width, int height) {
        if (layoutId <= 0) {
            this.icon = resouceId;
            Drawable iconDrawable = getResources().getDrawable(this.icon);
            iconDrawable.setBounds(0, 0, Util.dp2px(getContext(), width), Util.dp2px(getContext(), height));
            ((TextView) getChildAt(0)).setCompoundDrawables(null, iconDrawable, null, null);
        }
    }

    public void setNullText(String text) {
        if (layoutId <= 0) {
            this.text = text;
            ((TextView) getChildAt(0)).setText(text);
        }
    }

    private void setRecyclerViewSrcollerListener() {
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
    }

    public void setAdapter(RecyclerView.Adapter adapter) {
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        setRecyclerViewSrcollerListener();
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
            setRecyclerViewSrcollerListener();
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

        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        CustomSavedState ss = (CustomSavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        int index = 1;
        setLayoutId((int) ss.childrenStates.get(index++));
        setNullIcon((int) ss.childrenStates.get(index++));
        setNullText((String) ss.childrenStates.get(index++));
        setyPosition((int) ss.childrenStates.get(index++));
        showNullData((int) ss.childrenStates.get(index++));
        setRecyclerViewSrcollerListener();
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
        ss.childrenStates.put(index++, mRecyclerView.getAdapter().getItemCount());

        return ss;
    }

}
