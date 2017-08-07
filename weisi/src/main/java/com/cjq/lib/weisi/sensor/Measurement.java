package com.cjq.lib.weisi.sensor;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by CJQ on 2017/6/19.
 */

public class Measurement {

    private final DataType mDataType;
    private String mName;
    private Value mRealTimeValue = new Value(0, 0);
    private List<Value> mHistoryValues = new ArrayList<>();
    private List<Value> mUnmodifiableHistoryValues = Collections.unmodifiableList(mHistoryValues);
    private Measurement mNextMeasurement;

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

    public Measurement(@NonNull DataType dataType, String name) {
        if (dataType == null) {
            throw new NullPointerException("dataType can not be null");
        }
        mDataType = dataType;
        if (name != null) {
            mName = name;
        } else {
            mName = dataType.getDefaultName();
        }
    }

    public Measurement(DataType dataType) {
        this(dataType, null);
    }

    //除了BLE阵列传感器外，其余同DataType.getDefaultName()
    public String getName() {
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

    private static boolean enableSaveRealTimeValue = false;
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

    public void addHistoryValue(double rawValue) {
        addHistoryValue(System.currentTimeMillis(), rawValue);
    }

    public void addHistoryValue(long timestamp, double rawValue) {
        synchronized (mHistoryValues) {
            int size = mHistoryValues.size();
            Value newValue = new Value(timestamp, rawValue);
            if (size == 0 || timestamp > mHistoryValues.get(size - 1).mTimeStamp) {
                mHistoryValues.add(newValue);
            } else {
                int index = Collections.binarySearch(mHistoryValues, newValue, VALUE_ADD_COMPARATOR);
                if (index < 0) {
                    mHistoryValues.add(-index - 1, newValue);
                }
            }
        }
    }

    public void addHistoryValue(Collection<Value> values) {
        synchronized (mHistoryValues) {
            if (values != null) {
                mHistoryValues.addAll(values);
                Collections.sort(mHistoryValues, VALUE_ADD_COMPARATOR);
            }
        }
    }

    private static final Comparator<Value> VALUE_ADD_COMPARATOR = new Comparator<Value>() {
        @Override
        public int compare(Value v1, Value v2) {
            return (int)(v1.mTimeStamp - v2.mTimeStamp);
        }
    };

    public Value getLatestValue() {
        int size = mHistoryValues.size();
        return size > 0 ? mHistoryValues.get(size - 1) : null;
    }

    public List<Value> getHistoryValues() {
        return mUnmodifiableHistoryValues;
    }
}
