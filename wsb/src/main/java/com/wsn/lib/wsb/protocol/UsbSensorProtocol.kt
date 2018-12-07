package com.wsn.lib.wsb.protocol

import com.wsn.lib.wsb.util.NumericConverter

import java.util.Calendar

/**
 * Created by CJQ on 2018/1/10.
 */

abstract class UsbSensorProtocol<A : Analyzable> protected constructor(analyzer: A) : ControllableSensorProtocol<A>(analyzer) {

    override val timeSynchronizationCommandCode: Byte
        get() = COMMAND_CODE_TIME_SYNCHRONIZATION

    override val timeSynchronizationFrameBuilder: ControllableSensorProtocol<A>.TimeSynchronizationFrameBuilder
        get() = TimeSynchronizationFrameBuilderImp()

    override val crc: Crc
        get() = Crc.ccitt

    override val isCrcMsb: Boolean
        get() = true

    override fun onTimeSynchronizationAnalyzed(data: ByteArray,
                                               realDataZoneStart: Int,
                                               realDataZoneLength: Int,
                                               listener: OnFrameAnalyzedListener) {
        if (realDataZoneLength != TIME_ZONE_LENGTH) {
            return
        }
        listener.onTimeSynchronizationAnalyzed(analyzeTimestamp(data, realDataZoneStart))
    }

    protected fun analyzeTimestamp(data: ByteArray, position: Int): Long {
        return NumericConverter.int8toUInt32ByMSB(data,
                position) * 1000
    }

    inner class TimeSynchronizationFrameBuilderImp : ControllableSensorProtocol<A>.TimeSynchronizationFrameBuilder(COMMAND_CODE_TIME_SYNCHRONIZATION) {

        override val dataZoneLength: Int
            get() = TIME_ZONE_LENGTH

        override fun fillDataZone(frame: ByteArray, offset: Int) {
            var off = offset
            val c = Calendar.getInstance()
            c.add(Calendar.MONTH, -1)
            val time = (c.timeInMillis / 1000).toInt()
            frame[off] = (time shr 24).toByte()
            frame[++off] = (time shr 16 and 0xff).toByte()
            frame[++off] = (time shr 8 and 0xff).toByte()
            frame[++off] = (time and 0xff).toByte()
        }
    }

    companion object {

        const val TIME_ZONE_LENGTH = 4
        const val COMMAND_CODE_TIME_SYNCHRONIZATION: Byte = 0x62
        const val SENSOR_DATA_LENGTH = 20
        const val SENSOR_ADDRESS_LENGTH = 4
    }
}
