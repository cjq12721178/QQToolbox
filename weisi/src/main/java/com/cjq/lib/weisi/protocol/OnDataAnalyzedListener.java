package com.cjq.lib.weisi.protocol;

import com.cjq.lib.weisi.sensor.ValueBuildDelegator;

/**
 * Created by CJQ on 2017/8/30.
 */
public interface OnDataAnalyzedListener {
    //Measurement onDataAnalyzedStart(int sensorAddress, byte dataTypeValue, int dataTypeIndex);
    //void onDataAnalyzedEnd(long timestamp, double rawValue, float batteryVoltage);
//    void onDataAnalyzed(int sensorAddress,
//                        byte dataTypeValue,
//                        int dataTypeIndex,
//                        long timestamp,
//                        byte[] dataZone,
//                        int rawValueIndex,
//                        int batteryVoltageIndex);
    void onDataAnalyzed(int sensorAddress,
                        byte dataTypeValue,
                        int dataTypeIndex,
                        ValueBuildDelegator valueBuildDelegator);
}
