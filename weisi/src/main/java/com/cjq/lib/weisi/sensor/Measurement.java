package com.cjq.lib.weisi.sensor;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by CJQ on 2017/6/19.
 */

public class Measurement {

    private static final int DEFAULT_MAX_HISTORY_VALUE_CAPACITY = 50;
    private static boolean enableSaveRealTimeValue = false;

    private final DataType mDataType;
    private String mName;
    private Value mRealTimeValue = new Value(0, 0);
    private LinkedList<Value> mHistoryValues = new LinkedList<>();
    private Measurement mNextMeasurement;
    private MeasurementDecorator mDecorator;

    //用于生成测量参数及其相同数据类型的阵列（根据配置静态生成）
    public Measurement(@NonNull Configuration.MeasureParameter parameter) {
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
            nextMeasurement.mNextMeasurement = new Measurement(nextParameter.mNext);
            nextParameter = nextParameter.mNext;
            nextMeasurement = nextMeasurement.mNextMeasurement;
        }
    }

    //用于生成单个测量参数（动态添加）
    public Measurement(@NonNull DataType dataType, MeasurementDecorator decorator) {
        if (dataType == null) {
            throw new NullPointerException("dataType can not be null");
        }
        mDataType = dataType;
        mDecorator = decorator;
        mName = dataType.getDefaultName();
    }

    public Measurement(DataType dataType) {
        this(dataType, null);
    }

    public String getName() {
        return mDecorator != null ? mDecorator.getName() : mName;
    }

    public String getGeneralName() {
        return mName;
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

    public void setRealTimeValue(double rawValue) {
        setRealTimeValue(System.currentTimeMillis(), rawValue);
    }

    public void setRealTimeValue(long timestamp, double rawValue) {
        synchronized (mRealTimeValue) {
            mRealTimeValue.mTimeStamp = timestamp;
            mRealTimeValue.mRawValue = rawValue;
        }
    }

    public void setRealTimeValue(long timestamp, byte[] srcValue, int srcValueIndex) {
        setRealTimeValue(timestamp, mDataType.mBuilder.build(srcValue, srcValueIndex));
    }

    public Value getRealTimeValue() {
        return mRealTimeValue;
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

    public void setEnableSaveRealTimeValue(boolean enabled) {
        enableSaveRealTimeValue = enabled;
    }

    public void addDynamicValue(double rawValue) {
        addDynamicValue(System.currentTimeMillis(), rawValue);
    }

    public void addDynamicValue(long timestamp, double rawValue) {
        setRealTimeValue(timestamp, rawValue);
        if (enableSaveRealTimeValue) {
            addHistoryValue(timestamp, rawValue);
        }
    }

    public void addDynamicValue(long timestamp, byte[] srcValue, int srcValueIndex) {
        addDynamicValue(timestamp, mDataType.mBuilder.build(srcValue, srcValueIndex));
    }

    private void addHistoryValue(double rawValue) {
        addHistoryValue(System.currentTimeMillis(), rawValue);
    }

    private void addHistoryValue(long timestamp, double rawValue) {
        synchronized (mHistoryValues) {
            int size = mHistoryValues.size();
            if (size > 0) {
                Value newValue;
                if (size == DEFAULT_MAX_HISTORY_VALUE_CAPACITY) {
                    newValue = mHistoryValues.poll();
                    newValue.mTimeStamp = timestamp;
                    newValue.mRawValue = rawValue;
                } else {
                    newValue = new Value(timestamp, rawValue);
                }
                int index = findHistoryValueIndexByTimestamp(newValue);
                mHistoryValues.add(index, newValue);
            } else {
                mHistoryValues.add(new Value(timestamp, rawValue));
            }
        }
    }

    private int findHistoryValueIndexByTimestamp(Value target) {
        //不在size==0的情况下使用
        Value lastValue = mHistoryValues.peekLast();
        if (target.mTimeStamp > lastValue.mTimeStamp) {
            return mHistoryValues.size();
        }
        if (target.mTimeStamp == lastValue.mTimeStamp) {
            mHistoryValues.pollLast();
            return mHistoryValues.size();
        }
        Iterator<Value> values = mHistoryValues.descendingIterator();
        values.next();
        for (int i = mHistoryValues.size() - 1;values.hasNext();--i) {
            lastValue = values.next();
            if (target.mTimeStamp > lastValue.mTimeStamp) {
                return i;
            }
        }
        return 0;
    }

    public List<Value> getHistoryValues() {
        return Collections.unmodifiableList(mHistoryValues);
    }

    public void setDecorator(MeasurementDecorator decorator) {
        mDecorator = decorator;
    }
}
