package com.cjq.lib.weisi.iot;


import android.support.annotation.NonNull;

import com.cjq.lib.weisi.util.ExpandCollections;
import com.cjq.lib.weisi.util.ExpandComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by CJQ on 2017/6/16.
 */

public class PhysicalSensor extends Sensor<PhysicalSensor.Value, PhysicalSensor.Configuration> {

    private static final int MEASUREMENT_SEARCH_THRESHOLD = 3;
    private static final Configuration EMPTY_CONFIGURATION = new EmptyConfiguration();

    private final Type mType;
    private final List<LogicalSensor> mMeasurementKinds;
    private List<LogicalSensor> mMeasurementCollections;

    PhysicalSensor(int address) {
        this(new ID(address));
    }

    PhysicalSensor(@NonNull ID id) {
        super(checkId(id));
        //设置地址
        int address = id.getAddress();
        //根据配置生成测量参数列表
        Type type = SensorManager.findSensorType(address);
        if (type != null) {
            mMeasurementKinds = new ArrayList<>(type.mMeasureParameters.length);
            LogicalSensor logicalSensor;
            int dataTypeValueIndex;
            for (Type.MeasureParameter parameter :
                    type.mMeasureParameters) {
                dataTypeValueIndex = 0;
                logicalSensor = SensorManager.getLogicalSensor(address, dataTypeValueIndex, parameter);
                mMeasurementKinds.add(logicalSensor);
                for (;(parameter = parameter.mNext) != null;) {
                    ++dataTypeValueIndex;
                    if (logicalSensor.getNextSameDataTypeMeasurement() == null) {
                        logicalSensor.setSameDataTypeMeasurement(SensorManager.getLogicalSensor(address, dataTypeValueIndex, parameter));
                    }
                    logicalSensor = logicalSensor.getNextSameDataTypeMeasurement();
                }
                logicalSensor.setSameDataTypeMeasurement(null);
            }
        } else {
            type = UnknownType.SINGLETON;
            mMeasurementKinds = new ArrayList<>();
        }
        mType = type;
        generateMeasurementCollections();
        resetConfiguration();
    }

    private static ID checkId(ID id) {
        if (id.isLogical()) {
            throw new IllegalArgumentException(String.format("this id(%016X) is not an id for physical sensor", id.getId()));
        }
        return id;
    }

    private void generateMeasurementCollections() {
        int size = getMeasurementSizeForKinds();
        if (size != mMeasurementKinds.size()) {
            mMeasurementCollections = new ArrayList<>(size);
            for (LogicalSensor measurement :
                    mMeasurementKinds) {
                do {
                    mMeasurementCollections.add(measurement);
                } while ((measurement = measurement.getNextSameDataTypeMeasurement()) != null);
            }
        }
    }

    public Type getType() {
        return mType;
    }

    public boolean isUnknown() {
        return mType instanceof UnknownType;
    }

    //一般情况下传感器所拥有的测量量由配置文件读取，
    //对于未配置的传感器，可以采用该方法动态添加测量量
    //注：1. 传感器阵列测量量需要依次添加
    //    2. 动态添加无法保证阵列传感器中相同测量量的排列顺序
    //返回新添加的测量量
    private LogicalSensor addUnconfiguredMeasurement(int position,
                                                     byte dataTypeValue) {
        LogicalSensor newMeasurement;
        synchronized (mMeasurementKinds) {
            if (position >= 0) {
                int dataTypeValueIndex = 1;
                LogicalSensor measurement = mMeasurementKinds.get(position);
                for (;
                     measurement.getNextSameDataTypeMeasurement() != null;
                     measurement = measurement.getNextSameDataTypeMeasurement(),
                        ++dataTypeValueIndex);
                newMeasurement = SensorManager.getLogicalSensor(getRawAddress(), dataTypeValue, dataTypeValueIndex);
                measurement.setSameDataTypeMeasurement(newMeasurement);
            } else {
                newMeasurement = SensorManager.getLogicalSensor(getRawAddress(), dataTypeValue, 0);
                mMeasurementKinds.add(-position - 1, newMeasurement);
            }
            generateMeasurementCollections();
        }
        return newMeasurement;
    }

    @NonNull
    @Override
    protected ValueContainer<Value> onCreateDynamicValueContainer() {
        return new DynamicValueContainerImpl();
    }

    @NonNull
    @Override
    protected ValueContainer<Value> onCreateHistoryValueContainer() {
        return new HistoryValueContainerImpl();
    }

    @Override
    protected Configuration getEmptyConfiguration() {
        return EMPTY_CONFIGURATION;
    }

