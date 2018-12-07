package com.wsn.lib.wsb.protocol

/**
 * Created by CJQ on 2018/3/22.
 */

class GuomiEsbAnalyzer : EsbAnalyzer() {

    override fun analyzeBatteryVoltage(voltage: Byte, sensorAddress: Int): Float {
        return voltage * Analyzable.BATTERY_VOLTAGE_COEFFICIENT
    }
}
