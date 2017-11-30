package com.cjq.lib.weisi.sensor;

import android.support.annotation.NonNull;

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
    Measurement(@NonNull Configuration.MeasureParameter parameter,
                int maxDynamicValueSize) {
        super(maxDynamicValueSize);
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
            nextMeasurement.mNextMeasurement = new Measurement(nextParameter.mNext, maxDynamicValueSize);
            nextParameter = nextParameter.mNext;
            nextMeasurement = nextMeasurement.mNextMeasurement;
        }
    }

    //用于生成单个测量参数（动态添加）
    Measurement(@NonNull DataType dataType,
                MeasurementDecorator decorator,
                int maxValueSize) {
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
        if (mRealTimeValue.mTimeStamp < timestamp) {
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

    int addDynamicValue(int address, long timestamp, double rawValue) {
        setRealTimeValue(timestamp, rawValue);
        return setDynamicValueContent(addDynamicValue(timestamp), rawValue);
    }

    int addHistoryValue(long timestamp, double rawValue) {
        return setHistoryValueContent(addHistoryValue(timestamp), rawValue);
    }

    private int setHistoryValueContent(int position, double rawValue) {
        setValueContent(getHistoryValue(position < 0
                ? -position - 1
                : position), rawValue);
        return position;
    }

    private int setDynamicValueContent(int position, double rawValue) {
        if (position < 0) {
            setValueContent(getDynamicValue(-position - 1), rawValue);
        } else if (position < MAX_DYNAMIC_VALUE_SIZE) {
            setValueContent(getDynamicValue(position), rawValue);
        }
        return position;
    }

    private void setValueContent(Value value, double rawValue) {
        if (value != null) {
            value.mRawValue = rawValue;
        }
    }

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
