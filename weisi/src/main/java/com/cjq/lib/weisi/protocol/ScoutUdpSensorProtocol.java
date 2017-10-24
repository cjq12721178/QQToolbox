package com.cjq.lib.weisi.protocol;

import com.cjq.lib.weisi.sensor.ValueBuildDelegator;
import com.cjq.tool.qbox.util.NumericConverter;

/**
 * Created by CJQ on 2017/8/30.
 */

public class ScoutUdpSensorProtocol implements Constant {

    public static final byte COMMAND_CODE_REQUEST_DATA = 0x35;
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
    private static final int SENSOR_CRC_LENGTH = 2;
    private static final byte[] START_CHARACTER = new byte[] { (byte)0xAA, (byte)0xAA };
    private static final byte[] END_CHARACTER = new byte[] { 0x55, 0x55 };
    private static final int MIN_FRAME_LENGTH = START_CHARACTER.length
            + BASE_STATION_ADDRESS_LENGTH
            + DATA_ZONE_LENGTH_LENGTH
            + COMMAND_CODE_LENGTH
            + CRC16_LENGTH
            + END_CHARACTER.length;

    private final ValueBuildDelegator mValueBuildDelegator = new ValueBuildDelegator();

    public void analyze(byte[] udpData, OnDataAnalyzedListener listener) {
        //判断数据是否为空，以及数据长度是否大于最小数据长度
        if (udpData == null || listener == null || udpData.length < MIN_FRAME_LENGTH) {
            return;
        }

        //记录数据域长度
        int dataZoneLength = NumericConverter.int8ToUInt16(udpData[START_CHARACTER.length + BASE_STATION_ADDRESS_LENGTH]) - COMMAND_CODE_LENGTH;
        //计算实际数据长度
        int realDataLength = MIN_FRAME_LENGTH + dataZoneLength;
        //检查起始符和结束符
        if (udpData[0] != START_CHARACTER[0]
                || udpData[1] != START_CHARACTER[1]
                || udpData[realDataLength - 2] != END_CHARACTER[0]
                || udpData[realDataLength - 1] != END_CHARACTER[1]) {
            return;
        }

        //计算CRC16并校验
        if (!Crc.isCorrect16(udpData,
                START_CHARACTER.length,
                BASE_STATION_ADDRESS_LENGTH
                        + DATA_ZONE_LENGTH_LENGTH
                        + COMMAND_CODE_LENGTH
                        + dataZoneLength,
                true,
                false)) {
            return;
        }

        //获取并校验命令码
        int commandCode = (byte)(udpData[START_CHARACTER.length
                + BASE_STATION_ADDRESS_LENGTH
                + DATA_ZONE_LENGTH_LENGTH] -
                    FIXED_DIFFERENCE_FROM_COMMAND_TO_RESPONSE);

        //目前只需要这一个命令
        if (commandCode != COMMAND_CODE_REQUEST_DATA) {
            return;
        }

        //解析数据帧
        mValueBuildDelegator.setData(udpData);
        for (int start = START_CHARACTER.length
                + BASE_STATION_ADDRESS_LENGTH
                + DATA_ZONE_LENGTH_LENGTH
                + COMMAND_CODE_LENGTH,
                end = dataZoneLength / SENSOR_DATA_LENGTH * SENSOR_DATA_LENGTH,
                sensorValuePos,
                calendarPos,
                voltagePos;
                start < end;
                start += SENSOR_DATA_LENGTH) {
            if (Crc.calc8(udpData, start, SENSOR_DATA_LENGTH - 1) != udpData[start + SENSOR_DATA_LENGTH - 1]) {
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
            listener.onDataAnalyzed(NumericConverter.int8ToUInt16(udpData[start], udpData[start + 1]),
                    udpData[start + SENSOR_ADDRESS_LENGTH],
                    0,
                    mValueBuildDelegator
                            .setTimestampIndex(calendarPos)
                            .setRawValueIndex(sensorValuePos)
                            .setBatteryVoltageIndex(voltagePos));
        }
    }

    public byte[] makeDataRequestFrame() {
        return new EmptyDataZoneFrameBuilder().makeFrame(COMMAND_CODE_REQUEST_DATA);
    }

    private static abstract class FrameBuilder {

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
}
