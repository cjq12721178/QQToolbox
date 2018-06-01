package com.cjq.lib.weisi.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.util.SimpleCustomClassParcel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2018/5/25.
 */

public class Storage<E> implements Parcelable {

    private static final int VAL_PARCELABLE = 0;
    private static final int VAL_CLASS_NAME = 1;

    private final List<E> mElements = new ArrayList<>();
    private final FilterCollection<E> mFilters;
    private ElementsProvider<E> mElementsProvider;
    private Sorter<E> mSorter;
    private boolean mDescend;

    public Storage(@NonNull ElementsProvider<E> provider) {
        mFilters = new FilterCollection<>();
        bindElementsProvider(provider);
    }

    public void bindElementsProvider(@NonNull ElementsProvider<E> provider) {
        bindElementsProvider(provider, null, null);
    }

    public void bindElementsProvider(@NonNull ElementsProvider<E> provider,
                                     OnSortChangeListener<E> onSortChangeListener,
                                     OnFilterChangeListener onFilterChangeListener) {
        bindElementsProvider(provider, onSortChangeListener, onFilterChangeListener,
                onSortChangeListener != null && onFilterChangeListener != null);
    }

    public void bindElementsProvider(@NonNull ElementsProvider<E> provider,
                                     OnSortChangeListener<E> onSortChangeListener,
                                     OnFilterChangeListener onFilterChangeListener,
                                     boolean isRefresh) {
        if (mElementsProvider != provider) {
            mElementsProvider = provider;
            if (isRefresh) {
                refresh(onSortChangeListener, onFilterChangeListener);
            }
        }
    }

    public int size() {
        return mElements.size();
    }

    public E get(int position) {
        return mElements.get(getPhysicalPosition(position));
    }

    //逻辑位置转物理（指list中的）位置
    private int getPhysicalPosition(int logicalPosition) {
        return mDescend
                ? size() - 1 - logicalPosition
                : logicalPosition;
    }

    public void setSorter(Sorter<E> sorter, boolean descend) {
        setSorter(sorter, descend, null);
    }

    public void setSorter(Sorter<E> sorter, boolean descend, OnSortChangeListener<E> listener) {
        setSorter(sorter, descend, listener, listener != null);
    }

    public void setSorter(Sorter<E> sorter, boolean descend, OnSortChangeListener<E> listener, boolean commit) {
        if (mSorter != sorter) {
            mSorter = sorter;
            mDescend = descend;
            if (commit) {
                resort(listener);
            }
        } else if (mDescend != descend) {
            mDescend = descend;
            if (commit) {
                notifySortChangeListener(listener);
            }
        }
    }

    public void resort(OnSortChangeListener<E> listener) {
        if (mSorter != null) {
            mSorter.sort(mElements);
            notifySortChangeListener(listener);
        }
    }

    private void notifySortChangeListener(OnSortChangeListener<E> listener) {
        if (listener != null) {
            listener.onSortChange(mSorter, mDescend);
        }
    }

    public void addFilter(Filter<E> filter) {
        addFilter(filter, null);
    }

    public void addFilter(Filter<E> filter, OnFilterChangeListener listener) {
        addFilter(filter, listener, listener != null);
    }

    public void addFilter(Filter<E> filter, OnFilterChangeListener listener, boolean commit) {
        int filterSize = mFilters.size();
        mFilters.add(filter);
        if (commit && filterSize != mFilters.size()) {
            reFiltrate(listener);
        }
    }

    public int reFiltrate(OnFilterChangeListener listener) {
        int previousSize = size();
        mElements.clear();
        mElementsProvider.onProvideElements(mElements, mFilters);
        notifyFilterChangeListener(listener, previousSize);
        return previousSize;
    }

    private void notifyFilterChangeListener(OnFilterChangeListener listener, int previousSize) {
        if (listener != null) {
            listener.onSizeChange(previousSize, size());
        }
    }

    public void removeFilter(Filter<E> filter) {
        removeFilter(filter, null);
    }

    public void removeFilter(Filter<E> filter, OnFilterChangeListener listener) {
        removeFilter(filter, listener, listener != null);
    }

    public void removeFilter(Filter<E> filter, OnFilterChangeListener listener, boolean commit) {
        int filterSize = mFilters.size();
        mFilters.remove(filter);
        if (commit && filterSize != mFilters.size()) {
            reFiltrate(listener);
        }
    }

    public void refresh(OnSortChangeListener<E> onSortChangeListener,
                        OnFilterChangeListener onFilterChangeListener) {
        int previousSize = reFiltrate(null);
        resort(null);
        notifySortChangeListener(onSortChangeListener);
        notifyFilterChangeListener(onFilterChangeListener, previousSize);
    }

    public int add(E e) {
        int position;
        if (mFilters.match(e)) {
            if (mSorter != null) {
                position = mSorter.add(mElements, e);
            } else {
                if (mElements.add(e)) {
                    position = getPhysicalPosition(size() - 1);
                } else {
                    position = -1;
                }
            }
        } else {
            position = -1;
        }
        return position;
    }

    public int find(E e) {
        if (mSorter != null) {
            return mSorter.find(mElements, e);
        } else {
            return mElements.indexOf(e);
        }
    }

    public Sorter<E> getSorter() {
        return mSorter;
    }

    public boolean isDescend() {
        return mDescend;
    }

    public List<Filter<E>> getFilters() {
        return mFilters.getFilters();
    }

    public interface ElementsProvider<E> {
        void onProvideElements(@NonNull List<E> elements, FilterCollection<E> filters);
    }

    public interface OnSortChangeListener<E> {
        void onSortChange(Sorter<E> newSorter, boolean newOrder);
    }

    public interface OnFilterChangeListener {
        void onSizeChange(int previousSize, int currentSize);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mFilters, flags);
        SimpleCustomClassParcel.writeToParcel(dest, mElementsProvider, flags);
        SimpleCustomClassParcel.writeToParcel(dest, mSorter, flags);
        dest.writeByte(mDescend ? (byte) 1 : (byte) 0);
    }

    protected Storage(Parcel in) {
        mFilters = in.readParcelable(getClass().getClassLoader());
        mElementsProvider = SimpleCustomClassParcel.readFromParcel(in, this);
        mSorter = SimpleCustomClassParcel.readFromParcel(in, this);
        mDescend = in.readByte() != 0;
    }

    public static final Creator<Storage> CREATOR = new Creator<Storage>() {
        @Override
        public Storage createFromParcel(Parcel source) {
            return new Storage(source);
        }

        @Override
        public Storage[] newArray(int size) {
            return new Storage[size];
        }
    };
}