    @Override
    public String getDefaultName() {
        return mType.mSensorGeneralName;
    }

    public int getRawAddress() {
        return getId().getAddress();
    }

    public String getFormatAddress() {
        return getId().getFormatAddress();
    }

    public LogicalSensor getMeasurementByDataTypeValue(byte dataTypeValue) {
        return getMeasurementByDataTypeValue(dataTypeValue, 0);
    }

    public LogicalSensor getMeasurementByDataTypeValue(byte dataTypeValue, int index) {
        return getMeasurementByPosition(getMeasurementPosition(dataTypeValue), index);
    }

    public LogicalSensor getMeasurementByPosition(int position) {
        return getMeasurementByPosition(position, 0);
    }

    public LogicalSensor getMeasurementByPosition(int position, int index) {
        if (position < 0 || position >= mMeasurementKinds.size()) {
            return null;
        }
        return getMeasurementByPositionImpl(position, index);
    }

    private LogicalSensor getMeasurementByPositionImpl(int position, int index) {
        synchronized (mMeasurementKinds) {
            LogicalSensor result = mMeasurementKinds.get(position);
            for (int i = 0;
                 i < index && (result = result.getNextSameDataTypeMeasurement()) != null;
                 ++i);
            return result;
        }
    }

    public int getMeasurementPosition(byte dataTypeValue) {
        synchronized (mMeasurementKinds) {
            int position, size = mMeasurementKinds.size();
            if (size > MEASUREMENT_SEARCH_THRESHOLD) {
                return ExpandCollections.binarySearch(mMeasurementKinds,
                        dataTypeValue,
                        MEASUREMENT_SEARCH_HELPER);
            } else {
                int currentValue, targetValue = dataTypeValue & 0xff;
                for (position = 0;position < size;++position) {
                    currentValue = mMeasurementKinds.get(position).getDataType().getAbsValue();
                    if (currentValue == targetValue) {
                        return position;
                    } else if (targetValue < currentValue) {
                        return -(position + 1);
                    }
                }
                return -(position + 1);
            }
        }
    }

    private LogicalSensor getMeasurementByDataTypeValueWithAutoCreate(byte dataTypeValue, int index) {
        int position = getMeasurementPosition(dataTypeValue);
        LogicalSensor measurement;
        if (position >= 0 && position < mMeasurementKinds.size()) {
            measurement = getMeasurementByPositionImpl(position, index);
        } else if (position < 0) {
            measurement = addUnconfiguredMeasurement(position, dataTypeValue);
        } else {
            return null;
        }
        return measurement;
    }

    public List<LogicalSensor> getMeasurementKinds() {
        return Collections.unmodifiableList(mMeasurementKinds);
    }

    public List<LogicalSensor> getMeasurementCollections() {
        return mMeasurementCollections != null ? mMeasurementCollections : mMeasurementKinds;
    }

    private int getMeasurementSizeForKinds() {
        int size = 0;
        for (LogicalSensor measurement :
                mMeasurementKinds) {
            do {
                ++size;
            } while ((measurement = measurement.getNextSameDataTypeMeasurement()) != null);
        }
        return size;
    }

    @Override
    public int addDynamicValue(byte dataTypeValue,
                               int dataTypeValueIndex,
                               long timestamp,
                               float batteryVoltage,
                               double rawValue) {
        //获取相应测量量
        LogicalSensor measurement = getMeasurementByDataTypeValueWithAutoCreate(dataTypeValue, dataTypeValueIndex);
        if (measurement == null) {
            return ValueContainer.ADD_FAILED_RETURN_VALUE;
        }
        //修正时间戳
        long correctedTimestamp = correctTimestamp(timestamp);
        //将传感器实时数据添加至实时数据缓存
        int result = addPhysicalDynamicValue(correctedTimestamp, batteryVoltage);
        //修正原始数据
        double correctedValue = measurement.correctRawValue(rawValue);
        //为测量量添加动态数据（包括实时数据及其缓存）
        measurement.addLogicalDynamicValue(correctedTimestamp, correctedValue);
        notifyDynamicValueCaptured(dataTypeValue, dataTypeValueIndex, batteryVoltage, correctedTimestamp, correctedValue);
        return result;
    }

    //对于接收到的动态数据，若其时间差在2秒以内，视其为相同时间戳
    long correctTimestamp(long currentDynamicValueTimestamp) {
        Value v = getRealTimeValue();
        if (v == null) {
            return currentDynamicValueTimestamp;
        }
        long delta = currentDynamicValueTimestamp - v.mTimestamp;
        return delta > 0 && delta < 2000
                ? v.mTimestamp
                : currentDynamicValueTimestamp;
    }

