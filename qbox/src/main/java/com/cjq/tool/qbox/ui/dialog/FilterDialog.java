package com.cjq.tool.qbox.ui.dialog;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.cjq.tool.qbox.R;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by CJQ on 2017/11/2.
 */

public class FilterDialog extends BaseDialog<FilterDialog.Decorator> {

    //private static final String ARGUMENT_KEY_CHECK_STATES = "in_filter_states";
    private static final String ARGUMENT_KEY_FILTER_TYPES = "in_filter_types";

    //存放选中的FilterType.Tag序号
    //private ArrayList<ArrayList<Integer>> mSelectedFilterTypeTags;
    private ArrayList<FilterType> mFilterTypes;

    public FilterDialog() {
        super();
        setExitType(EXIT_TYPE_NULL);
    }

    public FilterDialog addFilterType(String label, String[] entries) {
        return addFilterType(label, getFilterTypes().size(), entries, buildDefaultEntryValues(entries), null);
    }

    private int[] buildDefaultEntryValues(String[] entries) {
        int size = entries.length;
        int[] result = new int[size];
        for (int i = 0;i < size;++i) {
            result[i] = i;
        }
        return result;
    }

    //entryValues不能相同，labelValue不能相同
    public FilterDialog addFilterType(String label, int labelValue, String[] entries, int[] entryValues) {
        return addFilterType(label, labelValue, entries, entryValues, null);
    }

    public FilterDialog addFilterType(String label, int labelValue, String[] entries, int[] entryValues, boolean[] entryDefaultStates) {
        if (entries == null || entryValues == null) {
            throw new NullPointerException("entry and value may not be null");
        }
        int size = entries.length;
        if (size == 0) {
            throw new IllegalArgumentException("entry size may not be 0");
        }
        if (size != entryValues.length || (entryDefaultStates != null && size != entryDefaultStates.length)) {
            throw new IllegalArgumentException("entry,value and state may have same size");
        }
        FilterType.Tag[] tags = new FilterType.Tag[size];
//        if (mSelectedFilterTypeTags == null) {
//            mSelectedFilterTypeTags = new ArrayList<>();
//        }
//        mSelectedFilterTypeTags.add(new ArrayList<Integer>());
        if (entryDefaultStates != null) {
            for (int i = 0;i < size;++i) {
                tags[i] = new FilterType.Tag(entries[i], entryValues[i], entryDefaultStates[i]);
//                if (entryDefaultStates[i]) {
//                    mSelectedFilterTypeTags.get(mSelectedFilterTypeTags.size() - 1).add(i);
//                }
            }
        } else {
            for (int i = 0;i < size;++i) {
                tags[i] = new FilterType.Tag(entries[i], entryValues[i]);
            }
        }
        getFilterTypes().add(new FilterType(label, labelValue, tags));
        return this;
    }

    private ArrayList<FilterType> getFilterTypes() {
        if (mFilterTypes == null) {
            mFilterTypes = new ArrayList<>();
        }
        return mFilterTypes;
    }


//    public FilterDialog addFilterType(String label, FilterType.Tag... tags) {
//        return addFilterType(new FilterType(label, tags));
//        //return addFilterType(label, tags, true);
//    }
//
//    public FilterDialog addFilterType(String label, List<FilterType.Tag> tags) {
//        FilterType.Tag[] copy = new FilterType.Tag[tags.size()];
//        return addFilterType(new FilterType(label, copy));
////        if (tags == null) {
////            return this;
////        }
////        FilterType.Tag[] copy = new FilterType.Tag[tags.size()];
////        return addFilterType(label, tags.toArray(copy), false);
//    }
//
//    public FilterDialog addFilterType(FilterType type) {
//        if (mFilterTypes == null) {
//            mFilterTypes = new ArrayList<>();
//        }
//        mFilterTypes.add(type);
//        return this;
//    }

//    private FilterDialog addFilterType(String label, FilterType.Tag[] tags, boolean needClone) {
//        checkLabel(label);
//        checkTags(tags);
//        ArrayList<FilterType> filterTypes = getArguments().getParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES);
//        if (filterTypes == null) {
//            filterTypes = new ArrayList<>();
//            getArguments().putParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES, filterTypes);
//        }
//        filterTypes.add(new FilterType(label, needClone ? tags.clone() : tags));
//        return this;
//    }

    public FilterDialog clearFilterTypes() {
        //getArguments().putParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES, null);
        //mSelectedFilterTypeTags.clear();
        mFilterTypes.clear();
        return this;
    }

