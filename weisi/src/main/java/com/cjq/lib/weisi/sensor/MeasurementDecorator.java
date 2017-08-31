package com.cjq.lib.weisi.sensor;

/**
 * Created by CJQ on 2017/8/9.
 */

public class MeasurementDecorator {

    private final byte mDataTypeValue;
    private String mName;

    public MeasurementDecorator(byte dataTypeValue) {
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
