package com.cjq.lib.weisi.iot.container;

import com.wsn.lib.wsb.util.ExpandCollections;

import java.util.ArrayList;
import java.util.List;

import static com.cjq.lib.weisi.iot.container.Value.SEARCH_HELPER;

/**
 * Created by CJQ on 2018/3/16.
 */

public abstract class DynamicValueContainer<V extends Value> extends BaseValueContainer<V> {

    private static final int DEFAULT_MAX_VALUE_SIZE = 50;
    protected final int MAX_VALUE_SIZE;
    private final List<V> mValues;
    private int mValueHead;
    private int mOnceExistValueSize;

    public DynamicValueContainer() {
        this(DEFAULT_MAX_VALUE_SIZE);
    }

    public DynamicValueContainer(int maxValueSize) {
        if (maxValueSize <= 0) {
            throw new IllegalArgumentException("max value size may not less than 0");
        }
        MAX_VALUE_SIZE = maxValueSize;
        mValues = new ArrayList<>(maxValueSize);
        mValueHead = 0;
        mOnceExistValueSize = 0;
    }

    @Override
    protected int onAddValue(long timestamp) {
        synchronized (this) {
            V v;
            int size = mValues.size();
            if (size < MAX_VALUE_SIZE) {
                for (int i = size - 1;i >= 0;--i) {
                    v = mValues.get(i);
                    if (timestamp > v.mTimestamp) {
                        v = createValue(timestamp);
                        mValues.add(i + 1, v);
                        return i + 1;
                    } else if (timestamp == v.mTimestamp) {
                        return encodePosition(i);
                    }
                }
                v = createValue(timestamp);
                mValues.add(0, v);
                return 0;
            } else {
                for (int i = mValueHead - 1; i >= 0; --i) {
                    v = mValues.get(i);
                    if (timestamp > v.mTimestamp) {
                        if (i == mValueHead - 1) {
                            v = mValues.get(mValueHead);
                        } else {
                            v = mValues.remove(mValueHead);
                            mValues.add(i + 1, v);
                        }
                        v.mTimestamp = timestamp;
                        int position = MAX_VALUE_SIZE - (mValueHead - i);
                        increaseDynamicValueHead();
                        return position;
                    } else if (timestamp == v.mTimestamp) {
                        return encodePosition(MAX_VALUE_SIZE - 1
                                - (mValueHead - 1 - i));
                    }
                }
                for (int i = MAX_VALUE_SIZE - 1; i >= mValueHead; --i) {
                    v = mValues.get(i);
                    if (timestamp > v.mTimestamp) {
                        int position = i - mValueHead;
                        if (i == MAX_VALUE_SIZE - 1) {
                            v = mValues.get(mValueHead);
                            increaseDynamicValueHead();
                        } else {
                            v = mValues.remove(mValueHead);
                            mValues.add(i, v);
                            increaseOnceExistValueSize();
                        }
                        v.mTimestamp = timestamp;
                        return position;
                    } else if (timestamp == v.mTimestamp) {
                        return encodePosition(i - mValueHead);
                    }
                }
                return ADD_FAILED_RETURN_VALUE;
            }
        }
    }

    @Override
    public V createValue(long timestamp) {
        increaseOnceExistValueSize();
        return super.createValue(timestamp);
    }

    private void increaseDynamicValueHead() {
        increaseOnceExistValueSize();
        if (++mValueHead == MAX_VALUE_SIZE) {
            mValueHead = 0;
        }
    }

    private void increaseOnceExistValueSize() {
        ++mOnceExistValueSize;
    }

    @Override
    public int interpretAddResult(int logicalPosition) {
        if (logicalPosition < 0) {
            return decodePosition(logicalPosition) >= MAX_VALUE_SIZE
                    ? ADD_VALUE_FAILED
                    : VALUE_UPDATED;
        }
        if (mOnceExistValueSize > MAX_VALUE_SIZE) {
            return LOOP_VALUE_ADDED;
        }
        return NEW_VALUE_ADDED;
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
        int pos = mValueHead + physicalPosition - MAX_VALUE_SIZE;
        return pos >= 0
                ? mValues.get(pos)
                : mValues.get(mValueHead + physicalPosition);
    }

    @Override
    public int findValuePosition(long timestamp) {
        synchronized (this) {
            int position;
            if (mValueHead == 0) {
                return ExpandCollections.binarySearch(mValues,
                        timestamp,
                        SEARCH_HELPER);
            }
            if (timestamp >= mValues.get(0).mTimestamp) {
                position = ExpandCollections.binarySearch(
                        mValues,
                        0,
                        mValueHead - 1,
                        timestamp,
                        SEARCH_HELPER);
                return position >= 0
                        ? MAX_VALUE_SIZE - mValueHead + position
                        : encodePosition(MAX_VALUE_SIZE - mValueHead + decodePosition(position));
            } else {
                position = ExpandCollections.binarySearch(
                        mValues,
                        mValueHead,
                        mValues.size() - 1,
                        timestamp,
                        SEARCH_HELPER);
                return position >= 0
                        ? position - mValueHead
                        : encodePosition(decodePosition(position) - mValueHead);
            }
        }
    }
}
