package com.cjq.tool.qbox.ui.dialog;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by KAT on 2017/4/11.
 */

public class ListDialog extends BaseDialog<ListDialog.Decorator> {

    private static final String ARGUMENT_KEY_ITEMS = "items";
    private static final String ARGUMENT_KEY_MULTIPLE_SELECT = "mul_select";
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

        final public @DrawableRes int getSelectedBackgroundDrawableRes() {
            return mParameters.getInt("dp_item_select_bg", getDefaultSelectedBackgroundDrawableRes());
        }

        public @DrawableRes int getDefaultSelectedBackgroundDrawableRes() {
            return R.color.qbox_background_button_pressed;
        }

        final public void setSelectedBackgroundDrawableRes(@DrawableRes int drawableRes) {
            mParameters.putInt("dp_item_select_bg", drawableRes);
        }

        @Override
        public float getDefaultContentGroupMaxHeightRatioToScreenHeight() {
            return 1 / 2f;
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
                if (isMultipleSelect()) {
                    mItemAdapter.mSelections[position] = !mItemAdapter.mSelections[position];
                    mItemAdapter.notifyItemChanged(position);
                } else {
                    OnItemSelectedListener listener = getListener(OnItemSelectedListener.class);
                    if (listener != null && mItemAdapter != null && mItemAdapter.getItems() != null) {
                        listener.onItemSelected(ListDialog.this, position);
                    }
                    dismiss();
                }
            }
        });
        mItemAdapter = new ItemAdapter(getArguments().getStringArray(ARGUMENT_KEY_ITEMS),
                getResources().getDimensionPixelSize(decorator.getItemTextSizeDimenRes()),
                isMultipleSelect()
                        ? getResources().getDrawable(decorator.getSelectedBackgroundDrawableRes())
                        : null);
        //mItemAdapter.setOnItemClickListener(this);
        rvItems.setAdapter(mItemAdapter);
    }

    @Override
    protected boolean onConfirm() {
        if (!isMultipleSelect()) {
            return true;
        }
        OnMultipleItemSelectedListener listener = getListener(OnMultipleItemSelectedListener.class);
        int[] positions = new int[mItemAdapter.getSelectedItemCount()];
        for (int i = 0, j = 0, size = mItemAdapter.mSelections.length;i < size;++i) {
            if (mItemAdapter.mSelections[i]) {
                positions[j++] = i;
            }
        }
        listener.onItemsSelected(this, positions);
        return true;
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

    public void setMultipleSelect(boolean enabled) {
        getArguments().putBoolean(ARGUMENT_KEY_MULTIPLE_SELECT, enabled);
        if (enabled) {
            super.setExitType(EXIT_TYPE_OK_CANCEL);
        } else {
            super.setExitType(EXIT_TYPE_NULL);
        }
    }

    public boolean isMultipleSelect() {
        return getArguments().getBoolean(ARGUMENT_KEY_MULTIPLE_SELECT, false);
    }

    private static class ItemAdapter extends RecyclerViewBaseAdapter<String> {

        private String[] mItems;
        private final float mItemTextSize;
        private final Drawable mSelectedBackground;
        private final boolean[] mSelections;

        public ItemAdapter(String[] items, float itemTextSize, Drawable selectedBackground) {
            //super();
            mItems = items;
            mItemTextSize = itemTextSize;
            mSelectedBackground = selectedBackground;
            mSelections = selectedBackground != null
                    ? new boolean[items.length]
                    : null;
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

        public int getSelectedItemCount() {
            if (mSelections == null) {
                return 0;
            }
            int count = 0;
            for (int i = 0, size = mSelections.length;i < size;++i) {
                if (mSelections[i]) {
                    ++count;
                }
            }
            return count;
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
            if (mSelections != null) {
                viewHolder.mTvItem.setBackground(mSelections[position] ? mSelectedBackground : null);
            }
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
        void onItemSelected(@NonNull ListDialog dialog, int position);
    }

    public interface OnMultipleItemSelectedListener {
        void onItemsSelected(@NonNull ListDialog dialog, @NonNull int[] positions);
    }
}
