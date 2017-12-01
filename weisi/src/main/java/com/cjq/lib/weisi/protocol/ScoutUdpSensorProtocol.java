package com.cjq.lib.weisi.protocol;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.sensor.ValueBuildDelegator;
import com.cjq.tool.qbox.util.NumericConverter;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by CJQ on 2017/8/30.
 */

public class ScoutUdpSensorProtocol implements Constant {

    public static final byte COMMAND_CODE_REQUEST_DATA = 0x35;
    public static final byte COMMAND_CODE_TIME_SYNCHRONIZATION = 0x42;
    private static final byte DEFAULT_BASE_STATION_ADDRESS_UP_BIT = 0x00;
    private static final byte DEFAULT_BASE_STATION_ADDRESS_DOWN_BIT = 0x00;
    private static final int BASE_STATION_ADDRESS_LENGTH = 2;
    protected static final int DATA_ZONE_LENGTH_LENGTH = 1;
    private static final int COMMAND_CODE_LENGTH = 1;
    private static final byte FIXED_DIFFERENCE_FROM_COMMAND_TO_RESPONSE = (byte)0x80;
    private static final int SENSOR_DATA_LENGTH = 16;
    private static final int SENSOR_DATA_RESERVE1_LENGTH = 3;
    private static final int SENSOR_DATA_RESERVE2_LENGTH = 1;
    private static final int SENSOR_VALUE_LENGTH = 2;
    private static final int SENSOR_BATTERY_VOLTAGE_LENGTH = 1;
    private static final int SENSOR_ADDRESS_LENGTH = 2;
    private static final byte[] START_CHARACTER = new byte[] { (byte)0xAA, (byte)0xAA };
    private static final byte[] END_CHARACTER = new byte[] { 0x55, 0x55 };
    private static final int DATA_ZONE_POSITION = START_CHARACTER.length
            + BASE_STATION_ADDRESS_LENGTH
            + DATA_ZONE_LENGTH_LENGTH;
    private static final int MIN_FRAME_LENGTH = DATA_ZONE_POSITION
            + COMMAND_CODE_LENGTH
            + CRC16_LENGTH
            + END_CHARACTER.length;

    private final ValueBuildDelegator mValueBuildDelegator = new ValueBuildDelegator();

    public void analyze(byte[] udpData, OnFrameAnalyzedListener listener) {
        analyze(udpData, 0, udpData.length, listener);
    }

    public void analyze(byte[] udpData, int offset, int length, OnFrameAnalyzedListener listener) {
        //判断数据是否为空，以及数据长度是否大于最小数据长度
        if (listener == null
                || offset < 0
                || offset + length > udpData.length
                || length < MIN_FRAME_LENGTH) {
            return;
        }

        //记录实际数据域长度（除去命令码长度之后的长度）
        int realDataZoneLength = NumericConverter.int8ToUInt16(udpData[offset + START_CHARACTER.length + BASE_STATION_ADDRESS_LENGTH]) - COMMAND_CODE_LENGTH;
        //计算实际数据长度
        int realDataLength = MIN_FRAME_LENGTH + realDataZoneLength;
        //检查起始符和结束符
        if (udpData[offset] != START_CHARACTER[0]
                || udpData[offset + 1] != START_CHARACTER[1]
                || udpData[offset + realDataLength - 2] != END_CHARACTER[0]
                || udpData[offset + realDataLength - 1] != END_CHARACTER[1]) {
            return;
        }

        //计算CRC16并校验
        if (!Crc.isCorrect16(udpData,
                offset + START_CHARACTER.length,
                BASE_STATION_ADDRESS_LENGTH
                        + DATA_ZONE_LENGTH_LENGTH
                        + COMMAND_CODE_LENGTH
                        + realDataZoneLength,
                true,
                false)) {
            return;
        }

        analyzeDataZone(udpData, offset + DATA_ZONE_POSITION, realDataZoneLength, listener);
    }

