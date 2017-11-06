package com.cjq.lib.weisi.sensor;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by CJQ on 2017/6/19.
 */

public class Measurement
        extends ValueContainer<Measurement.Value>
        implements DataTypeValueGetter {

    private static boolean enableSaveRealTimeValue = false;

    private final DataType mDataType;
    private Measurement mNextMeasurement;
    private MeasurementDecorator mDecorator;

    //用于生成测量参数及其相同数据类型的阵列（根据配置静态生成）
    Measurement(@NonNull Configuration.MeasureParameter parameter, int maxValueSize) {
        super(maxValueSize);
        if (parameter == null || parameter.mInvolvedDataType == null) {
            throw new NullPointerException("measure parameter can not be null");
        }
        mDataType = parameter.mInvolvedDataType;
        if (parameter.mDataTypeAccurateName != null) {
            mName = parameter.mDataTypeAccurateName;
        } else {
            mName = parameter.mInvolvedDataType.getDefaultName();
        }
        Configuration.MeasureParameter nextParameter = parameter;
        Measurement nextMeasurement = this;
        while (nextParameter.mNext != null) {
            nextMeasurement.mNextMeasurement = new Measurement(nextParameter.mNext, maxValueSize);
            nextParameter = nextParameter.mNext;
            nextMeasurement = nextMeasurement.mNextMeasurement;
        }
    }

    //用于生成单个测量参数（动态添加）
    Measurement(@NonNull DataType dataType, MeasurementDecorator decorator, int maxValueSize) {
        super(maxValueSize);
        if (dataType == null) {
            throw new NullPointerException("dataType can not be null");
        }
        mDataType = dataType;
        mDecorator = decorator;
        mName = dataType.getDefaultName();
    }

    public String getName() {
        return mDecorator != null ? mDecorator.getName() : mName;
    }

    @Override
    protected Value onCreateValue(long timestamp) {
        return new Value(timestamp, 0);
    }

    public DataType getDataType() {
        return mDataType;
    }

    public Measurement getNextSameDataTypeMeasurement() {
        return mNextMeasurement;
    }

    public Measurement getSameDataTypeMeasurement(int index) {
        Measurement result = this;
        int i = 0;
        for (;i <= index && result.mNextMeasurement != null;++i) {
            result = result.mNextMeasurement;
        }
        return i < index ? null : result;
    }

    public Measurement getLastSameDataTypeMeasurement() {
        Measurement result = this;
        while (result.mNextMeasurement != null) {
            result = result.mNextMeasurement;
        }
        return result;
    }

    Measurement setSameDataTypeMeasurement(Measurement next) {
        mNextMeasurement = next;
        return this;
    }

    void setRealTimeValue(long timestamp, double rawValue) {
        synchronized (mRealTimeValue) {
            mRealTimeValue.mTimeStamp = timestamp;
            mRealTimeValue.mRawValue = rawValue;
        }
    }

    public String getDecoratedRealTimeValue() {
        return mRealTimeValue.mTimeStamp != 0
                ? mDataType.getDecoratedValue(mRealTimeValue.mRawValue)
                : null;
    }

    public String getDecoratedRealTimeValueWithUnit() {
        return mRealTimeValue.mTimeStamp != 0
                ? mDataType.getDecoratedValueWithUnit(mRealTimeValue.mRawValue)
                : null;
    }

    void addDynamicValue(long timestamp, double rawValue) {
        setRealTimeValue(timestamp, rawValue);
        addHistoryValue(timestamp, rawValue);
    }

    private void addHistoryValue(long timestamp, double rawValue) {
        Value value = addHistoryValue(timestamp);
        if (value != null) {
            value.mRawValue = rawValue;
        }
//        synchronized (mHistoryValues) {
//            int size = mHistoryValues.size();
//            if (size > 0) {
//                Value newValue;
//                if (size == DEFAULT_MAX_HISTORY_VALUE_CAPACITY) {
//                    newValue = mHistoryValues.poll();
//                    newValue.mTimeStamp = timestamp;
//                    newValue.mRawValue = rawValue;
//                } else {
//                    newValue = new Value(timestamp, rawValue);
//                }
//                int index = findHistoryValueIndexByTimestamp(newValue);
//                mHistoryValues.add(index, newValue);
//            } else {
//                mHistoryValues.add(new Value(timestamp, rawValue));
//            }
//        }
    }

//    private int findHistoryValueIndexByTimestamp(Value target) {
//        //不在size==0的情况下使用
//        Value lastValue = mHistoryValues.peekLast();
//        if (target.mTimeStamp > lastValue.mTimeStamp) {
//            return mHistoryValues.size();
//        }
//        if (target.mTimeStamp == lastValue.mTimeStamp) {
//            mHistoryValues.pollLast();
//            return mHistoryValues.size();
//        }
//        Iterator<Value> values = mHistoryValues.descendingIterator();
//        values.next();
//        for (int i = mHistoryValues.size() - 1;values.hasNext();--i) {
//            lastValue = values.next();
//            if (target.mTimeStamp > lastValue.mTimeStamp) {
//                return i;
//            }
//        }
//        return 0;
//    }

    public void setDecorator(MeasurementDecorator decorator) {
        mDecorator = decorator;
    }

    @Override
    public byte getDataTypeValue() {
        return mDataType.mValue;
    }

    /**
     * Created by CJQ on 2017/6/16.
     */

    public static class Value extends ValueContainer.Value {

        double mRawValue;

        public Value(long timeStamp, double rawValue) {
            super(timeStamp);
            mRawValue = rawValue;
        }

        public double getRawValue() {
            return mRawValue;
        }
    }
}
