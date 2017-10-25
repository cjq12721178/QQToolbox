package com.cjq.tool.qbox.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.cjq.tool.qbox.R;

import java.util.ArrayList;

/**
 * Created by CJQ on 2017/10/24.
 */

public class SortDialog extends BaseDialog<SortDialog.Decorator> implements RadioGroup.OnCheckedChangeListener {

    public static final String TAG = "sort_dialog";
    private static final String ARGUMENT_KEY_SORT_TYPES = "in_sort_types";
    private static final String ARGUMENT_KEY_SELECTED_ID = "in_sort_selected_id";

    public SortDialog addSortType(@IdRes int id, @StringRes int labelRes, Context context) {
        return context != null ? addSortType(id, context.getString(labelRes)) : this;
    }

    public SortDialog addSortType(@IdRes int id, @NonNull String label) {
        if (id == 0 || TextUtils.isEmpty(label)) {
            return this;
        }
        ArrayList<SortType> sortTypes = getArguments().getParcelableArrayList(ARGUMENT_KEY_SORT_TYPES);
        if (sortTypes == null) {
            sortTypes = new ArrayList<>();
            getArguments().putParcelableArrayList(ARGUMENT_KEY_SORT_TYPES, sortTypes);
        }
        sortTypes.add(new SortType(id, label));
        return this;
    }

    public void clearSortType() {
        getArguments().putParcelableArrayList(ARGUMENT_KEY_SORT_TYPES, null);
    }

    public SortDialog setDefaultSelectedId(@IdRes int selectedId) {
        if (selectedId > 0) {
            setSelectedId(selectedId);
        }
        return this;
    }

    private void setSelectedId(Bundle arguments, @IdRes int selectedId) {
        arguments.putInt(ARGUMENT_KEY_SELECTED_ID, selectedId);
    }

    private void setSelectedId(@IdRes int selectedId) {
        setSelectedId(getArguments(), selectedId);
    }

    private @IdRes int getSelectedId() {
        return getSelectedId(getArguments());
    }

    private @IdRes int getSelectedId(Bundle arguments) {
        return arguments.getInt(ARGUMENT_KEY_SELECTED_ID, -1);
    }

    @Override
    protected void onSetContentView(View contentView, Decorator decorator, @Nullable Bundle savedInstanceState) {
        RadioGroup rgSort = (RadioGroup) contentView.findViewById(R.id.rg_sort);
        Bundle arguments = getArguments();
        ArrayList<SortType> sortTypes = arguments.getParcelableArrayList(ARGUMENT_KEY_SORT_TYPES);
        int selectedId = getSelectedId(arguments);
        for (SortType sortType :
                sortTypes) {
            RadioButton button = new RadioButton(getContext());
            button.setId(sortType.getId());
            if (selectedId == -1) {
                selectedId = sortType.getId();
                setSelectedId(arguments, selectedId);
            } else if (selectedId == sortType.getId()) {
                button.setChecked(true);
            }
            button.setText(sortType.getLabel());
            button.setButtonDrawable(null);
            button.setCompoundDrawables(null,
                    null,
                    ContextCompat.getDrawable(getContext(),
                            android.R.drawable.btn_radio),
                    null);
            button.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            rgSort.addView(button,
                    rgSort.getChildCount(),
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        rgSort.setOnCheckedChangeListener(this);
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        return super.show(transaction, tag, R.string.qbox_sort);
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag, R.string.qbox_sort);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        //getArguments().putInt(ARGUMENT_KEY_SELECTED_ID, checkedId);
        setSelectedId(checkedId);
    }

    @Override
    protected boolean onConfirm() {
        return onSortChanged(true);
    }

    @Override
    protected boolean onCancel() {
        return onSortChanged(false);
    }

    private boolean onSortChanged(boolean isAscend) {
        OnSortTypeChangedListener listener = getListener(OnSortTypeChangedListener.class);
        if (listener != null) {
            int selectedId = getSelectedId();
            if (selectedId != -1) {
                listener.onSortTypeChanged(selectedId, isAscend);
            }
        }
        return true;
    }

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int onSetContentLayout() {
            return R.layout.qbox_dialog_content_sort;
        }

        @Override
        public void reset() {
            super.reset();
            setOkLabel(R.string.qbox_ascend);
            setCancelLabel(R.string.qbox_descend);
        }
    }

    public interface OnSortTypeChangedListener {
        void onSortTypeChanged(@IdRes int checkedId, boolean isAscending);
    }

    public static class SortType implements Parcelable {

        private final int mId;
        private final String mLabel;

        public SortType(int id, String label) {
            mId = id;
            mLabel = label;
        }

        protected SortType(Parcel in) {
            mId = in.readInt();
            mLabel = in.readString();
        }

        public int getId() {
            return mId;
        }

        public String getLabel() {
            return mLabel;
        }

        public static final Creator<SortType> CREATOR = new Creator<SortType>() {
            @Override
            public SortType createFromParcel(Parcel in) {
                return new SortType(in);
            }

            @Override
            public SortType[] newArray(int size) {
                return new SortType[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(mId);
            dest.writeString(mLabel);
        }
    }
}