    int addPhysicalDynamicValue(long timestamp, float batteryVoltage) {
        int result = getDynamicValueContainer().addValue(timestamp);
        setValueContent(getValueByContainerAddMethodReturnValue(getDynamicValueContainer(), result), batteryVoltage);
        return result;
    }

    private void setValueContent(Value value, float batteryVoltage) {
        if (value != null) {
            value.mBatteryVoltage = batteryVoltage;
        }
    }

    @Override
    public int addHistoryValue(byte dataTypeValue, int dataTypeValueIndex, long timestamp, float batteryVoltage, double rawValue) {
        addLogicalHistoryValue(dataTypeValue, dataTypeValueIndex, timestamp, rawValue);
        return addPhysicalHistoryValue(timestamp, batteryVoltage);
    }

    public int addPhysicalHistoryValue(long timestamp, float batteryVoltage) {
        int result = getHistoryValueContainer().addValue(timestamp);
        setValueContent(getValueByContainerAddMethodReturnValue(getHistoryValueContainer(), result), batteryVoltage);
        return result;
    }

    public int addLogicalHistoryValue(long measurementValueId, long timestamp, double rawValue) {
        if (Sensor.ID.getAddress(measurementValueId) == getRawAddress()) {
            return addLogicalHistoryValue(
                    Sensor.ID.getDataTypeValue(measurementValueId),
                    Sensor.ID.getDataTypeValueIndex(measurementValueId),
                    timestamp, rawValue);
        }
        return ValueContainer.ADD_FAILED_RETURN_VALUE;
    }

    public int addLogicalHistoryValue(byte dataTypeValue, int dataTypeValueIndex, long timestamp, double rawValue) {
        LogicalSensor measurement = getMeasurementByDataTypeValueWithAutoCreate(dataTypeValue, dataTypeValueIndex);
        if (measurement == null) {
            return ValueContainer.ADD_FAILED_RETURN_VALUE;
        }
        return measurement.addLogicalHistoryValue(timestamp, rawValue);
    }

    private static final ExpandComparator<LogicalSensor, Byte> MEASUREMENT_SEARCH_HELPER = new ExpandComparator<LogicalSensor, Byte>() {
        @Override
        public int compare(LogicalSensor measurement, Byte targetDataTypeValue) {
            return measurement.getDataType().getAbsValue() - (targetDataTypeValue.intValue() & 0xff);
        }
    };

    public static class Value extends com.cjq.lib.weisi.iot.Value {

        float mBatteryVoltage;

        public Value(long timeStamp) {
            super(timeStamp);
        }

        public float getRawBatteryVoltage() {
            return mBatteryVoltage;
        }

        public String getFormattedBatteryVoltage() {
            return mBatteryVoltage < 0
                    ? String.format("%d\\%", (int) mBatteryVoltage)
                    : String.format("%.2fV", mBatteryVoltage);
        }
    }

    /**
     * Created by CJQ on 2017/6/16.
     */

    public static class Type {

        String mSensorGeneralName;
        int mStartAddress;
        int mEndAddress;
        MeasureParameter[] mMeasureParameters;

        Type() {
        }

        public static class MeasureParameter {

            String mDataTypeAccurateName;
            LogicalSensor.DataType mInvolvedDataType;
            MeasureParameter mNext;

            public MeasureParameter(LogicalSensor.DataType involvedDataType,
                                    String dataTypeAccurateName) {
                mDataTypeAccurateName = dataTypeAccurateName;
                mInvolvedDataType = involvedDataType;
            }

            public MeasureParameter getLast() {
                MeasureParameter result = this;
                while (result.mNext != null) {
                    result = result.mNext;
                }
                return result;
            }
        }

        public String getSensorGeneralName() {
            return mSensorGeneralName;
        }

        public int getStartAddress() {
            return mStartAddress;
        }

        public int getEndAddress() {
            return mEndAddress;
        }

        public MeasureParameter[] getMeasureParameters() {
            return mMeasureParameters;
        }
    }

    private static class UnknownType extends Type {

        private static final UnknownType SINGLETON;
        static {
            SINGLETON = new UnknownType();
            SINGLETON.mSensorGeneralName = "未知传感器";
        }
    }

    public interface Configuration extends Sensor.Configuration<Value> {
    }

    private static class EmptyConfiguration
            extends Sensor.EmptyConfiguration<Value>
            implements Configuration{
    }

    private static class DynamicValueContainerImpl extends DynamicValueContainer<Value> {
    }

    private static class HistoryValueContainerImpl extends HistoryValueContainer<Value> {
    }
}
