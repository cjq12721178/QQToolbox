package com.cjq.tool.qbox.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2017/10/18.
 * 用户无需设置viewType，但应注意，viewType的范围：[0，size)，其中size为delegate的数目
 */

public class ArrayAdapterDelegateManager<E> extends AdapterDelegateManager<E> {

    private List<AdapterDelegateDecorator<E>> mDelegateDecorators = new ArrayList<>();

    @Override
    public void addAdapterDelegate(AdapterDelegate<E> delegate) {
        if (delegate != null) {
            mDelegateDecorators.add(new AdapterDelegateDecorator(mDelegateDecorators.size(), delegate));
        }
    }

    @Override
    protected AdapterDelegate<E> getAdapterDelegate(int viewType) {
        return mDelegateDecorators.get(viewType);
    }

    private static class AdapterDelegateDecorator<E> implements AdapterDelegate<E> {

        private final int mViewType;
        private final AdapterDelegate<E> mRealDelegate;

        private AdapterDelegateDecorator(int viewType, AdapterDelegate<E> realDelegate) {
            mViewType = viewType;
            mRealDelegate = realDelegate;
        }

        @Override
        public int getItemViewType() {
            return mViewType;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
            return mRealDelegate.onCreateViewHolder(parent);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, E item, int position) {
            mRealDelegate.onBindViewHolder(holder, item, position);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, E item, int position, List payloads) {
            mRealDelegate.onBindViewHolder(holder, item, position, payloads);
        }
    }
}
