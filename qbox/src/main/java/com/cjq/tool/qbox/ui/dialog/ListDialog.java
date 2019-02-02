package com.cjq.tool.qbox.ui.dialog;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
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

import org.apache.commons.lang3.ArrayUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Created by KAT on 2017/4/11.
 */

public class ListDialog extends BaseDialog<ListDialog.Decorator> {

    private static final String ARGUMENT_KEY_ITEMS = "items";
    private static final String ARGUMENT_KEY_ITEM_DECORATOR = "item_decorator";
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
                    if (listener != null && mItemAdapter != null) {
                        listener.onItemSelected(ListDialog.this, position, getItems());
                    }
                    dismiss();
                }
            }
        });
        Bundle arguments = getArguments();
        //String[] tmpItems = arguments.getStringArray(ARGUMENT_KEY_ITEMS);
        mItemAdapter = new ItemAdapter(getItems(),
                (ItemDecorator) arguments.getSerializable(ARGUMENT_KEY_ITEM_DECORATOR),
                getResources().getDimensionPixelSize(decorator.getItemTextSizeDimenRes()),
                isMultipleSelect()
                        ? getResources().getDrawable(decorator.getSelectedBackgroundDrawableRes())
                        : null);
//        Bundle arguments = getArguments();
//        String[] tmpItems = arguments.getStringArray(ARGUMENT_KEY_ITEMS);
//        mItemAdapter = new ItemAdapter(tmpItems != null ? tmpItems : arguments.getParcelableArray(ARGUMENT_KEY_ITEMS),
//                arguments.getParcelable(ARGUMENT_KEY_ITEM_DECORATOR),
//                getResources().getDimensionPixelSize(decorator.getItemTextSizeDimenRes()),
//                isMultipleSelect()
//                        ? getResources().getDrawable(decorator.getSelectedBackgroundDrawableRes())
//                        : null);
        rvItems.setAdapter(mItemAdapter);
    }

//    @Override
//    protected View onPostCreateView(View baseView, Decorator decorator, @Nullable Bundle savedInstanceState) {
//        RecyclerView rvItems = baseView.findViewById(decorator.getListId());
//        //rvItems.setAdapter(null);
//        rvItems.setAdapter(mItemAdapter);
//        return baseView;
//    }



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
        listener.onItemsSelected(this, positions, getItems());
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
        //getArguments().putStringArray(ARGUMENT_KEY_ITEMS, items);
        //setItemDecorator(null);
        setItems(items, null);
    }

    public <I> void setItems(@NonNull I[] items, ItemDecorator<I> decorator) {
        //getArguments().putStringArray(ARGUMENT_KEY_ITEMS, items);

        //getArguments().putInt(ARGUMENT_KEY_ITEMS, items);
        //setItemDecorator(decorator);

        Bundle arguments = getArguments();
        if (items instanceof String[]) {
            arguments.putStringArray(ARGUMENT_KEY_ITEMS, (String[]) items);
        } else if (items instanceof Parcelable[]) {
            arguments.putParcelableArray(ARGUMENT_KEY_ITEMS, (Parcelable[]) items);
        } else if (items instanceof Integer[]) {
            arguments.putIntArray(ARGUMENT_KEY_ITEMS, ArrayUtils.toPrimitive((Integer[]) items));
        } else if (items instanceof Long[]) {
            arguments.putLongArray(ARGUMENT_KEY_ITEMS, ArrayUtils.toPrimitive((Long[]) items));
        } else if (items instanceof Float[]) {
            arguments.putFloatArray(ARGUMENT_KEY_ITEMS, ArrayUtils.toPrimitive((Float[]) items));
        } else if (items instanceof Double[]) {
            arguments.putDoubleArray(ARGUMENT_KEY_ITEMS, ArrayUtils.toPrimitive((Double[]) items));
        } else if (items instanceof Short[]) {
            arguments.putShortArray(ARGUMENT_KEY_ITEMS, ArrayUtils.toPrimitive((Short[]) items));
        } else if (items instanceof Character[]) {
            arguments.putCharArray(ARGUMENT_KEY_ITEMS, ArrayUtils.toPrimitive((Character[]) items));
        } else if (items instanceof CharSequence[]) {
            arguments.putCharSequenceArray(ARGUMENT_KEY_ITEMS, (CharSequence[]) items);
        } else if (items instanceof Byte[]) {
            arguments.putByteArray(ARGUMENT_KEY_ITEMS, ArrayUtils.toPrimitive((Byte[]) items));
        } else if (items instanceof Boolean[]) {
            arguments.putBooleanArray(ARGUMENT_KEY_ITEMS, ArrayUtils.toPrimitive((Boolean[]) items));
        } else {
            throw new IllegalArgumentException("invalid items");
        }
        arguments.putSerializable(ARGUMENT_KEY_ITEM_DECORATOR, decorator != null ? decorator : new DefaultItemDecorator());
    }

//    private <I extends Parcelable> void setItemDecorator(ItemDecorator<I> decorator) {
//        getArguments().putParcelable(ARGUMENT_KEY_ITEM_DECORATOR, decorator != null ? decorator : new DefaultItemDecorator());
//    }

    private Object[] getItems() {
//        String[] items = getArguments().getStringArray(ARGUMENT_KEY_ITEMS);
//        if (items != null) {
//            return items;
//        }
//        return getArguments().getParcelableArray(ARGUMENT_KEY_ITEMS);
        Object items = getArguments().get(ARGUMENT_KEY_ITEMS);
        return items != null ? (Object[]) items : null;
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

    public interface ItemDecorator<I> extends Serializable {
        String decorate(I item);
    }

    private static class DefaultItemDecorator implements ItemDecorator<Object> {

        private static final long serialVersionUID = 4081285457564809055L;

        public DefaultItemDecorator() {
        }

        @Override
        public String decorate(Object item) {
            return item.toString();
        }
    }

    private static class ItemAdapter<I> extends RecyclerViewBaseAdapter<I> {

        private final I[] mItems;
        private final ItemDecorator<I> mItemDecorator;
        private final float mItemTextSize;
        private final Drawable mSelectedBackground;
        private final boolean[] mSelections;

        public ItemAdapter(@NonNull I[] items, ItemDecorator<I> decorator, float itemTextSize, Drawable selectedBackground) {
            //super();
            mItems = items;
            mItemDecorator = decorator;
            mItemTextSize = itemTextSize;
            mSelectedBackground = selectedBackground;
            mSelections = selectedBackground != null
                    ? new boolean[items.length]
                    : null;
        }

        @Override
        public I getItemByPosition(int position) {
            return mItems[position];
        }

        @Override
        public int getItemCount() {
            return mItems != null ? mItems.length : 0;
        }

//        public String[] getItems() {
//            return mItems;
//        }

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
        public void onBindViewHolder(RecyclerView.ViewHolder holder, I item, int position) {
            ViewHolder viewHolder = (ViewHolder) holder;
            viewHolder.mTvItem.setText(mItemDecorator.decorate(item));
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
        void onItemSelected(@NonNull ListDialog dialog, int position, @NonNull Object[] items);
    }

    public interface OnMultipleItemSelectedListener {
        void onItemsSelected(@NonNull ListDialog dialog, @NonNull int[] positions, @NonNull Object[] items);
    }
}
