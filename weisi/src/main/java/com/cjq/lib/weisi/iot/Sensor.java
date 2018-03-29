package com.cjq.lib.weisi.iot;


import android.support.annotation.NonNull;

/**
 * Created by CJQ on 2017/11/3.
 */

public abstract class Sensor<V extends Value, C extends Sensor.Configuration<V>> {

    private static OnDynamicValueCaptureListener onDynamicValueCaptureListener;

    private final ID mId;
    private C mConfiguration;
    private ValueContainer<V> mDynamicValueContainer;
    private ValueContainer<V> mHistoryValueContainer;
    private long mNetInTimestamp;

    protected Sensor(int address, byte dataTypeValue, int dataTypeValueIndex) {
        this(new ID(address, dataTypeValue, dataTypeValueIndex));
    }

    protected Sensor(long id) {
        this(new ID(id));
    }

    protected Sensor(@NonNull ID id) {
        mId = id;
        //setConfiguration(null);
        mDynamicValueContainer = onCreateDynamicValueContainer();
        mHistoryValueContainer = onCreateHistoryValueContainer();
        resetConfiguration();
    }

    public ID getId() {
        return mId;
    }

    public long getNetInTimestamp() {
        return mNetInTimestamp;
    }

    public void setNetInTimestamp(long netInTimestamp) {
        mNetInTimestamp = netInTimestamp;
    }

    protected abstract @NonNull ValueContainer<V> onCreateDynamicValueContainer();

    protected abstract @NonNull ValueContainer<V> onCreateHistoryValueContainer();

    protected abstract @NonNull C getEmptyConfiguration();

    public void setConfiguration(C configuration) {
        if (configuration != null) {
            mConfiguration = configuration;
        } else {
            mConfiguration = getEmptyConfiguration();
        }
    }

    public @NonNull C getConfiguration() {
        return mConfiguration;
    }

    public abstract @NonNull String getDefaultName();

    public String getDecoratedName() {
        Decorator<V> decorator = mConfiguration.getDecorator();
        return decorator != null ? decorator.decorateName(getDefaultName()) : null;
    }

    public @NonNull String getName() {
        String decoratedName = getDecoratedName();
        return decoratedName != null ? decoratedName : getDefaultName();
    }

    public String decorateValue(@NonNull V v) {
        return decorateValue(v, 0);
    }

    public String decorateValue(V v, int para) {
        Decorator<V> decorator = mConfiguration.getDecorator();
        return decorator != null
                ? decorator.decorateValue(v, para)
                : null;
    }

    public String getDecoratedRealTimeValue() {
        return getDecoratedRealTimeValue(0);
    }

    public String getDecoratedRealTimeValue(int para) {
        V v = mDynamicValueContainer.getLatestValue();
        return v != null
                ? decorateValue(v, para)
                : null;
    }

    public V getRealTimeValue() {
        return mDynamicValueContainer.getLatestValue();
    }

    public ValueContainer<V> getDynamicValueContainer() {
        return mDynamicValueContainer;
    }

    public ValueContainer<V> getHistoryValueContainer() {
        return mHistoryValueContainer;
    }

    public V getValueByContainerAddMethodReturnValue(@NonNull ValueContainer<V> container, int addMethodReturnValue) {
        if (addMethodReturnValue >= 0) {
            return container.getValue(addMethodReturnValue);
        } else if (addMethodReturnValue != ValueContainer.ADD_FAILED_RETURN_VALUE) {
            return container.getValue(- addMethodReturnValue - 1);
        }
        return null;
    }

    public boolean hasRealTimeValue() {
        return !getDynamicValueContainer().empty();
    }

    public boolean hasHistoryValue() {
        return !getHistoryValueContainer().empty();
    }

