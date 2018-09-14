package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

/**
 * Created by CJQ on 2017/11/29.
 */

public final class ID implements Comparable<ID> {

    private static final long ADDRESS_MASK = 0xffffff00000000L;
    private static final long DATA_TYPE_MASK = 0xff000000L;
    private static final long DATA_TYPE_INDEX_MASK = 0xffffffL;
    private static final long MEASUREMENT_MASK = ADDRESS_MASK | DATA_TYPE_MASK | DATA_TYPE_INDEX_MASK;
    private static final int PROTOCOL_FAMILY_MASK = 0xff0000;
    private static final int ADDRESS_START_BIT = 32;
    private static final int DATA_TYPE_START_BIT = 24;
    private final long mId;

    public ID(long id) {
        mId = ensureSensor(id);
    }

    public ID(int address) {
        this(address, (byte) 0, 0);
    }

    public ID(int address, byte dataTypeValue, int dataTypeValueIndex) {
        mId = getId(address, dataTypeValue, dataTypeValueIndex);
    }

    public static long ensureSensor(long id) {
        return ensureMeasurement(id);
    }

    public static long ensureMeasurement(long id) {
        return id & MEASUREMENT_MASK;
    }

    public static long ensurePhysicalSensor(long id) {
        return ensureSensorInfo(id);
    }

    public static long ensureSensorInfo(long id) {
        return id & ADDRESS_MASK;
    }

    public static long getId(int address) {
        return getId(address, (byte) 0, 0);
    }

    public static long getId(int address, byte dataTypeValue, int dataTypeValueIndex) {
        return ((((long) address) << ADDRESS_START_BIT) & ADDRESS_MASK)
                | ((((long) dataTypeValue) << DATA_TYPE_START_BIT) & DATA_TYPE_MASK)
                | (((long) dataTypeValueIndex) & DATA_TYPE_INDEX_MASK);
    }

    public static boolean isBleProtocolFamily(long id) {
        return (getAddress(id) & PROTOCOL_FAMILY_MASK) != 0;
    }

    public static boolean isBleProtocolFamily(int address) {
        return (address & PROTOCOL_FAMILY_MASK) != 0;
    }

    public boolean isBleProtocolFamily() {
        return isBleProtocolFamily(mId);
    }

    public long getId() {
        return mId;
    }

    public int getAddress() {
        return getAddress(mId);
    }

    public String getFormatAddress() {
        return getFormatAddress(mId);
    }

    public static int getAddress(long id) {
        return (int) ((id & ADDRESS_MASK) >> ADDRESS_START_BIT);
    }

    public static String getFormatAddress(long id) {
        return getFormatAddress(getAddress(id));
    }

    public static String getFormatAddress(int address) {
        return isBleProtocolFamily(address)
                ? String.format("%06X", address)
                : String.format("%04X", address);
    }

    public byte getDataTypeValue() {
        return getDataTypeValue(mId);
    }

    public static byte getDataTypeValue(long id) {
        return (byte) getDataTypeAbsValue(id);
    }

    public int getDataTypeAbsValue() {
        return getDataTypeAbsValue(mId);
    }

    public static int getDataTypeAbsValue(long id) {
        return (int) ((id & DATA_TYPE_MASK) >> DATA_TYPE_START_BIT);
    }

    public int getDataTypeValueIndex() {
        return getDataTypeValueIndex(mId);
    }

    public static int getDataTypeValueIndex(long id) {
        return (int) (id & DATA_TYPE_INDEX_MASK);
    }

    public boolean isSensor() {
        return isSensor(mId);
    }

    public static boolean isSensor(long id) {
        return isMeasurement(id);
    }

    public boolean isLogicalSensor() {
        return isLogicalSensor(mId);
    }

    public static boolean isLogicalSensor(long id) {
        return isPracticalMeasurement(id);
    }

    public boolean isPhysicalSensor() {
        return isPhysicalSensor(mId);
    }

    public static boolean isPhysicalSensor(long id) {
        return isSensorInfo(id);
    }

    public boolean isMeasurement() {
        return isMeasurement(mId);
    }

    public static boolean isMeasurement(long id) {
        return (id & ~MEASUREMENT_MASK) == 0;
    }

    public boolean isSensorInfo() {
        return isSensorInfo(mId);
    }

    public static boolean isSensorInfo(long id) {
        return (id & ~ADDRESS_MASK) == 0;
    }

    public boolean isPracticalMeasurement() {
        return isPracticalMeasurement(mId);
    }

    public static boolean isPracticalMeasurement(long id) {
        return isMeasurement(id) && (id & DATA_TYPE_MASK) != 0;
    }

    public boolean isVirtualMeasurement() {
        return isVirtualMeasurement(mId);
    }

    public static boolean isVirtualMeasurement(long id) {
        return isMeasurement(id)
                && (id & DATA_TYPE_MASK) == 0
                && (id & DATA_TYPE_INDEX_MASK) != 0;
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

    @Override
    public String toString() {
        return String.format("%6X-%02X-%d", getAddress(), getDataTypeValue(), getDataTypeValueIndex());
    }

    @Override
    public int compareTo(@NonNull ID o) {
        return (mId < o.mId) ? -1 : (mId == o.mId ? 0 : 1);
    }
}
