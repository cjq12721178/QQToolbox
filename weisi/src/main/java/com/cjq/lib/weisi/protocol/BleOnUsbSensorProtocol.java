package com.cjq.lib.weisi.protocol;


import com.cjq.lib.weisi.util.NumericConverter;

/**
 * Created by CJQ on 2018/1/10.
 */

public class BleOnUsbSensorProtocol extends UsbSensorProtocol<BleAnalyzer> {

    public static final byte COMMAND_CODE_REQUEST_DATA = 0x6D;
    private static final int NODE_STATE_LENGTH = 1;
    private static final int SENSOR_VALUE_LENGTH = 4;

    protected BleOnUsbSensorProtocol() {
        super(new BleAnalyzer());
    }

    @Override
    protected byte getDataRequestCommandCode() {
        return COMMAND_CODE_REQUEST_DATA;
    }

    @Override
    protected void onDataAnalyzed(byte[] data, int realDataZoneStart, int realDataZoneLength, OnFrameAnalyzedListener listener) {
        for (int start = realDataZoneStart,
             end = realDataZoneLength / SENSOR_DATA_LENGTH * SENSOR_DATA_LENGTH,
             sensorValuePos,
             calendarPos,
             voltagePos;
             start < end;
             start += SENSOR_DATA_LENGTH) {
            if (!Crc.isCorrect16(data, start, SENSOR_DATA_LENGTH - 1, true, true)) {
                continue;
            }
            sensorValuePos = start
                    + SENSOR_ADDRESS_LENGTH
                    + NODE_STATE_LENGTH
                    + DATA_TYPE_VALUE_LENGTH;
            voltagePos = sensorValuePos + SENSOR_VALUE_LENGTH;
            calendarPos = voltagePos
                    + SENSOR_BATTERY_VOLTAGE_LENGTH
                    + RSSI_LENGTH;
            listener.onSensorInfoAnalyzed(
                    NumericConverter.int8ToInt32(
                            (byte) 0,
                            data[start],
                            data[start + 1],
                            data[start + 2]),
                    data[start + SENSOR_ADDRESS_LENGTH + NODE_STATE_LENGTH],
                    data[start + 3] - 1,
                    analyzeTimestamp(data, calendarPos),
                    mAnalyzer.analyzeBatteryVoltage(data[voltagePos]),
                    mAnalyzer.analyzeRawValue(data, sensorValuePos));
        }
    }
}
