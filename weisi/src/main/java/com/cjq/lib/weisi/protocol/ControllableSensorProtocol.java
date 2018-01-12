package com.cjq.lib.weisi.protocol;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.util.NumericConverter;


/**
 * Created by CJQ on 2018/1/9.
 */

public abstract class ControllableSensorProtocol<A extends Analyzable>
        extends BaseSensorProtocol<A> {

    private static final byte DEFAULT_BASE_STATION_ADDRESS_HIGH = (byte) 0xFF;
    private static final byte DEFAULT_BASE_STATION_ADDRESS_LOW = (byte) 0xFF;
    private static final int BASE_STATION_ADDRESS_LENGTH = 2;
    protected static final int DATA_ZONE_LENGTH_LENGTH = 1;
    private static final int COMMAND_CODE_LENGTH = 1;
    private static final byte FIXED_DIFFERENCE_FROM_COMMAND_TO_RESPONSE = (byte)0x80;
    private static final byte[] START_CHARACTER = new byte[] { (byte)0xAA, (byte)0xAA };
    private static final byte[] END_CHARACTER = new byte[] { 0x55, 0x55 };
    private static final int DATA_ZONE_POSITION = START_CHARACTER.length
            + BASE_STATION_ADDRESS_LENGTH
            + DATA_ZONE_LENGTH_LENGTH;
    private static final int MIN_FRAME_LENGTH = DATA_ZONE_POSITION
            + COMMAND_CODE_LENGTH
            + CRC16_LENGTH
            + END_CHARACTER.length;

    protected ControllableSensorProtocol(A analyzer) {
        super(analyzer);
    }

    public void analyze(byte[] data, OnFrameAnalyzedListener listener) {
        analyze(data, 0, data.length, listener);
    }

    public void analyze(byte[] data, int offset, int length, OnFrameAnalyzedListener listener) {
        //判断数据是否为空，以及数据长度是否大于最小数据长度
        if (listener == null
                || offset < 0
                || offset + length > data.length
                || length < MIN_FRAME_LENGTH) {
            return;
        }

        //记录实际数据域长度（除去命令码长度之后的长度）
        int realDataZoneLength = NumericConverter.int8ToUInt16(data[offset + START_CHARACTER.length + BASE_STATION_ADDRESS_LENGTH]) - COMMAND_CODE_LENGTH;
        //计算实际数据长度
        int realDataLength = MIN_FRAME_LENGTH + realDataZoneLength;
        //检查起始符和结束符
        if (data[offset] != START_CHARACTER[0]
                || data[offset + 1] != START_CHARACTER[1]
                || data[offset + realDataLength - 1] != END_CHARACTER[1]
                /* || data[offset + realDataLength - 2] != END_CHARACTER[0] */) {
            return;
        }

        //计算CRC16并校验
        if (!getCrc().isCorrect16WithCrcAppended(
                data,
                offset + START_CHARACTER.length,
                BASE_STATION_ADDRESS_LENGTH
                        + DATA_ZONE_LENGTH_LENGTH
                        + COMMAND_CODE_LENGTH
                        + realDataZoneLength,
                true,
                isCrcMsb())) {
            return;
        }
//        if (!CrcClass.isCorrect16(data,
//                offset + START_CHARACTER.length,
//                BASE_STATION_ADDRESS_LENGTH
//                        + DATA_ZONE_LENGTH_LENGTH
//                        + COMMAND_CODE_LENGTH
//                        + realDataZoneLength,
//                true,
//                false)) {
//            return;
//        }

        analyzeDataZone(data, offset + DATA_ZONE_POSITION, realDataZoneLength, listener);
    }

    private void analyzeDataZone(byte[] data, int dataZoneStart, int realDataZoneLength, OnFrameAnalyzedListener listener) {
        //获取并校验命令码
        int commandCode = (byte)(data[dataZoneStart] -
                FIXED_DIFFERENCE_FROM_COMMAND_TO_RESPONSE);

        //目前只需要这两个命令
        if (commandCode == getDataRequestCommandCode()) {
            //解析数据帧
            onDataAnalyzed(data, dataZoneStart + COMMAND_CODE_LENGTH, realDataZoneLength, listener);
        } else if (commandCode == getTimeSynchronizationCommandCode()) {
            onTimeSynchronizationAnalyzed(data, dataZoneStart + COMMAND_CODE_LENGTH, realDataZoneLength, listener);
        }
    }

    public abstract byte getDataRequestCommandCode();

    public abstract byte getTimeSynchronizationCommandCode();

    public int analyzeMultiplePackages(byte[] data, int offset, int length, OnFrameAnalyzedListener listener) {
        if (listener == null) {
            throw new NullPointerException("it is no meaning that listener is null");
        }
        if (data == null
                || offset < 0
                || offset + length > data.length) {
            return 0;
        }

        int start = offset;
        for (int end = offset + length,
             realDataZoneLength,
             realDataLength;
             start < end;
             start += realDataLength) {
            //查找起始字符位置
            while (start < end) {
                while (data[start++] != START_CHARACTER[0] && start < end);
                if (start >= end) {
                    break;
                }
                if (data[start++] == START_CHARACTER[1]) {
                    start -= START_CHARACTER.length;
                    break;
                }
            }
            if (start >= end) {
                break;
            }
            //判断数据长度是否大于最小数据长度
            if (start + MIN_FRAME_LENGTH > end) {
                break;
            }
            //记录实际数据域长度（除去命令码长度之后的长度）
            realDataZoneLength = NumericConverter.int8ToUInt16(data[start + START_CHARACTER.length + BASE_STATION_ADDRESS_LENGTH]) - COMMAND_CODE_LENGTH;
            //计算实际数据长度
            realDataLength = MIN_FRAME_LENGTH + realDataZoneLength;
            //检查结束符
            //本来还应该检查udpData[start + realDataLength - 2] != END_CHARACTER[0]
            //可惜部分硬件很坑爹，结束符前会多一个字节。。
            if (data[start + realDataLength - 1] != END_CHARACTER[1]) {
                continue;
            }
            //计算CRC16并校验
            if (!getCrc().isCorrect16WithCrcAppended(
                    data,
                    start + START_CHARACTER.length,
                    BASE_STATION_ADDRESS_LENGTH
                            + DATA_ZONE_LENGTH_LENGTH
                            + COMMAND_CODE_LENGTH
                            + realDataZoneLength,
                    true,
                    isCrcMsb())) {
                continue;
            }
//            if (!CrcClass.isCorrect16(data,
//                    start + START_CHARACTER.length,
//                    BASE_STATION_ADDRESS_LENGTH
//                            + DATA_ZONE_LENGTH_LENGTH
//                            + COMMAND_CODE_LENGTH
//                            + realDataZoneLength,
//                    true,
//                    false)) {
//                continue;
//            }

            analyzeDataZone(data, start + DATA_ZONE_POSITION, realDataZoneLength, listener);
        }
        return start - offset;
    }

    protected abstract void onDataAnalyzed(byte[] data,
                                           int realDataZoneStart,
                                           int realDataZoneLength,
                                           OnFrameAnalyzedListener listener);

//    private void onSensorInfoAnalyzed(byte[] data,
//                                int realDataZoneStart,
//                                int realDataZoneLength,
//                                OnFrameAnalyzedListener listener) {
//        mValueBuildDelegator.setData(data);
//        for (int start = realDataZoneStart,
//             end = realDataZoneLength / SENSOR_DATA_LENGTH * SENSOR_DATA_LENGTH,
//             sensorValuePos,
//             calendarPos,
//             voltagePos;
//             start < end;
//             start += SENSOR_DATA_LENGTH) {
//            if (CrcClass.calc8(data, start, SENSOR_DATA_LENGTH - 1) != data[start + SENSOR_DATA_LENGTH - 1]) {
//                continue;
//            }
//            sensorValuePos = start +
//                    SENSOR_ADDRESS_LENGTH +
//                    DATA_TYPE_VALUE_LENGTH +
//                    SENSOR_DATA_RESERVE1_LENGTH;
//            voltagePos = sensorValuePos + SENSOR_VALUE_LENGTH;
//            calendarPos = voltagePos +
//                    SENSOR_BATTERY_VOLTAGE_LENGTH +
//                    SENSOR_DATA_RESERVE2_LENGTH;
//            listener.onSensorInfoAnalyzed(NumericConverter.int8ToUInt16(data[start], data[start + 1]),
//                    data[start + SENSOR_ADDRESS_LENGTH],
//                    0,
//                    mValueBuildDelegator
//                            .setTimestampIndex(calendarPos)
//                            .setRawValueIndex(sensorValuePos)
//                            .setBatteryVoltageIndex(voltagePos));
//        }
//    }

    protected abstract void onTimeSynchronizationAnalyzed(byte[] data,
                                                          int realDataZoneStart,
                                                          int realDataZoneLength,
                                                          OnFrameAnalyzedListener listener);

//    //BLE电压
//    protected float analyzeBatteryVoltage(byte voltage) {
//        return voltage < 0
//                ? voltage
//                : voltage * BATTERY_VOLTAGE_COEFFICIENT;
//    }
//
//    //ESB电压
//    protected float analyzeBatteryVoltage(byte voltage, int sensorAddress) {
//        return sensorAddress < VOLTAGE_DIVIDE_VALUE
//                ? voltage * BATTERY_VOLTAGE_COEFFICIENT
//                : (voltage != 0
//                ? VOLTAGE_UP_CONVERSION_VALUE / voltage
//                : 0f);
//    }

//    private void onTimeSynchronizationAnalyzed(byte[] data,
//                                               int realDataZoneStart,
//                                               int realDataZoneLength,
//                                               OnFrameAnalyzedListener listener) {
//        //暂时，若之后有更多命令需要解析，则建立像FrameBuilder一样的FrameAnalyser
//        if (realDataZoneLength != 6) {
//            return;
//        }
//        int position = realDataZoneStart;
//        listener.onTimeSynchronizationAnalyzed(data[position],
//                data[++position] + 2000,
//                data[++position],
//                data[++position],
//                data[++position],
//                data[++position]);
//    }

    public byte[] makeGeneralCommandFrame(@NonNull FrameBuilder builder) {
        return builder.build();
    }

    public byte[] makeDataRequestFrame() {
        return makeGeneralCommandFrame(getEmptyDataZoneFrameBuilder(getDataRequestCommandCode()));
    }

    public EmptyDataZoneFrameBuilder getEmptyDataZoneFrameBuilder(byte commandCode) {
        return new EmptyDataZoneFrameBuilder(commandCode);
    }

    public byte[] makeTimeSynchronizationFrame() {
        return makeGeneralCommandFrame(getTimeSynchronizationFrameBuilder());
    }

    public abstract TimeSynchronizationFrameBuilder getTimeSynchronizationFrameBuilder();

    public abstract class FrameBuilder {

        private final byte mCommandCode;
        private byte mBaseStationAddressHigh;
        private byte mBaseStationAddressLow;

        protected FrameBuilder(byte commandCode) {
            mCommandCode = commandCode;
            mBaseStationAddressHigh = DEFAULT_BASE_STATION_ADDRESS_HIGH;
            mBaseStationAddressLow = DEFAULT_BASE_STATION_ADDRESS_LOW;
        }

        public byte[] build() {
            int dataZoneLength = getDataZoneLength();
            byte[] frame = new byte[MIN_FRAME_LENGTH + dataZoneLength];
            int offset = 0;
            frame[offset] = START_CHARACTER[0];
            frame[++offset] = START_CHARACTER[1];
            frame[++offset] = mBaseStationAddressHigh;
            frame[++offset] = mBaseStationAddressLow;
            //frame[++offset] = DEFAULT_BASE_STATION_ADDRESS_HIGH;
            //frame[++offset] = DEFAULT_BASE_STATION_ADDRESS_LOW;
            frame[++offset] = (byte) (dataZoneLength + COMMAND_CODE_LENGTH);
            frame[++offset] = mCommandCode;
            fillDataZone(frame, ++offset);
            offset += dataZoneLength;
            int crc16 = getCrc().calc16ByMsb(frame, START_CHARACTER.length, offset - START_CHARACTER.length);
            //int crc16 = CrcClass.calc16WeisiByMsb(frame, START_CHARACTER.length, offset - START_CHARACTER.length);
            if (isCrcMsb()) {
                frame[offset] = (byte) (crc16 >> 8);
                frame[++offset] = (byte) (crc16 & 0xff);
            } else {
                frame[offset] = (byte) (crc16 & 0xff);
                frame[++offset] = (byte) (crc16 >> 8);
            }
            frame[++offset] = END_CHARACTER[0];
            frame[++offset] = END_CHARACTER[1];
            return frame;
        }

        public FrameBuilder setBaseStationAddress(byte high, byte low) {
            mBaseStationAddressHigh = high;
            mBaseStationAddressLow = low;
            return this;
        }

        //返回除命令码之外的数据域长度
        protected abstract int getDataZoneLength();

        protected abstract void fillDataZone(byte[] frame, int offset);
    }

    public class EmptyDataZoneFrameBuilder extends FrameBuilder {

        public EmptyDataZoneFrameBuilder(byte commandCode) {
            super(commandCode);
        }

        @Override
        protected int getDataZoneLength() {
            return 0;
        }

        @Override
        protected void fillDataZone(byte[] frame, int offset) {
        }
    }

    public abstract class TimeSynchronizationFrameBuilder extends FrameBuilder {

        protected TimeSynchronizationFrameBuilder(byte commandCode) {
            super(commandCode);
        }
    }

}
