package com.cjq.lib.weisi.iot;


import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cjq.lib.weisi.iot.container.DynamicValueContainer;
import com.cjq.lib.weisi.iot.container.HistoryValueContainer;
import com.cjq.lib.weisi.iot.container.ValueContainer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by CJQ on 2017/11/3.
 */

public abstract class Sensor {

    private static final int MAX_COMMUNICATION_BREAK_TIME = 60000;
    private static OnValueAchievedListener onValueAchievedListener;
    private static OnDynamicValueCaptureListener onDynamicValueCaptureListener;
    private static OnValueAlarmListener onValueAlarmListener;

    protected final Info mInfo;
    private long mNetInTimestamp;

    protected Sensor(@NonNull Info info) {
        mInfo = info;
    }

    public Info getInfo() {
        return mInfo;
    }

    public ID getId() {
        return mInfo.getId();
    }

    public abstract Measurement getMainMeasurement();

    void addDynamicValue(byte dataTypeValue,
                         int dataTypeValueIndex,
                         long timestamp,
                         float batteryVoltage,
                         double rawValue) {
        addDynamicValue(dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue, null);
    }

    abstract void addDynamicValue(byte dataTypeValue,
                                  int dataTypeValueIndex,
                                  long timestamp,
                                  float batteryVoltage,
                                  double rawValue,
                                  OnValueAchievedListener listener);

    void addHistoryValue(byte dataTypeValue,
                         int dataTypeValueIndex,
                         long timestamp,
                         float batteryVoltage,
                         double rawValue) {
        addHistoryValue(dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue, null);
    }

    abstract void addHistoryValue(byte dataTypeValue,
                                  int dataTypeValueIndex,
                                  long timestamp,
                                  float batteryVoltage,
                                  double rawValue,
                                  OnValueAchievedListener listener);

    public long getNetInTimestamp() {
        return mNetInTimestamp;
    }

    public void setNetInTimestamp(long netInTimestamp) {
        mNetInTimestamp = netInTimestamp;
    }

    //对于接收到的动态数据，若其时间差在2秒以内，视其为相同时间戳
    long correctTimestamp(long currentDynamicValueTimestamp) {
        Info.Value v = mInfo.getRealTimeValue();
        if (v == null) {
            return currentDynamicValueTimestamp;
        }
        long delta = currentDynamicValueTimestamp - v.getTimestamp();
        return delta > 0 && delta < 2000
                ? v.getTimestamp()
                : currentDynamicValueTimestamp;
    }

//    public void resetConfiguration() {
//        mInfo.resetConfiguration();
//    }

    int addDynamicInfoValue(long timestamp, float batteryVoltage, OnValueAchievedListener listener) {
        int result = mInfo.getDynamicValueContainer().addValue(timestamp);
        if (mInfo.setValueContent(mInfo.getDynamicValueContainer(), result, batteryVoltage)) {
            notifyDynamicSensorInfoAchieved(result, listener);
            return result;
        }
        return ValueContainer.ADD_FAILED_RETURN_VALUE;
    }

    int addHistoryInfoValue(long timestamp, float batteryVoltage, OnValueAchievedListener listener) {
        int result = mInfo.getHistoryValueContainer().addValue(timestamp);
        if (mInfo.setValueContent(mInfo.getHistoryValueContainer(), result, batteryVoltage)) {
            notifyHistorySensorInfoAchieved(result, listener);
            return result;
        }
        return ValueContainer.ADD_FAILED_RETURN_VALUE;
    }

    int addDynamicMeasurementValue(@NonNull PracticalMeasurement measurement, long timestamp, double rawValue, OnValueAchievedListener listener) {
        int result = measurement.getDynamicValueContainer().addValue(timestamp);
        PracticalMeasurement.Value value = measurement.setValueContent(measurement.getDynamicValueContainer(), result, rawValue);
        if (value != null) {
            notifyDynamicMeasurementValueAchieved(measurement, result, listener);
            makeValueWarnerTest(measurement, value);
            return result;
        }
        return ValueContainer.ADD_FAILED_RETURN_VALUE;
    }

