package com.cjq.lib.weisi.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by CJQ on 2017/6/16.
 */

public class Sensor extends ValueContainer<Sensor.Value> implements OnRawAddressComparer {

    private static final int MEASUREMENT_SEARCH_THRESHOLD = 3;
    private static final int MAX_COMMUNICATION_BREAK_TIME = 60000;

    private boolean mUnknown;
    private final int mRawAddress;
    private final String mFormatAddress;
    private final List<Measurement> mMeasurementKinds;
    private List<Measurement> mMeasurementCollections;
    private SensorDecorator mDecorator;
    private long mFirstValueReceivedTimestamp;
    private long mNetInTimestamp;

    Sensor(int address, SensorDecorator decorator, int maxValueSize) {
        super(maxValueSize);
        //设置地址
        mRawAddress = address & 0xffffff;
        mFormatAddress = ConfigurationManager.isBleSensor(address)
                ? String.format("%06X", mRawAddress)
                : String.format("%04X", mRawAddress);
        //根据配置生成测量参数列表
        Configuration configuration = ConfigurationManager.findConfiguration(mRawAddress);
        if (configuration != null) {
            mUnknown = false;
            mName = configuration.getSensorGeneralName();
            mMeasurementKinds = new ArrayList<>(configuration.mMeasureParameters.length);
            for (Configuration.MeasureParameter parameter :
                    configuration.mMeasureParameters) {
                mMeasurementKinds.add(new Measurement(parameter, maxValueSize));
            }
        } else {
            mUnknown = true;
            mName = "未知传感器";
            mMeasurementKinds = new ArrayList<>();
        }
        generateMeasurementCollections();
        setDecorator(decorator);
    }

    private void generateMeasurementCollections() {
        int size = getMeasurementSizeForKinds();
        if (size != mMeasurementKinds.size()) {
            mMeasurementCollections = new ArrayList<>(size);
            for (Measurement measurement :
                    mMeasurementKinds) {
                do {
                    mMeasurementCollections.add(measurement);
                } while ((measurement = measurement.getNextSameDataTypeMeasurement()) != null);
            }
        }
    }

    public boolean isUnknown() {
        return mUnknown;
    }

    public void setDecorator(SensorDecorator decorator) {
        if (decorator == mDecorator) {
            return;
        }
        mDecorator = decorator;
        if (decorator == null) {
            for (Measurement measurement :
                    mMeasurementKinds) {
                measurement.setDecorator(null);
            }
        } else {
            Iterator<Measurement> measurementIterator = mMeasurementKinds.iterator();
            Measurement measurement = measurementIterator.next();
            MeasurementDecorator[] measurementDecorators = decorator.getMeasurementDecorators();
            for (int measurementDecoratorIndex = 0;
                 measurementDecoratorIndex < measurementDecorators.length
                    && measurement != null;) {
                if (measurement.getDataType().mValue
                        == measurementDecorators[measurementDecoratorIndex].getDataTypeValue()) {
                    measurement.setDecorator(measurementDecorators[measurementDecoratorIndex]);
                    measurement = measurement.getNextSameDataTypeMeasurement();
                    if (measurement == null) {
                        measurement = measurementIterator.next();
                    }
                    ++measurementDecoratorIndex;
                } else {
                    measurement = measurementIterator.next();
                }
            }
        }
    }

    //一般情况下传感器所拥有的测量量由配置文件读取，
    //对于未配置的传感器，可以采用该方法动态添加测量量
    //注：1. 传感器阵列测量量需要依次添加
    //    2. 动态添加无法保证阵列传感器中相同测量量的排列顺序
    //返回新添加的测量量
    private Measurement addMeasurement(int position,
                                       byte dataTypeValue,
                                       MeasurementDecorator decorator) {
        Measurement newMeasurement = new Measurement(
                ConfigurationManager.getDataType(mRawAddress, dataTypeValue, true),
                decorator,
                MAX_SIZE);
        if (position >= 0) {
            mMeasurementKinds.get(position)
                    .getLastSameDataTypeMeasurement()
                    .setSameDataTypeMeasurement(newMeasurement);
        } else {
            mMeasurementKinds.add(-position - 1, newMeasurement);
        }
        generateMeasurementCollections();
        return newMeasurement;
    }

