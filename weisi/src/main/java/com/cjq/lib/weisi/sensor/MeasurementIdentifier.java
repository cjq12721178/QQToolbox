package com.cjq.lib.weisi.sensor;

/**
 * Created by CJQ on 2017/11/29.
 */

public class MeasurementIdentifier {

    private final long mId;

    public MeasurementIdentifier(long id) {
        mId = id;
    }

    public MeasurementIdentifier(int address, byte dataTypeValue, int dataTypeValueIndex) {
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
