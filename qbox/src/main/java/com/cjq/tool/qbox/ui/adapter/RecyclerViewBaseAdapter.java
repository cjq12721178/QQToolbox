package com.cjq.tool.qbox.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by KAT on 2017/3/31.
 * 支持点击、长按事件，以及通过view的selected属性进行突显
 */

public abstract class RecyclerViewBaseAdapter<T, E>
        extends RecyclerView.Adapter
        implements View.OnClickListener,
        View.OnLongClickListener {

    private static final int UPDATE_TYPE_SELECTED_INDEX_CHANGED = 1;
    protected T mItems;
    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private int mSelectedIndex = -1;
    private boolean mUpdateSelectedState;
    private final RecyclerViewBaseDataObserver mDataObserver = new RecyclerViewBaseDataObserver();

    public RecyclerViewBaseAdapter() {
        onAddAdapterDelegate();
    }

    public RecyclerViewBaseAdapter(T items) {
        this();
        setItems(items);
    }

    public void setItems(T items) {
        if (items != mItems) {
            mItems = items;
            notifyDataSetChanged();
        }
    }

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;
    }

    public E getSelectedItem() {
        return mSelectedIndex >= 0 && mSelectedIndex < getItemCount()
                ? getItemByPosition(mSelectedIndex)
                : null;
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

    public abstract void onAddAdapterDelegate();

    protected abstract AdapterDelegate<E> getAdapterDelegate(int viewType);

    public abstract E getItemByPosition(int position);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = getAdapterDelegate(viewType).onCreateViewHolder(parent);
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(this);
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnClickListener(this);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mUpdateSelectedState) {
            holder.itemView.setSelected(position == mSelectedIndex);
        }
        getAdapterDelegate(holder.getItemViewType())
                .onBindViewHolder(holder,
                        getItemByPosition(position),
                        position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            if (mUpdateSelectedState &&
                    (int)payloads.get(0) == UPDATE_TYPE_SELECTED_INDEX_CHANGED) {
                holder.itemView.setSelected(position == mSelectedIndex);
            } else {
                getAdapterDelegate(holder.getItemViewType())
                        .onBindViewHolder(holder,
                                getItemByPosition(position),
                                position,
                                payloads);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        registerAdapterDataObserver(mDataObserver);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        unregisterAdapterDataObserver(mDataObserver);
    }

    @Override
    public void onClick(View v) {
        try {
            Field holderField = RecyclerView.LayoutParams.class.getDeclaredField("mViewHolder");
            holderField.setAccessible(true);
            RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) holderField.get(v.getLayoutParams());
            int currentPosition = holder.getLayoutPosition();
            int lastPosition = mSelectedIndex;
            if (currentPosition != RecyclerView.NO_POSITION) {
                mSelectedIndex = currentPosition;
                mOnItemClickListener.onItemClick(v, currentPosition);
                if (mUpdateSelectedState) {
                    if (lastPosition != -1) {
                        notifyItemChanged(lastPosition, UPDATE_TYPE_SELECTED_INDEX_CHANGED);
                    }
                    notifyItemChanged(currentPosition, UPDATE_TYPE_SELECTED_INDEX_CHANGED);
                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
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
                    notifyItemChanged(position, UPDATE_TYPE_SELECTED_INDEX_CHANGED);
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

    private class RecyclerViewBaseDataObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            mSelectedIndex = -1;
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            if (mSelectedIndex >= positionStart) {
                mSelectedIndex += itemCount;
            }
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            if (fromPosition <= mSelectedIndex && mSelectedIndex < fromPosition + itemCount) {
                mSelectedIndex -= fromPosition - toPosition;
            } else if (fromPosition + itemCount <= mSelectedIndex && mSelectedIndex < toPosition) {
                mSelectedIndex -= itemCount;
            } else if (toPosition <= mSelectedIndex && mSelectedIndex < fromPosition) {
                mSelectedIndex += itemCount;
            }
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            if (positionStart <= mSelectedIndex && mSelectedIndex < positionStart + itemCount) {
                mSelectedIndex = -1;
            } else if (positionStart + itemCount <= mSelectedIndex) {
                mSelectedIndex -= itemCount;
            }
        }
    }
}
