package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

/**
 * Created by CJQ on 2017/6/19.
 */

public class LogicalSensor extends Sensor {

    private final PracticalMeasurement mPracticalMeasurement;

    protected LogicalSensor(@NonNull Info mainMeasurement, @NonNull PracticalMeasurement practicalMeasurement) {
        super(mainMeasurement);
        mPracticalMeasurement = practicalMeasurement;
    }

//    private static final Configuration EMPTY_CONFIGURATION = new EmptyConfiguration();
//
//
//    private final DataType mDataType;
//    private final String mName;
//    private LogicalSensor mNextMeasurement;
//
//    LogicalSensor(long id, @NonNull DataType dataType) {
//        this(id, dataType, null);
//    }
//
//    LogicalSensor(long id, @NonNull DataType dataType, String name) {
//        this(new ID(id), dataType, name);
//    }
//
//    LogicalSensor(@NonNull ID id, @NonNull DataType dataType) {
//        this(id, dataType, null);
//    }
//
//    LogicalSensor(@NonNull ID id, @NonNull DataType dataType, String name) {
//        super(id);
//        mDataType = dataType;
//        mName = name != null ? name : dataType.getDefaultName();
//    }
//
//    @NonNull
//    @Override
//    protected ValueContainer<Value> onCreateDynamicValueContainer() {
//        return new DynamicValueContainerImpl();
//    }
//
//    @NonNull
//    @Override
//    protected ValueContainer<Value> onCreateHistoryValueContainer() {
//        return new HistoryValueContainerImpl();
//    }
//
//    @Override
//    protected Configuration getEmptyConfiguration() {
//        return EMPTY_CONFIGURATION;
//    }
//
//    @Override
//    public String getDefaultName() {
//        return mName;
//    }

    @Override
    public int addDynamicValue(byte dataTypeValue, int dataTypeValueIndex, long timestamp, float batteryVoltage, double rawValue) {
        //获取所属物理传感器
        //PhysicalSensor sensor = getPhysicalSensor();
        //修正时间戳
        //long correctedTimestamp = sensor.correctTimestamp(timestamp);
        long correctedTimestamp = correctTimestamp(timestamp);
        //修正原始数据
        double correctedValue = mPracticalMeasurement.correctRawValue(rawValue);
        //将逻辑传感器实时数据添加至实时数据缓存
        //int result = addLogicalDynamicValue(correctedTimestamp, correctedValue);
        int result = mPracticalMeasurement.addDynamicValue(correctedTimestamp, correctedValue);
        //为物理传感器添加动态数据
        //sensor.addPhysicalDynamicValue(timestamp, batteryVoltage);
        mInfo.addDynamicValue(correctedTimestamp, batteryVoltage);
        notifyDynamicValueCaptured(dataTypeValue, dataTypeValueIndex, batteryVoltage, correctedTimestamp, correctedValue);
        return result;
    }

    @Override
    public int addHistoryValue(byte dataTypeValue, int dataTypeValueIndex, long timestamp, float batteryVoltage, double rawValue) {
        //getPhysicalSensor().addPhysicalHistoryValue(timestamp, batteryVoltage);
        mInfo.addHistoryValue(timestamp, batteryVoltage);
        return mPracticalMeasurement.addHistoryValue(timestamp, rawValue);
    }

//    public int addLogicalHistoryValue(long timestamp, double rawValue) {
//        int result = getHistoryValueContainer().addValue(timestamp);
//        setValueContent(getValueByContainerAddMethodReturnValue(getHistoryValueContainer(), result), rawValue);
//        return result;
//    }

//    public PhysicalSensor getPhysicalSensor() {
//        return SensorManager.getPhysicalSensor(getId().getAddress(), true);
//    }

//    public PracticalMeasurement.DataType getDataType() {
//        return mDataType;
//    }

//    public int testValue(@NonNull Warner<Value> warner, @NonNull Value value) {
//        return warner.test(value);
//    }
//
//    public int testValue(@NonNull Value value) {
//        Warner<Value> warner = getConfiguration().getWarner();
//        return warner != null ? warner.test(value) : Warner.RESULT_NORMAL;
//    }
//
//    public int testRealTimeValue() {
//        return testValue(getRealTimeValue());
//    }

//    public LogicalSensor getNextSameDataTypeMeasurement() {
//        return mNextMeasurement;
//    }
//
//    public LogicalSensor getSameDataTypeMeasurement(int index) {
//        LogicalSensor result = this;
//        int i = 0;
//        for (;i <= index && result.mNextMeasurement != null;++i) {
//            result = result.mNextMeasurement;
//        }
//        return i < index ? null : result;
//    }
//
//    public LogicalSensor getLastSameDataTypeMeasurement() {
//        LogicalSensor result = this;
//        while (result.mNextMeasurement != null) {
//            result = result.mNextMeasurement;
//        }
//        return result;
//    }
//
//    LogicalSensor setSameDataTypeMeasurement(LogicalSensor next) {
//        mNextMeasurement = next;
//        return this;
//    }

//    public String getFormattedRealTimeValue() {
//        return formatValue(getRealTimeValue());
//    }
//
//    public String getFormattedRealTimeValueWithUnit() {
//        return formatValueWithUnit(getRealTimeValue());
//    }
//
//    public String formatValue(Value v) {
//        return mDataType.formatValue(v);
//    }
//
//    public String formatValue(double rawValue) {
//        return mDataType.formatValue(rawValue);
//    }
//
//    public String formatValueWithUnit(@NonNull Value v) {
//        return mDataType.formatValueWithUnit(v);
//    }
//
//    public String formatValueWithUnit(double rawValue) {
//        return mDataType.formatValueWithUnit(rawValue);
//    }

//    int addLogicalDynamicValue(long timestamp, double rawValue) {
//        int result = getDynamicValueContainer().addValue(timestamp);
//        setValueContent(getValueByContainerAddMethodReturnValue(getDynamicValueContainer(), result), rawValue);
//        return result;
//    }
//
//    private void setValueContent(Value value, double rawValue) {
//        if (value != null) {
//            value.mRawValue = rawValue;
//        }
//    }

//    double correctRawValue(double value) {
////        return mDataType.mCorrector != null
////                ? mDataType.mCorrector.ensureSensor(value)
////                : value;
//        return mPracticalMeasurement.getDataType().mCorrector != null
//                ? mPracticalMeasurement.getDataType().mCorrector.ensureSensor(value)
//                : value;
//    }