    @Override
    protected void onSetContentView(View contentView, Decorator decorator, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFilterTypes = savedInstanceState.getParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES);
            //mSelectedFilterTypeTags = getCheckStates(savedInstanceState);
        }
        ViewPager vpFilter = contentView.findViewById(R.id.vp_filter);
        PagerTabStrip ptsFilter = vpFilter.findViewById(R.id.pts_filter);
        ptsFilter.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(decorator.getFilterLabelTextSizeRes()));
        ptsFilter.setTextColor(ContextCompat.getColor(getContext(), decorator.getFilterLabelTextColorRes()));
        ptsFilter.setTabIndicatorColorResource(decorator.getFilterLabelUnderlineColorRes());
        ptsFilter.setDrawFullUnderline(decorator.isDrawFilterLabelFullUnderline());
        if (decorator.getFilterLabelFullBackgroundRes() != 0) {
            ptsFilter.setBackgroundResource(decorator.getFilterLabelFullBackgroundRes());
        }
        if (decorator.getFilterPaneHeightDimenRes() != 0) {
            ViewGroup.LayoutParams params = vpFilter.getLayoutParams();
            params.height = getResources().getDimensionPixelSize(decorator.getFilterPaneHeightDimenRes());
            vpFilter.setLayoutParams(params);
        }
        vpFilter.setAdapter(new FilterPageAdapter(mFilterTypes, decorator));
//        ConstraintLayout clFilter = contentView.findViewById(R.id.vp_filter);
//        Bundle arguments = getArguments();
//        if (filterTypes != null) {
//            Context context = getContext();
//            Resources resources = context.getResources();
//            ConstraintSet constraintSet = new ConstraintSet();
//            constraintSet.clone(clFilter);
//            float textSize = resources.getDimensionPixelSize(decorator.getFilterTypeTextSize());
//            int labelWidth = resources.getDimensionPixelSize(decorator.getFilterLabelWidth());
//            int tagMargin = resources.getDimensionPixelSize(decorator.getFilterTagMargin());
//            Drawable tagBackground = ContextCompat.getDrawable(context, decorator.getFilterTagBackground());
//            int currTvLabelId, prevTvLabelId = 0, currViewId, prevViewId = 0;
//            clFilter.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//            final int maxWidth = clFilter.getMeasuredWidth();
//            int headWidth = 0, noLimitTagWidth = 0;
//            boolean hasFilter;
//            for (int i = 0;i < filterTypes.size();++i) {
//                FilterType filterType = filterTypes.get(i);
//                //添加label view
//                TextView tvLabel = new TextView(context);
//                currTvLabelId = View.generateViewId();
//                tvLabel.setId(currTvLabelId);
//                tvLabel.setText(filterType.getEntry());
//                tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
//                tvLabel.setLines(1);
//                tvLabel.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//                clFilter.addView(tvLabel);
//                constraintSet.constrainWidth(currTvLabelId, labelWidth);
//                constraintSet.constrainHeight(currTvLabelId, ConstraintSet.WRAP_CONTENT);
//                constraintSet.setMargin(currTvLabelId,
//                        ConstraintSet.START,
//                        tagMargin);
//                constraintSet.connect(currTvLabelId,
//                        ConstraintSet.START,
//                        ConstraintSet.PARENT_ID,
//                        ConstraintSet.START);
//                if (prevTvLabelId == 0) {
//                    constraintSet.connect(currTvLabelId,
//                            ConstraintSet.TOP,
//                            ConstraintSet.PARENT_ID,
//                            ConstraintSet.TOP);
//                } else {
//                    constraintSet.connect(currTvLabelId,
//                            ConstraintSet.TOP,
//                            prevTvLabelId,
//                            ConstraintSet.BOTTOM);
//                }
//                prevTvLabelId = currTvLabelId;
//                //添加“全部”按钮
//                hasFilter = filterType.hasFilter();
//                currViewId = View.generateViewId();
//                Button btnNoLimit = new Button(context);
//                btnNoLimit.setId(currViewId);
//                btnNoLimit.setText(R.string.qbox_no_limit);
//                btnNoLimit.setTextSize(textSize);
//                btnNoLimit.setBackground(tagBackground);
//                btnNoLimit.setSelected(!hasFilter);
//                btnNoLimit.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//                if (noLimitTagWidth == 0) {
//                    btnNoLimit.measure(0, 0);
//                    noLimitTagWidth = btnNoLimit.getMeasuredWidth();
//                }
//                clFilter.addView(btnNoLimit);
//                constraintSet.setMargin(currViewId,
//                        ConstraintSet.START,
//                        tagMargin);
//                headWidth = labelWidth + noLimitTagWidth + tagMargin + tagMargin;
//
//            }
//        }
    }

//    private ArrayList<ArrayList<Integer>> getCheckStates(@NonNull Bundle savedInstanceState) {
//        ArrayList<ArrayList<Integer>> checkStates = new ArrayList<>();
//        ArrayList<Integer> childCheckStates;
//        for (int i = 0;;++i) {
//            childCheckStates = savedInstanceState.getIntegerArrayList(ARGUMENT_KEY_CHECK_STATES + i);
//            if (childCheckStates != null) {
//                checkStates.add(childCheckStates);
//            } else {
//                break;
//            }
//        }
//        return checkStates;
//    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            //putCheckStates(outState);
            outState.putParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES, mFilterTypes);
        }
    }

    @Override
    protected int getDefaultTitleRes() {
        return R.string.qbox_filter;
    }

    //    private void putCheckStates(Bundle outState) {
