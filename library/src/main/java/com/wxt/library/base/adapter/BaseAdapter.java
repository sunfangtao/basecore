package com.wxt.library.base.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wxt.library.base.adapter.holder.ViewHolder;
import com.wxt.library.base.fragment.BaseFragment;
import com.wxt.library.listener.RecyclerViewItemClickListener;
import com.wxt.library.listener.RecyclerViewItemLongClickListener;

import java.util.List;

public abstract class BaseAdapter extends RecyclerView.Adapter<BaseAdapter.RVHolder> {

    private List list;

    private RecyclerViewItemClickListener itemClickListener;
    private RecyclerViewItemLongClickListener itemLongClickListener;

    protected Context context;

    protected float screenDestiny;

    private BaseFragment fragment;

    public class RVHolder extends RecyclerView.ViewHolder {

        private ViewHolder viewHolder;
        private int viewType;

        public RVHolder(View itemView, int viewType) {
            super(itemView);
            this.viewType = viewType;
            viewHolder = ViewHolder.getViewHolder(itemView);
        }

        public ViewHolder getViewHolder() {
            return viewHolder;
        }

        public int getViewType() {
            return viewType;
        }
    }

    public BaseAdapter(Context context, List<?> list) {
        this.list = list;
        this.context = context;
        this.screenDestiny = context.getResources().getDisplayMetrics().density;
    }

    public BaseAdapter(Context context, BaseFragment fragment, List<?> list) {
        this.list = list;
        this.context = context;
        this.fragment = fragment;
        this.screenDestiny = context.getResources().getDisplayMetrics().density;
    }

    public void setItemClickListener(RecyclerViewItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setItemLongClickListener(RecyclerViewItemLongClickListener listener) {
        this.itemLongClickListener = listener;
    }

    public void appendData(List<?> list) {
        int size = this.list.size();
        this.list.addAll(list);
        notifyItemRangeChanged(size - 1, list.size());
    }

    public void updateData(List<?> list) {
        if (list == null) {
            return;
        }
        updateData(list, true);
    }

    public void updateData(List<?> list, boolean isRefresh) {
        if (list == null) {
            return;
        }
        this.list = list;
        if (isRefresh) {
            notifyDataSetChanged();
        }
    }

    public Object getObject(int position) {
        if (position < 0 || position > list.size() - 1) {
            return null;
        }
        return list.get(position);
    }

    public Object getList() {
        return list;
    }

    @Override
    public RVHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        int resourceId = onCreateViewLayoutID(viewType);
        if (resourceId > 0) {
            view = LayoutInflater.from(context).inflate(resourceId, parent, false);
        } else {
            view = onCreateViewNoLayoutID();
        }

        if (view == null) {
            throw new IllegalArgumentException("onCreateViewLayoutID 返回<=0时，请重写onCreateViewNoLayoutID()返回View");
        }

        return new RVHolder(view, viewType);
    }

    public View onCreateViewNoLayoutID() {
        return null;
    }

    public abstract int onCreateViewLayoutID(int viewType);

    @Override
    public void onBindViewHolder(final RVHolder holder, final int position) {
        onBindViewHolder(holder.getViewType(), holder.getViewHolder().getConvertView(), holder.getAdapterPosition());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (itemClickListener != null) {
                    itemClickListener.onRecyclerViewItemClick(BaseAdapter.this, v, holder.getAdapterPosition());
                } else if (fragment != null && fragment instanceof RecyclerViewItemClickListener) {
                    ((RecyclerViewItemClickListener) fragment).onRecyclerViewItemClick(BaseAdapter.this, v, holder.getAdapterPosition());
                } else if (context instanceof RecyclerViewItemClickListener) {
                    ((RecyclerViewItemClickListener) context).onRecyclerViewItemClick(BaseAdapter.this, v, holder.getAdapterPosition());
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (itemLongClickListener != null) {
                    itemLongClickListener.onRecyclerViewLongItemClick(BaseAdapter.this, v, holder.getAdapterPosition());
                } else if (fragment != null && fragment instanceof RecyclerViewItemLongClickListener) {
                    ((RecyclerViewItemLongClickListener) fragment).onRecyclerViewLongItemClick(BaseAdapter.this, v, holder.getAdapterPosition());
                } else if (context instanceof RecyclerViewItemLongClickListener) {
                    ((RecyclerViewItemLongClickListener) context).onRecyclerViewLongItemClick(BaseAdapter.this, v, holder.getAdapterPosition());
                }
                return true;
            }
        });
    }

    public abstract void onBindViewHolder(int viewType, View view, int position);

    @Override
    public int getItemCount() {
        return list.size();
    }

}
