package com.cjq.tool.qbox.ui.adapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2017/10/17.
 */

public abstract class ListAdapterDelegateManager<E> extends AdapterDelegateManager<E> {

    private List<AdapterDelegate<E>> mAdapterDelegates = new ArrayList<>();

    @Override
    public void addAdapterDelegate(AdapterDelegate<E> delegate) {
        if (delegate != null) {
            mAdapterDelegates.add(delegate);
        }
    }

    @Override
    protected AdapterDelegate<E> getAdapterDelegate(int viewType) {
        AdapterDelegate<E> delegate;
        for (int i = 0, n = mAdapterDelegates.size();i < n;++i) {
            delegate = mAdapterDelegates.get(i);
            if (delegate.getItemViewType() == viewType) {
                return delegate;
            }
        }
        throw new NullPointerException("can not find suitable adapter delegate");
    }
}
