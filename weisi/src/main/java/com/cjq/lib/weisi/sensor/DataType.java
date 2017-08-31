package com.cjq.lib.weisi.sensor;

import android.text.TextUtils;

/**
 * Created by CJQ on 2017/6/16.
 */

public class DataType {

    final byte mValue;
    String mName;
    String mUnit = "";
    ValueInterpreter mInterpreter = DefaultInterpreter.getInstance();
    ValueBuilder mBuilder;

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

    public ValueBuilder getBuilder() {
        return mBuilder;
    }
}