    private void makeValueWarnerTest(@NonNull PracticalMeasurement measurement, @NonNull DisplayMeasurement.Value value) {
        if (onValueAlarmListener != null) {
            onValueAlarmListener.onValueTestResult(getInfo(), measurement, value, measurement.testValue(value));
        }
    }

    int addHistoryMeasurementValue(@NonNull PracticalMeasurement measurement, long timestamp, double rawValue, OnValueAchievedListener listener) {
        int result = measurement.getHistoryValueContainer().addValue(timestamp);
        PracticalMeasurement.Value value = measurement.setValueContent(measurement.getHistoryValueContainer(), result, rawValue);
        if (value != null) {
            notifyHistoryMeasurementValueAchieved(measurement, result, listener);
            return result;
        }
        return ValueContainer.ADD_FAILED_RETURN_VALUE;
    }

    protected void notifyDynamicRawValueAchieved(byte dataTypeValue,
                                                 int dataTypeValueIndex,
                                                 float batteryVoltage,
                                                 long correctedTimestamp,
                                                 double correctedValue) {
        //传感器及其测量量实时数据捕获
        if (onDynamicValueCaptureListener != null) {
            onDynamicValueCaptureListener.onDynamicValueCapture(
                    getId().getAddress(),
                    dataTypeValue, dataTypeValueIndex,
                    correctedTimestamp,
                    batteryVoltage,
                    correctedValue);
        }
    }

    protected void notifyDynamicSensorInfoAchieved(int addMethodReturnValue, OnValueAchievedListener listener) {
        if (listener != null) {
            listener.onDynamicSensorInfoAchieved(this, addMethodReturnValue);
        } else if (onValueAchievedListener != null) {
            onValueAchievedListener.onDynamicSensorInfoAchieved(this, addMethodReturnValue);
        }
    }

    protected void notifyHistorySensorInfoAchieved(int addMethodReturnValue, OnValueAchievedListener listener) {
        if (listener != null) {
            listener.onHistorySensorInfoAchieved(this, addMethodReturnValue);
        } else if (onValueAchievedListener != null) {
            onValueAchievedListener.onHistorySensorInfoAchieved(this, addMethodReturnValue);
        }
    }

    protected void notifyDynamicMeasurementValueAchieved(@NonNull PracticalMeasurement measurement, int addMethodReturnValue, OnValueAchievedListener listener) {
        if (listener != null) {
            listener.onDynamicMeasurementValueAchieved(this, measurement, addMethodReturnValue);
        } else if (onValueAchievedListener != null) {
            onValueAchievedListener.onDynamicMeasurementValueAchieved(this, measurement, addMethodReturnValue);
        }
    }

    protected void notifyHistoryMeasurementValueAchieved(@NonNull PracticalMeasurement measurement, int addMethodReturnValue, OnValueAchievedListener listener) {
        if (listener != null) {
            listener.onHistoryMeasurementValueAchieved(this, measurement, addMethodReturnValue);
        } else if (onValueAchievedListener != null) {
            onValueAchievedListener.onHistoryMeasurementValueAchieved(this, measurement, addMethodReturnValue);
        }
    }

    public @State int getState() {
        if (!mInfo.hasRealTimeValue()) {
            return NEVER_CONNECTED;
        }
        return System.currentTimeMillis() - mInfo.getRealTimeValue().getTimestamp() < MAX_COMMUNICATION_BREAK_TIME
                ? ON_LINE
                : OFF_LINE;
    }

    @IntDef({NEVER_CONNECTED, ON_LINE, OFF_LINE})
    @Retention(RetentionPolicy.SOURCE)
    @interface State {
    }

    public static final int NEVER_CONNECTED = 0;
    public static final int ON_LINE = 1;
    public static final int OFF_LINE = 2;

    public static void setOnValueAchievedListener(OnValueAchievedListener listener) {
        onValueAchievedListener = listener;
    }

