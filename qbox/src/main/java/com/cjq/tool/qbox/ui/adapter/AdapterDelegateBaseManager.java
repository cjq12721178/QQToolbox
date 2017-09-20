package com.cjq.tool.qbox.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by CJQ on 2017/9/20.
 */

public abstract class AdapterDelegateBaseManager<T> {

    public abstract AdapterDelegateBaseManager<T> addDelegate(@NonNull AdapterDelegate<T> delegate);

    public abstract int getItemViewType(T item);

    public abstract AdapterDelegate<T> getDelegate(int viewType);

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return getDelegate(viewType).onCreateViewHolder(parent);
    }

    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, T item) {
        getDelegate(holder.getItemViewType()).onBindViewHolder(holder, item);
    }
}
