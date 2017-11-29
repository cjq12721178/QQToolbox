package com.cjq.lib.weisi.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by CJQ on 2017/11/3.
 */

public abstract class ValueContainer<V extends ValueContainer.Value> {

    //分为两种情况，一是在传感器中添加数据的时候，未能按照
    //dataTypeValue和dataTypeValueIndex找到相应measurement;
    //二是在添加动态数据的时候，所要加入的数据早于所有已有数据，
    //这意味着该次数据添加毫无意义
    public static final int ADD_VALUE_FAILED = 0;
    //当添加数据时在数据集中新增一条数据
    public static final int NEW_VALUE_ADDED = 1;
    //添加数据时遇到与已有数据集中具有相同timestamp的数据，
    //则对该条数据的其他信息进行更新
    public static final int VALUE_UPDATED = 2;
    //在动态添加数据时，由于采用了循环数组以节省内存空间，
    //当需要新增数据而数据集规模已达预计最大时，取出最早数据，
    //并将新数据插入相应位置
    public static final int LOOP_VALUE_ADDED = 3;

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
            mDynamicValues = new ArrayList<>();
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

    public V getEarliestValue() {
        return mHistoryValues.size() > 0
                ? mHistoryValues.get(0)
                : null;
    }

    public V getLatestValue() {
        int size = mHistoryValues.size();
        if (size == 0) {
            return null;
        }
        return mHistoryValues.get(size - 1);
    }

//    public boolean canCacheDynamicValue() {
//        return MAX_DYNAMIC_VALUE_SIZE > 0;
//    }

    //注意：该方法不检查index范围
    public V getHistoryValue(int index) {
        return mHistoryValues.get(index);
    }

    public int getHistoryValueSize() {
        return mHistoryValues.size();
    }

    public V getDynamicValue(int index) {
        int pos = mDynamicValueHead + index - MAX_DYNAMIC_VALUE_SIZE;
        return pos > 0
                ? mDynamicValues.get(pos)
                : mDynamicValues.get(mDynamicValueHead + index);
    }

    public int getDynamicValueSize() {
        return mDynamicValues.size();
    }

    //若有新的数据添加，返回position
    //若只是原有数据的更新，返回-position-1
    //注意和Collections.binarySearch()返回值相反
    protected synchronized int addHistoryValue(long timestamp) {
        V v;
        int size = mHistoryValues.size();
        if (size > 0) {
            v = mHistoryValues.get(size - 1);
            if (timestamp > v.getTimeStamp()) {
                v = onCreateValue(timestamp);
                mHistoryValues.add(v);
                return size;
            } else if (timestamp < v.getTimeStamp()) {
                int position = findHistoryValue(timestamp);
                if (position < 0) {
                    v = onCreateValue(timestamp);
                    mHistoryValues.add(-position - 1, v);
                }
                return -position - 1;
            }
            //-(size - 1) - 1
            return -size;
        } else {
            v = onCreateValue(timestamp);
            mHistoryValues.add(v);
            return 0;
        }
    }

    protected synchronized int addDynamicValue(long timestamp) {
        V v;
        int size = mDynamicValues.size();
        if (size < MAX_DYNAMIC_VALUE_SIZE) {
            for (int i = size - 1;i >= 0;--i) {
                v = mDynamicValues.get(i);
                if (timestamp > v.mTimeStamp) {
                    v = onCreateValue(timestamp);
                    mDynamicValues.add(i + 1, v);
                    return i + 1;
                } else if (timestamp == v.mTimeStamp) {
                    return -i - 1;
                }
            }
            v = onCreateValue(timestamp);
            mDynamicValues.add(0, v);
            return 0;
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
                    int position = MAX_DYNAMIC_VALUE_SIZE - (mDynamicValueHead - i);
                    if (++mDynamicValueHead == MAX_DYNAMIC_VALUE_SIZE) {
                        mDynamicValueHead = 0;
                    }
                    return position;
                } else if (timestamp == v.mTimeStamp) {
                    return -(MAX_DYNAMIC_VALUE_SIZE - 1
                            - (mDynamicValueHead - 1 - i)) - 1;
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
                    int position = i - mDynamicValueHead;
                    if (++mDynamicValueHead == MAX_DYNAMIC_VALUE_SIZE) {
                        mDynamicValueHead = 0;
                    }
                    return position;
                } else if (timestamp == v.mTimeStamp) {
                    return -(i - mDynamicValueHead) - 1;
                }
            }
            return MAX_DYNAMIC_VALUE_SIZE;
        }
    }

    public int interpretAddResult(int addMethodReturnValue, boolean isRealTime) {
        if (addMethodReturnValue < 0) {
            return VALUE_UPDATED;
        } else if (addMethodReturnValue == MAX_DYNAMIC_VALUE_SIZE
                && isRealTime) {
            return ADD_VALUE_FAILED;
        } else if (mDynamicValues.size() == MAX_DYNAMIC_VALUE_SIZE
                && isRealTime) {
            //此处有一种情况本应属于NEW_VALUE_ADDED，
            //但由于区别困难所以也放在了LOOP_VALUE_ADDED一类：
            //即当动态添加数据集规模正好达到最大时的那条数据
            return LOOP_VALUE_ADDED;
        } else {
            return NEW_VALUE_ADDED;
        }
    }

    //若possiblePosition>=0，则在possiblePosition附近寻找时间戳等于timestamp的Value
    //若possiblePosition<0，则在所有历史数据里面寻找
    //没有找到返回null
    public V findHistoryValue(int possiblePosition, long timestamp) {
        V value = null;
        if (possiblePosition >= 0) {
            for (int i = possiblePosition,
                 j = possiblePosition,
                 n = getHistoryValueSize();
                 i < n && i >= 0;) {
                value = getHistoryValue(i);
                long valueTimestamp = value.getTimeStamp();
                if (valueTimestamp == timestamp) {
                    break;
                } else if (valueTimestamp > timestamp) {
                    if (i < j) {
                        value = null;
                        break;
                    }
                    j = i--;
                } else {
                    if (i > j) {
                        value = null;
                        break;
                    }
                    j = i++;
                }
            }
        } else {
            int position = findHistoryValue(timestamp);
            if (position >= 0) {
                value = mHistoryValues.get(position);
            }
        }
        return value;
    }

    private int findHistoryValue(long timestamp) {
        synchronized (Value.VALUE_COMPARER) {
            Value.VALUE_COMPARER.mTimeStamp = timestamp;
            return Collections.binarySearch(mHistoryValues,
                    Value.VALUE_COMPARER,
                    Value.VALUE_COMPARATOR);
        }
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
