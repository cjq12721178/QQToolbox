package com.cjq.lib.weisi.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;


import com.cjq.lib.weisi.util.SimpleCustomClassParcel;
import com.wsn.lib.wsb.util.ExpandComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2018/5/25.
 */

public class Storage<E> implements Parcelable {

    //private static final int VAL_PARCELABLE = 0;
    //private static final int VAL_CLASS_NAME = 1;

    private final List<E> mElements = new ArrayList<>();
    private final FilterCollection<E> mFilters;
    private ElementsProvider<E> mElementsProvider;
    private Sorter<E> mSorter;
    private boolean mAscending = true;

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
        return mElements.get(getOrderPosition(position));
    }
    
    private int getOrderPosition(int position) {
        return mAscending
                ? position
                : size() - 1 - position;
    }

    public void setSorter(Sorter<E> sorter, boolean ascending) {
        setSorter(sorter, ascending, null);
    }

    public void setSorter(Sorter<E> sorter, boolean ascending, OnSortChangeListener<E> listener) {
        setSorter(sorter, ascending, listener, listener != null);
    }

    public void setSorter(Sorter<E> sorter, boolean ascending, OnSortChangeListener<E> listener, boolean commit) {
        if (mSorter != sorter) {
            mSorter = sorter;
            mAscending = ascending;
            if (commit) {
                resort(listener);
            }
        } else if (mAscending != ascending) {
            mAscending = ascending;
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
            listener.onSortChange(mSorter, mAscending);
        }
    }

    public void addFilter(int id, Filter<E> filter) {
        addFilter(id, filter, null);
    }

    public void addFilter(int id, Filter<E> filter, OnFilterChangeListener listener) {
        addFilter(id, filter, listener, listener != null);
    }

    public void addFilter(int id, Filter<E> filter, OnFilterChangeListener listener, boolean commit) {
        int filterSize = mFilters.size();
        mFilters.put(id, filter);
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

    public void removeFilter(int filterId) {
        removeFilter(filterId, null);
    }

    public void removeFilter(int filterId, OnFilterChangeListener listener) {
        removeFilter(filterId, listener, listener != null);
    }

    public void removeFilter(int filterId, OnFilterChangeListener listener, boolean commit) {
        int filterSize = mFilters.size();
        mFilters.remove(filterId);
        if (commit && filterSize != mFilters.size()) {
            reFiltrate(listener);
        }
    }

    public void clearFilters() {
        clearFilters(null);
    }

    public void clearFilters(OnFilterChangeListener listener) {
        clearFilters(listener, listener != null);
    }

    public void clearFilters(OnFilterChangeListener listener, boolean commit) {
        int filterSize = mFilters.size();
        mFilters.clear();
        if (commit && filterSize != mFilters.size()) {
            reFiltrate(listener);
        }
    }

    public Filter<E> getFilter(int filterId) {
        return mFilters.get(filterId);
    }

    public void refresh(OnSortChangeListener<E> onSortChangeListener,
                        OnFilterChangeListener onFilterChangeListener) {
        int previousSize = reFiltrate(null);
        resort(null);
        notifySortChangeListener(onSortChangeListener);
        notifyFilterChangeListener(onFilterChangeListener, previousSize);
    }

    public int add(@NonNull E e) {
        int position;
        if (mFilters.match(e)) {
            if (mSorter != null) {
                position = mSorter.add(mElements, e);
            } else {
                if (mElements.add(e)) {
                    position = size() - 1;
                } else {
                    position = -1;
                }
            }
        } else {
            position = -1;
        }
        return position != -1 ? getOrderPosition(position) : -1;
    }

    public int find(@NonNull E e) {
        int position;
        if (mFilters.match(e)) {
            if (mSorter != null) {
                position = mSorter.find(mElements, e);
            } else {
                position = mElements.indexOf(e);
            }
        } else {
            position = -1;
        }
        return position != -1 ? getOrderPosition(position) : -1;
    }

    public <T> int find(@NonNull T t, @NonNull ExpandComparator<E, T> comparator) {
        int i = 0, count = size();
        for (;i < count;++i) {
            if (comparator.compare(mElements.get(i), t) == 0) {
                break;
            }
        }
        return i == count ? -1 : getOrderPosition(i);
    }

//    public <T> int find(@NonNull T t, @NonNull Comparator<E, T> comparator) {
//        int i = 0, count = size();
//        for (;i < count;++i) {
//            if (comparator.equals(mElements.get(i), t)) {
//                break;
//            }
//        }
//        return i == count ? -1 : getOrderPosition(i);
//    }

    public Sorter<E> getSorter() {
        return mSorter;
    }

    public boolean isAscending() {
        return mAscending;
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
        dest.writeByte(mAscending ? (byte) 1 : (byte) 0);
    }

    protected Storage(Parcel in) {
        mFilters = in.readParcelable(getClass().getClassLoader());
        mElementsProvider = SimpleCustomClassParcel.readFromParcel(in, this);
        mSorter = SimpleCustomClassParcel.readFromParcel(in, this);
        mAscending = in.readByte() != 0;
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

//    public interface Comparator<E, T> {
//        boolean equals(@NonNull E e, @NonNull T t);
//    }
}
