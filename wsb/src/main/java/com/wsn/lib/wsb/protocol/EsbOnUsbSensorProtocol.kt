package com.wsn.lib.wsb.protocol


import com.wsn.lib.wsb.util.NumericConverter

/**
 * Created by CJQ on 2018/1/10.
 */

class EsbOnUsbSensorProtocol : UsbSensorProtocol<EsbAnalyzer>(EsbAnalyzer()) {

    override val dataRequestCommandCode: Byte
        get() = COMMAND_CODE_REQUEST_DATA

    override fun onDataAnalyzed(data: ByteArray, realDataZoneStart: Int, realDataZoneLength: Int, listener: OnFrameAnalyzedListener) {
        var dataTypeValue: Byte
        var start = realDataZoneStart
        val end = realDataZoneLength / UsbSensorProtocol.SENSOR_DATA_LENGTH * UsbSensorProtocol.SENSOR_DATA_LENGTH
        var sensorValuePos: Int
        var calendarPos: Int
        var voltagePos: Int
        var address: Int
        while (start < end) {
            if (!crc.isCorrect16WithCrcAppended(
                            data,
                            start,
                            UsbSensorProtocol.SENSOR_DATA_LENGTH - BaseSensorProtocol.CRC16_LENGTH,
                            true,
                            isCrcMsb)) {
                start += UsbSensorProtocol.SENSOR_DATA_LENGTH
                continue
            }
            //            if (!CrcClass.isCorrect16(data, start, SENSOR_DATA_LENGTH - 1, true, true)) {
            //                continue;
            //            }
            address = NumericConverter.int8ToUInt16(data[start + SENSOR_ADDRESS_RESERVE_LENGTH], data[start + SENSOR_ADDRESS_RESERVE_LENGTH + 1])
            dataTypeValue = data[start + UsbSensorProtocol.SENSOR_ADDRESS_LENGTH]
            sensorValuePos = (start
                    + UsbSensorProtocol.SENSOR_ADDRESS_LENGTH
                    + BaseSensorProtocol.DATA_TYPE_VALUE_LENGTH
                    + CHECK_TYPE_LENGTH
                    + LOOP_COUNT_LENGTH
                    + SENSOR_VALUE_COMPLEMENT_LENGTH)
            voltagePos = sensorValuePos + SENSOR_VALUE_LENGTH
            calendarPos = voltagePos + BaseSensorProtocol.SENSOR_BATTERY_VOLTAGE_LENGTH
            listener.onSensorInfoAnalyzed(
                    address,
                    dataTypeValue,
                    0,
                    analyzeTimestamp(data, calendarPos),
                    analyzer.analyzeBatteryVoltage(data[voltagePos], address),
                    analyzer.analyzeRawValue(data, sensorValuePos, dataTypeValue))
            start += UsbSensorProtocol.SENSOR_DATA_LENGTH
        }
    }

    companion object {

        const val COMMAND_CODE_REQUEST_DATA: Byte = 0x6C
        private const val CHECK_TYPE_LENGTH = 1
        private const val LOOP_COUNT_LENGTH = 1
        private const val SENSOR_VALUE_COMPLEMENT_LENGTH = 2
        private const val SENSOR_ADDRESS_RESERVE_LENGTH = 2
        private const val SENSOR_VALUE_LENGTH = 2
    }
}
