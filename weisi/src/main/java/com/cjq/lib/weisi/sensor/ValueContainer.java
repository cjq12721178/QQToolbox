package com.cjq.lib.weisi.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by CJQ on 2017/11/3.
 */

public abstract class ValueContainer<V extends ValueContainer.Value> {

    protected final int MAX_DYNAMIC_VALUE_SIZE;
    protected final V mRealTimeValue;
    //用于缓存实时数据
    private final List<V> mDynamicValues;
    private final List<V> mHistoryValues;
    private int mDynamicValueHead;
    protected String mName;

    public ValueContainer(int maxDynamicValueSize) {
        MAX_DYNAMIC_VALUE_SIZE = maxDynamicValueSize;
        mRealTimeValue = onCreateValue(0);
        if (maxDynamicValueSize > 0) {
            mDynamicValues = new ArrayList<>(maxDynamicValueSize);
        } else {
            mDynamicValues = null;
        }
        mHistoryValues = new ArrayList<>();
        mDynamicValueHead = 0;
    }

    protected abstract V onCreateValue(long timestamp);

    public String getGeneralName() {
        return mName;
    }

    public V getRealTimeValue() {
        return mRealTimeValue;
    }

    public V getLatestValue() {
        int size = mHistoryValues.size();
        if (size == 0) {
            return null;
        }
        return mHistoryValues.get(size - 1);
    }

    public boolean canCacheDynamicValue() {
        return MAX_DYNAMIC_VALUE_SIZE > 0;
    }

    //注意：该方法不检查index范围
    public V getHistoryValue(int index) {
//        if (canCacheDynamicValue()) {
//            return mHistoryValues.get(index);
//        }
//        int pos = mDynamicValueHead + index - MAX_DYNAMIC_VALUE_SIZE;
//        return pos > 0
//                ? mHistoryValues.get(pos)
//                : mHistoryValues.get(mDynamicValueHead + index);
        return mHistoryValues.get(index);
    }

    public int getHistoryValueSize() {
        return mHistoryValues.size();
    }

    public V getDynamicValue(int index) {
        if (!canCacheDynamicValue()) {
            return null;
        }
        int pos = mDynamicValueHead + index - MAX_DYNAMIC_VALUE_SIZE;
        return pos > 0
                ? mDynamicValues.get(pos)
                : mDynamicValues.get(mDynamicValueHead + index);
    }

    public int getDynamicValueSize() {
        if (!canCacheDynamicValue()) {
            return 0;
        }
        return mDynamicValues.size();
    }

    protected synchronized V addHistoryValue(long timestamp) {
        V v;
        int size = mHistoryValues.size();
        if (size > 0) {
            v = mHistoryValues.get(size -1);
            if (timestamp > v.getTimeStamp()) {
                v = onCreateValue(timestamp);
                mHistoryValues.add(v);
            } else if (timestamp < v.getTimeStamp()) {
                synchronized (Value.VALUE_COMPARATOR) {
                    Value.VALUE_COMPARER.mTimeStamp = timestamp;
                    int position = Collections.binarySearch(mHistoryValues,
                            Value.VALUE_COMPARER,
                            Value.VALUE_COMPARATOR);
                    if (position >= 0) {
                        v = mHistoryValues.get(position);
                    } else {
                        v = onCreateValue(timestamp);
                        mHistoryValues.add(-position-1, v);
                    }
                }
            }
        } else {
            v = onCreateValue(timestamp);
            mHistoryValues.add(v);
        }
        return v;
//        V v;
//        for (int i = mHistoryValues.size() - 1;i >= 0;--i) {
//            v = mHistoryValues.get(i);
//            if (timestamp > v.mTimeStamp) {
//                v = onCreateValue(timestamp);
//                mHistoryValues.add(i + 1, v);
//                return v;
//            } else if (timestamp == v.mTimeStamp) {
//                return v;
//            }
//        }
//        v = onCreateValue(timestamp);
//        mHistoryValues.add(0, v);
//        return v;
    }

    protected synchronized V addDynamicValue(long timestamp) {
        if (!canCacheDynamicValue()) {
            return null;
        }
        V v;
        int size = mDynamicValues.size();
        if (size < MAX_DYNAMIC_VALUE_SIZE) {
            for (int i = size - 1;i >= 0;--i) {
                v = mDynamicValues.get(i);
                if (timestamp > v.mTimeStamp) {
                    v = onCreateValue(timestamp);
                    mDynamicValues.add(i + 1, v);
                    return v;
                } else if (timestamp == v.mTimeStamp) {
                    return v;
                }
            }
            v = onCreateValue(timestamp);
            mDynamicValues.add(0, v);
        } else {
            for (int i = mDynamicValueHead - 1; i >= 0; --i) {
                v = mDynamicValues.get(i);
                if (timestamp > v.mTimeStamp) {
                    v = mDynamicValues.get(mDynamicValueHead);
                    if (i < mDynamicValueHead - 1) {
                        System.arraycopy(mDynamicValues,
                                i + 1,
                                mDynamicValues,
                                i + 2,
                                mDynamicValueHead - 1 - (i + 1) + 1);
                        mDynamicValues.set(i + 1, v);
                    }
                    v.mTimeStamp = timestamp;
                    if (++mDynamicValueHead == MAX_DYNAMIC_VALUE_SIZE) {
                        mDynamicValueHead = 0;
                    }
                    return v;
                } else if (timestamp == v.mTimeStamp) {
                    return v;
                }
            }
            for (int i = MAX_DYNAMIC_VALUE_SIZE - 1; i >= mDynamicValueHead; --i) {
                v = mDynamicValues.get(i);
                if (timestamp > v.mTimeStamp) {
                    v = mDynamicValues.get(mDynamicValueHead);
                    System.arraycopy(mDynamicValues,
                            mDynamicValueHead + 1,
                            mDynamicValues,
                            mDynamicValueHead,
                            i - mDynamicValueHead);
                    mDynamicValues.set(i + 1, v);
                    if (++mDynamicValueHead == MAX_DYNAMIC_VALUE_SIZE) {
                        mDynamicValueHead = 0;
                    }
                    return v;
                } else if (timestamp == v.mTimeStamp) {
                    return v;
                }
            }
            v = null;
        }
        return v;
    }

    public static class Value {

        private static final Value VALUE_COMPARER = new Value(0);
        private static final Comparator<Value> VALUE_COMPARATOR = new Comparator<Value>() {
            @Override
            public int compare(Value v1, Value v2) {
                return (v1.mTimeStamp < v2.mTimeStamp)
                        ? -1
                        : ((v1.mTimeStamp == v2.mTimeStamp)
                            ? 0
                            : 1);
            }
        };

        long mTimeStamp;

        public Value(long timeStamp) {
            mTimeStamp = timeStamp;
        }

        public long getTimeStamp() {
            return mTimeStamp;
        }
    }
}
