package com.cjq.lib.weisi.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.util.SimpleCustomClassParcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by CJQ on 2018/5/25.
 */

public class FilterCollection<E> implements Filter<E>, Parcelable {

    private final List<Filter<E>> mFilters = new ArrayList<>();

    public FilterCollection add(Filter<E> filter) {
        if (filter != null && !mFilters.contains(filter)) {
            mFilters.add(filter);
        }
        return this;
    }

    public void clear() {
        mFilters.clear();
    }

    public void remove(Filter<E> filter) {
        mFilters.remove(filter);
    }

    public int size() {
        return mFilters.size();
    }

    public List<Filter<E>> getFilters() {
        return Collections.unmodifiableList(mFilters);
    }

    @Override
    public boolean match(@NonNull E e) {
        for (int i = 0, size = mFilters.size();i < size;++i) {
            if (!mFilters.get(i).match(e)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        SimpleCustomClassParcel.writeToParcel(dest, mFilters, flags);
//        int n = mFilters.size();
//        dest.writeInt(n);
//        Filter<E> filter;
//        for (int i = 0;i < n;++i) {
//            filter = mFilters.get(i);
//            if (filter instanceof Parcelable) {
//                dest.writeInt(VAL_PARCELABLE);
//                dest.writeParcelable((Parcelable) filter, 0);
//            } else {
//                dest.writeInt(VAL_CLASS_NAME);
//                dest.writeString(filter.getClass().getName());
//            }
//        }
    }

    public FilterCollection() {
    }

    protected FilterCollection(Parcel in) {
        SimpleCustomClassParcel.readFromParcel(in, mFilters, this);
//        int n = in.readInt();
//        for (int i = 0;i < n;++i) {
//            int val = in.readInt();
//            if (val == VAL_PARCELABLE) {
//                mFilters.add((Filter<E>) in.readParcelable(getClass().getClassLoader()));
//            } else {
//                try {
//                    mFilters.add((Filter<E>) Class.forName(in.readString()).newInstance());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    public static final Creator<FilterCollection> CREATOR = new Creator<FilterCollection>() {
        @Override
        public FilterCollection createFromParcel(Parcel source) {
            return new FilterCollection(source);
        }

        @Override
        public FilterCollection[] newArray(int size) {
            return new FilterCollection[size];
        }
    };
}
