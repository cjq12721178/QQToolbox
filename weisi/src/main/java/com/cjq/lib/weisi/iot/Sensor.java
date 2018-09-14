package com.cjq.lib.weisi.iot;


import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Created by CJQ on 2017/11/3.
 */

public abstract class Sensor {

    private static final int MAX_COMMUNICATION_BREAK_TIME = 60000;
    private static OnDynamicValueCaptureListener onDynamicValueCaptureListener;

    //    private final ID mId;
//    private C mConfiguration;
//    private ValueContainer<V> mDynamicValueContainer;
//    private ValueContainer<V> mHistoryValueContainer;
    protected final Info mInfo;
    private long mNetInTimestamp;

    protected Sensor(@NonNull Info info) {
        mInfo = info;
    }

    public Info getInfo() {
        return mInfo;
    }

    //    protected Sensor(int address, byte dataTypeValue, int dataTypeValueIndex) {
//        this(new ID(address, dataTypeValue, dataTypeValueIndex));
//    }
//
//    protected Sensor(long id) {
//        this(new ID(id));
//    }
//
//    protected Sensor(@NonNull ID id) {
//        mInfo =
//        mId = id;
//        //setConfiguration(null);
//        mDynamicValueContainer = onCreateDynamicValueContainer();
//        mHistoryValueContainer = onCreateHistoryValueContainer();
//        resetConfiguration();
//    }

    public ID getId() {
        //return mId;
        return mInfo.getId();
    }

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
        long delta = currentDynamicValueTimestamp - v.mTimestamp;
        return delta > 0 && delta < 2000
                ? v.mTimestamp
                : currentDynamicValueTimestamp;
    }

    //protected abstract @NonNull ValueContainer<V> onCreateDynamicValueContainer();

    //protected abstract @NonNull ValueContainer<V> onCreateHistoryValueContainer();

    //protected abstract @NonNull C getEmptyConfiguration();

//    public void setConfiguration(C configuration) {
//        if (configuration != null) {
//            mConfiguration = configuration;
//        } else {
//            mConfiguration = getEmptyConfiguration();
//        }
//    }

//    public @NonNull C getConfiguration() {
//        return mConfiguration;
//    }
//
//    public abstract @NonNull String getDefaultName();
//
//    public String getDecoratedName() {
//        Decorator<V> decorator = mConfiguration.getDecorator();
//        return decorator != null ? decorator.decorateName(getDefaultName()) : null;
//    }
//
//    public @NonNull String getName() {
//        String decoratedName = getDecoratedName();
//        return decoratedName != null ? decoratedName : getDefaultName();
//    }
//
//    public String decorateValue(@NonNull V v) {
//        return decorateValue(v, 0);
//    }
//
//    public String decorateValue(V v, int para) {
//        Decorator<V> decorator = mConfiguration.getDecorator();
//        return decorator != null
//                ? decorator.decorateValue(v, para)
//                : null;
//    }
//
//    public String getDecoratedRealTimeValue() {
//        return getDecoratedRealTimeValue(0);
//    }
//
//    public String getDecoratedRealTimeValue(int para) {
//        V v = mDynamicValueContainer.getLatestValue();
//        return v != null
//                ? decorateValue(v, para)
//                : null;
//    }
//
//    public V getRealTimeValue() {
//        return mDynamicValueContainer.getLatestValue();
//    }
//
//    public ValueContainer<V> getDynamicValueContainer() {
//        return mDynamicValueContainer;
//    }
//
//    public ValueContainer<V> getHistoryValueContainer() {
//        return mHistoryValueContainer;
//    }
//
//    public V getValueByContainerAddMethodReturnValue(@NonNull ValueContainer<V> container, int addMethodReturnValue) {
//        if (addMethodReturnValue >= 0) {
//            return container.getValue(addMethodReturnValue);
//        } else if (addMethodReturnValue != ValueContainer.ADD_FAILED_RETURN_VALUE) {
//            return container.getValue(- addMethodReturnValue - 1);
//        }
//        return null;
//    }
//
//    public boolean hasRealTimeValue() {
//        return !getDynamicValueContainer().empty();
//    }
//
//    public boolean hasHistoryValue() {
//        return !getHistoryValueContainer().empty();
//    }
//
//    public void resetConfiguration() {
//        SensorManager.MeasurementConfigurationProvider provider = SensorManager.getConfigurationProvider();
//        if (provider == null) {
//            setConfiguration(null);
//        } else {
//            C configuration = provider.getConfiguration(mId);
//            setConfiguration(configuration);
//        }
//    }

    public void resetConfiguration() {
        if (mInfo.resetConfiguration()) {
            onMainConfigurationChanged();
        }
    }

    protected void onMainConfigurationChanged() {
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

//    public enum State {
//        NEVER_CONNECTED,
//        ON_LINE,
//        OFF_LINE
//    }

    public static void setOnDynamicValueCaptureListener(OnDynamicValueCaptureListener listener) {
        onDynamicValueCaptureListener = listener;
    }

    public abstract int addDynamicValue(byte dataTypeValue,
                                        int dataTypeValueIndex,
                                        long timestamp,
                                        float batteryVoltage,
                                        double rawValue);

    public abstract int addHistoryValue(byte dataTypeValue,
                                        int dataTypeValueIndex,
                                        long timestamp,
                                        float batteryVoltage,
                                        double rawValue);

    public interface OnDynamicValueCaptureListener {
        void onDynamicValueCapture(int address,
                                   byte dataTypeValue,
                                   int dataTypeValueIndex,
                                   long timestamp,
                                   float batteryVoltage,
                                   double rawValue);
    }

//    /**
//     * Created by CJQ on 2018/3/16.
//     */
//    public interface Configuration<V extends Value> {
//        Decorator<V> getDecorator();
//
//        void setDecorator(Decorator<V> decorator);
//    }

    public static class Info extends Measurement<Info.Value, Info.Configuration> {

        private static final Configuration EMPTY_CONFIGURATION = new EmptyConfiguration();

        protected Info(int address, String name) {
            super(address, (byte) 0, 0, name);
        }

        protected Info(long id, String name) {
            this(ID.getAddress(id), name);
        }

        protected Info(@NonNull ID id, String name) {
            super(id, name);
            if (!id.isSensorInfo()) {
                throw new IllegalArgumentException("main measurement data type and index may both be 0");
            }
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

        int addHistoryValue(long timestamp, float batteryVoltage) {
            int result = getHistoryValueContainer().addValue(timestamp);
            setValueContent(getValueByContainerAddMethodReturnValue(getHistoryValueContainer(), result), batteryVoltage);
            return result;
        }

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

        public interface Configuration extends com.cjq.lib.weisi.iot.Configuration<Value> {
            List<ID> getVirtualMeasurementIdList();
        }

        private static class EmptyConfiguration
                extends Measurement.EmptyConfiguration<Value>
                implements Configuration{
            @Override
            public List<ID> getVirtualMeasurementIdList() {
                return null;
            }
        }

        private static class DynamicValueContainerImpl extends DynamicValueContainer<Value> {
        }

        private static class HistoryValueContainerImpl extends HistoryValueContainer<Value> {
        }
    }
}