//        int pos = 0;
//        for (int size = mSelectedFilterTypeTags.size(); pos < size; ++pos) {
//            outState.putIntegerArrayList(ARGUMENT_KEY_CHECK_STATES + pos, mSelectedFilterTypeTags.get(pos));
//        }
//        for (;;) {
//            if (outState.getIntegerArrayList(ARGUMENT_KEY_CHECK_STATES + pos) != null) {
//                outState.putIntegerArrayList(ARGUMENT_KEY_CHECK_STATES + pos, null);
//            } else {
//                break;
//            }
//        }
//    }


    @Override
    protected boolean onCancel() {
        return onDismiss();
    }

    @Override
    protected boolean onConfirm() {
        return onDismiss();
    }

    private boolean onDismiss() {
        OnFilterChangeListener listener = getListener(OnFilterChangeListener.class);
        if (listener != null && checkAndUpdateFiltersState()) {
            //changeSelectedFilterTypeTags();
            int size = mFilterTypes.size();
            boolean[] hasFilters = new boolean[size];
            List<Integer>[] checkedFilterEntryValues = new List[size];
            FilterType type;
            List<Integer> selectedTagEntryValues;
            for (int filterNo = 0;filterNo < size;++filterNo) {
                type = mFilterTypes.get(filterNo);
                hasFilters[filterNo] = type.hasFilter();
                selectedTagEntryValues = new ArrayList<>(type.mLastSelectedTagNos.size());
                for (Integer selectedTagNo
                        : type.mLastSelectedTagNos) {
                    selectedTagEntryValues.add(type.mTags[selectedTagNo].mEntryValue);
                }
                checkedFilterEntryValues[filterNo] = selectedTagEntryValues;
            }
            listener.onFilterChange(this, hasFilters, checkedFilterEntryValues);
        }
        return true;
    }

    private boolean checkAndUpdateFiltersState() {
        boolean result = false;
        for (FilterType type
                : mFilterTypes) {
            if (type.isStateChanged()) {
                result = true;
                type.updateLastSelectedTagNos();
            }
        }
        return result;
//        for (int filterNo = 0, filterSize = mSelectedFilterTypeTags.size(), tagSize;
//             filterNo < filterSize; ++filterNo) {
//            FilterType type = mFilterTypes.get(filterNo);
//            List<Integer> selectedTagNos = mSelectedFilterTypeTags.get(filterNo);
//            tagSize = selectedTagNos.size();
//            if (type.mLastSelectedTagNos.size() != tagSize) {
//                return true;
//            }
//            for (int tagNo = 0;tagNo < tagSize;++tagNo) {
//                Integer selectedTagNo = selectedTagNos.get(tagNo);
//                if (!type.mLastSelectedTagNos.contains(selectedTagNo)) {
//                    return true;
//                }
//            }
//        }
//        return false;
    }

//    private void changeSelectedFilterTypeTags() {
//        for (int filterNo = 0, filterSize = mFilterTypes.size();
//                filterNo < filterSize;++filterNo) {
//            ArrayList<Integer> selectedTagNos = mSelectedFilterTypeTags.get(filterNo);
//            selectedTagNos.clear();
//            for (Integer selectedTagNo
//                    : mFilterTypes.get(filterNo).mLastSelectedTagNos) {
//                selectedTagNos.add(selectedTagNo);
//            }
//        }
//    }

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int onSetContentLayout() {
            return R.layout.qbox_dialog_content_filter;
        }

        @Override
        public void reset() {
            super.reset();
            setFilterLabelTextSizeRes(R.dimen.qbox_dialog_filter_label_text_size);
            setFilterLabelTextColorRes(R.color.qbox_background_check_box_checked);
            setFilterLabelUnderlineColorRes(R.color.qbox_background_check_box_checked);
            setDrawFilterLabelFullUnderline(false);
            setFilterLabelFullBackgroundRes(0);
            setFilterTagBackgroundRes(R.drawable.qbox_selector_background_check_box);
            setFilterTagTextSizeRes(R.dimen.qbox_size_text_dialog_view);
            setFilterTagTextColorStateListRes(R.color.qbox_selector_text_color_check_box);
            setFilterTagTextColorRes(0);
            setDisplayTagAll(true);
            setTagAllLabelRes(R.string.qbox_all);
            setFilterTagsPaddingDimenRes(R.dimen.qbox_padding);
            setFilterTagsHorizontalIntervalDimenRes(R.dimen.qbox_dialog_filter_tag_margin);
            setFilterTagsVerticalIntervalDimenRes(R.dimen.qbox_dialog_filter_tag_margin);
            setFilterPaneHeightDimenRes(0);
        }

