package com.cjq.lib.weisi.sensor;

/**
 * Created by CJQ on 2017/8/7.
 */

public interface ValueBuilder {
    long buildTimestamp(byte[] src, int timestampIndex);
    double buildRawValue(byte[] src, int rawValueIndex);
    //为了兼容UDP协议在电压计算中的坑爹设置，附加了sensorAddress参数
    float buildBatteryVoltage(byte[] src, int batteryVoltageIndex, int sensorAddress);
}
