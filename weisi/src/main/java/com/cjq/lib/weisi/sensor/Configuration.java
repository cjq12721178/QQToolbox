package com.cjq.lib.weisi.sensor;

/**
 * Created by CJQ on 2017/6/16.
 */

public class Configuration {

    String mSensorGeneralName;
    int mStartAddress;
    int mEndAddress;
    MeasureParameter[] mMeasureParameters;

    public static class MeasureParameter {

        String mDataTypeAccurateName;
        DataType mInvolvedDataType;
        MeasureParameter mNext;

        public MeasureParameter(DataType involvedDataType,
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
