package com.cjq.lib.weisi.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import com.cjq.lib.weisi.util.SimpleCustomClassParcel;


/**
 * Created by CJQ on 2018/5/25.
 */

public class FilterCollection<E> implements Filter<E>, Parcelable {

    private final SparseArray<Filter<E>> mFilters = new SparseArray<>();

    public FilterCollection<E> put(int filterId, @NonNull Filter<E> filter) {
        mFilters.put(filterId, filter);
        return this;
    }

    public Filter<E> get(int filterId) {
        return mFilters.get(filterId);
    }

    public void clear() {
        mFilters.clear();
    }

    public void remove(int filterId) {
        mFilters.remove(filterId);
    }

    public int size() {
        return mFilters.size();
    }

    @Override
    public boolean match(@NonNull E e) {
        for (int i = 0, size = mFilters.size();i < size;++i) {
            if (!mFilters.valueAt(i).match(e)) {
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
    }

    public FilterCollection() {
    }

    protected FilterCollection(Parcel in) {
        SimpleCustomClassParcel.readFromParcel(in, mFilters, this);
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