    @Override
    protected Value onCreateValue(long timestamp) {
        return new Value(timestamp, 0);
    }

    //返回传感器名称（可以经过SensorDecorator修饰）
    public String getName() {
        return mDecorator != null ? mDecorator.getName() : mName;
    }

    @Override
    public int getRawAddress() {
        return mRawAddress;
    }

    public String getFormatAddress() {
        return mFormatAddress;
    }

    public Measurement getMeasurementByDataTypeValue(byte dataTypeValue) {
        return getMeasurementByDataTypeValue(dataTypeValue, 0);
    }

    public Measurement getMeasurementByDataTypeValue(byte dataTypeValue, int index) {
        return getMeasurementByPosition(getMeasurementPosition(dataTypeValue), index);
    }

    public Measurement getMeasurementByPosition(int position) {
        return getMeasurementByPosition(position, 0);
    }

    public Measurement getMeasurementByPosition(int position, int index) {
        if (position < 0 || position >= mMeasurementKinds.size()) {
            return null;
        }
        return getMeasurementByPositionImpl(position, index);
    }

    private Measurement getMeasurementByPositionImpl(int position, int index) {
        Measurement result = mMeasurementKinds.get(position);
        for (int i = 0;
             i < index && (result = result.getNextSameDataTypeMeasurement()) != null;
             ++i);
        return result;
    }

    public int getMeasurementPosition(byte dataTypeValue) {
        int position, size = mMeasurementKinds.size();
        if (size > MEASUREMENT_SEARCH_THRESHOLD) {
            synchronized (MEASUREMENT_GET_COMPARER) {
                MEASUREMENT_GET_COMPARER.setDataTypeValue(dataTypeValue);
                position = Collections.binarySearch(mMeasurementKinds,
                        MEASUREMENT_GET_COMPARER,
                        MEASUREMENT_GET_COMPARATOR);
            }
            return position;
        } else {
            byte currentValue;
            for (position = 0;position < size;++position) {
                currentValue = mMeasurementKinds.get(position).getDataType().getValue();
                if (currentValue == dataTypeValue) {
                    return position;
                } else if (dataTypeValue < currentValue) {
                    return -(position + 1);
                }
            }
            return -(position + 1);
        }
    }

    private Measurement getMeasurementByDataTypeValueWithAutoCreate(byte dataTypeValue, int index) {
        int position = getMeasurementPosition(dataTypeValue);
        Measurement measurement;
        if (position >= 0 && position < mMeasurementKinds.size()) {
            measurement = getMeasurementByPositionImpl(position, index);
        } else if (position < 0) {
            measurement = addMeasurement(position, dataTypeValue, null);
        } else {
            measurement = null;
        }
        return measurement;
    }

    public List<Measurement> getMeasurementKinds() {
        return Collections.unmodifiableList(mMeasurementKinds);
    }

    public List<Measurement> getMeasurementCollections() {
        return mMeasurementCollections != null ? mMeasurementCollections : mMeasurementKinds;
    }

    private int getMeasurementSizeForKinds() {
        int size = 0;
        for (Measurement measurement :
                mMeasurementKinds) {
            do {
                ++size;
            } while ((measurement = measurement.getNextSameDataTypeMeasurement()) != null);
        }
        return size;
    }

