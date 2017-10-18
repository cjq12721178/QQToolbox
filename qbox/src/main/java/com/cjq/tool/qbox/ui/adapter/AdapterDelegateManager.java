package com.cjq.tool.qbox.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by CJQ on 2017/10/17.
 */

public abstract class AdapterDelegateManager<E> {

    public abstract void addAdapterDelegate(AdapterDelegate<E> delegate);

    protected abstract AdapterDelegate<E> getAdapterDelegate(int viewType);

    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return getAdapterDelegate(viewType).onCreateViewHolder(parent);
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, E item, int position) {
        getAdapterDelegate(holder.getItemViewType())
                .onBindViewHolder(holder,
                        item,
                        position);
    }

    public void onBindViewHolder(RecyclerView.ViewHolder holder, E item, int position, List payloads) {
        getAdapterDelegate(holder.getItemViewType())
                .onBindViewHolder(holder,
                        item,
                        position,
                        payloads);
    }
}
