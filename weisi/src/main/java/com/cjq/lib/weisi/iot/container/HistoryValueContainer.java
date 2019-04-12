package com.cjq.lib.weisi.iot.container;

import com.wsn.lib.wsb.util.ExpandCollections;

import java.util.ArrayList;
import java.util.List;

import static com.cjq.lib.weisi.iot.container.Value.SEARCH_HELPER;

/**
 * Created by CJQ on 2018/3/23.
 */

public abstract class HistoryValueContainer<V extends Value> extends MultipleValueContainer<V> {

    private final List<V> mValues = new ArrayList<>();

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
                        mValues.add(decodePosition(position), v);
                    }
                    return encodePosition(position);
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
    public int interpretAddResult(int logicalPosition) {
        if (logicalPosition >= 0) {
            return NEW_VALUE_ADDED;
        }
        return logicalPosition == ADD_FAILED_RETURN_VALUE
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
    public V getValue(int physicalPosition) {
        return mValues.get(physicalPosition);
    }

    @Override
    public int findValuePosition(long timestamp) {
        synchronized (this) {
            return ExpandCollections.binarySearch(mValues,
                    timestamp,
                    SEARCH_HELPER);
        }
    }
}
