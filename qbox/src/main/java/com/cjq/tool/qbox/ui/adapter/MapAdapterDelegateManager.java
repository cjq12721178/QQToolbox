package com.cjq.tool.qbox.ui.adapter;

import android.support.annotation.NonNull;
import android.util.SparseArray;

/**
 * Created by CJQ on 2018/6/11.
 */

public abstract class MapAdapterDelegateManager<E> extends AdapterDelegateManager<E> {

    private SparseArray<AdapterDelegate<E>> mDelegates = new SparseArray<>();

    @Override
    protected AdapterDelegate<E> getAdapterDelegate(int viewType) {
        AdapterDelegate<E> delegate = mDelegates.get(viewType);
        if (delegate == null) {
            delegate = onCreateAdapterDelegate(viewType);
            addAdapterDelegate(delegate);
        }
        return delegate;
    }

    protected abstract @NonNull AdapterDelegate<E> onCreateAdapterDelegate(int viewType);

    @Override
    public void addAdapterDelegate(@NonNull AdapterDelegate<E> delegate) {
        mDelegates.put(delegate.getItemViewType(), delegate);
    }

//    public void addAdapterDelegate(int viewType, @NonNull AdapterDelegate<E> delegate) {
//        if (delegate == null) {
//            throw new NullPointerException("adapter delete may not be null");
//        }
//        mDelegates.put(viewType, delegate);
//    }
}
