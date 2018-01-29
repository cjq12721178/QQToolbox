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

    private static final String ARGUMENT_KEY_FILTER_TYPES = "in_filter_types";

    //存放选中的FilterType.Tag序号
    private ArrayList<FilterType> mFilterTypes;

    public FilterDialog() {
        super();
        setExitType(EXIT_TYPE_NULL);
    }

    public FilterDialog addFilterType(String label, String[] entries) {
        return addFilterType(label, entries, buildDefaultEntryValues(entries), null);
    }

    private int[] buildDefaultEntryValues(String[] entries) {
        int size = entries.length;
        int[] result = new int[size];
        for (int i = 0;i < size;++i) {
            result[i] = i;
        }
        return result;
    }

    public FilterDialog addFilterType(String label, String[] entries, boolean[] entryDefaultStates) {
        return addFilterType(label, entries, buildDefaultEntryValues(entries), entryDefaultStates);
    }

    //entryValues不能相同
    public FilterDialog addFilterType(String label, String[] entries, int[] entryValues) {
        return addFilterType(label, entries, entryValues, null);
    }

    public FilterDialog addFilterType(String label, String[] entries, int[] entryValues, boolean[] entryDefaultStates) {
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
        if (entryDefaultStates != null) {
            for (int i = 0;i < size;++i) {
                tags[i] = new FilterType.Tag(entries[i], entryValues[i], entryDefaultStates[i]);
            }
        } else {
            for (int i = 0;i < size;++i) {
                tags[i] = new FilterType.Tag(entries[i], entryValues[i]);
            }
        }
        getFilterTypes().add(new FilterType(label, tags));
        return this;
    }

    private ArrayList<FilterType> getFilterTypes() {
        if (mFilterTypes == null) {
            mFilterTypes = new ArrayList<>();
        }
        return mFilterTypes;
    }

    public FilterDialog clearFilterTypes() {
        mFilterTypes.clear();
        return this;
    }

    @Override
    protected void onSetContentView(View contentView, Decorator decorator, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mFilterTypes = savedInstanceState.getParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES);
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
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES, mFilterTypes);
        }
    }

    @Override
    protected int getDefaultTitleRes() {
        return R.string.qbox_filter;
    }

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
    }

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int getDefaultContentLayoutRes() {
            return R.layout.qbox_dialog_content_filter;
        }

        final public void setFilterLabelTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("fdp_label_text_size", textSizeRes);
        }

        final public @DimenRes int getFilterLabelTextSizeRes() {
            return mParameters.getInt("fdp_label_text_size", getDefaultFilterLabelTextSizeRes());
        }

        public @DimenRes int getDefaultFilterLabelTextSizeRes() {
            return R.dimen.qbox_dialog_filter_label_text_size;
        }

        final public void setFilterLabelTextColor(@ColorRes int colorRes) {
            mParameters.putInt("fdp_label_text_color", colorRes);
        }

        final public @ColorRes int getFilterLabelTextColorRes() {
            return mParameters.getInt("fdp_label_text_color", getDefaultFilterLabelTextColorRes());
        }

        public @ColorRes int getDefaultFilterLabelTextColorRes() {
            return R.color.qbox_background_check_box_checked;
        }

        final public void setFilterLabelUnderlineColor(@ColorRes int colorRes) {
            mParameters.putInt("fdp_label_line_color", colorRes);
        }

        final public @ColorRes int getFilterLabelUnderlineColorRes() {
            return mParameters.getInt("fdp_label_line_color", getDefaultFilterLabelUnderlineColorRes());
        }

        public @ColorRes int getDefaultFilterLabelUnderlineColorRes() {
            return R.color.qbox_background_check_box_checked;
        }

        final public void setDrawFilterLabelFullUnderline(boolean allowed) {
            mParameters.putBoolean("fdp_full_line_enable", allowed);
        }

        final public boolean isDrawFilterLabelFullUnderline() {
            return mParameters.getBoolean("fdp_full_line_enable", isDefaultDrawFilterLabelFullUnderline());
        }

        public boolean isDefaultDrawFilterLabelFullUnderline() {
            return false;
        }

        final public void setFilterLabelFullBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("fdp_label_bg", backgroundRes);
        }

        final public @DrawableRes int getFilterLabelFullBackgroundRes() {
            return mParameters.getInt("fdp_label_bg", getDefaultFilterLabelFullBackgroundRes());
        }

        public @DrawableRes int getDefaultFilterLabelFullBackgroundRes() {
            return 0;
        }

        final public void setFilterTagBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("fdp_tag_bg", backgroundRes);
        }

        final public @DrawableRes int getFilterTagBackgroundRes() {
            return mParameters.getInt("fdp_tag_bg", getDefaultFilterTagBackgroundRes());
        }

        public @DrawableRes int getDefaultFilterTagBackgroundRes() {
            return R.drawable.qbox_selector_background_check_box;
        }

        final public void setFilterTagTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("fdp_tag_text_size", textSizeRes);
        }

        final public @DimenRes int getFilterTagTextSizeRes() {
            return mParameters.getInt("fdp_tag_text_size", getDefaultFilterTagTextSizeRes());
        }

        public @DimenRes int getDefaultFilterTagTextSizeRes() {
            return R.dimen.qbox_size_text_dialog_view;
        }

        final public void setFilterTagTextColorStateList(@ColorRes int colorRes) {
            mParameters.putInt("fdp_tag_text_colors", colorRes);
        }

        final public @ColorRes int getFilterTagTextColorStateListRes() {
            return mParameters.getInt("fdp_tag_text_colors", getDefaultFilterTagTextColorStateListRes());
        }

        public @ColorRes int getDefaultFilterTagTextColorStateListRes() {
            return R.color.qbox_selector_text_color_check_box;
        }

        final public void setFilterTagTextColor(@ColorRes int colorRes) {
            mParameters.putInt("fdp_tag_text_color", colorRes);
        }

        final public @ColorRes int getFilterTagTextColorRes() {
            return mParameters.getInt("fdp_tag_text_color", getDefaultFilterTagTextColorRes());
        }

        public @ColorRes int getDefaultFilterTagTextColorRes() {
            return 0;
        }

        final public void setDisplayTagAll(boolean enable) {
            mParameters.putBoolean("fdp_tag_all_enable", enable);
        }

        final public boolean isDisplayTagAll() {
            return mParameters.getBoolean("fdp_tag_all_enable", isDefaultDisplayTagAll());
        }

        public boolean isDefaultDisplayTagAll() {
            return true;
        }

        final public void setTagAllLabel(@StringRes int labelRes) {
            mParameters.putInt("fdp_tag_all_label", labelRes);
        }

        final public @StringRes int getTagAllLabelRes() {
            return mParameters.getInt("fdp_tag_all_label", getDefaultTagAllLabelRes());
        }

        public @StringRes int getDefaultTagAllLabelRes() {
            return R.string.qbox_all;
        }

        final public void setFilterTagsPadding(@DimenRes int dimenRes) {
            mParameters.putInt("fdp_tags_padding", dimenRes);
        }

        final public @DimenRes int getFilterTagsPaddingDimenRes() {
            return mParameters.getInt("fdp_tags_padding", getDefaultFilterTagsPaddingDimenRes());
        }

        public @DimenRes int getDefaultFilterTagsPaddingDimenRes() {
            return R.dimen.qbox_padding;
        }

        final public void setFilterTagsHorizontalInterval(@DimenRes int dimenRes) {
            mParameters.putInt("fdp_tags_hi", dimenRes);
        }

        final public @DimenRes int getFilterTagsHorizontalIntervalDimenRes() {
            return mParameters.getInt("fdp_tags_hi", getDefaultFilterTagsHorizontalIntervalDimenRes());
        }

        public @DimenRes int getDefaultFilterTagsHorizontalIntervalDimenRes() {
            return R.dimen.qbox_dialog_filter_tag_margin;
        }

        final public void setFilterTagsVerticalInterval(@DimenRes int dimenRes) {
            mParameters.putInt("fdp_tags_vi", dimenRes);
        }

        final public @DimenRes int getFilterTagsVerticalIntervalDimenRes() {
            return mParameters.getInt("fdp_tags_vi", getDefaultFilterTagsVerticalIntervalDimenRes());
        }

        public @DimenRes int getDefaultFilterTagsVerticalIntervalDimenRes() {
            return R.dimen.qbox_dialog_filter_tag_margin;
        }

        final public void setFilterPaneHeightDimenRes(@DimenRes int dimenRes) {
            mParameters.putInt("fdp_pane_height", dimenRes);
        }

        final public @DimenRes int getFilterPaneHeightDimenRes() {
            return mParameters.getInt("fdp_pane_height", getDefaultFilterPaneHeightDimenRes());
        }

        public @DimenRes int getDefaultFilterPaneHeightDimenRes() {
            return 0;
        }
    }

    private static class FilterType implements Parcelable {

        private final String mEntry;
        //private final int mEntryValue;
        //根据用户操作实时更新选中状态
        private final Tag[] mTags;
        private int mSelectedTagCount;
        //只在点击确定时更新选中状态，用于和mTags，mSelectedTagCount
        // 进行比较，快速确定Filter状态是否改变
        private final LinkedList<Integer> mLastSelectedTagNos;// = new LinkedList<>();

        public FilterType(String entry,
                          Tag[] tags) {
            Tag.checkEntry(entry);
            checkTags(tags);
            mEntry = entry;
            //mEntryValue = entryValue;
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
            //mEntryValue = in.readInt();
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
            //dest.writeInt(mEntryValue);
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

//        public int getEntryValue() {
//            return mEntryValue;
//        }

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
                    mType.setSelected(isChecked, this);
                }
            } else {
                if (mType.isSelected() && isChecked) {
                    mType.setTagSelected(tagNo, isChecked);
                    if (mChkAll != null) {
                        mChkAll.setChecked(false);
                    }
                } else if (mType.mSelectedTagCount == 1 && !isChecked) {
                    mType.setTagSelected(tagNo, isChecked);
                    if (mChkAll != null) {
                        mChkAll.setChecked(true);
                    }
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
            private final int mTagTextSize;
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
                mTagTextSize = resources.getDimensionPixelOffset(decorator.getFilterTagTextSizeRes());
                mTagsPadding = resources.getDimensionPixelOffset(decorator.getFilterTagsPaddingDimenRes());
                mTagHorizontalInterval = resources.getDimensionPixelOffset(decorator.getFilterTagsHorizontalIntervalDimenRes());
                mTagVerticalInterval = resources.getDimensionPixelOffset(decorator.getFilterTagsVerticalIntervalDimenRes());
                mTagAllLabel = resources.getString(decorator.getTagAllLabelRes());
                mPaneMaxWidth = paneMaxWidth - mTagHorizontalInterval * 2;
                mPaneMaxHeight = decorator.getFilterPaneHeightDimenRes() != 0
                        ? resources.getDimensionPixelOffset(decorator.getFilterPaneHeightDimenRes())
                        : 0;
            }

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
                box.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTagTextSize);
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
                        params.addRule(RelativeLayout.BELOW, mLastTagRowHeadId);
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
