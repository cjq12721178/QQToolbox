package com.cjq.test.qbox.ui.dialog;

import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cjq.test.qbox.R;
import com.cjq.test.qbox.ui.adapter.RecyclerViewBaseAdapter;
import com.cjq.test.qbox.ui.decoration.SpaceItemDecoration;

/**
 * Created by KAT on 2017/4/11.
 */

public class ListDialog
        extends BaseDialog<ListDialog.Decorator>
        implements RecyclerViewBaseAdapter.OnItemClickListener {

    private static final String ARGUMENT_KEY_ITEMS = "items";
    private ItemAdapter mItemAdapter;

    public static class Decorator extends BaseDialog.Decorator {
        @Override
        public int getContentLayout() {
            return R.layout.dialog_content_list;
        }

        @IdRes
        public int getListId() {
            return R.id.rv_items;
        }

        @DimenRes
        public int getItemVerticalInterval() {
            return R.dimen.list_item_interval_vertical;
        }
    }

    @Override
    protected void onSetContentView(View content, Decorator decorator, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        setExitType(EXIT_TYPE_NULL);
        RecyclerView rvItems = (RecyclerView)content.findViewById(decorator.getListId());
        int itemIntervalDimenRes = decorator.getItemVerticalInterval();
        if (itemIntervalDimenRes != 0) {
            rvItems.addItemDecoration(new SpaceItemDecoration(getResources().
                    getDimensionPixelSize(itemIntervalDimenRes), true));
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvItems.setLayoutManager(linearLayoutManager);
        mItemAdapter = new ItemAdapter(getArguments().getStringArray(ARGUMENT_KEY_ITEMS));
        mItemAdapter.setOnItemClickListener(this);
        rvItems.setAdapter(mItemAdapter);
    }

    public void setItems(String[] items) {
        getArguments().putStringArray(ARGUMENT_KEY_ITEMS, items);
    }

    public int show(FragmentTransaction transaction, String tag, String title, String[] items) {
        setItems(items);
        return super.show(transaction, tag, title);
    }

    public void show(FragmentManager manager, String tag, String title, String[] items) {
        setItems(items);
        super.show(manager, tag, title);
    }

    public int show(FragmentTransaction transaction, String tag, @StringRes int titleRes, String[] items) {
        setItems(items);
        return super.show(transaction, tag, titleRes);
    }

    public void show(FragmentManager manager, String tag, @StringRes int titleRes, String[] items) {
        setItems(items);
        super.show(manager, tag, titleRes);
    }

    @Override
    public void onItemClick(View item, int position) {
        OnItemSelectedListener listener = getListener(OnItemSelectedListener.class);
        if (listener != null && mItemAdapter != null && mItemAdapter.mItems != null) {
            listener.onItemSelected(this, mItemAdapter.mItems[position]);
        }
        dismiss();
    }

    private static class ItemAdapter extends RecyclerViewBaseAdapter<ItemAdapter.ViewHolder> {

        private String[] mItems;

        public ItemAdapter(String[] items) {
            mItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_dialog_item, parent, false), this);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            super.onBindViewHolder(holder, position);
            holder.mTvItem.setText(mItems[position]);
        }

        @Override
        public int getItemCount() {
            return mItems != null ? mItems.length : 0;
        }

        public static class ViewHolder extends RecyclerViewBaseAdapter.ViewHolder {

            private TextView mTvItem;

            public ViewHolder(View itemView, ItemAdapter itemAdapter) {
                super(itemView, itemAdapter);
                mTvItem = (TextView)itemView.findViewById(R.id.tv_item);
            }
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(ListDialog dialog, String item);
    }
}