    private void analyzeDataZone(byte[] data, int dataZoneStart, int realDataZoneLength, OnFrameAnalyzedListener listener) {
        //获取并校验命令码
        int commandCode = (byte)(data[dataZoneStart] -
                FIXED_DIFFERENCE_FROM_COMMAND_TO_RESPONSE);

        //目前只需要这两个命令
        if (commandCode == COMMAND_CODE_REQUEST_DATA) {
            //解析数据帧
            onDataAnalyzed(data, dataZoneStart + COMMAND_CODE_LENGTH, realDataZoneLength, listener);
        } else if (commandCode == COMMAND_CODE_TIME_SYNCHRONIZATION) {
            onTimeSynchronizationAnalyzed(data, dataZoneStart + COMMAND_CODE_LENGTH, realDataZoneLength, listener);
        }
    }

    private void onDataAnalyzed(byte[] data,
                                int realDataZoneStart,
                                int realDataZoneLength,
                                OnFrameAnalyzedListener listener) {
        mValueBuildDelegator.setData(data);
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
            sensorValuePos = start +
                    SENSOR_ADDRESS_LENGTH +
                    DATA_TYPE_VALUE_LENGTH +
                    SENSOR_DATA_RESERVE1_LENGTH;
            voltagePos = sensorValuePos + SENSOR_VALUE_LENGTH;
            calendarPos = voltagePos +
                    SENSOR_BATTERY_VOLTAGE_LENGTH +
                    SENSOR_DATA_RESERVE2_LENGTH;
            listener.onDataAnalyzed(NumericConverter.int8ToUInt16(data[start], data[start + 1]),
                    data[start + SENSOR_ADDRESS_LENGTH],
                    0,
                    mValueBuildDelegator
                            .setTimestampIndex(calendarPos)
                            .setRawValueIndex(sensorValuePos)
                            .setBatteryVoltageIndex(voltagePos));
        }
    }

    private void onTimeSynchronizationAnalyzed(byte[] data,
                                               int realDataZoneStart,
                                               int realDataZoneLength,
                                               OnFrameAnalyzedListener listener) {
        //暂时，若之后有更多命令需要解析，则建立像FrameBuilder一样的FrameAnalyser
        if (realDataZoneLength != 6) {
            return;
        }
        int position = realDataZoneStart;
        listener.onTimeSynchronizationFinished(data[position],
                data[++position] + 2000,
                data[++position],
                data[++position],
                data[++position],
                data[++position]);
    }

    public byte[] makeGeneralCommandFrame(@NonNull FrameBuilder builder, byte commandCode) {
        return builder.makeFrame(commandCode);
    }

    public byte[] makeDataRequestFrame() {
        return makeGeneralCommandFrame(new EmptyDataZoneFrameBuilder(), COMMAND_CODE_REQUEST_DATA);
    }

    public byte[] makeTimeSynchronizationFrame() {
        return makeGeneralCommandFrame(new TimeSynchronizationFrameBuilder(), COMMAND_CODE_TIME_SYNCHRONIZATION);
    }

    public static abstract class FrameBuilder {

        public byte[] makeFrame(byte commandCode) {
            int dataZoneLength = getDataZoneLength();
            byte[] frame = new byte[MIN_FRAME_LENGTH + dataZoneLength];
            int offset = 0;
            frame[offset] = START_CHARACTER[0];
            frame[++offset] = START_CHARACTER[1];
            frame[++offset] = DEFAULT_BASE_STATION_ADDRESS_UP_BIT;
            frame[++offset] = DEFAULT_BASE_STATION_ADDRESS_DOWN_BIT;
            frame[++offset] = (byte) (dataZoneLength + COMMAND_CODE_LENGTH);
            frame[++offset] = commandCode;
            fillDataZone(frame, ++offset);
            offset += dataZoneLength;
            int crc16 = Crc.calc16ByMsb(frame, START_CHARACTER.length, offset - START_CHARACTER.length);
            frame[offset] = (byte)(crc16 & 0xff);
            frame[++offset] = (byte) (crc16 >> 8);
            frame[++offset] = END_CHARACTER[0];
            frame[++offset] = END_CHARACTER[1];
            return frame;
        }

        //返回除命令码之外的数据域长度
        protected abstract int getDataZoneLength();

        protected abstract void fillDataZone(byte[] frame, int offset);
    }

    private static class EmptyDataZoneFrameBuilder extends FrameBuilder {

        @Override
        protected int getDataZoneLength() {
            return 0;
        }

        @Override
        protected void fillDataZone(byte[] frame, int offset) {
        }
    }

    private static class TimeSynchronizationFrameBuilder extends FrameBuilder {

        @Override
        protected int getDataZoneLength() {
            return 6;
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
