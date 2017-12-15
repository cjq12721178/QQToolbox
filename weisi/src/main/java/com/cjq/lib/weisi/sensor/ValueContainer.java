package com.cjq.lib.weisi.sensor;

import com.cjq.tool.qbox.util.ExpandCollections;
import com.cjq.tool.qbox.util.ExpandComparator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
    //private final List<V> mHistoryValues;
    private final List<DailyHistoryValuePool<V>> mHistoryValues;
    private int mDynamicValueHead;
    protected String mName;
    private DailyHistoryValuePool<V> mCurrentDailyHistoryValuePool;

    public ValueContainer(int maxDynamicValueSize) {
        MAX_DYNAMIC_VALUE_SIZE = maxDynamicValueSize;
        mRealTimeValue = onCreateValue(0);
        if (maxDynamicValueSize > 0) {
            mDynamicValues = new ArrayList<>(maxDynamicValueSize);
        } else {
            mDynamicValues = new ArrayList<>();
        }
        //mHistoryValues = new ArrayList<>();
        mHistoryValues = new ArrayList<>();
        mDynamicValueHead = 0;
    }

    protected abstract V onCreateValue(long timestamp);

    public String getGeneralName() {
        return mName;
    }

    public void setIntraday(long dateTime) {
        if (mCurrentDailyHistoryValuePool == null || !mCurrentDailyHistoryValuePool.contains(dateTime)) {
            mCurrentDailyHistoryValuePool = getDailyHistoryValuePool(dateTime);
        }
    }

    public long getIntraday() {
        return mCurrentDailyHistoryValuePool != null
                ? mCurrentDailyHistoryValuePool.mIntradayStartTime
                : 0;
    }

    private DailyHistoryValuePool<V> findDailyHistoryValuePool(long dateTime) {
        int position = findDailyHistoryValuePoolPosition(dateTime);
        return position >= 0 ? mHistoryValues.get(position) : null;
    }

    private int findDailyHistoryValuePoolPosition(long dateTime) {
//        synchronized (Value.VALUE_COMPARER) {
//            Value.VALUE_COMPARER.mTimestamp = dateTime;
//            return Collections.binarySearch(mHistoryValues, dateTime, DailyHistoryValuePool.CLASSIFIER);
//        }
        synchronized (mHistoryValues) {
            return ExpandCollections.binarySearch(mHistoryValues, dateTime, DailyHistoryValuePool.CLASSIFIER);
        }
    }

    public V getRealTimeValue() {
        return mRealTimeValue;
    }

    public V getEarliestHistoryValue() {
        return mHistoryValues.size() > 0
                ? getSomedayEarliestHistoryValue(mHistoryValues.get(0))
                : null;
    }

    public V getIntradayEarliestHistoryValue() {
        return getSomedayEarliestHistoryValue(mCurrentDailyHistoryValuePool);
    }

    public V getSomedayEarliestHistoryValue(long somedayMills) {
        return getSomedayEarliestHistoryValue(findDailyHistoryValuePool(somedayMills));
    }

    private V getSomedayEarliestHistoryValue(DailyHistoryValuePool<V> pool) {
        return pool != null
                ? pool.getEarliestValue()
                : null;
    }

    public V getLatestHistoryValue() {
        int size = mHistoryValues.size();
        if (size == 0) {
            return null;
        }
        return getSomedayLatestHistoryValue(mHistoryValues.get(size - 1));
    }

    public V getIntradayLatestHistoryValue() {
        return getSomedayLatestHistoryValue(mCurrentDailyHistoryValuePool);
    }

    public V getSomedayLatestHistoryValue(long somedayMills) {
        return getSomedayLatestHistoryValue(findDailyHistoryValuePool(somedayMills));
    }

    private V getSomedayLatestHistoryValue(DailyHistoryValuePool<V> pool) {
        return pool != null
                ? pool.getLatestValue()
                : null;
    }

    //注意：该方法不检查index范围
    public V getHistoryValue(int index) {
        DailyHistoryValuePool<V> pool;
        for (int i = 0, n = mHistoryValues.size(), size = 0;i < n;++i) {
            pool = mHistoryValues.get(i);
            size += pool.mValues.size();
            if (i < size) {
                return pool.mValues.get(index);
            }
        }
        return null;
    }

    //在调用该方法前需先调用setIntraday方法
    public V getIntradayHistoryValue(int index) {
        return mCurrentDailyHistoryValuePool.mValues.get(index);
    }

    public V getSomedayHistoryValue(long date, int index) {
        int position = findDailyHistoryValuePoolPosition(date);
        return position >= 0
                ? mHistoryValues.get(position).mValues.get(index)
                : null;
    }

    public int getHistoryValueSize() {
        return getHistoryValueSize(mHistoryValues.size());
    }

    //统计positionBefore之前的历史数据数量
    public int getHistoryValueSize(int positionBefore) {
        int size = 0;
        for (int i = 0;i < positionBefore;++i) {
            size += mHistoryValues.get(i).mValues.size();
        }
        return size;
    }

    public int getIntradayHistoryValueSize() {
        return mCurrentDailyHistoryValuePool.mValues.size();
    }

    public int getSomedayHistoryValueSize(long date) {
        int position = findDailyHistoryValuePoolPosition(date);
        return position >= 0
                ? mHistoryValues.get(position).mValues.size()
                : 0;
    }

    public V getDynamicValue(int index) {
        int pos = mDynamicValueHead + index - MAX_DYNAMIC_VALUE_SIZE;
        return pos >= 0
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
        DailyHistoryValuePool<V> pool;
        if (mCurrentDailyHistoryValuePool != null
                && mCurrentDailyHistoryValuePool.contains(timestamp)) {
            pool = mCurrentDailyHistoryValuePool;
        } else {
            pool = getDailyHistoryValuePool(timestamp);
        }
        return pool.addValue(this, timestamp);
//        V v;
//        int size = mHistoryValues.size();
//        if (size > 0) {
//            v = mHistoryValues.get(size - 1);
//            if (timestamp > v.getTimestamp()) {
//                v = onCreateValue(timestamp);
//                mHistoryValues.add(v);
//                return size;
//            } else if (timestamp < v.getTimestamp()) {
//                int position = findHistoryValuePosition(timestamp);
//                if (position < 0) {
//                    v = onCreateValue(timestamp);
//                    mHistoryValues.add(-position - 1, v);
//                }
//                return -position - 1;
//            }
//            //-(size - 1) - 1
//            return -size;
//        } else {
//            v = onCreateValue(timestamp);
//            mHistoryValues.add(v);
//            return 0;
//        }
    }

    private DailyHistoryValuePool<V> getDailyHistoryValuePool(long timestamp) {
        DailyHistoryValuePool<V> pool;
        synchronized (mHistoryValues) {
            int position = findDailyHistoryValuePoolPosition(timestamp);
            if (position >= 0) {
                pool = mHistoryValues.get(position);
            } else {
                pool = new DailyHistoryValuePool<>(timestamp);
                mHistoryValues.add(-position - 1, pool);
            }
        }
        return pool;
    }

    protected int addDynamicValue(long timestamp) {
        synchronized (mDynamicValues) {
            V v;
            int size = mDynamicValues.size();
            if (size < MAX_DYNAMIC_VALUE_SIZE) {
                for (int i = size - 1;i >= 0;--i) {
                    v = mDynamicValues.get(i);
                    if (timestamp > v.mTimestamp) {
                        v = onCreateValue(timestamp);
                        mDynamicValues.add(i + 1, v);
                        return i + 1;
                    } else if (timestamp == v.mTimestamp) {
                        return -i - 1;
                    }
                }
                v = onCreateValue(timestamp);
                mDynamicValues.add(0, v);
                return 0;
            } else {
                for (int i = mDynamicValueHead - 1; i >= 0; --i) {
                    v = mDynamicValues.get(i);
                    if (timestamp > v.mTimestamp) {
                        if (i == mDynamicValueHead - 1) {
                            v = mDynamicValues.get(mDynamicValueHead);
                        } else {
                            v = mDynamicValues.remove(mDynamicValueHead);
                            mDynamicValues.add(i + 1, v);
                        }
//                    v = mDynamicValues.get(mDynamicValueHead);
//                    if (i < mDynamicValueHead - 1) {
//                        System.arraycopy(mDynamicValues,
//                                i + 1,
//                                mDynamicValues,
//                                i + 2,
//                                mDynamicValueHead - 1 - (i + 1) + 1);
//                        mDynamicValues.set(i + 1, v);
//                    }
                        v.mTimestamp = timestamp;
                        int position = MAX_DYNAMIC_VALUE_SIZE - (mDynamicValueHead - i);
                        increaseDynamicValueHead();
                        return position;
                    } else if (timestamp == v.mTimestamp) {
                        return -(MAX_DYNAMIC_VALUE_SIZE - 1
                                - (mDynamicValueHead - 1 - i)) - 1;
                    }
                }
                for (int i = MAX_DYNAMIC_VALUE_SIZE - 1; i >= mDynamicValueHead; --i) {
                    v = mDynamicValues.get(i);
                    if (timestamp > v.mTimestamp) {
                        int position = i - mDynamicValueHead;
                        if (i == MAX_DYNAMIC_VALUE_SIZE - 1) {
                            v = mDynamicValues.get(mDynamicValueHead);
                            increaseDynamicValueHead();
                        } else {
                            v = mDynamicValues.remove(mDynamicValueHead);
                            mDynamicValues.add(i, v);
                        }
                        v.mTimestamp = timestamp;
//                    v = mDynamicValues.get(mDynamicValueHead);
//                    System.arraycopy(mDynamicValues,
//                            mDynamicValueHead + 1,
//                            mDynamicValues,
//                            mDynamicValueHead,
//                            i - mDynamicValueHead);
//                    mDynamicValues.set(i + 1, v);
                        return position;
                    } else if (timestamp == v.mTimestamp) {
                        return -(i - mDynamicValueHead) - 1;
                    }
                }
                return MAX_DYNAMIC_VALUE_SIZE;
            }
        }
    }

    private void increaseDynamicValueHead() {
        if (++mDynamicValueHead == MAX_DYNAMIC_VALUE_SIZE) {
            mDynamicValueHead = 0;
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

    public V findDynamicValue(int possiblePosition, long timestamp) {
        int actualPosition = findDynamicValuePosition(possiblePosition, timestamp);
        return actualPosition >= 0
                ? getDynamicValue(actualPosition)
                : null;
    }

//    public int findDynamicValuePosition(int possiblePosition, long timestamp) {
//        return findValuePosition(true, possiblePosition, timestamp);
//    }

    //若possiblePosition>=0，则在possiblePosition附近寻找时间戳等于timestamp的Value
    //若possiblePosition<0，则在所有数据里面寻找
    //没有找到返回负数
    public int findDynamicValuePosition(int possiblePosition, long timestamp) {
        int size = getDynamicValueSize();
        if (possiblePosition >= 0 && possiblePosition < size) {
            V value;
            for (int currentPosition = possiblePosition,
                 lastPosition = currentPosition;
                 currentPosition < size && currentPosition >= 0;) {
                value = getDynamicValue(currentPosition);
                long valueTimestamp = value.mTimestamp;
                if (valueTimestamp == timestamp) {
                    return currentPosition;
                } else if (valueTimestamp > timestamp) {
                    if (currentPosition > lastPosition) {
                        break;
                    }
                    lastPosition = currentPosition--;
                } else {
                    if (currentPosition < lastPosition) {
                        break;
                    }
                    lastPosition = currentPosition++;
                }
            }
        } else {
            return findDynamicValuePosition(timestamp);
        }
        return -1;
    }

//    //若possiblePosition>=0，则在possiblePosition附近寻找时间戳等于timestamp的Value
//    //若possiblePosition<0，则在所有数据里面寻找
//    //没有找到返回负数
//    private int findValuePosition(boolean isRealTime, int possiblePosition, long timestamp) {
//        int size = isRealTime ? getDynamicValueSize() : getHistoryValueSize();
//        if (possiblePosition >= 0 && possiblePosition < size) {
//            V value;
//            for (int currentPosition = possiblePosition,
//                 lastPosition = currentPosition;
//                 currentPosition < size && currentPosition >= 0;) {
//                value = isRealTime
//                        ? getDynamicValue(currentPosition)
//                        : getHistoryValue(currentPosition);
//                long valueTimestamp = value.mTimestamp;
//                if (valueTimestamp == timestamp) {
//                    return currentPosition;
//                } else if (valueTimestamp > timestamp) {
//                    if (currentPosition > lastPosition) {
//                        break;
//                    }
//                    lastPosition = currentPosition--;
//                } else {
//                    if (currentPosition < lastPosition) {
//                        break;
//                    }
//                    lastPosition = currentPosition++;
//                }
//            }
//        } else {
//            return isRealTime
//                    ? findDynamicValuePosition(timestamp)
//                    : findHistoryValuePosition(timestamp);
//        }
//        return -1;
//    }

    //返回的是数组中的物理位置
    private int findDynamicValuePosition(long timestamp) {
        synchronized (mDynamicValues) {
            int position;
            //Value.VALUE_COMPARER.mTimestamp = timestamp;
            if (mDynamicValueHead == 0) {
                return ExpandCollections.binarySearch(mDynamicValues,
                        timestamp,
                        Value.SEARCH_HELPER);
            }
            if (timestamp >= mDynamicValues.get(0).mTimestamp) {
                position = ExpandCollections.binarySearch(
                        mDynamicValues,
                        0,
                        mDynamicValueHead - 1,
                        timestamp,
                        Value.SEARCH_HELPER);
                return position >= 0
                        ? MAX_DYNAMIC_VALUE_SIZE - mDynamicValueHead + position
                        : position;
            } else {
                return ExpandCollections.binarySearch(
                        mDynamicValues,
                        mDynamicValueHead,
                        mDynamicValues.size() - 1,
                        timestamp,
                        Value.SEARCH_HELPER);
            }
        }
//        synchronized (Value.VALUE_COMPARER) {
//            int position;
//            Value.VALUE_COMPARER.mTimestamp = timestamp;
//            if (mDynamicValueHead == 0) {
//                return Collections.binarySearch(mDynamicValues,
//                        Value.VALUE_COMPARER,
//                        Value.VALUE_COMPARATOR);
//            }
//            if (timestamp >= mDynamicValues.get(0).mTimestamp) {
//                position = indexedBinarySearch(mDynamicValues,
//                        0,
//                        mDynamicValueHead - 1,
//                        Value.VALUE_COMPARER,
//                        Value.VALUE_COMPARATOR);
//                return position >= 0
//                        ? MAX_DYNAMIC_VALUE_SIZE - mDynamicValueHead + position
//                        : position;
//            } else {
//                return indexedBinarySearch(mDynamicValues,
//                        mDynamicValueHead,
//                        mDynamicValues.size() - 1,
//                        Value.VALUE_COMPARER,
//                        Value.VALUE_COMPARATOR);
//            }
//        }
    }

//    private static <T> int indexedBinarySearch(List<? extends T> l, int start, int end, T key, Comparator<? super T> c) {
//        int low = start;
//        int high = end;
//
//        while (low <= high) {
//            int mid = (low + high) >>> 1;
//            T midVal = l.get(mid);
//            int cmp = c.compare(midVal, key);
//
//            if (cmp < 0)
//                low = mid + 1;
//            else if (cmp > 0)
//                high = mid - 1;
//            else
//                return mid; // key found
//        }
//        return -(low + 1);  // key not found
//    }

    public V findHistoryValue(int possiblePosition, long timestamp) {
        int position = findDailyHistoryValuePoolPosition(timestamp);
        return position >= 0
                ? mHistoryValues.get(position).findValue(possiblePosition, timestamp)
                : null;
    }

    //返回在所有历史数据中的位置
    public int findHistoryValuePosition(int possiblePosition, long timestamp) {
        int poolPosition = findDailyHistoryValuePoolPosition(timestamp);
        return poolPosition >= 0
                ? getHistoryValueSize(poolPosition)
                + mHistoryValues.get(poolPosition).findValuePosition(possiblePosition, timestamp)
                : -1;
    }

    public V findIntradayHistoryValue(int possiblePosition, long timestamp) {
        return mCurrentDailyHistoryValuePool.findValue(possiblePosition, timestamp);
    }

    //返回在当日历史数据中的位置
    public int findIntradayHistoryValuePosition(int possiblePosition, long timestamp) {
        return mCurrentDailyHistoryValuePool.findValuePosition(possiblePosition, timestamp);
    }

    //返回在当日历史数据中的位置
    public int findSomedayHistoryValuePosition(int possiblePosition, long timestamp) {
        int position = findDailyHistoryValuePoolPosition(timestamp);
        return position >= 0
                ? mHistoryValues.get(position).findValuePosition(possiblePosition, timestamp)
                : -1;
    }

//    public V findHistoryValue(int possiblePosition, long timestamp) {
//        int actualPosition = findHistoryValuePosition(possiblePosition, timestamp);
//        return actualPosition >= 0
//                ? mHistoryValues.get(actualPosition)
//                : null;
//    }
//
//    public int findHistoryValuePosition(int possiblePosition, long timestamp) {
//        return findValuePosition(false, possiblePosition, timestamp);
//    }
//
//    private int findHistoryValuePosition(long timestamp) {
//        synchronized (Value.VALUE_COMPARER) {
//            Value.VALUE_COMPARER.mTimestamp = timestamp;
//            return Collections.binarySearch(mHistoryValues,
//                    Value.VALUE_COMPARER,
//                    Value.VALUE_COMPARATOR);
//        }
//    }

    public static class Value {

        private static final Value VALUE_COMPARER = new Value(0);
        private static final ExpandComparator<Value, Long> SEARCH_HELPER = new ExpandComparator<Value, Long>() {
            @Override
            public int compare(Value value, Long targetTimestamp) {
                return (value.mTimestamp < targetTimestamp)
                        ? -1
                        : ((value.mTimestamp == targetTimestamp)
                        ? 0
                        : 1);
            }
        };
//        private static final Comparator<Value> VALUE_COMPARATOR = new Comparator<Value>() {
//            @Override
//            public int compare(Value v1, Value v2) {
//                return (v1.mTimestamp < v2.mTimestamp)
//                        ? -1
//                        : ((v1.mTimestamp == v2.mTimestamp)
//                            ? 0
//                            : 1);
//            }
//        };

        long mTimestamp;

        public Value(long timestamp) {
            mTimestamp = timestamp;
        }

        public long getTimestamp() {
            return mTimestamp;
        }

        protected void setTimestamp(long timeStamp) {
            mTimestamp = timeStamp;
        }
    }

    private static class DailyHistoryValuePool<V extends Value> {

        private static final long ONE_DAY_MILLISECONDS = TimeUnit.DAYS.toMillis(1);
        private static final ExpandComparator<DailyHistoryValuePool, Long> CLASSIFIER = new ExpandComparator<DailyHistoryValuePool, Long>() {
            @Override
            public int compare(DailyHistoryValuePool dailyHistoryValuePool, Long date) {
                long delta = dailyHistoryValuePool.mIntradayStartTime - date;
                if (delta > 0) {
                    return 1;
                } else if (delta + ONE_DAY_MILLISECONDS > 0) {
                    return 0;
                } else {
                    return -1;
                }
//                long delta = date - dailyHistoryValuePool.mIntradayStartTime;
//                if (delta < 0) {
//                    return -1;
//                } else if (delta - ONE_DAY_MILLISECONDS < 0) {
//                    return 0;
//                } else {
//                    return 1;
//                }
            }
        };

        private long mIntradayStartTime;
        private List<V> mValues;

        public DailyHistoryValuePool(long dateTime) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            mIntradayStartTime = calendar.getTimeInMillis();
            mValues = new ArrayList<>();
        }

        public V getEarliestValue() {
            return mValues.size() > 0
                    ? mValues.get(0)
                    : null;
        }

        public V getLatestValue() {
            int size = mValues.size();
            return size > 0
                    ? mValues.get(size - 1)
                    : null;
        }

        public boolean contains(long dateTime) {
            return mIntradayStartTime <= dateTime
                    && dateTime < mIntradayStartTime + ONE_DAY_MILLISECONDS;
        }

        //若有新的数据添加，返回position
        //若只是原有数据的更新，返回-position-1
        //注意和Collections.binarySearch()返回值相反
        protected int addValue(ValueContainer<V> container, long timestamp) {
            synchronized (mValues) {
                V v;
                int size = mValues.size();
                if (size > 0) {
                    v = mValues.get(size - 1);
                    if (timestamp > v.getTimestamp()) {
                        v = container.onCreateValue(timestamp);
                        mValues.add(v);
                        return size;
                    } else if (timestamp < v.getTimestamp()) {
                        int position = findValuePosition(timestamp);
                        if (position < 0) {
                            v = container.onCreateValue(timestamp);
                            mValues.add(-position - 1, v);
                        }
                        return -position - 1;
                    }
                    //-(size - 1) - 1
                    return -size;
                } else {
                    v = container.onCreateValue(timestamp);
                    mValues.add(v);
                    return 0;
                }
            }
        }

        public V findValue(int possiblePosition, long timestamp) {
            int actualPosition = findValuePosition(possiblePosition, timestamp);
            return actualPosition >= 0
                    ? mValues.get(actualPosition)
                    : null;
        }

        public int findValuePosition(int possiblePosition, long timestamp) {
            int size = mValues.size();
            if (possiblePosition >= 0 && possiblePosition < size) {
                V value;
                for (int currentPosition = possiblePosition,
                     lastPosition = currentPosition;
                     currentPosition < size && currentPosition >= 0;) {
                    value = mValues.get(currentPosition);
                    long valueTimestamp = value.mTimestamp;
                    if (valueTimestamp == timestamp) {
                        return currentPosition;
                    } else if (valueTimestamp > timestamp) {
                        if (currentPosition > lastPosition) {
                            break;
                        }
                        lastPosition = currentPosition--;
                    } else {
                        if (currentPosition < lastPosition) {
                            break;
                        }
                        lastPosition = currentPosition++;
                    }
                }

            } else {
                return findValuePosition(timestamp);
            }
            return -1;
        }

        private int findValuePosition(long timestamp) {
            synchronized (mValues) {
                //Value.VALUE_COMPARER.mTimestamp = timestamp;
                return ExpandCollections.binarySearch(mValues,
                        timestamp,
                        Value.SEARCH_HELPER);
            }
//            synchronized (Value.VALUE_COMPARER) {
//                Value.VALUE_COMPARER.mTimestamp = timestamp;
//                return Collections.binarySearch(mValues,
//                        Value.VALUE_COMPARER,
//                        Value.VALUE_COMPARATOR);
//            }
        }
    }
}
