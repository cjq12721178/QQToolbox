package com.cjq.lib.weisi.node;


import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cjq.lib.weisi.util.ExpandCollections;
import com.cjq.lib.weisi.util.ExpandComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by CJQ on 2017/6/16.
 */

public class Sensor extends ValueContainer<Sensor.Value> {

    private static final int MEASUREMENT_SEARCH_THRESHOLD = 3;
    private static final int MAX_COMMUNICATION_BREAK_TIME = 60000;
    private static final int BLE_PROTOCOL_FAMILY_SENSOR_ADDRESS_LENGTH = 6;
    private static OnDynamicValueCaptureListener onDynamicValueCaptureListener;

    //private boolean mUnknown;
    private final int mRawAddress;
    private final Type mType;
    //private final String mFormatAddress;
    private final List<Measurement> mMeasurementKinds;
    private List<Measurement> mMeasurementCollections;
    private Decorator mDecorator;
    private long mFirstValueReceivedTimestamp;
    private long mNetInTimestamp;

    Sensor(int address, Decorator decorator, int maxDynamicValueSize) {
        super(maxDynamicValueSize);
        //设置地址
        mRawAddress = address & 0xffffff;
//        mFormatAddress = isBleProtocolFamily(address)
//                ? String.format("%06X", mRawAddress)
//                : String.format("%04X", mRawAddress);
        //根据配置生成测量参数列表
        Type type = SensorManager.findSensorType(mRawAddress);
        if (type != null) {
            //mUnknown = false;
            //mName = type.getSensorGeneralName();
            mMeasurementKinds = new ArrayList<>(type.mMeasureParameters.length);
            for (Type.MeasureParameter parameter :
                    type.mMeasureParameters) {
                mMeasurementKinds.add(new Measurement(parameter, maxDynamicValueSize));
            }
        } else {
            //mUnknown = true;
            ///mName = "未知传感器";
            type = UnknownType.SINGLETON;
            mMeasurementKinds = new ArrayList<>();
        }
        mType = type;
        //mName = type.getSensorGeneralName();
        generateMeasurementCollections();
        setDecorator(decorator);
    }

    public static void setOnDynamicValueCaptureListener(OnDynamicValueCaptureListener listener) {
        onDynamicValueCaptureListener = listener;
    }

    public static boolean isBleProtocolFamily(int address) {
        return (address & 0xff0000) != 0;
    }

    public static boolean isBleProtocolFamily(String address) {
        return address.length() == BLE_PROTOCOL_FAMILY_SENSOR_ADDRESS_LENGTH;
    }