    public void addDynamicValue(byte dataTypeValue,
                                           int dataTypeValueIndex,
                                           ValueBuildDelegator valueBuildDelegator) {
        if (isStatic()) {
            return;
        }
        Measurement measurement = getMeasurementByDataTypeValueWithAutoCreate(dataTypeValue, dataTypeValueIndex);
        if (measurement == null) {
            return;
        }
        valueBuildDelegator.setValueBuilder(measurement.getDataType().getValueBuilder());
        long timestamp = valueBuildDelegator.getTimestamp();
        float batteryVoltage = valueBuildDelegator.getBatteryVoltage();
        double rawValue = valueBuildDelegator.getRawValue();
        addDynamicValue(timestamp, batteryVoltage);
        measurement.addDynamicValue(timestamp, rawValue);
        if (SensorManager.onSensorRawValueCaptureListener != null) {
            SensorManager
                    .onSensorRawValueCaptureListener
                    .onSensorRawValueCapture(mRawAddress,
                            dataTypeValue,
                            dataTypeValueIndex,
                            timestamp,
                            batteryVoltage,
                            rawValue);
        }
    }

    private void addDynamicValue(long timestamp, float batteryVoltage) {
        setRealTimeValue(timestamp, batteryVoltage);
        addHistoryValue(timestamp, batteryVoltage);
    }

    private void setRealTimeValue(long timestamp, float batteryVoltage) {
        if (mRealTimeValue.mTimeStamp < timestamp) {
            if (mRealTimeValue.mTimeStamp == 0) {
                mFirstValueReceivedTimestamp = timestamp;
            }
            mRealTimeValue.mTimeStamp = timestamp;
            mRealTimeValue.mBatteryVoltage = batteryVoltage;
        }
    }

    public Value getRealTimeValue() {
        return mRealTimeValue;
    }

    private void addHistoryValue(long timestamp, float voltage) {
        Value value = addHistoryValue(timestamp);
        if (value != null) {
            value.mBatteryVoltage = voltage;
        }
    }

    public void addStaticValue(byte dataTypeValue,
                               int dataTypeValueIndex,
                               long timestamp,
                               float batteryVoltage,
                               double rawValue) {
        if (mUnknown || !isStatic()) {
            return;
        }
        Measurement measurement = getMeasurementByDataTypeValueWithAutoCreate(dataTypeValue, dataTypeValueIndex);
        if (measurement == null) {
            return;
        }
        addHistoryValue(timestamp, batteryVoltage);
        measurement.addHistoryValue(timestamp, rawValue);
    }

    public long getFirstValueReceivedTimestamp() {
        return mFirstValueReceivedTimestamp;
    }

    public long getNetInTimestamp() {
        return mNetInTimestamp;
    }

    public void setNetInTimestamp(long netInTimestamp) {
        mNetInTimestamp = netInTimestamp;
    }

    public State getState() {
        if (mRealTimeValue.mTimeStamp == 0) {
            return State.NEVER_CONNECTED;
        }
        return System.currentTimeMillis() - mRealTimeValue.mTimeStamp < MAX_COMMUNICATION_BREAK_TIME
                ? State.ON_LINE
                : State.OFF_LINE;
    }

    public enum State {
        NEVER_CONNECTED,
        ON_LINE,
        OFF_LINE
    }

    private static final Comparator<DataTypeValueGetter> MEASUREMENT_GET_COMPARATOR = new Comparator<DataTypeValueGetter>() {

        @Override
        public int compare(DataTypeValueGetter o1, DataTypeValueGetter o2) {
            return o1.getDataTypeValue() - o2.getDataTypeValue();
        }
    };

    private static final ModifiableDataType MEASUREMENT_GET_COMPARER = new ModifiableDataType();

    private static class ModifiableDataType implements DataTypeValueGetter {

        private byte mModifiableValue;

        public void setDataTypeValue(byte value) {
            mModifiableValue = value;
        }

        @Override
        public byte getDataTypeValue() {
            return mModifiableValue;
        }
    }

    public static class Value extends ValueContainer.Value {

        float mBatteryVoltage;

        public Value(long timeStamp, float batteryVoltage) {
            super(timeStamp);
            mBatteryVoltage = batteryVoltage;
        }

        public float getBatteryVoltage() {
            return mBatteryVoltage;
        }
    }
}
