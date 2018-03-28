package com.cjq.lib.weisi.iot;

import com.cjq.lib.weisi.util.ExpandCollections;

import java.util.ArrayList;
import java.util.List;

import static com.cjq.lib.weisi.iot.Value.SEARCH_HELPER;

/**
 * Created by CJQ on 2018/3/23.
 */

public abstract class HistoryValueContainer<V extends Value> extends BaseValueContainer<V> {

    private List<V> mValues = new ArrayList<>();

    @Override
    protected int onAddValue(long timestamp) {
        synchronized (this) {
            V v;
            int size = mValues.size();
            if (size > 0) {
                v = mValues.get(size - 1);
                if (timestamp > v.getTimestamp()) {
                    v = createValue(timestamp);
                    mValues.add(v);
                    return size;
                } else if (timestamp < v.getTimestamp()) {
                    int position = findValuePosition(timestamp);
                    if (position < 0) {
                        v = createValue(timestamp);
                        mValues.add(-position - 1, v);
                    }
                    return -position - 1;
                }
                return -size;
            } else {
                v = createValue(timestamp);
                mValues.add(v);
                return 0;
            }
        }
    }

    @Override
    public int interpretAddResult(int addMethodReturnValue) {
        if (addMethodReturnValue >= 0) {
            return NEW_VALUE_ADDED;
        }
        return addMethodReturnValue == ADD_FAILED_RETURN_VALUE
                ? ADD_VALUE_FAILED
                : VALUE_UPDATED;
    }

    @Override
    public int size() {
        return mValues.size();
    }

    @Override
    public boolean empty() {
        return mValues.isEmpty();
    }

    @Override
    public V getValue(int position) {
        return mValues.get(position);
    }

    @Override
    public int findValuePosition(long timestamp) {
        synchronized (mValues) {
            return ExpandCollections.binarySearch(mValues,
                    timestamp,
                    SEARCH_HELPER);
        }
    }
}
