package com.cjq.tool.qbox.ui.adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2017/10/18.
 * 用户设置的viewType范围：[0，size)，其中size为delegate的数目
 */

public abstract class AutoGeneratedAdapterDelegateManager<E> extends AdapterDelegateManager<E> {

    private List<AdapterDelegate<E>> mAdapterDelegates = new ArrayList<>();

    @Override
    public void addAdapterDelegate(AdapterDelegate<E> delegate) {
        if (delegate != null) {
            checkViewType(delegate, mAdapterDelegates.size());
            mAdapterDelegates.add(delegate);
        }
    }

    @Override
    protected AdapterDelegate<E> getAdapterDelegate(int viewType) {
        while (viewType >= mAdapterDelegates.size()) {
            mAdapterDelegates.add(null);
        }
        AdapterDelegate<E> delegate = mAdapterDelegates.get(viewType);
        if (delegate == null) {
            delegate = onCreateAdapterDelegate(viewType);
            checkViewType(delegate, viewType);
            mAdapterDelegates.set(viewType, delegate);
        }
        return delegate;
    }

    private void checkViewType(AdapterDelegate<E> delegate, int viewType) {
        if (delegate.getItemViewType() != viewType) {
            throw new IllegalViewTypeException("viewType expected to be " + viewType);
        }
    }

    protected abstract AdapterDelegate<E> onCreateAdapterDelegate(int viewType);

    public static class IllegalViewTypeException extends RuntimeException {

        private static final long serialVersionUID = -1814664554033931539L;

        public IllegalViewTypeException() {
            super();
        }

        public IllegalViewTypeException(String message) {
            super(message);
        }
    }
}