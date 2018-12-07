package com.wsn.lib.wsb.protocol


import com.wsn.lib.wsb.util.NumericConverter
import kotlin.experimental.and

/**
 * Created by CJQ on 2018/1/10.
 */

class BleAnalyzer : Analyzable {

    override fun analyzeTimestamp(src: ByteArray, position: Int): Long {
        return NumericConverter.int8toUInt32ByMSB(src, position) * 1000
    }

    fun analyzeBatteryVoltage(voltage: Byte): Float {
        return if (voltage < 0)
            (-(voltage and 0x7F)).toFloat()
        else
            voltage * Analyzable.BATTERY_VOLTAGE_COEFFICIENT
    }

    fun analyzeRawValue(src: ByteArray, position: Int): Double {
        return NumericConverter.bytesToFloatByMSB(src, position).toDouble()
    }
}
