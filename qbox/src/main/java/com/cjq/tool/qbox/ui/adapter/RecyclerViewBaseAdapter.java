package com.cjq.tool.qbox.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by KAT on 2017/3/31.
 * 支持点击、长按事件，以及通过view的selected属性进行突显
 */

public abstract class RecyclerViewBaseAdapter<VH extends RecyclerViewBaseAdapter.ViewHolder>
        extends RecyclerView.Adapter<VH> {

    private OnItemClickListener mOnItemClickListener;
    private OnItemLongClickListener mOnItemLongClickListener;
    private int mSelectedIndex = -1;
    private boolean mUpdateSelectedState;

    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public void setSelectedIndex(int index) {
        mSelectedIndex = index;
    }

    public void setUpdateSelectedState(boolean updateSelectedState) {
        mUpdateSelectedState = updateSelectedState;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mOnItemLongClickListener = listener;
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        if (mUpdateSelectedState) {
            holder.itemView.setSelected(position == mSelectedIndex);
        }
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener, View.OnLongClickListener {

        private RecyclerViewBaseAdapter mAdapter;

        public ViewHolder(View itemView, RecyclerViewBaseAdapter adapter) {
            super(itemView);
            mAdapter = adapter;
            if (mAdapter.mOnItemClickListener != null) {
                itemView.setOnClickListener(this);
            }
            if (mAdapter.mOnItemLongClickListener != null) {
                itemView.setOnLongClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            int position = getLayoutPosition();
            if (position != RecyclerView.NO_POSITION) {
                mAdapter.mSelectedIndex = position;
                mAdapter.mOnItemClickListener.onItemClick(v, position);
                if (mAdapter.mUpdateSelectedState) {
                    mAdapter.notifyDataSetChanged();
                }
            }
        }

        @Override
        public boolean onLongClick(View v) {
            int position = getLayoutPosition();
            if (position != RecyclerView.NO_POSITION) {
                mAdapter.mSelectedIndex = position;
                mAdapter.mOnItemLongClickListener.onItemLongClick(v, position);
                if (mAdapter.mUpdateSelectedState) {
                    mAdapter.notifyDataSetChanged();
                }
            }
            return true;
        }
    }

    public interface OnItemClickListener {
        void onItemClick(View item, int position);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(View item, int position);
    }
}
