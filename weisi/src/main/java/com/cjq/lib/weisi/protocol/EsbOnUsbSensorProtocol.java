package com.cjq.lib.weisi.protocol;


import com.cjq.lib.weisi.util.NumericConverter;

/**
 * Created by CJQ on 2018/1/10.
 */

public class EsbOnUsbSensorProtocol extends UsbSensorProtocol<EsbAnalyzer> {

    public static final byte COMMAND_CODE_REQUEST_DATA = 0x6C;
    private static final int CHECK_TYPE_LENGTH = 1;
    private static final int LOOP_COUNT_LENGTH = 1;
    private static final int SENSOR_VALUE_COMPLEMENT_LENGTH = 2;
    private static final int SENSOR_ADDRESS_RESERVE_LENGTH = 2;
    private static final int SENSOR_VALUE_LENGTH = 2;

    public EsbOnUsbSensorProtocol() {
        super(new EsbAnalyzer());
    }

    @Override
    public byte getDataRequestCommandCode() {
        return COMMAND_CODE_REQUEST_DATA;
    }

    @Override
    protected void onDataAnalyzed(byte[] data, int realDataZoneStart, int realDataZoneLength, OnFrameAnalyzedListener listener) {
        byte dataTypeValue;
        for (int start = realDataZoneStart,
             end = realDataZoneLength / SENSOR_DATA_LENGTH * SENSOR_DATA_LENGTH,
             sensorValuePos,
             calendarPos,
             voltagePos,
             address;
             start < end;
             start += SENSOR_DATA_LENGTH) {
            if (!Crc.isCorrect16(data, start, SENSOR_DATA_LENGTH - 1, true, true)) {
                continue;
            }
            address = NumericConverter.int8ToUInt16(data[start + SENSOR_ADDRESS_RESERVE_LENGTH], data[start + SENSOR_ADDRESS_RESERVE_LENGTH + 1]);
            dataTypeValue = data[start + SENSOR_ADDRESS_LENGTH];
            sensorValuePos = start
                    + SENSOR_ADDRESS_LENGTH
                    + DATA_TYPE_VALUE_LENGTH
                    + CHECK_TYPE_LENGTH
                    + LOOP_COUNT_LENGTH
                    + SENSOR_VALUE_COMPLEMENT_LENGTH;
            voltagePos = sensorValuePos + SENSOR_VALUE_LENGTH;
            calendarPos = voltagePos + SENSOR_BATTERY_VOLTAGE_LENGTH;
            listener.onSensorInfoAnalyzed(
                    address,
                    dataTypeValue,
                    0,
                    analyzeTimestamp(data, calendarPos),
                    mAnalyzer.analyzeBatteryVoltage(data[voltagePos], address),
                    mAnalyzer.analyzeRawValue(data, sensorValuePos, dataTypeValue));
        }
    }
}
