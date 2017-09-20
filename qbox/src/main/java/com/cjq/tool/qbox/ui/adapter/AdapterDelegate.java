package com.cjq.tool.qbox.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

/**
 * Created by CJQ on 2017/9/20.
 */

public interface AdapterDelegate<T> {
    int getItemViewType();
    RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent);
    void onBindViewHolder(RecyclerView.ViewHolder holder, T item);
}
