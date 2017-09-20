package com.cjq.lib.weisi.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by CJQ on 2017/6/16.
 */

public class Sensor implements OnRawAddressComparer {

    private static final int MEASUREMENT_SEARCH_THRESHOLD = 3;
    private static final int MAX_COMMUNICATION_BREAK_TIME = 60000;

    private String mName;
    private final int mRawAddress;
    private final String mFormatAddress;
    private final Value mRealTimeValue = new Value(0, 0);
    private final LinkedList<Value> mHistoryValues = new LinkedList<>();
    private final List<Measurement> mMeasurementKinds;
    private List<Measurement> mMeasurementCollections;
    private SensorDecorator mDecorator;
    private long mFirstValueReceivedTimestamp;
    private long mNetInTimestamp;

    Sensor(int address) {
        this(address, null);
    }

    Sensor(int address, SensorDecorator decorator) {
        //设置地址
        mRawAddress = address & 0xffffff;
        mFormatAddress = ConfigurationManager.isBleSensor(address)
                ? String.format("%06X", mRawAddress)
                : String.format("%04X", mRawAddress);
        //根据配置生成测量参数列表
        Configuration configuration = ConfigurationManager.findConfiguration(mRawAddress);
        if (configuration != null) {
            mName = configuration.getSensorGeneralName();
            mMeasurementKinds = new ArrayList<>(configuration.mMeasureParameters.length);
            for (Configuration.MeasureParameter parameter :
                    configuration.mMeasureParameters) {
                mMeasurementKinds.add(new Measurement(parameter));
            }
        } else {
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
        //int position = getMeasurementPosition(dataTypeValue);
        Measurement newMeasurement = new Measurement(
                ConfigurationManager.getDataType(mRawAddress, dataTypeValue, true),
                decorator);
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

    //返回传感器通用名称
    public String getGeneralName() {
        return mName;
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
            synchronized (MODIFIABLE_DATA_TYPE) {
                MODIFIABLE_DATA_TYPE.setValue(dataTypeValue);
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

    public void addMeasurementDynamicValue(byte dataTypeValue,
                                           int dataTypeValueIndex,
                                           ValueBuildDelegator valueBuildDelegator) {
        int position = getMeasurementPosition(dataTypeValue);
        Measurement measurement;
        if (position >= 0 && position < mMeasurementKinds.size()) {
            measurement = getMeasurementByPositionImpl(position, dataTypeValueIndex);
        } else if (position < 0) {
            measurement = addMeasurement(position, dataTypeValue, null);
        } else {
            return;
        }
//        Measurement measurement = getMeasurementByDataTypeValue(dataTypeValue, dataTypeValueIndex);
//        if (measurement == null) {
//            measurement = addMeasurement(dataTypeValue, null);
//        }
        valueBuildDelegator.setValueBuilder(measurement.getDataType().getValueBuilder());
        long timestamp = valueBuildDelegator.getTimestamp();
        addDynamicValue(timestamp, valueBuildDelegator.getBatteryVoltage());
        measurement.addDynamicValue(timestamp, valueBuildDelegator.getRawValue());
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
        synchronized (mHistoryValues) {
            int size = mHistoryValues.size();
            if (size > 0) {
                if (mRealTimeValue.mTimeStamp == timestamp) {
                    mHistoryValues.peekLast().mBatteryVoltage = voltage;
                } else {
                    Value newValue;
                    if (size == Measurement.DEFAULT_MAX_HISTORY_VALUE_CAPACITY) {
                        newValue = mHistoryValues.poll();
                        newValue.mTimeStamp = timestamp;
                        newValue.mBatteryVoltage = voltage;
                    } else {
                        newValue = new Value(timestamp, voltage);
                    }
                    int index = findHistoryValueIndexByTimestamp(newValue);
                    mHistoryValues.add(index, newValue);
                }
            } else {
                mHistoryValues.add(new Value(timestamp, voltage));
            }
        }
    }

    private int findHistoryValueIndexByTimestamp(Value target) {
        //不在size==0的情况下使用
        Value lastValue = mHistoryValues.peekLast();
        if (target.mTimeStamp > lastValue.mTimeStamp) {
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

    private static final Comparator<Measurement> MEASUREMENT_GET_COMPARATOR = new Comparator<Measurement>() {
        @Override
        public int compare(Measurement m1, Measurement m2) {
            return m1.getDataType().getValue() - m2.getDataType().getValue();
        }
    };

    private static final ModifiableDataType MODIFIABLE_DATA_TYPE = new ModifiableDataType((byte)0);
    private static final Measurement MEASUREMENT_GET_COMPARER = new Measurement(MODIFIABLE_DATA_TYPE);

    private static class ModifiableDataType extends DataType {

        private byte mModifiableValue;

        public ModifiableDataType(byte value) {
            super(value);
        }

        @Override
        public byte getValue() {
            return mModifiableValue;
        }

        public void setValue(byte value) {
            mModifiableValue = value;
        }
    }

    public static class Value {

        long mTimeStamp;
        float mBatteryVoltage;

        public Value(long timeStamp, float batteryVoltage) {
            mTimeStamp = timeStamp;
            mBatteryVoltage = batteryVoltage;
        }

        public long getTimeStamp() {
            return mTimeStamp;
        }

        public float getBatteryVoltage() {
            return mBatteryVoltage;
        }
    }
}
