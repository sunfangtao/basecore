package com.wxt.library.base.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wxt.library.base.adapter.holder.ViewHolder;
import com.wxt.library.listener.RecyclerExpandListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseExpandableAdapter extends RecyclerView.Adapter<BaseExpandableAdapter.RVHolder> {

    // key:列表实际下标，不是list中的下标，直接使用会出现越界
    // value:是否为展开的孩子
    private Map<Integer, Boolean> positionChildShow = new HashMap<>();

    private List list;

    protected Context context;

    protected float screenDestiny;

    public static final int GROUP_TYPE = 0;
    public static final int CHILD_TYPE = 1;

    private RecyclerExpandListener expandListener;

    public void setExpandListener(RecyclerExpandListener listener) {
        expandListener = listener;
    }

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

    public BaseExpandableAdapter(Context context, List<?> list) {
        this.list = list;
        this.context = context;
        this.screenDestiny = context.getResources().getDisplayMetrics().density;
    }

    public void appendData(List<?> list) {
        int size = this.list.size();
        this.list.addAll(list);
        notifyItemRangeChanged(size - 1, list.size());
    }

    public void updateData(List<?> list) {
        updateData(list, true);
    }

    public void updateData(List<?> list, boolean isRefresh) {
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

    public boolean isExpand(int position) {
        return positionChildShow.get(position + 1) != null;
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
    public int getItemViewType(int position) {
        return positionChildShow.get(position) == null ? GROUP_TYPE : CHILD_TYPE;
    }

    @Override
    public void onBindViewHolder(final RVHolder holder, final int position) {
        Boolean isShow = positionChildShow.get(position);

        int tempPosition = 0;

        for (int i = 0; i < position; i++) {
            // 不包含找到的是父亲
            if (!positionChildShow.containsKey(i)) {
                tempPosition++;
            }
        }

        final int realPosition = tempPosition;
        if (isShow == null) {
            // 不是展开的数据即父亲
            onBindGroupViewHolder(holder.getViewType(), holder.getViewHolder().getConvertView(), realPosition);
            // 展开、收起
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<Integer, Boolean> tempPositionChildShow = new HashMap<>();
                    Boolean isHasChild = positionChildShow.get(position + 1);
                    if (isHasChild != null) {
                        // 收起
                        positionChildShow.remove(position + 1);
                        for (Integer pos : positionChildShow.keySet()) {
                            // 没有连续下标
                            if (pos < position) {
                                tempPositionChildShow.put(pos, true);
                            } else {
                                tempPositionChildShow.put(pos - 1, true);
                            }
                        }
                        positionChildShow = tempPositionChildShow;
                        notifyDataSetChanged();
//                        notifyItemChanged(position);
//                        notifyItemRemoved(position + 1);
                    } else {
                        for (Integer pos : positionChildShow.keySet()) {
                            // 没有连续下标
                            if (pos < position) {
                                tempPositionChildShow.put(pos, true);
                            } else {
                                tempPositionChildShow.put(pos + 1, true);
                            }
                        }
                        // 展开
                        tempPositionChildShow.put(position + 1, true);
                        positionChildShow = tempPositionChildShow;

//                        notifyItemChanged(position);
//                        notifyItemInserted(position + 1);
                        notifyDataSetChanged();
                    }

                    if (expandListener != null) {
                        expandListener.onExpandChanged(isHasChild == null, realPosition);
                    } else if (context instanceof RecyclerExpandListener) {
                        ((RecyclerExpandListener) context).onExpandChanged(isHasChild == null, realPosition);
                    }

                }
            });
        } else {
            onBindChildViewHolder(holder.getViewType(), holder.getViewHolder().getConvertView(), realPosition - 1);
        }
    }

    public abstract void onBindGroupViewHolder(int viewType, View view, int position);

    public abstract void onBindChildViewHolder(int viewType, View view, int position);

    @Override
    public int getItemCount() {
        if (list.size() == 0) {
            positionChildShow.clear();
            return 0;
        }
        int count = 0;
        for (boolean isShow : positionChildShow.values()) {
            count += (isShow ? 1 : 0);
        }
        return list.size() + count;
    }

}
