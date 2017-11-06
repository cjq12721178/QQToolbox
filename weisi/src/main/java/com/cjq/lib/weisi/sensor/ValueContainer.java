package com.cjq.lib.weisi.sensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2017/11/3.
 */

public abstract class ValueContainer<V extends ValueContainer.Value> {

    protected final int MAX_SIZE;
    protected final V mRealTimeValue;
    private final List<V> mHistoryValues;
    private int mHead;
    protected String mName;

    public ValueContainer(int maxSize) {
        MAX_SIZE = maxSize;
        if (maxSize > 0) {
            mHistoryValues = new ArrayList<>(maxSize);
        } else {
            mHistoryValues = new ArrayList<>();
        }
        mRealTimeValue = onCreateValue(0);
        mHead = 0;
    }

    protected abstract V onCreateValue(long timestamp);

    public String getGeneralName() {
        return mName;
    }

    public V getRealTimeValue() {
        return mRealTimeValue;
    }

    //注意：该方法不检查index范围
    public V getHistoryValue(int index) {
        if (MAX_SIZE <= 0) {
            return mHistoryValues.get(index);
        }
        int pos = mHead + index - MAX_SIZE;
        return pos > 0
                ? mHistoryValues.get(pos)
                : mHistoryValues.get(mHead + index);
    }

    public int getHistoryValueSize() {
        return mHistoryValues.size();
    }

    public synchronized V addHistoryValue(long timestamp) {
        V v;
        int size = mHistoryValues.size();
        if (MAX_SIZE <= 0 || size == 0) {
            v = onCreateValue(timestamp);
            mHistoryValues.add(v);
        } else {
            if (size == MAX_SIZE) {
                for (int i = mHead - 1;i >= 0;--i) {
                    v = mHistoryValues.get(i);
                    if (timestamp > v.mTimeStamp) {
                        v = mHistoryValues.get(mHead);
                        if (i < mHead - 1) {
                            System.arraycopy(mHistoryValues,
                                    i + 1,
                                    mHistoryValues,
                                    i + 2,
                                    mHead - 1 - (i + 1) + 1);
                            mHistoryValues.set(i + 1, v);
                        }
                        v.mTimeStamp = timestamp;
                        if (++mHead == MAX_SIZE) {
                            mHead = 0;
                        }
                        return v;
                    } else if (timestamp == v.mTimeStamp) {
                        return v;
                    }
                }
                for (int i = MAX_SIZE - 1;i >= mHead;--i) {
                    v = mHistoryValues.get(i);
                    if (timestamp > v.mTimeStamp) {
                        v = mHistoryValues.get(mHead);
                        System.arraycopy(mHistoryValues,
                                mHead + 1,
                                mHistoryValues,
                                mHead,
                                i - mHead);
                        mHistoryValues.set(i + 1, v);
                        if (++mHead == MAX_SIZE) {
                            mHead = 0;
                        }
                        return v;
                    } else if (timestamp == v.mTimeStamp) {
                        return v;
                    }
                }
                v = null;
            } else {
                for (int i = size - 1;i >= 0;--i) {
                    v = mHistoryValues.get(i);
                    if (timestamp > v.mTimeStamp) {
                        v = onCreateValue(timestamp);
                        mHistoryValues.add(i + 1, v);
                        return v;
                    } else if (timestamp == v.mTimeStamp) {
                        return v;
                    }
                }
                v = onCreateValue(timestamp);
                mHistoryValues.add(0, v);
            }
        }
        return v;
    }

    public static class Value {

        long mTimeStamp;

        public Value(long timeStamp) {
            mTimeStamp = timeStamp;
        }

        public long getTimeStamp() {
            return mTimeStamp;
        }
    }
}
