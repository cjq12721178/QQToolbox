package com.cjq.tool.qbox.ui.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by CJQ on 2017/9/20.
 */

public interface AdapterDelegate<E> {
    int getItemViewType();
    RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent);
    void onBindViewHolder(RecyclerView.ViewHolder holder, E item, int position);
    void onBindViewHolder(RecyclerView.ViewHolder holder, E item, int position, List payloads);
}
