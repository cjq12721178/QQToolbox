package com.cjq.lib.weisi.sensor;

/**
 * Created by CJQ on 2017/9/4.
 */

public class ValueBuildDelegator {

    private ValueBuilder mValueBuilder;
    private byte[] mData;
    private long mTimestampIndex;
    private int mRawValueIndex;
    private int mBatteryVoltageIndex;
    private int mSensorAddress;

    public void setValueBuilder(ValueBuilder builder) {
        mValueBuilder = builder;
    }

    public ValueBuildDelegator setData(byte[] data) {
        mData = data;
        return this;
    }

    public ValueBuildDelegator setTimestampIndex(long timestampIndex) {
        mTimestampIndex = timestampIndex;
        return this;
    }

    public ValueBuildDelegator setRawValueIndex(int rawValueIndex) {
        mRawValueIndex = rawValueIndex;
        return this;
    }

    public ValueBuildDelegator setBatteryVoltageIndex(int batteryVoltageIndex) {
        mBatteryVoltageIndex = batteryVoltageIndex;
        return this;
    }

    public ValueBuildDelegator setSensorAddress(int sensorAddress) {
        mSensorAddress = sensorAddress;
        return this;
    }

    public long getTimestamp() {
        return mValueBuilder.buildTimestamp(mData, mTimestampIndex);
    }

    public double getRawValue() {
        return mValueBuilder.buildRawValue(mData, mRawValueIndex);
    }

    public float getBatteryVoltage() {
        return mValueBuilder.buildBatteryVoltage(mData, mBatteryVoltageIndex, mSensorAddress);
    }
}