//        public void setFilterTypeTextSize(@DimenRes int textSizeRes) {
//            mParameters.putInt("fdp_text_size", textSizeRes);
//        }
//
//        public @DimenRes int getFilterTypeTextSize() {
//            return mParameters.getInt("fdp_text_size");
//        }

        public void setFilterLabelTextSizeRes(@DimenRes int textSizeRes) {
            mParameters.putInt("fdp_label_text_size", textSizeRes);
        }

        public @DimenRes int getFilterLabelTextSizeRes() {
            return mParameters.getInt("fdp_label_text_size");
        }

        public void setFilterLabelTextColorRes(@ColorRes int colorRes) {
            mParameters.putInt("fdp_label_text_color", colorRes);
        }

        public @ColorRes int getFilterLabelTextColorRes() {
            return mParameters.getInt("fdp_label_text_color");
        }

        public void setFilterLabelUnderlineColorRes(@ColorRes int colorRes) {
            mParameters.putInt("fdp_label_line_color", colorRes);
        }

        public @ColorRes int getFilterLabelUnderlineColorRes() {
            return mParameters.getInt("fdp_label_line_color");
        }

        public void setDrawFilterLabelFullUnderline(boolean allowed) {
            mParameters.putBoolean("fdp_full_line_enable", allowed);
        }

        public boolean isDrawFilterLabelFullUnderline() {
            return mParameters.getBoolean("fdp_full_line_enable");
        }

        public void setFilterLabelFullBackgroundRes(@DrawableRes int backgroundRes) {
            mParameters.putInt("fdp_label_bg", backgroundRes);
        }

        public @DrawableRes int getFilterLabelFullBackgroundRes() {
            return mParameters.getInt("fdp_label_bg");
        }

//        public void setFilterLabelWidth(@DimenRes int widthRes) {
//            mParameters.putInt("fdp_label_width", widthRes);
//        }
//
//        public @DimenRes int getFilterLabelWidth() {
//            return mParameters.getInt("fdp_label_width");
//        }

        public void setFilterTagBackgroundRes(@DrawableRes int backgroundRes) {
            mParameters.putInt("fdp_tag_bg", backgroundRes);
        }

        public @DrawableRes int getFilterTagBackgroundRes() {
            return mParameters.getInt("fdp_tag_bg");
        }

        public void setFilterTagTextSizeRes(@DimenRes int textSizeRes) {
            mParameters.putInt("fdp_tag_text_size", textSizeRes);
        }

        public @DimenRes int getFilterTagTextSizeRes() {
            return mParameters.getInt("fdp_tag_text_size");
        }

        public void setFilterTagTextColorStateListRes(@ColorRes int colorRes) {
            mParameters.putInt("fdp_tag_text_colors", colorRes);
        }

        public @ColorRes int getFilterTagTextColorStateListRes() {
            return mParameters.getInt("fdp_tag_text_colors");
        }

        public void setFilterTagTextColorRes(@ColorRes int colorRes) {
            mParameters.putInt("fdp_tag_text_color", colorRes);
        }

        public @ColorRes int getFilterTagTextColorRes() {
            return mParameters.getInt("fdp_tag_text_color");
        }

        public void setDisplayTagAll(boolean enable) {
            mParameters.putBoolean("fdp_tag_all_enable", enable);
        }

        public boolean isDisplayTagAll() {
            return mParameters.getBoolean("fdp_tag_all_enable");
        }

        public void setTagAllLabelRes(@StringRes int labelRes) {
            mParameters.putInt("fdp_tag_all_label", labelRes);
        }

        public @StringRes int getTagAllLabelRes() {
            return mParameters.getInt("fdp_tag_all_label");
        }

        public void setFilterTagsPaddingDimenRes(@DimenRes int dimenRes) {
            mParameters.putInt("fdp_tags_padding", dimenRes);
        }

        public @DimenRes int getFilterTagsPaddingDimenRes() {
            return mParameters.getInt("fdp_tags_padding");
        }

        public void setFilterTagsHorizontalIntervalDimenRes(@DimenRes int dimenRes) {
            mParameters.putInt("fdp_tags_hi", dimenRes);
        }

        public @DimenRes int getFilterTagsHorizontalIntervalDimenRes() {
            return mParameters.getInt("fdp_tags_hi");
        }

        public void setFilterTagsVerticalIntervalDimenRes(@DimenRes int dimenRes) {
            mParameters.putInt("fdp_tags_vi", dimenRes);
        }

        public @DimenRes int getFilterTagsVerticalIntervalDimenRes() {
            return mParameters.getInt("fdp_tags_vi");
        }

        public void setFilterPaneHeightDimenRes(@DimenRes int dimenRes) {
            mParameters.putInt("fdp_pane_height", dimenRes);
        }

        public @DimenRes int getFilterPaneHeightDimenRes() {
            return mParameters.getInt("fdp_pane_height");
        }