    public interface OnValueAchievedListener {
        void onDynamicSensorInfoAchieved(@NonNull Sensor sensor, int infoValuePosition);
        void onDynamicMeasurementValueAchieved(@NonNull Sensor sensor, @NonNull PracticalMeasurement measurement, int measurementValuePosition);
        void onHistorySensorInfoAchieved(@NonNull Sensor sensor, int infoValuePosition);
        void onHistoryMeasurementValueAchieved(@NonNull Sensor sensor, @NonNull PracticalMeasurement measurement, int measurementValuePosition);
    }

    public static void setOnDynamicValueCaptureListener(OnDynamicValueCaptureListener listener) {
        onDynamicValueCaptureListener = listener;
    }

    public interface OnDynamicValueCaptureListener {
        void onDynamicValueCapture(int address,
                                   byte dataTypeValue,
                                   int dataTypeValueIndex,
                                   long timestamp,
                                   float batteryVoltage,
                                   double rawValue);
    }

    public static void setOnValueAlarmListener(OnValueAlarmListener listener) {
        onValueAlarmListener = listener;
    }

    public interface OnValueAlarmListener {
        void onValueTestResult(@NonNull Info info,
                               @NonNull PracticalMeasurement measurement,
                               @NonNull DisplayMeasurement.Value value,
                               int warnResult);
    }

    public static class Info extends Measurement<Info.Value, Info.Configuration> {

        private static final Configuration EMPTY_CONFIGURATION = new EmptyConfiguration();

        protected Info(@NonNull ID id, String name) {
            super(id, TextUtils.isEmpty(name) ? "未知传感器" : name);
            if (!id.isSensorInfo()) {
                throw new IllegalArgumentException("main measurement data type and index may both be 0");
            }
        }

        @Override
        public int getCurveType() {
            return CURVE_TYPE_SENSOR_INFO;
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

        @NonNull
        @Override
        protected Configuration getEmptyConfiguration() {
            return EMPTY_CONFIGURATION;
        }

        @Override
        public @NonNull String formatValue(double rawValue, int para) {
            if (para == 0) {
                return Value.getFormattedBatteryVoltage(rawValue);
            } else {
                return "";
            }
        }

        @Override
        public @NonNull String getValueLabel(int para) {
            if (para == 0) {
                return "电源电压";
            }
            return "";
        }

        boolean setValueContent(@NonNull ValueContainer<Value> container, int addMethodReturnValue, float batteryVoltage) {
            Value value = getValueByContainerAddMethodReturnValue(container, addMethodReturnValue);
            if (value != null) {
                if (addMethodReturnValue >= 0 || Math.abs(value.mBatteryVoltage - batteryVoltage) > 0.00001f) {
                    value.mBatteryVoltage = batteryVoltage;
                    return true;
                }
            }
            return false;
        }

        public static class Value extends com.cjq.lib.weisi.iot.container.Value {

            float mBatteryVoltage;

            public Value(long timeStamp) {
                super(timeStamp);
            }

            public float getRawBatteryVoltage() {
                return mBatteryVoltage;
            }

            public @NonNull String getFormattedBatteryVoltage() {
                return getFormattedBatteryVoltage(mBatteryVoltage);
            }

            @Override
            public double getRawValue(int para) {
                if (para == 0) {
                    return mBatteryVoltage;
                }
                return 0;
            }

            static @NonNull String getFormattedBatteryVoltage(double voltage) {
                return voltage < 0
                        ? String.format("%d%%", ((int) voltage) & 0x7F)
                        : String.format("%.2fV", voltage);
            }
        }

        public interface Configuration extends com.cjq.lib.weisi.iot.Configuration<Value> {
        }

        private static class EmptyConfiguration
                extends Measurement.EmptyConfiguration<Value>
                implements Configuration{
        }

        private static class DynamicValueContainerImpl extends DynamicValueContainer<Value> {
        }

        private static class HistoryValueContainerImpl extends HistoryValueContainer<Value> {
        }
    }
}
