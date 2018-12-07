package com.wsn.lib.wsb.protocol

/**
 * Created by CJQ on 2018/1/11.
 */

abstract class BaseSensorProtocol<A : Analyzable> protected constructor(protected val analyzer: A) {

    abstract val crc: Crc

    abstract val isCrcMsb: Boolean

    companion object {

        const val CRC16_LENGTH = Crc.CRC16_LENGTH
        const val DATA_TYPE_VALUE_LENGTH = 1
        const val SENSOR_BATTERY_VOLTAGE_LENGTH = 1
        const val RSSI_LENGTH = 1
    }
}
