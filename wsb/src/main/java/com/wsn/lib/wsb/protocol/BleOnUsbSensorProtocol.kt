package com.wsn.lib.wsb.protocol


import com.wsn.lib.wsb.util.NumericConverter

/**
 * Created by CJQ on 2018/1/10.
 */

class BleOnUsbSensorProtocol : UsbSensorProtocol<BleAnalyzer>(BleAnalyzer()) {

    override val dataRequestCommandCode: Byte
        get() = COMMAND_CODE_REQUEST_DATA

    override fun onDataAnalyzed(data: ByteArray, realDataZoneStart: Int, realDataZoneLength: Int, listener: OnFrameAnalyzedListener) {
        var start = realDataZoneStart
        val end = realDataZoneLength / UsbSensorProtocol.SENSOR_DATA_LENGTH * UsbSensorProtocol.SENSOR_DATA_LENGTH
        var sensorValuePos: Int
        var calendarPos: Int
        var voltagePos: Int
        while (start < end) {
            if (!crc.isCorrect16WithCrcAppended(
                            data,
                            start,
                            UsbSensorProtocol.SENSOR_DATA_LENGTH - BaseSensorProtocol.CRC16_LENGTH,
                            true,
                            true)) {
                start += UsbSensorProtocol.SENSOR_DATA_LENGTH
                continue
            }
            //            if (!CrcClass.isCorrect16(data, start, SENSOR_DATA_LENGTH - 1, true, true)) {
            //                continue;
            //            }
            sensorValuePos = (start
                    + UsbSensorProtocol.SENSOR_ADDRESS_LENGTH
                    + NODE_STATE_LENGTH
                    + BaseSensorProtocol.DATA_TYPE_VALUE_LENGTH)
            voltagePos = sensorValuePos + SENSOR_VALUE_LENGTH
            calendarPos = (voltagePos
                    + BaseSensorProtocol.SENSOR_BATTERY_VOLTAGE_LENGTH
                    + BaseSensorProtocol.RSSI_LENGTH)
            listener.onSensorInfoAnalyzed(
                    NumericConverter.int8ToInt32(
                            0.toByte(),
                            data[start],
                            data[start + 1],
                            data[start + 2]),
                    data[start + UsbSensorProtocol.SENSOR_ADDRESS_LENGTH + NODE_STATE_LENGTH],
                    data[start + 3] - 1,
                    analyzeTimestamp(data, calendarPos),
                    analyzer.analyzeBatteryVoltage(data[voltagePos]),
                    analyzer.analyzeRawValue(data, sensorValuePos))
            start += UsbSensorProtocol.SENSOR_DATA_LENGTH
        }
    }

    companion object {

        const val COMMAND_CODE_REQUEST_DATA: Byte = 0x6D
        private const val NODE_STATE_LENGTH = 1
        private const val SENSOR_VALUE_LENGTH = 4
    }
}
