package com.cjq.tool.qbox.ui.adapter;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2017/9/20.
 * 建议用于type不多的情况
 */

public abstract class AdapterDelegateLineManager<T> extends AdapterDelegateBaseManager<T> {

    private final List<AdapterDelegate<T>> mDelegates = new ArrayList<>();

    @Override
    public AdapterDelegateBaseManager<T> addDelegate(@NonNull AdapterDelegate<T> delegate) {
        mDelegates.add(delegate);
        return this;
    }

    @Override
    public AdapterDelegate<T> getDelegate(int viewType) {
        AdapterDelegate<T> delegate;
        for (int i = 0, size = mDelegates.size();i < size;++i) {
            delegate = mDelegates.get(i);
            if (delegate.getItemViewType() == viewType) {
                return delegate;
            }
        }
        return null;
    }
}
