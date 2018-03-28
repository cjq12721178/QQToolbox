package com.cjq.lib.weisi.protocol;

/**
 * Created by CJQ on 2018/3/22.
 */

public class GuomiEsbAnalyzer extends EsbAnalyzer {

    @Override
    public float analyzeBatteryVoltage(byte voltage, int sensorAddress) {
        return voltage * BATTERY_VOLTAGE_COEFFICIENT;
    }
}
