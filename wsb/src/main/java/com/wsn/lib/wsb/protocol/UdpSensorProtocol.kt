package com.wsn.lib.wsb.protocol


import com.wsn.lib.wsb.util.NumericConverter

import java.util.Calendar
import java.util.GregorianCalendar

/**
 * Created by CJQ on 2018/1/10.
 */

class UdpSensorProtocol : ControllableSensorProtocol<EsbAnalyzer> {

    override val dataRequestCommandCode: Byte
        get() = COMMAND_CODE_REQUEST_DATA

    override val timeSynchronizationCommandCode: Byte
        get() = COMMAND_CODE_TIME_SYNCHRONIZATION

    override val timeSynchronizationFrameBuilder: ControllableSensorProtocol<EsbAnalyzer>.TimeSynchronizationFrameBuilder
        get() = TimeSynchronizationFrameBuilderImp()

    override val crc: Crc
        get() = Crc.Weisi

    override val isCrcMsb: Boolean
        get() = false

    constructor() : super(EsbAnalyzer()) {}

    constructor(analyzer: EsbAnalyzer) : super(analyzer) {}

    override fun onDataAnalyzed(data: ByteArray,
                                realDataZoneStart: Int,
                                realDataZoneLength: Int,
                                listener: OnFrameAnalyzedListener) {
        var dataTypeValue: Byte
        var address: Int
        var start = realDataZoneStart
        val end = start + realDataZoneLength / SENSOR_DATA_LENGTH * SENSOR_DATA_LENGTH
        var sensorValuePos: Int
        var calendarPos: Int
        var voltagePos: Int
        while (start < end) {
            //            if (getCrc().calc8(data, start, SENSOR_DATA_LENGTH - 1)
            //                    != data[start + SENSOR_DATA_LENGTH - 1]) {
            //                continue;
            //            }
            address = NumericConverter.int8ToUInt16(data[start], data[start + 1])
            dataTypeValue = data[start + SENSOR_ADDRESS_LENGTH]
            sensorValuePos = start +
                    SENSOR_ADDRESS_LENGTH +
                    BaseSensorProtocol.DATA_TYPE_VALUE_LENGTH +
                    SENSOR_DATA_RESERVE1_LENGTH
            voltagePos = sensorValuePos + SENSOR_VALUE_LENGTH
            calendarPos = voltagePos +
                    BaseSensorProtocol.SENSOR_BATTERY_VOLTAGE_LENGTH +
                    SENSOR_DATA_RESERVE2_LENGTH
            listener.onSensorInfoAnalyzed(
                    address,
                    dataTypeValue,
                    0,
                    analyzer.analyzeTimestamp(data, calendarPos),
                    analyzer.analyzeBatteryVoltage(data[voltagePos], address),
                    analyzer.analyzeRawValue(data, sensorValuePos, dataTypeValue))
            start += SENSOR_DATA_LENGTH
        }
    }

    override fun onTimeSynchronizationAnalyzed(data: ByteArray, realDataZoneStart: Int, realDataZoneLength: Int, listener: OnFrameAnalyzedListener) {
        //暂时，若之后有更多命令需要解析，则建立像FrameBuilder一样的FrameAnalyser
        if (realDataZoneLength != TIME_ZONE_LENGTH) {
            return
        }
        var position = realDataZoneStart
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, data[++position] - 1)
        calendar.set(Calendar.DAY_OF_MONTH, data[++position].toInt())
        calendar.set(Calendar.HOUR, data[++position].toInt())
        calendar.set(Calendar.MINUTE, data[++position].toInt())
        calendar.set(Calendar.SECOND, data[++position].toInt())
        listener.onTimeSynchronizationAnalyzed(calendar.timeInMillis)
    }

    inner class TimeSynchronizationFrameBuilderImp : ControllableSensorProtocol<EsbAnalyzer>.TimeSynchronizationFrameBuilder(COMMAND_CODE_TIME_SYNCHRONIZATION) {

        override val dataZoneLength: Int
            get() = TIME_ZONE_LENGTH

        override fun fillDataZone(frame: ByteArray, offset: Int) {
            var off = offset
            val calendar = GregorianCalendar()
            frame[off] = (calendar.get(Calendar.YEAR) % 100).toByte()
            frame[++off] = (calendar.get(Calendar.MONTH) + 1).toByte()
            frame[++off] = calendar.get(Calendar.DAY_OF_MONTH).toByte()
            frame[++off] = calendar.get(Calendar.HOUR).toByte()
            frame[++off] = calendar.get(Calendar.MINUTE).toByte()
            frame[++off] = calendar.get(Calendar.SECOND).toByte()
        }
    }

    companion object {

        private const val TIME_ZONE_LENGTH = 6
        const val COMMAND_CODE_REQUEST_DATA: Byte = 0x35
        const val COMMAND_CODE_TIME_SYNCHRONIZATION: Byte = 0x42
        private const val SENSOR_DATA_LENGTH = 16
        private const val SENSOR_DATA_RESERVE1_LENGTH = 3
        private const val SENSOR_DATA_RESERVE2_LENGTH = 1
        private const val SENSOR_ADDRESS_LENGTH = 2
        private const val SENSOR_VALUE_LENGTH = 2
    }
}
