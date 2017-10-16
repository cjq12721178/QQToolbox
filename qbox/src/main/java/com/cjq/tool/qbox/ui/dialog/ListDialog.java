package com.cjq.tool.qbox.ui.dialog;

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

import com.cjq.tool.qbox.R;
import com.cjq.tool.qbox.ui.adapter.AdapterDelegate;
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter;
import com.cjq.tool.qbox.ui.decoration.SpaceItemDecoration;

import java.util.List;

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
        public void reset() {
            super.reset();
            setListId(R.id.rv_items);
            setItemVerticalInterval(R.dimen.qbox_list_item_interval_vertical);
        }

        @Override
        protected int onSetContentLayout() {
            return R.layout.qbox_dialog_content_list;
        }

        @IdRes
        public int getListId() {
            return mParameters.getInt("dp_list_id");
        }

        public void setListId(@IdRes int listId) {
            mParameters.putInt("dp_list_id", listId);
        }

        @DimenRes
        public int getItemVerticalInterval() {
            return mParameters.getInt("dp_item_view_vertical_interval");
        }

        public void setItemVerticalInterval(@DimenRes int intervalRes) {
            mParameters.putInt("dp_item_view_vertical_interval", intervalRes);
        }
    }

    @Override
    protected void onSetContentView(View contentView, Decorator decorator, @Nullable Bundle savedInstanceState) {
        setCancelable(false);
        setExitType(EXIT_TYPE_NULL);
        RecyclerView rvItems = (RecyclerView) contentView.findViewById(decorator.getListId());
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
        if (listener != null && mItemAdapter != null && mItemAdapter.getItems() != null) {
            listener.onItemSelected(this, mItemAdapter.getItems()[position]);
        }
        dismiss();
    }

    private static class ItemAdapter extends RecyclerViewBaseAdapter<String[], String> {

        private final ItemAdapterDelegate mDelegate = new ItemAdapterDelegate();

        public ItemAdapter(String[] items) {
            super(items);
        }

        @Override
        public void onAddAdapterDelegate() {
        }

        @Override
        public AdapterDelegate<String> getAdapterDelegate(int viewType) {
            return mDelegate;
        }

        @Override
        public String getItemByPosition(int position) {
            return mItems[position];
        }

        @Override
        public int getItemCount() {
            return mItems != null ? mItems.length : 0;
        }

        public String[] getItems() {
            return mItems;
        }
    }

    private static class ItemAdapterDelegate implements AdapterDelegate<String> {

        @Override
        public int getItemViewType() {
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
            return new ViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.qbox_list_dialog_item,
                            parent,
                            false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, String item, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.mTvItem.setText(item);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, String item, int position, List payloads) {

        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvItem;

        public ViewHolder(View itemView) {
            super(itemView);
            mTvItem = (TextView)itemView.findViewById(R.id.tv_item);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(ListDialog dialog, String item);
    }
}
