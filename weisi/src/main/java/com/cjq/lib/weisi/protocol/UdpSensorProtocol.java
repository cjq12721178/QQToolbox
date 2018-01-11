package com.cjq.lib.weisi.protocol;


import com.cjq.lib.weisi.util.NumericConverter;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by CJQ on 2018/1/10.
 */

public class UdpSensorProtocol extends ControllableSensorProtocol<EsbAnalyzer> {

    public static final byte COMMAND_CODE_REQUEST_DATA = 0x35;
    public static final byte COMMAND_CODE_TIME_SYNCHRONIZATION = 0x42;
    private static final int SENSOR_DATA_LENGTH = 16;
    private static final int SENSOR_DATA_RESERVE1_LENGTH = 3;
    private static final int SENSOR_DATA_RESERVE2_LENGTH = 1;
    private static final int SENSOR_ADDRESS_LENGTH = 4;
    private static final int SENSOR_VALUE_LENGTH = 2;

    public UdpSensorProtocol() {
        super(new EsbAnalyzer());
    }

    @Override
    public byte getDataRequestCommandCode() {
        return COMMAND_CODE_REQUEST_DATA;
    }

    @Override
    public byte getTimeSynchronizationCommandCode() {
        return COMMAND_CODE_TIME_SYNCHRONIZATION;
    }

    @Override
    protected void onDataAnalyzed(byte[] data,
                                int realDataZoneStart,
                                int realDataZoneLength,
                                OnFrameAnalyzedListener listener) {
        byte dataTypeValue;
        int address;
        for (int start = realDataZoneStart,
             end = realDataZoneLength / SENSOR_DATA_LENGTH * SENSOR_DATA_LENGTH,
             sensorValuePos,
             calendarPos,
             voltagePos;
             start < end;
             start += SENSOR_DATA_LENGTH) {
            if (Crc.calc8(data, start, SENSOR_DATA_LENGTH - 1) != data[start + SENSOR_DATA_LENGTH - 1]) {
                continue;
            }
            address = NumericConverter.int8ToUInt16(data[start], data[start + 1]);
            dataTypeValue = data[start + SENSOR_ADDRESS_LENGTH];
            sensorValuePos = start +
                    SENSOR_ADDRESS_LENGTH +
                    DATA_TYPE_VALUE_LENGTH +
                    SENSOR_DATA_RESERVE1_LENGTH;
            voltagePos = sensorValuePos + SENSOR_VALUE_LENGTH;
            calendarPos = voltagePos +
                    SENSOR_BATTERY_VOLTAGE_LENGTH +
                    SENSOR_DATA_RESERVE2_LENGTH;
            listener.onSensorInfoAnalyzed(
                    address,
                    dataTypeValue,
                    0,
                    mAnalyzer.analyzeTimestamp(data, calendarPos),
                    mAnalyzer.analyzeBatteryVoltage(data[voltagePos], address),
                    mAnalyzer.analyzeRawValue(data, sensorValuePos, dataTypeValue));
        }
    }

    @Override
    protected void onTimeSynchronizationAnalyzed(byte[] data, int realDataZoneStart, int realDataZoneLength, OnFrameAnalyzedListener listener) {
        //暂时，若之后有更多命令需要解析，则建立像FrameBuilder一样的FrameAnalyser
        if (realDataZoneLength != TimeSynchronizationFrameBuilderImp.TIME_ZONE_LENGTH) {
            return;
        }
        int position = realDataZoneStart;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, data[++position] - 1);
        calendar.set(Calendar.DAY_OF_MONTH, data[++position]);
        calendar.set(Calendar.HOUR, data[++position]);
        calendar.set(Calendar.MINUTE, data[++position]);
        calendar.set(Calendar.SECOND, data[++position]);
        listener.onTimeSynchronizationAnalyzed(calendar.getTimeInMillis());
    }

    @Override
    public TimeSynchronizationFrameBuilder getTimeSynchronizationFrameBuilder() {
        return new TimeSynchronizationFrameBuilderImp();
    }

    public static class TimeSynchronizationFrameBuilderImp extends TimeSynchronizationFrameBuilder {

        private static final int TIME_ZONE_LENGTH = 6;

        protected TimeSynchronizationFrameBuilderImp() {
            super(COMMAND_CODE_TIME_SYNCHRONIZATION);
        }

        @Override
        protected int getDataZoneLength() {
            return TIME_ZONE_LENGTH;
        }

        @Override
        protected void fillDataZone(byte[] frame, int offset) {
            GregorianCalendar calendar = new GregorianCalendar();
            frame[offset] = (byte) (calendar.get(Calendar.YEAR) % 100);
            frame[++offset] = (byte) (calendar.get(Calendar.MONTH) + 1);
            frame[++offset] = (byte) calendar.get(Calendar.DAY_OF_MONTH);
            frame[++offset] = (byte) calendar.get(Calendar.HOUR);
            frame[++offset] = (byte) calendar.get(Calendar.MINUTE);
            frame[++offset] = (byte) calendar.get(Calendar.SECOND);
        }
    }
}
