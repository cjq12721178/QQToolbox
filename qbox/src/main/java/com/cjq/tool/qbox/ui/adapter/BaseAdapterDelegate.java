package com.cjq.tool.qbox.ui.adapter;

/**
 * Created by CJQ on 2017/10/18.
 */

public abstract class BaseAdapterDelegate<E> implements AdapterDelegate<E> {

    private final int mViewType;

    public BaseAdapterDelegate(int viewType) {
        mViewType = viewType;
    }

    @Override
    public int getItemViewType() {
        return mViewType;
    }
}
