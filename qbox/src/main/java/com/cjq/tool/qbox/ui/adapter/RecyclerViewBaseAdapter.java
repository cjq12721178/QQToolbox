package com.cjq.tool.qbox.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by KAT on 2017/3/31.
 * 支持点击、长按事件，以及通过view的selected属性进行突显
 */

public abstract class RecyclerViewBaseAdapter<E>
        extends RecyclerView.Adapter
        implements AdapterDelegate<E> {

    private static final int UPDATE_TYPE_SELECTED_INDEX_CHANGED = 100000;
    //private OnItemClickListener mOnItemClickListener;
    //private OnItemLongClickListener mOnItemLongClickListener;
    private int mSelectedIndex = RecyclerView.NO_POSITION;
    private boolean mUpdateSelectedState;
    private final RecyclerViewBaseDataObserver mDataObserver = new RecyclerViewBaseDataObserver();
    private final AdapterDelegateManager<E> mAdapterDelegateManager;

    public RecyclerViewBaseAdapter(AdapterDelegateManager<E> manager) {
        mAdapterDelegateManager = manager == null
                ? new SingleAdapterDelegateManager()
                : manager;
    }

    public RecyclerViewBaseAdapter() {
        this(null);
    }

    public boolean isUpdateSelectedState() {
        return mUpdateSelectedState;
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

    public void toggleSelection(int position) {
        if (mSelectedIndex != RecyclerView.NO_POSITION) {
            notifyItemChanged(mSelectedIndex, UPDATE_TYPE_SELECTED_INDEX_CHANGED);
        }
        if (position < 0 || position >= getItemCount()) {
            mSelectedIndex = RecyclerView.NO_POSITION;
        } else {
            mSelectedIndex = position;
            notifyItemChanged(mSelectedIndex, UPDATE_TYPE_SELECTED_INDEX_CHANGED);
        }
    }

    public void setUpdateSelectedState(boolean updateSelectedState) {
        mUpdateSelectedState = updateSelectedState;
    }

//    //请在setAdapter之前调用，下同
//    public void setOnItemClickListener(OnItemClickListener listener) {
//        mOnItemClickListener = listener;
//    }
//
//    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
//        mOnItemLongClickListener = listener;
//    }

    public abstract E getItemByPosition(int position);

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return mAdapterDelegateManager.onCreateViewHolder(parent, viewType);
//        RecyclerView.ViewHolder holder = mAdapterDelegateManager.onCreateViewHolder(parent, viewType);
//        if (mOnItemClickListener != null) {
//            holder.itemView.setOnClickListener(this);
//        }
//        if (mOnItemLongClickListener != null) {
//            holder.itemView.setOnClickListener(this);
//        }
//        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//        if (mUpdateSelectedState) {
//            holder.itemView.setSelected(position == mSelectedIndex);
//        }
        mAdapterDelegateManager.onBindViewHolder(holder, getItemByPosition(position), position);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
        if (mUpdateSelectedState) {
            holder.itemView.setSelected(position == mSelectedIndex);
        }
        holder.itemView.setTag(position);
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
        } else {
            Object payload = payloads.get(0);
            if (payload instanceof Integer &&
                    (int)payload == UPDATE_TYPE_SELECTED_INDEX_CHANGED) {
                return;
            }
            mAdapterDelegateManager.onBindViewHolder(holder, getItemByPosition(position), position, payloads);
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

//    private boolean onItemsSelected(View v) {
//        int currentPosition = getPositionByItemView(v);
//        if (currentPosition == -1) {
//            return false;
//        }
//        int lastPosition = mSelectedIndex;
////        if (currentPosition == RecyclerView.NO_POSITION) {
////            return false;
////        }
//        mSelectedIndex = currentPosition;
//        if (mUpdateSelectedState) {
//            if (lastPosition != -1) {
//                notifyItemChanged(lastPosition, UPDATE_TYPE_SELECTED_INDEX_CHANGED);
//            }
//            notifyItemChanged(currentPosition, UPDATE_TYPE_SELECTED_INDEX_CHANGED);
//        }
//        return true;
////        try {
////            Field holderField = RecyclerView.LayoutParams.class.getDeclaredField("mViewHolder");
////            holderField.setAccessible(true);
////            RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) holderField.get(v.getLayoutParams());
////            int currentPosition = holder.getLayoutPosition();
////            int lastPosition = mSelectedIndex;
////            if (currentPosition != RecyclerView.NO_POSITION) {
////                mSelectedIndex = currentPosition;
////                if (mUpdateSelectedState) {
////                    if (lastPosition != -1) {
////                        notifyItemChanged(lastPosition, UPDATE_TYPE_SELECTED_INDEX_CHANGED);
////                    }
////                    notifyItemChanged(currentPosition, UPDATE_TYPE_SELECTED_INDEX_CHANGED);
////                }
////                return true;
////            }
////        } catch (NoSuchFieldException e) {
////            e.printStackTrace();
////        } catch (IllegalAccessException e) {
////            e.printStackTrace();
////        }
////        return false;
//    }

//    public static int getPositionByItemView(View v) {
//        Object tag = v.getTag();
//        if (tag instanceof Integer) {
//            return (int) tag;
//        }
//        return -1;
////        try {
////            Field holderField = RecyclerView.LayoutParams.class.getDeclaredField("mViewHolder");
////            holderField.setAccessible(true);
////            RecyclerView.ViewHolder holder = (RecyclerView.ViewHolder) holderField.get(v.getLayoutParams());
////            return holder.getLayoutPosition();
////        } catch (NoSuchFieldException e) {
////            e.printStackTrace();
////        } catch (IllegalAccessException e) {
////            e.printStackTrace();
////        }
////        return -1;
//    }

//    public static int getPositionByItemChildView(View v) {
//        ViewParent parent = v.getParent();
//        if (parent instanceof View) {
//            return getPositionByItemView((View) parent);
//        } else {
//            return -1;
//        }
//    }

//    @Override
//    public void onClick(View v) {
//        if (onItemsSelected(v)) {
//            mOnItemClickListener.onItemClick(v, mSelectedIndex);
//        }
//    }
//
//    @Override
//    public boolean onLongClick(View v) {
//        if (onItemsSelected(v)) {
//            mOnItemLongClickListener.onItemLongClick(v, mSelectedIndex);
//        }
//        return true;
//    }

    //以下4个方法用于只有一个viewType时候重载使用
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, E item, int position) {
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, E item, int position, List payloads) {

    }

    @Override
    public int getItemViewType() {
        return 0;
    }

    public void notifyDataSetChanged(int previousSize) {
        int currentSize = getItemCount();
        if (previousSize < currentSize) {
            notifyItemRangeChanged(0, previousSize);
            notifyItemRangeInserted(previousSize, currentSize - previousSize);
        } else if (previousSize > currentSize) {
            notifyItemRangeChanged(0, currentSize);
            notifyItemRangeRemoved(currentSize, previousSize - currentSize);
        } else {
            notifyItemRangeChanged(0, currentSize);
        }
    }

//    public interface OnItemClickListener {
//        void onItemClick(View item, int position);
//    }
//
//    public interface OnItemLongClickListener {
//        void onItemLongClick(View item, int position);
//    }

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

    private class SingleAdapterDelegateManager extends AdapterDelegateManager<E> {

        @Override
        public void addAdapterDelegate(AdapterDelegate<E> delegate) {

        }

        @Override
        protected AdapterDelegate<E> getAdapterDelegate(int viewType) {
            return RecyclerViewBaseAdapter.this;
        }
    }
}
