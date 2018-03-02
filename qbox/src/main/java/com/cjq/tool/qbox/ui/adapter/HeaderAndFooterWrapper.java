package com.cjq.tool.qbox.ui.adapter;

import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by CJQ on 2018/3/1.
 */

public class HeaderAndFooterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int BASE_HEADER_VIEW_TYPE = 100000;
    private static final int BASE_FOOTER_VIEW_TYPE = 200000;

    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFootViews = new SparseArrayCompat<>();

    private RecyclerView.Adapter mInnerAdapter;
    private InnerAdapterDataObserver mInnerAdapterDataObserver = new InnerAdapterDataObserver();

    public HeaderAndFooterWrapper(RecyclerView.Adapter adapter) {
        mInnerAdapter = adapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (isHeaderViewByViewType(viewType)) {
            return new ViewHolder(mHeaderViews.get(viewType));
        } else if (isFooterViewByViewType(viewType)) {
            return new ViewHolder(mFootViews.get(viewType));
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        if (isHeaderViewByPosition(position)) {
            return;
        }
        if (isFooterViewByPosition(position)) {
            return;
        }
        mInnerAdapter.onBindViewHolder(holder, position - getHeaderCount(), payloads);
    }

    @Override
    public int getItemViewType(int position) {
        if (isHeaderViewByPosition(position)) {
            return mHeaderViews.keyAt(position);
        } else if (isFooterViewByPosition(position)) {
            return mFootViews.keyAt(position - getHeaderCount() - getRealItemCount());
        }
        return mInnerAdapter.getItemViewType(position - getHeaderCount());
    }

    private int getRealItemCount() {
        return mInnerAdapter.getItemCount();
    }

    @Override
    public int getItemCount() {
        return getHeaderCount() + getFooterCount() + getRealItemCount();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mInnerAdapter.registerAdapterDataObserver(mInnerAdapterDataObserver);
        mInnerAdapter.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (isWrapperViewByPosition(position)) {
                        return gridLayoutManager.getSpanCount();
                    }
                    if (spanSizeLookup != null) {
                        return spanSizeLookup.getSpanSize(position);
                    }
                    return 1;
                }
            });
            gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
        }
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mInnerAdapter.unregisterAdapterDataObserver(mInnerAdapterDataObserver);
        mInnerAdapter.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        mInnerAdapter.onViewAttachedToWindow(holder);
        int position = holder.getLayoutPosition();
        if (isWrapperViewByPosition(position)) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
                StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                p.setFullSpan(true);
            }
        }
    }

    public boolean isWrapperViewByPosition(int position) {
        return isHeaderViewByPosition(position) || isFooterViewByPosition(position);
    }

    private boolean isHeaderViewByPosition(int position) {
        return position < getHeaderCount();
    }

    private boolean isFooterViewByPosition(int position) {
        return position >= getHeaderCount() + getRealItemCount();
    }

    private boolean isHeaderViewByViewType(int viewType) {
        int viewTypeDelta = viewType - BASE_HEADER_VIEW_TYPE;
        return viewTypeDelta >= 0 && viewTypeDelta < getHeaderCount();
    }

    private boolean isFooterViewByViewType(int viewType) {
        int viewTypeDelta = viewType - BASE_FOOTER_VIEW_TYPE;
        return viewTypeDelta >= 0 && viewTypeDelta < getFooterCount();
    }

    public void addHeaderView(View view) {
        mHeaderViews.put(getHeaderCount() + BASE_HEADER_VIEW_TYPE, view);
    }

    public void addFootView(View view) {
        mFootViews.put(getFooterCount() + BASE_FOOTER_VIEW_TYPE, view);
    }

    public int getHeaderCount() {
        return mHeaderViews.size();
    }

    public int getFooterCount() {
        return mFootViews.size();
    }

    public int getWrapperPosition(int innerPosition) {
        return innerPosition + getHeaderCount();
    }

    public int getInnerPosition(int wrapperPosition) {
        return wrapperPosition - getHeaderCount();
    }

    public RecyclerView.Adapter getInnerAdapter() {
        return mInnerAdapter;
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private class InnerAdapterDataObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            notifyDataSetChanged();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            notifyItemRangeChanged(getWrapperPosition(positionStart), itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            notifyItemRangeChanged(getWrapperPosition(positionStart), itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyItemRangeInserted(getWrapperPosition(positionStart), itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            notifyItemRangeRemoved(getWrapperPosition(positionStart), itemCount);
        }

        //RecyclerView源码中没有notifyItemRangeMoved方法，一脸懵逼。。
        //这解释我给满分："Moving more than 1 item is not supported yet"
        //其实我猜应该是如果要move range的话，需要存3个参数，而之前都只需2个参数。。
        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            notifyItemMoved(getWrapperPosition(fromPosition), getWrapperPosition(toPosition));
        }
    }
}