//        public void setFilterTagIntervalDimenRes(@DimenRes int marginRes) {
//            mParameters.putInt("fdp_tag_margin", marginRes);
//        }
//
//        public @DimenRes int getFilterTagIntervalDimenRes() {
//            return mParameters.getInt("fdp_tag_margin");
//        }
    }

    private static class FilterType implements Parcelable {

        private final String mEntry;
        private final int mEntryValue;
        //根据用户操作实时更新选中状态
        private final Tag[] mTags;
        private int mSelectedTagCount;
        //只在点击确定时更新选中状态，用于和mTags，mSelectedTagCount
        // 进行比较，快速确定Filter状态是否改变
        private final LinkedList<Integer> mLastSelectedTagNos;// = new LinkedList<>();

        public FilterType(String entry,
                          int entryValue,
                          Tag[] tags) {
            Tag.checkEntry(entry);
            checkTags(tags);
            mEntry = entry;
            mEntryValue = entryValue;
            mTags = tags;
            mLastSelectedTagNos = new LinkedList<>();
            updateLastSelectedTagNos();
            //mLastSelectedTagNos = buildLastSelectedTagNos(tags);
            mSelectedTagCount = mLastSelectedTagNos.size();
            //initializeSelectedTags();
        }

        public static final Creator<FilterType> CREATOR = new Creator<FilterType>() {
            @Override
            public FilterType createFromParcel(Parcel in) {
                return new FilterType(in);
            }

            @Override
            public FilterType[] newArray(int size) {
                return new FilterType[size];
            }
        };

//        private LinkedList<Integer> buildLastSelectedTagNos(Tag[] tags) {
//            LinkedList<Integer> selectedTagNos = new LinkedList<>();
//            for (int i = 0, size = tags.length;i < size;++i) {
//                if (tags[i].mSelected) {
//                    selectedTagNos.addLast(i);
//                }
//            }
//            return selectedTagNos;
//        }

        public void updateLastSelectedTagNos() {
            mLastSelectedTagNos.clear();
            for (int i = 0, size = mTags.length;i < size;++i) {
                if (mTags[i].mSelected) {
                    mLastSelectedTagNos.addLast(i);
                }
            }
        }

//        private void initializeSelectedTags() {
//            for (int i = 0, size = mTags.length;i < size;++i) {
//                if (mTags[i].mSelected) {
//                    mLastSelectedTagNos.addLast(i);
//                }
//            }
//        }

        protected FilterType(Parcel in) {
            mEntry = in.readString();
            mEntryValue = in.readInt();
            mTags = in.createTypedArray(Tag.CREATOR);
            mSelectedTagCount = in.readInt();
            mLastSelectedTagNos = new LinkedList<>(in.readArrayList(Integer.class.getClassLoader()));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mEntry);
            dest.writeInt(mEntryValue);
            dest.writeTypedArray(mTags, flags);
            dest.writeInt(mSelectedTagCount);
            dest.writeList(mLastSelectedTagNos);
        }

        private void checkTags(Tag[] tags) {
            if (tags == null) {
                throw new NullPointerException("tags may not be null");
            }
        }

        public boolean isStateChanged() {
            if (mLastSelectedTagNos.size() != mSelectedTagCount) {
                return true;
            }
            for (Integer selectedTagNo
                    : mLastSelectedTagNos) {
                if (!mTags[selectedTagNo].mSelected) {
                    return true;
                }
            }
            return false;
        }

        //全选选项是否选中
        public boolean isSelected() {
            //return mLastSelectedTagNos.size() == 0;
            return mSelectedTagCount == 0;
        }

        //子选项是否全部选中
        public boolean isTagsAllSelected() {
            //return mLastSelectedTagNos.size() == mTags.length;
            return mSelectedTagCount == mTags.length;
        }

        public boolean hasFilter() {
            return !isSelected() && !isTagsAllSelected();
        }

        public String getEntry() {
            return mEntry;
        }

        public int getEntryValue() {
            return mEntryValue;
        }

        public boolean setSelected(boolean selected, OnTagStateChangeListener listener) {
            if (!isSelected() && selected) {
                for (int i = 0, size = mTags.length;i < size;++i) {
                    if (setTagSelected(i, false)) {
                        if (listener != null) {
                            listener.onTagStateChange(i);
                        }
                    }
                }
//                for (int tagNo
//                        : mLastSelectedTagNos) {
//                    mTags[tagNo].mSelected = false;
//                }
                //mLastSelectedTagNos.clear();
                //mSelectedTagCount = 0;
                return true;
            }
            return false;
        }

        public boolean setTagSelected(int tagPosition, boolean selected) {
            Tag tag = mTags[tagPosition];
            if (tag.mSelected != selected) {
                tag.mSelected = selected;
                if (selected) {
                    //mLastSelectedTagNos.addLast(tagPosition);
                    ++mSelectedTagCount;
                } else {
                    //mLastSelectedTagNos.remove((Integer) tagPosition);
                    --mSelectedTagCount;
                }
                return true;
            }
            return false;
        }

        public interface OnTagStateChangeListener {
            void onTagStateChange(int tagNo);
        }

        private static class Tag implements Parcelable {

            private final String mEntry;
            private final int mEntryValue;
            private boolean mSelected;

            protected Tag(Parcel in) {
                mEntry = in.readString();
                mEntryValue = in.readInt();
                mSelected = in.readByte() != 0;
            }

            public static final Creator<Tag> CREATOR = new Creator<Tag>() {
                @Override
                public Tag createFromParcel(Parcel in) {
                    return new Tag(in);
                }

                @Override
                public Tag[] newArray(int size) {
                    return new Tag[size];
                }
            };

            public Tag(String entry, int entryValue) {
                this(entry, entryValue, false);
            }

            public Tag(String entry, int entryValue, boolean selected) {
                checkEntry(entry);
                mEntry = entry;
                mEntryValue = entryValue;
                mSelected = selected;
            }

            private static void checkEntry(String entry) {
                if (TextUtils.isEmpty(entry)) {
                    throw new IllegalArgumentException("entry may not be empty");
                }
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(mEntry);
                dest.writeInt(mEntryValue);
                dest.writeByte((byte) (mSelected ? 1 : 0));
            }

//            private boolean setSelected(boolean selected) {
//                if (mSelected != selected) {
//                    mSelected = selected;
//                    return true;
//                }
//                return false;
//            }
        }
    }

    private static class Page implements CompoundButton.OnCheckedChangeListener, FilterType.OnTagStateChangeListener {

        private static int adaptivePaneMaxHeight;

        private final FilterType mType;
        private View mView;
        private CheckBox mChkAll;
        private final CheckBox[] mChkTags;

        public Page(FilterType type) {
            mType = type;
            mChkTags = new CheckBox[type.mTags.length];
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            int tagNo = (int) buttonView.getTag();
            if (tagNo == TagBuilder.TAG_ALL_NO) {
                mChkAll.setEnabled(!isChecked);
                if (!mType.isSelected() && isChecked) {
//                    for (Integer selectedTagNo
//                            : mType.mLastSelectedTagNos) {
//                        CheckBox chkTag = mChkTags[selectedTagNo];
//                        chkTag.setOnCheckedChangeListener(null);
//                        chkTag.setChecked(false);
//                        chkTag.setOnCheckedChangeListener(this);
//                    }
                    mType.setSelected(isChecked, this);
                }
            } else {
                if (mType.isSelected() && isChecked) {
                    //mType.setTagSelected(tagNo, isChecked);
                    mType.setTagSelected(tagNo, isChecked);
                    mChkAll.setChecked(false);
                } else if (mType.mLastSelectedTagNos.size() == 1 && !isChecked) {
                    mType.setTagSelected(tagNo, isChecked);
                    mChkAll.setChecked(true);
                } else {
                    mType.setTagSelected(tagNo, isChecked);
                }
            }
        }

        @Override
        public void onTagStateChange(int tagNo) {
            CheckBox chkTag = mChkTags[tagNo];
            chkTag.setOnCheckedChangeListener(null);
            chkTag.setChecked(false);
            chkTag.setOnCheckedChangeListener(this);
        }

        public View getView(ViewGroup container, Decorator decorator) {
            if (mView == null) {
                Context context = container.getContext();
                TagBuilder tagBuilder = new TagBuilder(context, decorator, container.getMeasuredWidth());
                ScrollView view = new ScrollView(context);
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                RelativeLayout layout = new RelativeLayout(context);
                layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                layout.setPadding(tagBuilder.mTagsPadding, tagBuilder.mTagsPadding, tagBuilder.mTagsPadding, tagBuilder.mTagsPadding);
                //添加“全部”Tag
                if (decorator.isDisplayTagAll()) {
                    mChkAll = tagBuilder.build(context, this, Page.TagBuilder.TAG_ALL_NO);
                    layout.addView(mChkAll);
                }
                //添加Filter.Tag
                for (int tagNo = 0, tagCount = mType.mTags.length;tagNo < tagCount;++tagNo) {
                    CheckBox box = tagBuilder.build(context, this, tagNo);
                    mChkTags[tagNo] = box;
                    layout.addView(box);
                }
                view.addView(layout);
                //设置FilterPane高度
                container.setLayoutParams(new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        getPageMaxHeight(tagBuilder)
                                + container.findViewById(R.id.pts_filter).getMeasuredHeight()));
                mView = view;
            }
            return mView;
        }

        private static int getPageMaxHeight(TagBuilder builder) {
            int paneMaxHeight;
            if (builder.mPaneMaxHeight != 0) {
                paneMaxHeight = builder.mPaneMaxHeight;
            } else {
                adaptivePaneMaxHeight = Math.max(adaptivePaneMaxHeight, builder.mPaneHeight);
                paneMaxHeight = adaptivePaneMaxHeight + builder.mTagsPadding * 2;
            }
            return paneMaxHeight;
        }

        public static class TagBuilder {

            private static final int TAG_ALL_NO = -1;

            private static final int LOCATION_FIRST = 0;
            private static final int LOCATION_APPEND = 1;
            private static final int LOCATION_HEAD = 2;

            private final Drawable mTagButtonFrame;
            private final @DrawableRes int mTagBackgroundRes;
            private final ColorStateList mTagTextColorStateList;
            private final @ColorInt int mTagTextColorInt;
            private final int mTagsPadding;
            private final int mTagHorizontalInterval;
            private final int mTagVerticalInterval;
            private final String mTagAllLabel;
            private final int mPaneMaxWidth;
            private final int mPaneMaxHeight;
            private int mPaneWidth;
            private int mPaneHeight;
            private int mLastTagRowHeadId;

            public TagBuilder(Context context, Decorator decorator, int paneMaxWidth) {
                mTagButtonFrame = new ColorDrawable(Color.TRANSPARENT);
                mTagBackgroundRes = decorator.getFilterTagBackgroundRes();
                mTagTextColorStateList = decorator.getFilterTagTextColorStateListRes() != 0
                        ? ContextCompat.getColorStateList(context, decorator.getFilterTagTextColorStateListRes())
                        : null;
                mTagTextColorInt = decorator.getFilterTagTextColorRes() != 0
                        ? ContextCompat.getColor(context, decorator.getFilterTagTextColorRes())
                        : 0;
                Resources resources = context.getResources();
                mTagsPadding = resources.getDimensionPixelOffset(decorator.getFilterTagsPaddingDimenRes());
                mTagHorizontalInterval = resources.getDimensionPixelOffset(decorator.getFilterTagsHorizontalIntervalDimenRes());
                mTagVerticalInterval = resources.getDimensionPixelOffset(decorator.getFilterTagsVerticalIntervalDimenRes());
                mTagAllLabel = resources.getString(decorator.getTagAllLabelRes());
                mPaneMaxWidth = paneMaxWidth - mTagHorizontalInterval * 2;
                mPaneMaxHeight = decorator.getFilterPaneHeightDimenRes() != 0
                        ? resources.getDimensionPixelOffset(decorator.getFilterPaneHeightDimenRes())
                        : 0;
            }

//            public void measureMaxWidth(View tagsPaneParent, View tagsPane, boolean recalculate) {
//                if (mPaneMaxWidth == 0 || recalculate) {
//                    int specWidth = ViewGroup.getChildMeasureSpec(View.MeasureSpec.UNSPECIFIED, tagsPane.getPaddingLeft(), tagsPane.getLayoutParams().width);
//                    mPaneMaxWidth = getMeasuredWidth(tagsPaneParent, specWidth);
//                    //mPaneMaxWidth = getMeasuredWidth(tagsPane);
//                }
//            }

            public CheckBox build(Context context, Page page, int tagNo) {
                CheckBox box = new CheckBox(context);
                //区分“全部”Tag和普通Tag
                if (tagNo == TAG_ALL_NO) {
                    box.setText(mTagAllLabel);
                    box.setChecked(page.mType.isSelected());
                    if (box.isChecked()) {
                        box.setEnabled(false);
                    }
                } else {
                    FilterType.Tag tag = page.mType.mTags[tagNo];
                    box.setText(tag.mEntry);
                    box.setChecked(tag.mSelected);
                }
                //设置参数
                box.setId(View.generateViewId());
                box.setTag(tagNo);
                box.setButtonDrawable(mTagButtonFrame);
                box.setBackgroundResource(mTagBackgroundRes);
                if (mTagTextColorStateList != null) {
                    box.setTextColor(mTagTextColorStateList);
                } else if (mTagTextColorInt != 0) {
                    box.setTextColor(mTagTextColorInt);
                }
                //安排位置
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                int location = arrangeLocation(box);
                if (location != LOCATION_FIRST) {
                    if (location == LOCATION_APPEND) {
                        int previousId = (tagNo > 0
                                ? page.mChkTags[tagNo - 1]
                                : page.mChkAll).getId();
                        params.addRule(RelativeLayout.RIGHT_OF, previousId);
                        params.addRule(RelativeLayout.ALIGN_BASELINE, previousId);
                        params.leftMargin = mTagHorizontalInterval;
                    } else if (location == LOCATION_HEAD) {
//                        if (box.getText().equals("OPQ")) {
//                            params.addRule(RelativeLayout.BELOW, page.mChkTags[3].getId());
//                        } else {
//                            params.addRule(RelativeLayout.BELOW, previousId);
//                        }
                        params.addRule(RelativeLayout.BELOW, mLastTagRowHeadId);
                        //params.addRule(RelativeLayout.BELOW, previousId);
                        params.topMargin = mTagVerticalInterval;
                        mLastTagRowHeadId = box.getId();
                    }
                } else {
                    mLastTagRowHeadId = box.getId();
                }
                box.setLayoutParams(params);
                box.setOnCheckedChangeListener(page);
                return box;
            }

            private int getMeasuredWidth(View view) {
                return getMeasuredWidth(view, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            }

            private int getMeasuredWidth(View view, int specWidth) {
                int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                view.measure(specWidth, h);
                return view.getMeasuredWidth();
            }

            private boolean canAppend(int tagWidth) {
                return mPaneWidth
                        + tagWidth
                        + mTagHorizontalInterval
                        <= mPaneMaxWidth;
            }

            private void increasePaneWidth(View tag, boolean withHorizontalInterval) {
                increasePaneWidth(tag.getMeasuredWidth(), withHorizontalInterval);
            }

            private void increasePaneWidth(int tagWidth, boolean withHorizontalInterval) {
                mPaneWidth += tagWidth
                        + (withHorizontalInterval
                        ? mTagHorizontalInterval : 0);
            }

            private void increasePaneHeight(View tag, boolean withVerticalInterval) {
                increasePaneHeight(tag.getMeasuredHeight(), withVerticalInterval);
            }

            private void increasePaneHeight(int tagHeight, boolean withVerticalInterval) {
                mPaneHeight += tagHeight
                        + (withVerticalInterval
                        ? mTagVerticalInterval : 0);
            }

            private void resetPaneWidth() {
                mPaneWidth = 0;
            }

            private int arrangeLocation(View tag) {
                int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
                tag.measure(widthSpec, heightSpec);
                if (mPaneWidth == 0) {
                    increasePaneWidth(tag, false);
                    increasePaneHeight(tag, false);
                    return LOCATION_FIRST;
                } else {
                    int tagWidth = tag.getMeasuredWidth();
                    if (canAppend(tagWidth)) {
                        increasePaneWidth(tagWidth, true);
                        return LOCATION_APPEND;
                    } else {
                        resetPaneWidth();
                        increasePaneWidth(tagWidth, false);
                        increasePaneHeight(tag, true);
                        return LOCATION_HEAD;
                    }
                }
            }
        }
    }

    private static class FilterPageAdapter extends PagerAdapter {

        //private final ArrayList<FilterType> mFilterTypes;
        private final Decorator mDecorator;
        private final Page[] mPages;

        public FilterPageAdapter(ArrayList<FilterType> types, Decorator decorator) {
            //mFilterTypes = types;
            mDecorator = decorator;
            mPages = buildPagesByTypes(types);
        }

        private Page[] buildPagesByTypes(ArrayList<FilterType> types) {
            int size = types.size();
            Page[] pages = new Page[size];
            //Page.TagBuilder parameter = new Page.TagBuilder(context, decorator);
            for (int i = 0;i < size;++i) {
                pages[i] = new Page(types.get(i));
                //pages[i] = buildPageByType(context, types.get(i), decorator, parameter);
            }
            return pages;
        }

//        private Page buildPageByType(Context context, FilterType type, Decorator decorator, Page.TagBuilder tagBuilder) {
//            ScrollView view = new ScrollView(context);
//            Page page = new Page(type, view);
//            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            RelativeLayout layout = new RelativeLayout(context);
//            layout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            layout.setPadding(tagBuilder.mTagsPadding, tagBuilder.mTagsPadding, tagBuilder.mTagsPadding, tagBuilder.mTagsPadding);
//            view.addView(layout);
//            tagBuilder.measureMaxWidth(layout, false);
//            //添加“全部”Tag
//            if (decorator.isDisplayTagAll()) {
//                page.mChkAll = tagBuilder.build(context, page, Page.TagBuilder.TAG_ALL_NO);
//                layout.addView(page.mChkAll);
//            }
//            for (int tagNo = 0, tagCount = type.mTags.length;tagNo < tagCount;++tagNo) {
//                CheckBox box = tagBuilder.build(context, page, tagNo);
//                page.mChkTags[tagNo] = box;
//                layout.addView(box);
//            }
//            return page;
//        }

//        private int buildTag(Context context, Page page, int tagNo, Page.TagBuilder parameter) {
//            CheckBox box = new CheckBox(context);
//            box.setOnCheckedChangeListener(page);
//            //区分“全部”Tag和普通Tag
//            if (tagNo == -1) {
//                box.setText(parameter.mTagAllLabel);
//                box.setChecked(page.mType.isSelected());
//            } else {
//                FilterType.Tag tag = page.mType.mTags[i];
//                box.setText(tag.mEntry);
//                box.setChecked(tag.mSelected);
//            }
//            box.setTag(tagNo);
//            box.setButtonDrawable(parameter.mTagButtonFrame);
//            box.setBackground(parameter.mTagBackgroundRes);
//            if (parameter.mTagTextColorStateList != null) {
//                box.setTextColor(parameter.mTagTextColorStateList);
//            } else if (parameter.mTagTextColorInt != 0) {
//                box.setTextColor(parameter.mTagTextColorInt);
//            }
//            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            if (parameter.getPaneCurrentWidth() == 0) {
//                if (tagNo != -1) {
//                    params.addRule(RelativeLayout.BELOW, previousId);
//                }
//            }
//            if (previousId != 0) {
//                params.addRule(RelativeLayout.BELOW, previousId);
//            }
//            box.setLayoutParams(params);
//            previousId = View.generateViewId();
//            box.setId(previousId);
//            layout.addView(box);
//            page.mChkTags[tagNo] = box;
//            return previousId;
//        }

        @Override
        public int getCount() {
            return mPages.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(mPages[position].getView(container, mDecorator));
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = mPages[position].getView(container, mDecorator);
            container.addView(view);
            return view;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return mPages[position].mType.mEntry;
        }
    }

    public interface OnFilterChangeListener {
        //一维数组与FilterType[]一一对应，二维数组与Tag一一对应
        //当选中某个tag时，hasFilter=True，
        // 当所有tag均未选中或均选中时，hasFilter=False
        void onFilterChange(FilterDialog dialog, boolean[] hasFilters, List<Integer>[] checkedFilterEntryValues);
    }
}