    public boolean isBleProtocolFamily() {
        return isBleProtocolFamily(mRawAddress);
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

    public Type getType() {
        return mType;
    }

    public boolean isUnknown() {
        return mType instanceof UnknownType;
    }

    public void setDecorator(Decorator decorator) {
        if (decorator == mDecorator) {
            return;
        }
        mDecorator = decorator;
        synchronized (mMeasurementKinds) {
            if (decorator == null) {
                for (Measurement measurement :
                        mMeasurementKinds) {
                    measurement.setDecorator(null);
                }
            } else {
                Iterator<Measurement> measurementIterator = mMeasurementKinds.iterator();
                Measurement measurement = measurementIterator.next();
                Measurement.Decorator[] measurementDecorators = decorator.getMeasurementDecorators();
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
    }

    //一般情况下传感器所拥有的测量量由配置文件读取，
    //对于未配置的传感器，可以采用该方法动态添加测量量
    //注：1. 传感器阵列测量量需要依次添加
    //    2. 动态添加无法保证阵列传感器中相同测量量的排列顺序
    //返回新添加的测量量
    private Measurement addMeasurement(int position,
                                       byte dataTypeValue,
                                       Measurement.Decorator decorator) {
        Measurement newMeasurement = new Measurement(
                SensorManager.getDataType(mRawAddress, dataTypeValue, true),
                decorator,
                MAX_DYNAMIC_VALUE_SIZE);
        synchronized (mMeasurementKinds) {
            if (position >= 0) {
                mMeasurementKinds.get(position)
                        .getLastSameDataTypeMeasurement()
                        .setSameDataTypeMeasurement(newMeasurement);
            } else {
                mMeasurementKinds.add(-position - 1, newMeasurement);
            }
            generateMeasurementCollections();
        }
        return newMeasurement;
    }

    @Override
    protected Value onCreateValue(long timestamp) {
        return new Value(timestamp, 0);
    }

    //返回传感器名称（可以经过SensorDecorator修饰）
    public String getName() {
        return mDecorator != null ? mDecorator.getName() : mType.mSensorGeneralName;
    }

    public int getRawAddress() {
        return mRawAddress;
    }

    public String getFormatAddress() {
        return isBleProtocolFamily(mRawAddress)
                ? String.format("%06X", mRawAddress)
                : String.format("%04X", mRawAddress);
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
        synchronized (mMeasurementKinds) {
            Measurement result = mMeasurementKinds.get(position);
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
//            synchronized (mMeasurementKinds) {
//                return ExpandCollections.binarySearch(mMeasurementKinds,
//                        dataTypeValue,
//                        MEASUREMENT_SEARCH_HELPER);
//            }
//            synchronized (MEASUREMENT_GET_COMPARER) {
//                MEASUREMENT_GET_COMPARER.setDataTypeValue(dataTypeValue);
//                position = Collections.binarySearch(mMeasurementKinds,
//                        MEASUREMENT_GET_COMPARER,
//                        MEASUREMENT_GET_COMPARATOR);
//            }
//            return position;
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

    public int addDynamicValue(byte dataTypeValue,
                               int dataTypeValueIndex,
                               long timestamp,
                               float batteryVoltage,
                               double rawValue) {
        //获取相应测量量
        Measurement measurement = getMeasurementByDataTypeValueWithAutoCreate(dataTypeValue, dataTypeValueIndex);
        if (measurement == null) {
            return MAX_DYNAMIC_VALUE_SIZE;
        }
        //解析原始数据
        long correctedTimestamp = correctTimestamp(timestamp);
        boolean canValueCaptured = measurement.mRealTimeValue.mTimestamp < correctedTimestamp
                && onDynamicValueCaptureListener != null;
        //设置传感器实时数据
        setRealTimeValue(correctedTimestamp, batteryVoltage);
        //将传感器实时数据添加至实时数据缓存
        int result = setDynamicValueContent(addDynamicValue(correctedTimestamp), batteryVoltage);
        //为测量量添加动态数据（包括实时数据及其缓存）
        measurement.addDynamicValue(mRawAddress, correctedTimestamp, rawValue);
        //传感器及其测量量实时数据捕获
        if (canValueCaptured) {
            onDynamicValueCaptureListener.onDynamicValueCapture(
                    mRawAddress,
                    dataTypeValue, dataTypeValueIndex,
                    correctedTimestamp,
                    batteryVoltage,
                    rawValue);
        }
        return result;
    }

    //对于接收到的动态数据，若其时间差在1秒以内，视其为相同时间戳
    private long correctTimestamp(long currentDynamicValueTimestamp) {
        long delta = currentDynamicValueTimestamp - mRealTimeValue.mTimestamp;
        return delta > 0 && delta < 1000
                ? mRealTimeValue.mTimestamp
                : currentDynamicValueTimestamp;
    }

    private int setDynamicValueContent(int position, float batteryVoltage) {
        if (position < 0) {
            setValueContent(getDynamicValue(-position - 1), batteryVoltage);
        } else if (position < MAX_DYNAMIC_VALUE_SIZE) {
            setValueContent(getDynamicValue(position), batteryVoltage);
        }
        return position;
    }

    private int setHistoryValueContent(int position, float batteryVoltage) {

        setValueContent(getHistoryValue(position < 0
                ? -position - 1
                : position), batteryVoltage);
        return position;
    }

    private void setValueContent(Value value, float batteryVoltage) {
        if (value != null) {
            value.mBatteryVoltage = batteryVoltage;
        }
    }

    private void setRealTimeValue(long timestamp, float batteryVoltage) {
        if (mRealTimeValue.mTimestamp < timestamp) {
            if (mRealTimeValue.mTimestamp == 0) {
                mFirstValueReceivedTimestamp = timestamp;
            }
            mRealTimeValue.mTimestamp = timestamp;
            mRealTimeValue.mBatteryVoltage = batteryVoltage;
//            if (onDynamicValueCaptureListener != null) {
//                onDynamicValueCaptureListener
//                        .onSensorValueCapture(mRawAddress,
//                                timestamp,
//                                batteryVoltage);
//            }
        }
    }

    public int addHistoryValue(long timestamp, float batteryVoltage) {
        DailyHistoryValuePool<Value> pool = fastGetDailyHistoryValuePool(timestamp);
        int position = pool.addValue(this, timestamp);
        setValueContent(pool.getValue((position < 0
                ? -position - 1
                : position)), batteryVoltage);
        return position;
    }

    public int addHistoryValue(long measurementValueId, long timestamp, double rawValue) {
        if (Measurement.ID.getAddress(measurementValueId) == mRawAddress) {
            return addHistoryValue(
                    Measurement.ID.getDataTypeValue(measurementValueId),
                    Measurement.ID.getDataTypeValueIndex(measurementValueId),
                    timestamp, rawValue);
        }
        throw new IllegalArgumentException("address not matched, value not belong to sensor");
    }

    public int addHistoryValue(byte dataTypeValue, int dataTypeValueIndex, long timestamp, double rawValue) {
        Measurement measurement = getMeasurementByDataTypeValueWithAutoCreate(dataTypeValue, dataTypeValueIndex);
        if (measurement == null) {
            throw new NullPointerException("no appropriate measurement found");
        }
        return measurement.addHistoryValue(timestamp, rawValue);
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

    @Override
    public void setIntraday(long dateTime) {
        setIntraday(dateTime, true);
    }

    public void setIntraday(long dateTime, boolean withMeasurements) {
        super.setIntraday(dateTime);
        if (withMeasurements) {
            List<Measurement> measurements = getMeasurementCollections();
            synchronized (measurements) {
                for (int i = 0, n = measurements.size();i < n;++i) {
                    measurements.get(i).setIntraday(dateTime);
                }
            }
        }
    }

    public State getState() {
        if (mRealTimeValue.mTimestamp == 0) {
            return State.NEVER_CONNECTED;
        }
        return System.currentTimeMillis() - mRealTimeValue.mTimestamp < MAX_COMMUNICATION_BREAK_TIME
                ? State.ON_LINE
                : State.OFF_LINE;
    }

    public enum State {
        NEVER_CONNECTED,
        ON_LINE,
        OFF_LINE
    }

    private static final ExpandComparator<Measurement, Byte> MEASUREMENT_SEARCH_HELPER = new ExpandComparator<Measurement, Byte>() {
        @Override
        public int compare(Measurement measurement, Byte targetDataTypeValue) {
            return measurement.getDataType().mValue - targetDataTypeValue;
        }
    };

//    private static final Comparator<DataTypeValueGetter> MEASUREMENT_GET_COMPARATOR = new Comparator<DataTypeValueGetter>() {
//
//        @Override
//        public int compare(DataTypeValueGetter o1, DataTypeValueGetter o2) {
//            return o1.getDataTypeValue() - o2.getDataTypeValue();
//        }
//    };

    //private static final ModifiableDataType MEASUREMENT_GET_COMPARER = new ModifiableDataType();

    public interface OnDynamicValueCaptureListener {
        void onDynamicValueCapture(int address,
                                   byte dataTypeValue,
                                   int dataTypeValueIndex,
                                   long timestamp,
                                   float batteryVoltage,
                                   double rawValue);
//        void onSensorValueCapture(int address,
//                                  long timestamp,
//                                  float batteryVoltage);
//        void onMeasurementValueCapture(int address,
//                                       byte dataTypeValue,
//                                       int dataTypeValueIndex,
//                                       long timestamp,
//                                       double rawValue);
    }

//    private static class ModifiableDataType implements DataTypeValueGetter {
//
//        private byte mModifiableValue;
//
//        public void setDataTypeValue(byte value) {
//            mModifiableValue = value;
//        }
//
//        @Override
//        public byte getDataTypeValue() {
//            return mModifiableValue;
//        }
//    }

    public static class Value extends ValueContainer.Value {

        float mBatteryVoltage;

        public Value(long timeStamp, float batteryVoltage) {
            super(timeStamp);
            mBatteryVoltage = batteryVoltage;
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
            Measurement.DataType mInvolvedDataType;
            MeasureParameter mNext;

            public MeasureParameter(Measurement.DataType involvedDataType,
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

    /**
     * Created by CJQ on 2017/9/13.
     */

    public static interface Filter {
        boolean isMatch(Sensor sensor);
    }

    /**
     * Created by CJQ on 2017/6/19.
     */

    public static class Measurement
            extends ValueContainer<Measurement.Value> {

        //private static boolean enableSaveRealTimeValue = false;

        private final DataType mDataType;
        private final String mName;
        private Measurement mNextMeasurement;
        private Decorator mDecorator;

        //用于生成测量参数及其相同数据类型的阵列（根据配置静态生成）
        Measurement(@NonNull Type.MeasureParameter parameter,
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
            Type.MeasureParameter nextParameter = parameter;
            Measurement nextMeasurement = this;
            while (nextParameter.mNext != null) {
                nextMeasurement.mNextMeasurement = new Measurement(nextParameter.mNext, maxDynamicValueSize);
                nextParameter = nextParameter.mNext;
                nextMeasurement = nextMeasurement.mNextMeasurement;
            }
        }

        //用于生成单个测量参数（动态添加）
        Measurement(@NonNull DataType dataType,
                    Decorator decorator,
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
            if (mRealTimeValue.mTimestamp < timestamp) {
                mRealTimeValue.mTimestamp = timestamp;
                mRealTimeValue.mRawValue = rawValue;
            }
        }

        public String getDecoratedRealTimeValue() {
            return mRealTimeValue.mTimestamp != 0
                    ? mDataType.getDecoratedValue(mRealTimeValue.mRawValue)
                    : null;
        }

        public String getDecoratedRealTimeValueWithUnit() {
            return mRealTimeValue.mTimestamp != 0
                    ? mDataType.getDecoratedValueWithUnit(mRealTimeValue.mRawValue)
                    : null;
        }

        int addDynamicValue(int address, long timestamp, double rawValue) {
            setRealTimeValue(timestamp, rawValue);
            return setDynamicValueContent(addDynamicValue(timestamp), rawValue);
        }

        int addHistoryValue(long timestamp, double rawValue) {
            DailyHistoryValuePool<Value> pool = fastGetDailyHistoryValuePool(timestamp);
            int position = pool.addValue(this, timestamp);
            setValueContent(pool.getValue((position < 0
                    ? -position - 1
                    : position)), rawValue);
            return position;
            //return setHistoryValueContent(addHistoryValue(timestamp), rawValue);
        }

    //    private int setHistoryValueContent(int position, double rawValue) {
    //        setValueContent(getHistoryValue(position < 0
    //                ? -position - 1
    //                : position), rawValue);
    //        return position;
    //    }

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

        public void setDecorator(Decorator decorator) {
            mDecorator = decorator;
        }

    //    @Override
    //    public byte getDataTypeValue() {
    //        return mDataType.mValue;
    //    }

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

        /**
         * Created by CJQ on 2017/6/16.
         */

        public static class DataType {

            final byte mValue;
            String mName;
            String mUnit = "";
            ValueInterpreter mInterpreter = DefaultInterpreter.getInstance();

            public DataType(byte value) {
                mValue = value;
            }

            public byte getValue() {
                return mValue;
            }

            public String getName() {
                return mName;
            }

            public String getDefaultName() {
                return TextUtils.isEmpty(mName) ? String.valueOf(mValue) : mName;
            }

            public String getUnit() {
                return mUnit;
            }

            public String getDecoratedValue(Value value) {
                return value != null ? getDecoratedValue(value.mRawValue) : "";
            }

            public String getDecoratedValue(double rawValue) {
                return mInterpreter.interpret(rawValue);
            }

            public String getDecoratedValueWithUnit(Value value) {
                return value != null ? getDecoratedValueWithUnit(value.mRawValue) : "";
            }

            public String getDecoratedValueWithUnit(double rawValue) {
                return mUnit != "" ?
                        mInterpreter.interpret(rawValue) + mUnit :
                        mInterpreter.interpret(rawValue);
            }
        }

        /**
         * Created by CJQ on 2017/11/29.
         */

        public static class ID {

            private final long mId;

            public ID(long id) {
                mId = id;
            }

            public ID(int address, byte dataTypeValue, int dataTypeValueIndex) {
                mId = getId(address, dataTypeValue, dataTypeValueIndex);
            }

            public static long getId(int address, byte dataTypeValue, int dataTypeValueIndex) {
                return ((long) (address & 0xffffff) << 32)
                        | ((long) (dataTypeValue & 0xff) << 24)
                        | (dataTypeValueIndex & 0xffffff);
            }

            public long getId() {
                return mId;
            }

            public int getAddress() {
                return getAddress(mId);
            }

            public static int getAddress(long id) {
                return (int) (id >> 32) & 0xffffff;
            }

            public byte getDataTypeValue() {
                return getDataTypeValue(mId);
            }

            public static byte getDataTypeValue(long id) {
                return (byte) (id >> 24);
            }

            public int getDataTypeValueIndex() {
                return getDataTypeValueIndex(mId);
            }

            public static int getDataTypeValueIndex(long id) {
                return (int) (id & 0xffffff);
            }
        }

        /**
         * Created by CJQ on 2017/8/9.
         */

        public static class Decorator {

            private final byte mDataTypeValue;
            private String mName;

            public Decorator(byte dataTypeValue) {
                mDataTypeValue = dataTypeValue;
            }

            public byte getDataTypeValue() {
                return mDataTypeValue;
            }

            public String getName() {
                return mName;
            }

            public void setName(String name) {
                mName = name;
            }
        }
    }

    /**
     * Created by CJQ on 2017/8/9.
     */

    public static class Decorator {

        private final int mRawAddress;
        private String mName;
        private final Measurement.Decorator[] mMeasurementDecorators;

        public Decorator(int rawAddress) {
            if ((rawAddress & 0xff000000) != 0) {
                throw new IllegalArgumentException("raw address error");
            }
            //根据配置生成测量参数修饰器列表
            Type type = SensorManager.findSensorType(rawAddress);
            if (type == null) {
                throw new NullPointerException("current sensor address is not in type range");
            }
            mRawAddress = rawAddress;
            List<Measurement.Decorator> measurementDecorators = new ArrayList<>();
            for (Type.MeasureParameter parameter :
                    type.getMeasureParameters()) {
                do {
                    measurementDecorators.add(new Measurement.Decorator(parameter.mInvolvedDataType.mValue));
                } while ((parameter = parameter.mNext) != null);
            }
            mMeasurementDecorators = new Measurement.Decorator[measurementDecorators.size()];
            measurementDecorators.toArray(mMeasurementDecorators);
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }

        public Measurement.Decorator[] getMeasurementDecorators() {
            return mMeasurementDecorators;
        }
    }
}
