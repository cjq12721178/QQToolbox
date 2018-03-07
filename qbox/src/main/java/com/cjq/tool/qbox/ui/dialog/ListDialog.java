package com.cjq.tool.qbox.ui.dialog;

import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cjq.tool.qbox.R;
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter;
import com.cjq.tool.qbox.ui.decoration.SpaceItemDecoration;
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener;

/**
 * Created by KAT on 2017/4/11.
 */

public class ListDialog extends BaseDialog<ListDialog.Decorator> {

    private static final String ARGUMENT_KEY_ITEMS = "items";
    private ItemAdapter mItemAdapter;

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int getDefaultContentLayoutRes() {
            return R.layout.qbox_dialog_content_list;
        }

        final public @IdRes int getListId() {
            return mParameters.getInt("dp_list_id", getDefaultListId());
        }

        public @IdRes int getDefaultListId() {
            return R.id.rv_items;
        }

        final public void setListId(@IdRes int listId) {
            mParameters.putInt("dp_list_id", listId);
        }

        final public @DimenRes int getItemVerticalIntervalDimenRes() {
            return mParameters.getInt("dp_item_view_vertical_interval", getDefaultItemVerticalIntervalDimenRes());
        }

        public @DimenRes int getDefaultItemVerticalIntervalDimenRes() {
            return R.dimen.qbox_list_item_interval_vertical;
        }

        final public void setItemVerticalInterval(@DimenRes int intervalRes) {
            mParameters.putInt("dp_item_view_vertical_interval", intervalRes);
        }

        final public @DimenRes int getItemTextSizeDimenRes() {
            return mParameters.getInt("dp_item_text_size", getDefaultItemTextSizeDimenRes());
        }

        public @DimenRes int getDefaultItemTextSizeDimenRes() {
            return R.dimen.qbox_dialog_list_item_text_size;
        }

        final public void setItemTextSizeDimenRes(@DimenRes int dimenRes) {
            mParameters.putInt("dp_item_text_size", dimenRes);
        }
    }

    @Override
    protected void onSetContentView(View contentView, Decorator decorator, @Nullable Bundle savedInstanceState) {
        //setCancelable(false);
        RecyclerView rvItems = contentView.findViewById(decorator.getListId());
        int itemIntervalDimenRes = decorator.getItemVerticalIntervalDimenRes();
        if (itemIntervalDimenRes != 0) {
            rvItems.addItemDecoration(new SpaceItemDecoration(getResources().
                    getDimensionPixelSize(itemIntervalDimenRes), true));
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rvItems.setLayoutManager(linearLayoutManager);
        rvItems.addOnItemTouchListener(new SimpleRecyclerViewItemTouchListener(rvItems) {
            @Override
            public void onItemClick(View v, int position) {
                OnItemSelectedListener listener = getListener(OnItemSelectedListener.class);
                if (listener != null && mItemAdapter != null && mItemAdapter.getItems() != null) {
                    listener.onItemSelected(ListDialog.this, mItemAdapter.getItems()[position]);
                }
                dismiss();
            }
        });
        mItemAdapter = new ItemAdapter(getArguments().getStringArray(ARGUMENT_KEY_ITEMS),
                getResources().getDimensionPixelSize(decorator.getItemTextSizeDimenRes()));
        //mItemAdapter.setOnItemClickListener(this);
        rvItems.setAdapter(mItemAdapter);
    }

    @Override
    protected int getDefaultExitType() {
        return EXIT_TYPE_NULL;
    }

    @Override
    public void setExitType(int type) {
    }

    public void setItems(String[] items) {
        getArguments().putStringArray(ARGUMENT_KEY_ITEMS, items);
    }

    private static class ItemAdapter extends RecyclerViewBaseAdapter<String> {

        private String[] mItems;
        private final float mItemTextSize;

        public ItemAdapter(String[] items, float itemTextSize) {
            //super();
            mItems = items;
            mItemTextSize = itemTextSize;
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
                            false),
                    mItemTextSize);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, String item, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.mTvItem.setText(item);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvItem;

        public ViewHolder(View itemView, float textSize) {
            super(itemView);
            mTvItem = (TextView)itemView.findViewById(R.id.tv_item);
            mTvItem.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        }
    }

    public interface OnItemSelectedListener {
        void onItemSelected(ListDialog dialog, String item);
    }
}
