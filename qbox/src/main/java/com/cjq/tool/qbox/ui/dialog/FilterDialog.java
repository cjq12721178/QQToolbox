package com.cjq.tool.qbox.ui.dialog;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cjq.tool.qbox.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2017/11/2.
 */

public class FilterDialog extends BaseDialog<FilterDialog.Decorator> {

    private static final String ARGUMENT_KEY_FILTER_TYPES = "in_filter_types";

    public FilterDialog addFilterType(String label, FilterType.Tag... tags) {
        return addFilterType(label, tags, true);
    }

    public FilterDialog addFilterType(String label, List<FilterType.Tag> tags) {
        if (tags == null) {
            return this;
        }
        FilterType.Tag[] copy = new FilterType.Tag[tags.size()];
        return addFilterType(label, tags.toArray(copy), false);
    }

    private FilterDialog addFilterType(String label, FilterType.Tag[] tags, boolean needClone) {
        checkLabel(label);
        checkTags(tags);
        ArrayList<FilterType> filterTypes = getArguments().getParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES);
        if (filterTypes == null) {
            filterTypes = new ArrayList<>();
            getArguments().putParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES, filterTypes);
        }
        filterTypes.add(new FilterType(label, needClone ? tags.clone() : tags));
        return this;
    }

    private void checkLabel(String label) {
        if (TextUtils.isEmpty(label)) {
            throw new IllegalArgumentException("label may not be empty");
        }
    }

    private void checkTags(FilterType.Tag[] tags) {
        if (tags == null) {
            throw new NullPointerException("tags may not be null");
        }
        for (int i = 0, n = tags.length;i < n;++i) {
            if (TextUtils.isEmpty(tags[i].getContent())) {
                throw new IllegalArgumentException("tag " + i + " content may not be empty");
            }
        }
    }

    public void clearFilterTypes() {
        getArguments().putParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES, null);
    }

    @Override
    protected void onSetContentView(View contentView, Decorator decorator, @Nullable Bundle savedInstanceState) {
        ConstraintLayout clFilter = (ConstraintLayout) contentView.findViewById(R.id.cl_filter);
        Bundle arguments = getArguments();
        ArrayList<FilterType> filterTypes = arguments.getParcelableArrayList(ARGUMENT_KEY_FILTER_TYPES);
        if (filterTypes != null) {
            Context context = getContext();
            Resources resources = context.getResources();
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(clFilter);
            float textSize = resources.getDimensionPixelSize(decorator.getFilterTypeTextSize());
            int labelWidth = resources.getDimensionPixelSize(decorator.getFilterLabelWidth());
            int tagMargin = resources.getDimensionPixelSize(decorator.getFilterTagMargin());
            Drawable tagBackground = ContextCompat.getDrawable(context, decorator.getFilterTagBackground());
            int currTvLabelId, prevTvLabelId = 0, currViewId, prevViewId = 0;
            clFilter.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            final int maxWidth = clFilter.getMeasuredWidth();
            int headWidth = 0, noLimitTagWidth = 0;
            boolean hasLimit;
            for (int i = 0;i < filterTypes.size();++i) {
                FilterType filterType = filterTypes.get(i);
                //添加label view
                TextView tvLabel = new TextView(context);
                currTvLabelId = View.generateViewId();
                tvLabel.setId(currTvLabelId);
                tvLabel.setText(filterType.getLabel());
                tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                tvLabel.setLines(1);
                tvLabel.setEllipsize(TextUtils.TruncateAt.MARQUEE);
                clFilter.addView(tvLabel);
                constraintSet.constrainWidth(currTvLabelId, labelWidth);
                constraintSet.constrainHeight(currTvLabelId, ConstraintSet.WRAP_CONTENT);
                constraintSet.setMargin(currTvLabelId,
                        ConstraintSet.START,
                        tagMargin);
                constraintSet.connect(currTvLabelId,
                        ConstraintSet.START,
                        ConstraintSet.PARENT_ID,
                        ConstraintSet.START);
                if (prevTvLabelId == 0) {
                    constraintSet.connect(currTvLabelId,
                            ConstraintSet.TOP,
                            ConstraintSet.PARENT_ID,
                            ConstraintSet.TOP);
                } else {
                    constraintSet.connect(currTvLabelId,
                            ConstraintSet.TOP,
                            prevTvLabelId,
                            ConstraintSet.BOTTOM);
                }
                prevTvLabelId = currTvLabelId;
                //添加“全部”按钮
                hasLimit = filterType.hasLimit();
                currViewId = View.generateViewId();
                Button btnNoLimit = new Button(context);
                btnNoLimit.setId(currViewId);
                btnNoLimit.setText(R.string.qbox_no_limit);
                btnNoLimit.setTextSize(textSize);
                btnNoLimit.setBackground(tagBackground);
                btnNoLimit.setSelected(!hasLimit);
                btnNoLimit.setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                if (noLimitTagWidth == 0) {
                    btnNoLimit.measure(0, 0);
                    noLimitTagWidth = btnNoLimit.getMeasuredWidth();
                }
                clFilter.addView(btnNoLimit);
                constraintSet.setMargin(currViewId,
                        ConstraintSet.START,
                        tagMargin);
                headWidth = labelWidth + noLimitTagWidth + tagMargin + tagMargin;

            }
        }
    }

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int onSetContentLayout() {
            return R.layout.qbox_dialog_content_filter;
        }

        @Override
        public void reset() {
            super.reset();
            setFilterTypeTextSize(R.dimen.qbox_size_text_dialog_view);
            setFilterLabelWidth(R.dimen.qbox_dialog_filter_label_width);
            setFilterTagBackground(R.drawable.qbox_shape_button_background_unselected);
        }

        public void setFilterTypeTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("fdp_text_size", textSizeRes);
        }

        public @DimenRes int getFilterTypeTextSize() {
            return mParameters.getInt("fdp_text_size");
        }

        public void setFilterLabelWidth(@DimenRes int widthRes) {
            mParameters.putInt("fdp_label_width", widthRes);
        }

        public @DimenRes int getFilterLabelWidth() {
            return mParameters.getInt("fdp_label_width");
        }

        public void setFilterTagBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("fdp_tag_bg", backgroundRes);
        }

        public @DrawableRes int getFilterTagBackground() {
            return mParameters.getInt("fdp_tag_bg");
        }

        public void setFilterTagMargin(@DimenRes int marginRes) {
            mParameters.putInt("fdp_tag_margin", marginRes);
        }

        public @DimenRes int getFilterTagMargin() {
            return mParameters.getInt("fdp_tag_margin");
        }
    }

    public static class FilterType implements Parcelable {

        private final String mLabel;
        private final Tag[] mTags;

        protected FilterType(Parcel in) {
            mLabel = in.readString();
            mTags = in.createTypedArray(Tag.CREATOR);
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

        public FilterType(String label, Tag[] tags) {
            mLabel = label;
            mTags = tags;
        }

        public String getLabel() {
            return mLabel;
        }

        public Tag[] getTags() {
            return mTags;
        }

        public boolean hasLimit() {
            for (Tag tag :
                    mTags) {
                if (!tag.isSelected()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(mLabel);
            dest.writeTypedArray(mTags, flags);
        }

        public static class Tag implements Parcelable {

            private final String mContent;
            private boolean mSelected;

            public String getContent() {
                return mContent;
            }

            public boolean isSelected() {
                return mSelected;
            }

            public void setSelected(boolean selected) {
                mSelected = selected;
            }

            public Tag(String content, boolean selected) {
                mContent = content;
                mSelected = selected;
            }

            protected Tag(Parcel in) {
                mContent = in.readString();
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

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(mContent);
                dest.writeByte((byte) (mSelected ? 1 : 0));
            }
        }
    }
}
