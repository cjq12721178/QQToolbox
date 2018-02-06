package com.cjq.tool.qbox.ui.dialog;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewStub;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.cjq.tool.qbox.R;
import com.cjq.tool.qbox.ui.adapter.AdapterDelegate;
import com.cjq.tool.qbox.ui.adapter.AdapterDelegateManager;
import com.cjq.tool.qbox.ui.adapter.ListAdapterDelegateManager;
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by CJQ on 2018/2/2.
 */

public class SearchDialog extends BaseEditDialog<SearchDialog.Decorator> implements View.OnTouchListener, TextView.OnEditorActionListener, View.OnFocusChangeListener {

    private List<String> mSearchRecords;
    private SearchRecordAdapter mSearchRecordAdapter;

    private TextWatcher mSearchContentWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (mSearchRecordAdapter != null) {
                if (before == 0) {
                    if (count > 0) {
                        int previousSize = mSearchRecordAdapter.getItemCount();
                        mSearchRecordAdapter.setCorrespondSearchRecords(mSearchRecordAdapter.getCorrespondSearchRecords(), s.toString());
                        mSearchRecordAdapter.notifyDataSetChanged(previousSize);
                    }
                } else {
                    int previousSize = mSearchRecordAdapter.getItemCount();
                    mSearchRecordAdapter.setCorrespondSearchRecords(mSearchRecords, s.toString());
                    mSearchRecordAdapter.notifyDataSetChanged(previousSize);
                }
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setStyle(STYLE_NO_FRAME, android.R.style.Theme_Holo_Light);
        }
    }

    @Override
    protected void onSetContentView(View contentView, Decorator decorator, @Nullable Bundle savedInstanceState) {
        super.onSetContentView(contentView, decorator, savedInstanceState);
        EditText editText = getEditText();
        if (decorator.isSearchIconAsDrawableOfEditText()) {
            int drawableRes = decorator.getSearchIconRes();
            if (drawableRes != 0) {
                Drawable icon = ContextCompat.getDrawable(getContext(), drawableRes);
                setSearchIconBounds(icon);
                editText.setCompoundDrawables(null, null, icon, null);
                editText.setOnTouchListener(this);
            }
        } else {
            int searchIconId = decorator.getCustomSearchViewId();
            if (searchIconId != 0) {
                contentView.findViewById(searchIconId).setOnClickListener(this);
            }
        }
        editText.setOnEditorActionListener(this);
        editText.addTextChangedListener(mSearchContentWatcher);
        editText.setOnFocusChangeListener(this);

        //罗列历史搜索记录
        mSearchRecords = importSearchRecords();
        if (mSearchRecords != null) {
            ViewStub vsRecords = contentView.findViewById(R.id.vs_search_record);
            RecyclerView rvRecords = (RecyclerView) vsRecords.inflate();
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            rvRecords.setLayoutManager(linearLayoutManager);
            ListAdapterDelegateManager<String> listAdapterDelegateManager = new ListAdapterDelegateManager<>();
            listAdapterDelegateManager.addAdapterDelegate(new CommonSearchRecordAdapterDelegate());
            listAdapterDelegateManager.addAdapterDelegate(new ClearSearchRecordAdapterDelegate());
            mSearchRecordAdapter = new SearchRecordAdapter(getContext(), listAdapterDelegateManager);
            mSearchRecordAdapter.setCorrespondSearchRecords(mSearchRecords, editText.getText().toString());
            rvRecords.setAdapter(mSearchRecordAdapter);
        }
    }

    private void setSearchIconBounds(Drawable icon) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        EditText et = getEditText();
        et.measure(w, h);
        int bounds = et.getMeasuredHeight();
        icon.setBounds(0, 0, bounds, bounds);
    }

    private List<String> importSearchRecords() {
        SharedPreferences preferences = getContext().getSharedPreferences("qbox_search_record", Context.MODE_PRIVATE);
        Set<String> recordSet = preferences.getStringSet("records", null);
        List<String> recordList;
        if (recordSet != null && !recordSet.isEmpty()) {
            recordList = new ArrayList<>(recordSet);
            Collections.sort(recordList);
        } else {
            recordList = null;
        }
        return recordList;
    }

    private void exportSearchRecords() {
        getContext()
                .getSharedPreferences("qbox_search_record", Context.MODE_PRIVATE)
                .edit()
                .putStringSet("records", mSearchRecords != null ? new HashSet<>(mSearchRecords) : null)
                .apply();
    }

    private void onSearch() {
        String searchContent = getEditText().getText().toString();
        if (!TextUtils.isEmpty(searchContent)) {
            if (mSearchRecords == null) {
                mSearchRecords = new ArrayList<>(1);
                mSearchRecords.add(searchContent);
                exportSearchRecords();
            } else if (!mSearchRecords.contains(searchContent)) {
                mSearchRecords.add(searchContent);
                exportSearchRecords();
            }
        }
        OnSearchListener listener = getListener(OnSearchListener.class);
        if (listener != null) {
            listener.onSearch(searchContent);
        }
        dismiss();
    }

    @Override
    protected int getDefaultExitType() {
        return EXIT_TYPE_NULL;
    }

    @Override
    public boolean isDefaultDrawTitle() {
        return false;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        Decorator decorator = getDecorator(null);
        @IdRes int id = v.getId();
        if (id == decorator.getCustomSearchViewId()) {
            onSearch();
        } else if (id == decorator.getRecordItemContentViewId()) {
            int position = getAdapterPosition(v);
            if (position != -1) {
                getEditText().setText(mSearchRecordAdapter.getItemByPosition(position));
                onSearch();
            }
        } else if (id == decorator.getRecordItemRemoveViewId()) {
            int position = getAdapterPosition(v);
            if (position != -1) {
                String selectedSearchRecord = mSearchRecordAdapter.getItemByPosition(position);
                mSearchRecordAdapter.removeRecord(position);
                mSearchRecords.remove(selectedSearchRecord);
                exportSearchRecords();
            }
        } else if (id == decorator.getClearItemViewId()) {
            int position = getAdapterPosition(v);
            if (position != -1) {
                mSearchRecordAdapter.clearRecords();
                mSearchRecords.clear();
                exportSearchRecords();
            }
        }
    }

    private int getAdapterPosition(View v) {
        ViewParent parent = v.getParent();
        if (parent instanceof View) {
            return RecyclerViewBaseAdapter.getPositionByItemView((View) parent);
        } else {
            return -1;
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        // editText.getCompoundDrawables()得到一个长度为4的数组，分别表示左右上下四张图片
        EditText editText = (EditText) v;
        Drawable drawable = editText.getCompoundDrawables()[2];
        //如果右边没有图片，不再处理
        if (drawable == null)
            return false;
        //如果不是按下事件，不再处理
        if (event.getAction() != MotionEvent.ACTION_UP)
            return false;
        if (event.getX() > editText.getWidth()
                - editText.getPaddingRight()
                - drawable.getIntrinsicWidth()){
            onSearch();
            return true;
//            String newSearchText = editText.getText().toString();
//            if (onSearchListener != null) {
//                onSearchListener.onSearch(!newSearchText.equals(searchText), newSearchText.split(" "));
//            }
//            searchText = newSearchText;
        }
        return false;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            onSearch();
            return true;
        }
        return false;
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            getEditText().selectAll();
        }
    }

    public interface OnSearchListener {
        void onSearch(String target);
    }

    public static class Decorator extends BaseEditDialog.Decorator {

        @Override
        protected int getDefaultContentLayoutRes() {
            return R.layout.qbox_dialog_content_search;
        }

        @Override
        public int getDefaultEditId() {
            return R.id.et_search_content;
        }

        final public boolean isSearchIconAsDrawableOfEditText() {
            return mParameters.getBoolean("dp_search_icon_drawable", isDefaultSearchIconAsDrawableOfEditText());
        }

        public boolean isDefaultSearchIconAsDrawableOfEditText() {
            return true;
        }

        final public void setSearchIconAsDrawableOfEditText(boolean isDrawable) {
            mParameters.putBoolean("dp_search_icon_drawable", isDrawable);
        }

        final public @IdRes int getCustomSearchViewId() {
            return mParameters.getInt("dp_search_id", getDefaultCustomSearchViewId());
        }

        public @IdRes int getDefaultCustomSearchViewId() {
            return 0;
        }

        final public void setCustomSearchViewId(@IdRes int id) {
            mParameters.putInt("dp_search_id", id);
        }

        final public @DrawableRes int getSearchIconRes() {
            return mParameters.getInt("dp_search_icon", getDefaultSearchIconRes());
        }

        public @DrawableRes int getDefaultSearchIconRes() {
            return R.drawable.qbox_ic_search;
        }

        final public void setSearchIcon(@DrawableRes int iconRes) {
            mParameters.putInt("dp_search_icon", iconRes);
        }

        final public @LayoutRes int getRecordItemViewLayoutRes() {
            return mParameters.getInt("dp_record_layout", getDefaultRecordItemViewLayoutRes());
        }

        public @LayoutRes int getDefaultRecordItemViewLayoutRes() {
            return R.layout.qbox_dialog_appendix_search_record;
        }

        final public void setRecordItemViewLayout(@LayoutRes int layoutRes) {
            mParameters.putInt("dp_record_layout", layoutRes);
        }

        final public @IdRes int getRecordItemContentViewId() {
            return mParameters.getInt("dp_record_id", getDefaultRecordItemContentViewId());
        }

        public @IdRes int getDefaultRecordItemContentViewId() {
            return R.id.tv_record;
        }

        final public void setRecordItemContentViewId(@IdRes int id) {
            mParameters.putInt("dp_record_id", id);
        }

        final public @IdRes int getRecordItemRemoveViewId() {
            return mParameters.getInt("dp_remove_id", getDefaultRecordItemRemoveViewId());
        }

        public @IdRes int getDefaultRecordItemRemoveViewId() {
            return R.id.ib_remove;
        }

        final public void setRecordItemRemoveViewId(@IdRes int id) {
            mParameters.putInt("dp_remove_id", id);
        }

        final public @LayoutRes int getClearItemViewLayoutRes() {
            return mParameters.getInt("dp_clear_layout", getDefaultClearItemViewLayoutRes());
        }

        public @LayoutRes int getDefaultClearItemViewLayoutRes() {
            return R.layout.qbox_dialog_appendix_search_clear;
        }

        final public void setClearItemViewLayout(@LayoutRes int layoutRes) {
            mParameters.putInt("dp_clear_layout", layoutRes);
        }

        final public @IdRes int getClearItemViewId() {
            return mParameters.getInt("dp_clear_id", getDefaultClearItemViewId());
        }

        public @IdRes int getDefaultClearItemViewId() {
            return R.id.tv_clear;
        }

        final public void setClearItemViewId(@IdRes int id) {
            mParameters.putInt("dp_clear_id", id);
        }
    }

    private static class SearchRecordAdapter extends RecyclerViewBaseAdapter<String> {

        private final String mClearRecordsLabel;
        private List<String> mCorrespondSearchRecords;

        public SearchRecordAdapter(Context context, AdapterDelegateManager<String> manager) {
            super(manager);
            mClearRecordsLabel = context.getString(R.string.qbox_clear_search_records);
        }

        @Override
        public String getItemByPosition(int position) {
            return position != mCorrespondSearchRecords.size()
                    ? mCorrespondSearchRecords.get(position)
                    : mClearRecordsLabel;
        }

        @Override
        public int getItemCount() {
            return mCorrespondSearchRecords.isEmpty() ? 0 : mCorrespondSearchRecords.size() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            return position != mCorrespondSearchRecords.size()
                    ? CommonSearchRecordAdapterDelegate.VIEW_TYPE
                    : ClearSearchRecordAdapterDelegate.VIEW_TYPE;
        }

        public List<String> getCorrespondSearchRecords() {
            return mCorrespondSearchRecords;
        }

        public void setCorrespondSearchRecords(List<String> records, String currentSearchContent) {
            mCorrespondSearchRecords = filtrateRecords(records, currentSearchContent);
        }

        private List<String> filtrateRecords(List<String> records, String prefix) {
            List<String> result = new ArrayList<>();
            if (records != null) {
                for (String record : records) {
                    if (record.startsWith(prefix)) {
                        result.add(record);
                    }
                }
            }
            return result;
        }

        public void removeRecord(int position) {
            if (position >= 0 && position < mCorrespondSearchRecords.size()) {
                mCorrespondSearchRecords.remove(position);
                if (mCorrespondSearchRecords.isEmpty()) {
                    notifyItemRangeRemoved(0, 2);
                } else {
                    notifyItemRemoved(position);
                }
            }
        }

        public void clearRecords() {
            int previousSize = getItemCount();
            mCorrespondSearchRecords.clear();
            notifyItemRangeRemoved(0, previousSize);
        }
    }

    private class CommonSearchRecordAdapterDelegate implements AdapterDelegate<String> {

        public static final int VIEW_TYPE = 1;
//        private final Decorator mDecorator;
//        private final View.OnClickListener mOnClickListener;

//        public CommonSearchRecordAdapterDelegate(Decorator decorator, View.OnClickListener onClickListener) {
//            mDecorator = decorator;
//            mOnClickListener = onClickListener;
//        }

        @Override
        public int getItemViewType() {
            return VIEW_TYPE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
            Decorator decorator = getDecorator(null);
            return new CommonViewHolder(
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(decorator.getRecordItemViewLayoutRes(),
                                    parent,
                                    false),
                    decorator,
                    SearchDialog.this);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, String item, int position) {
            CommonViewHolder viewHolder = (CommonViewHolder) holder;
            viewHolder.mTvRecord.setText(item);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, String item, int position, List payloads) {

        }
    }

    public static class CommonViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvRecord;
        //private ImageButton mIbRemove;

        public CommonViewHolder(View itemView, Decorator decorator, View.OnClickListener listener) {
            super(itemView);
            mTvRecord = itemView.findViewById(decorator.getRecordItemContentViewId());
            mTvRecord.setOnClickListener(listener);
            itemView.findViewById(decorator.getRecordItemRemoveViewId())
                    .setOnClickListener(listener);
        }
    }

    private class ClearSearchRecordAdapterDelegate implements AdapterDelegate<String> {

        public static final int VIEW_TYPE = 2;

        @Override
        public int getItemViewType() {
            return VIEW_TYPE;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
            Decorator decorator = getDecorator(null);
            return new ClearViewHolder(
                    LayoutInflater
                            .from(parent.getContext())
                            .inflate(decorator.getClearItemViewLayoutRes(),
                                    parent,
                                    false),
                    decorator,
                    SearchDialog.this);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, String item, int position) {
            ClearViewHolder viewHolder = (ClearViewHolder) holder;
            viewHolder.mTvClear.setText(item);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, String item, int position, List payloads) {

        }
    }

    public static class ClearViewHolder extends RecyclerView.ViewHolder {

        private TextView mTvClear;

        public ClearViewHolder(View itemView, Decorator decorator, View.OnClickListener listener) {
            super(itemView);
            mTvClear = itemView.findViewById(decorator.getClearItemViewId());
            mTvClear.setOnClickListener(listener);
        }
    }
}
