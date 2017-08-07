package com.cjq.lib.weisi.sensor;

/**
 * Created by CJQ on 2017/6/16.
 */

public class Value {

    long mTimeStamp;
    double mRawValue;

    public Value(long timeStamp, double rawValue) {
        mTimeStamp = timeStamp;
        mRawValue = rawValue;
    }

    public long getTimeStamp() {
        return mTimeStamp;
    }

    public double getRawValue() {
        return mRawValue;
    }
}
