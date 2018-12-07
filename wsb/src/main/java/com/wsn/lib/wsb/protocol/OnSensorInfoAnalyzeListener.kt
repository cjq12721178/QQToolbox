package com.wsn.lib.wsb.protocol

/**
 * Created by CJQ on 2018/1/11.
 */

interface OnSensorInfoAnalyzeListener {
    fun onSensorInfoAnalyzed(sensorAddress: Int,
                             dataTypeValue: Byte,
                             dataTypeIndex: Int,
                             timestamp: Long,
                             batteryVoltage: Float,
                             rawValue: Double)
}