    @Override
    public void resetConfiguration() {
        super.resetConfiguration();
        mPracticalMeasurement.resetConfiguration();
    }

    /**
     * Created by CJQ on 2017/6/16.
     */

//    public static class Value extends com.cjq.lib.weisi.iot.Value {
//
//        double mRawValue;
//
//        public Value(long timeStamp) {
//            super(timeStamp);
//        }
//
//        public double getRawValue() {
//            return mRawValue;
//        }
//    }

    /**
     * Created by CJQ on 2017/6/16.
     */

//    public static class DataType {
//
//        final byte mValue;
//        String mName;
//        String mUnit = "";
//        ValueInterpreter mInterpreter = DefaultInterpreter.getInstance();
//        ValueCorrector mCorrector;
//
//        public DataType(byte value) {
//            mValue = value;
//        }
//
//        public byte getValue() {
//            return mValue;
//        }
//
//        public int getAbsValue() {
//            return mValue & 0xff;
//        }
//
//        public String getFormattedValue() {
//            return String.format("%02X", mValue);
//        }
//
//        public String getName() {
//            return mName;
//        }
//
//        public String getDefaultName() {
//            return TextUtils.isEmpty(mName) ? "未知测量量" : mName;
//        }
//
//        public String getUnit() {
//            return mUnit;
//        }
//
//        public String formatValue(Value value) {
//            return value != null ? formatValue(value.mRawValue) : "";
//        }
//
//        public String formatValue(double rawValue) {
//            return mInterpreter.interpret(rawValue);
//        }
//
//        public String formatValueWithUnit(Value value) {
//            return value != null ? formatValueWithUnit(value.mRawValue) : "";
//        }
//
//        public String formatValueWithUnit(double rawValue) {
//            return mUnit != "" ?
//                    mInterpreter.interpret(rawValue) + mUnit :
//                    mInterpreter.interpret(rawValue);
//        }
//    }
//
//    public interface Configuration extends Sensor.Configuration<Value> {
//
//        Warner<Value> getWarner();
//
//        void setWarner(Warner<Value> warner);
//    }
//
//    private static class EmptyConfiguration
//            extends Measurement.EmptyConfiguration<Value>
//            implements Configuration {
//
//        @Override
//        public Warner<Value> getWarner() {
//            return null;
//        }
//
//        @Override
//        public void setWarner(Warner<Value> warner) {
//            throw new UnsupportedOperationException("inner configuration can not set warner");
//        }
//    }
//
//    public interface SingleRangeWarner extends Warner<Value> {
//        @IntDef({RESULT_NORMAL,
//                RESULT_ABOVE_HIGH_LIMIT,
//                RESULT_BELOW_LOW_LIMIT})
//        @Retention(RetentionPolicy.SOURCE)
//        @interface Result {
//        }
//
//        int RESULT_ABOVE_HIGH_LIMIT = 1;
//        int RESULT_BELOW_LOW_LIMIT = 2;
//
//        @Override
//        @SingleRangeWarner.Result
//        int test(Value value);
//    }
//
//    public interface SwitchWarner extends Warner<Value> {
//        @IntDef({RESULT_IN_NORMAL_STATE, RESULT_IN_ABNORMAL_STATE})
//        @Retention(RetentionPolicy.SOURCE)
//        @interface Result {
//        }
//
//        int RESULT_IN_NORMAL_STATE = RESULT_NORMAL;
//        int RESULT_IN_ABNORMAL_STATE = 1;
//
//        @Override
//        @SwitchWarner.Result
//        int test(Value value);
//    }
//
//    private static class DynamicValueContainerImpl extends DynamicValueContainer<Value> {
//    }
//
//    private static class HistoryValueContainerImpl extends HistoryValueContainer<Value> {
//    }
}
