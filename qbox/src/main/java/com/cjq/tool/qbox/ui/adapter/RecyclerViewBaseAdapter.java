package com.cjq.tool.qbox.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by KAT on 2017/3/31.
 * 支持点击、长按事件，以及通过view的selected属性进行突显
 */

public abstract class RecyclerViewBaseAdapter<T, E>
        extends RecyclerView.Adapter
        implements View.OnClickListener,
        View.OnLongClickListener {

    protected T mItems;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private int mSelectedIndex = -1;
    private boolean mUpdateSelectedState;

    public RecyclerViewBaseAdapter() {
    }

    public RecyclerViewBaseAdapter(T items) {
        setItems(items);
    }

    public void setItems(T items) {
        mItems = items;
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;
    }

    public void setUpdateSelectedState(boolean updateSelectedState) {
        mUpdateSelectedState = updateSelectedState;
    }

    //请在setAdapter之前调用，下同
    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    public abstract void addAdapterDelegate(AdapterDelegate<E> delegate);

    protected abstract AdapterDelegate<E> getAdapterDelegate(int viewType);

    public abstract E getItemByPosition(int position);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = getAdapterDelegate(viewType).onCreateViewHolder(parent);
        boolean needTag = false;
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(this);
            needTag = true;
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnClickListener(this);
            needTag = true;
        }
        if (needTag) {
            holder.itemView.setTag(holder);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mUpdateSelectedState) {
            holder.itemView.setSelected(position == mSelectedIndex);
        }
        getAdapterDelegate(holder.getItemViewType()).onBindViewHolder(holder, getItemByPosition(position), position);
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof RecyclerView.ViewHolder) {
            RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) v.getTag();
            int position = holder.getLayoutPosition();
            if (position != RecyclerView.NO_POSITION) {
                mSelectedIndex = position;
                mOnItemClickListener.onItemClick(v, position);
                if (mUpdateSelectedState) {
                    notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (v.getTag() instanceof RecyclerView.ViewHolder) {
            RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) v.getTag();
            int position = holder.getLayoutPosition();
            if (position != RecyclerView.NO_POSITION) {
                mSelectedIndex = position;
                mOnItemLongClickListener.onItemLongClick(v, position);
                if (mUpdateSelectedState) {
                    notifyDataSetChanged();
                }
            }
        }
        return true;
    }

    public interface OnItemClickListener {
        void onItemClick(View item, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View item, int position);
    }
}
