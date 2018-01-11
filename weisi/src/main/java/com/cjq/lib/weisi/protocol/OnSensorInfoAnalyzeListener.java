package com.cjq.lib.weisi.protocol;

/**
 * Created by CJQ on 2018/1/11.
 */

public interface OnSensorInfoAnalyzeListener {
    void onSensorInfoAnalyzed(int sensorAddress,
                              byte dataTypeValue,
                              int dataTypeIndex,
                              long timestamp,
                              float batteryVoltage,
                              double rawValue);
}