    public void resetConfiguration() {
        SensorManager.SensorConfigurationProvider provider = SensorManager.getConfigurationProvider();
        if (provider == null) {
            setConfiguration(null);
        } else {
            C configuration = provider.getSensorConfiguration(mId);
            setConfiguration(configuration);
        }
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

    /**
     * Created by CJQ on 2017/9/13.
     */

    public interface Filter<S extends Sensor> {
        boolean isMatch(@NonNull S sensor);
    }

    protected static class EmptyConfiguration<V extends Value> implements Configuration<V> {

        @Override
        public Decorator getDecorator() {
            return null;
        }

        @Override
        public void setDecorator(Decorator decorator) {
            throw new UnsupportedOperationException("inner configuration can not set decorator");
        }
    }

    /**
     * Created by CJQ on 2017/11/29.
     */

    public static class ID {

        private static final long ADDRESS_MASK = 0xffffff00000000L;
        private static final long DATA_TYPE_MASK = 0xff000000;
        private static final long DATA_TYPE_INDEX_MASK = 0xffffff;
        private static final int PROTOCOL_FAMILY_MASK = 0xff0000;
        private static final int ADDRESS_START_BIT = 32;
        private static final int DATA_TYPE_START_BIT = 24;
        private final long mId;

        public ID(long id) {
            mId = id;
        }

        public ID(int address) {
            this(address, (byte) 0, 0);
        }

        public ID(int address, byte dataTypeValue, int dataTypeValueIndex) {
            mId = getId(address, dataTypeValue, dataTypeValueIndex);
        }

        public static long getId(int address) {
            return getId(address, (byte) 0, 0);
        }

        public static long getId(int address, byte dataTypeValue, int dataTypeValueIndex) {
            return ((((long) address) << ADDRESS_START_BIT) & ADDRESS_MASK)
                    | ((((long) dataTypeValue) << DATA_TYPE_START_BIT) & DATA_TYPE_MASK)
                    | (((long) dataTypeValueIndex) & DATA_TYPE_INDEX_MASK);
//            return ((long) (address & ADDRESS_MASK) << 32)
//                    | ((long) (dataTypeValue & DATA_TYPE_MASK) << 24)
//                    | (dataTypeValueIndex & DATA_TYPE_INDEX_MASK);
        }

        public static boolean isBleProtocolFamily(int address) {
            return (address & PROTOCOL_FAMILY_MASK) != 0;
        }

        public boolean isBleProtocolFamily() {
            return (getAddress() & PROTOCOL_FAMILY_MASK) != 0;
        }

        public long getId() {
            return mId;
        }

        public int getAddress() {
            return getAddress(mId);
        }

        public String getFormatAddress() {
            return isBleProtocolFamily()
                    ? String.format("%06X", getAddress())
                    : String.format("%04X", getAddress());
        }

        public static int getAddress(long id) {
            return (int) ((id & ADDRESS_MASK) >> ADDRESS_START_BIT);
            //return (int) (id >> 32) & 0xffffff;
        }

        public static String getFormatAddress(long id) {
            return isBleProtocolFamily(getAddress(id))
                    ? String.format("%06X", getAddress(id))
                    : String.format("%04X", getAddress(id));
        }

        public byte getDataTypeValue() {
            return getDataTypeValue(mId);
        }

        public static byte getDataTypeValue(long id) {
            return (byte) ((id & DATA_TYPE_MASK) >> DATA_TYPE_START_BIT);
            //return (byte) (id >> 24);
        }

        public int getDataTypeValueIndex() {
            return getDataTypeValueIndex(mId);
        }

        public static int getDataTypeValueIndex(long id) {
            return (int) (id & DATA_TYPE_INDEX_MASK);
            //return (int) (id & 0xffffff);
        }

        public boolean isLogical() {
            return ((mId & ~ADDRESS_MASK) != 0);
        }

        @Override
        public int hashCode() {
            return (int)(mId ^ (mId >>> 32));
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (o instanceof ID) {
                return mId == ((ID) o).mId;
            }
            if (o instanceof Long) {
                return mId == ((Long)o).longValue();
            }
            return false;
        }
    }

    /**
     * Created by CJQ on 2018/3/16.
     */
    public interface Configuration<V extends Value> {
        Decorator<V> getDecorator();

        void setDecorator(Decorator<V> decorator);
    }
}
