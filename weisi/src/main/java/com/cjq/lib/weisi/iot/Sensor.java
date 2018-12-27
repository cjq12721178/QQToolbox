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
    private static OnDynamicValueCaptureListener onDynamicValueCaptureListener;

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

    public int addInfoHistoryValue(long timestamp, float batteryVoltage) {
        return mInfo.addHistoryValue(timestamp, batteryVoltage);
    }

    protected void notifyDynamicValueCaptured(byte dataTypeValue, int dataTypeValueIndex, float batteryVoltage, long correctedTimestamp, double correctedValue) {
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

        int addDynamicValue(long timestamp, float batteryVoltage) {
            int result = getDynamicValueContainer().addValue(timestamp);
            setValueContent(getValueByContainerAddMethodReturnValue(getDynamicValueContainer(), result), batteryVoltage);
            return result;
        }

        private void setValueContent(Value value, float batteryVoltage) {
            if (value != null) {
                value.mBatteryVoltage = batteryVoltage;
            }
        }

        public int addHistoryValue(long timestamp, float batteryVoltage) {
            int result = getHistoryValueContainer().addValue(timestamp);
            setValueContent(getValueByContainerAddMethodReturnValue(getHistoryValueContainer(), result), batteryVoltage);
            return result;
        }

        public static class Value extends com.cjq.lib.weisi.iot.container.Value {

//            public Value(long timestamp, float batteryVoltage) {
//                super(timestamp);
//                mBatteryVoltage = batteryVoltage;
//            }

            float mBatteryVoltage;

            public Value(long timeStamp) {
                super(timeStamp);
            }

            public float getRawBatteryVoltage() {
                return mBatteryVoltage;
            }

            public String getFormattedBatteryVoltage() {
                return mBatteryVoltage < 0
                        ? String.format("%d%%", ((int) mBatteryVoltage) & 0x7F)
                        : String.format("%.2fV", mBatteryVoltage);
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
