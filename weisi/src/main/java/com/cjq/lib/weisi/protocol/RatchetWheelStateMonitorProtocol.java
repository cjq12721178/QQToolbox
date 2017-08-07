package com.cjq.lib.weisi.protocol;

import com.cjq.tool.qbox.util.NumericConverter;

/**
 * Created by CJQ on 2017/7/10.
 */

public class RatchetWheelStateMonitorProtocol implements Constant {

    private static final int CONTROL_ZONE_LENGTH = 2;
    private static final int TIME_LENGTH = 4;
    private static final int ADDRESS_LENGTH = 4;
    private static final int TIMESTAMP_LENGTH = 4;
    private static final int FUNCTION_CODE_QUERY_REAL_TIME_DATA = 0x10 << 3;
    private static final int FUNCTION_CODE_QUERY_HISTORY_DATA = 0x11 << 3;
    private static final int DIRECTION_UP = 0 << 2;
    private static final int DIRECTION_DOWN = 1 << 2;
    private static final int FIRST_FRAME = 1 << 1;
    private static final int FINAL_FRAME = 1;
    private static final int MAX_FRAME_SERIAL_NUMBER = 7 << 5;

    private final byte[] mQueryRealTimeDataFrame;
    private final byte[] mQueryHistoryDataFrame;

    public RatchetWheelStateMonitorProtocol() {
        mQueryRealTimeDataFrame = new byte[CONTROL_ZONE_LENGTH + CRC16_LENGTH];
        mQueryHistoryDataFrame = new byte[CONTROL_ZONE_LENGTH + TIME_LENGTH + TIME_LENGTH + CRC16_LENGTH];
    }

    public byte[] makeQueryRealTimeDataFrame(int frameSerialNumber) {
        setControlZone(mQueryRealTimeDataFrame,
                FUNCTION_CODE_QUERY_REAL_TIME_DATA,
                frameSerialNumber);
        setCrc(mQueryRealTimeDataFrame);
        return mQueryRealTimeDataFrame;
    }

    public byte[] makeQueryHistoryDataFrame(int frameSerialNumber, long startTime, long endTime) {
        setControlZone(mQueryHistoryDataFrame,
                FUNCTION_CODE_QUERY_HISTORY_DATA,
                frameSerialNumber);
        setTimeSpan(mQueryHistoryDataFrame, startTime, endTime);
        setCrc(mQueryHistoryDataFrame);
        return mQueryHistoryDataFrame;
    }

    //控制域CF
    //MSBit                                                             LSBit
    //FC	DIR	    FIR	    FIN	    FSN	    DL
    //5 bit	1 bit	1 bit	1 bit	3 bit	5 bit

    //FC	帧功能码。
    // 功能码可分为查询指令、控制指令、配置指令这三大类。
    // 从机应答主机命令时，返回相同的功能码。

    //DIR	传输方向。
    // 0b：上行，从机至主机。
    // 1b：下行，主机至从机。

    //FIR	响应帧起始帧标志。
    // 1b：第一帧/起始帧。

    //FIN	响应帧结束帧标志。
    // 1b：最后一帧/结束帧。

    //FIR/FIN
    // 00b：多帧，中间帧。
    // 01b：多帧，结束帧。
    // 10b：多帧，起始帧。
    // 11b：单帧

    //FSN	帧序列号。主机、从机有独立的FSN计数器，每次发送数据帧后FSN循环加1递增，数值范围为0～7。

    //DL	数据域（DF）长度，数值范围为0～16。
    private void setControlZone(byte[] frame, int functionCode, int frameSerialNumber) {
        frame[0] = (byte) ((frameSerialNumber << 5) | frame.length - CONTROL_ZONE_LENGTH - CRC16_LENGTH);
        frame[1] = (byte) (functionCode | DIRECTION_DOWN | FIRST_FRAME | FINAL_FRAME);
    }

    private void setTimeSpan(byte[] frame, long startTime, long endTime) {
        NumericConverter.floatToBytesByLSB(startTime / 1000, frame, CONTROL_ZONE_LENGTH);
        NumericConverter.floatToBytesByLSB(endTime / 1000, frame, CONTROL_ZONE_LENGTH + TIME_LENGTH);
    }

    private void setCrc(byte[] frame) {
        int crcPos = frame.length - CRC16_LENGTH;
        int crc16 = Crc.calc16ByMSB(frame, 0, crcPos);
        frame[crcPos] = (byte) (crc16 & 0xff);
        frame[crcPos + 1] = (byte) (crc16 & 0xff00);
    }

    public void analyze(byte[] frame, OnDataAnalyzedListener listener) {
        if (listener == null) {
            return;
        }
        if (!Crc.isCorrect16(frame, true)) {
            return;
        }
        int dataZoneLength = frame[0] & 0x1F;
        if (dataZoneLength + CONTROL_ZONE_LENGTH + CRC16_LENGTH != frame.length) {
            return;
        }
        if ((frame[1] & DIRECTION_DOWN) != 0) {
            return;
        }
        int functionCode = (frame[1] & 0xF8);
        if (functionCode == FUNCTION_CODE_QUERY_REAL_TIME_DATA) {
            listener.onRealTimeDataAnalyzed(frame[CONTROL_ZONE_LENGTH],
                    NumericConverter.int8toUInt32ByLSB(frame,
                            CONTROL_ZONE_LENGTH + ADDRESS_LENGTH) * 1000,
                    NumericConverter.bytesToFloatByLSB(frame,
                            CONTROL_ZONE_LENGTH + ADDRESS_LENGTH + TIMESTAMP_LENGTH));
        } else if (functionCode == FUNCTION_CODE_QUERY_HISTORY_DATA) {
            if ((frame[1] & FIRST_FRAME) != 0) {
                listener.onFirstHistoryDataAnalyzed();
            }
            long timestamp = NumericConverter.int8toUInt32ByLSB(frame,
                    CONTROL_ZONE_LENGTH) * 1000;
            listener.onHistoryDataAnalyzed(frame[CONTROL_ZONE_LENGTH + TIMESTAMP_LENGTH],
                    timestamp,
                    NumericConverter.bytesToFloatByLSB(frame, CONTROL_ZONE_LENGTH
                                    + TIMESTAMP_LENGTH
                                    + DATA_TYPE_VALUE_LENGTH));
            listener.onHistoryDataAnalyzed(frame[CONTROL_ZONE_LENGTH
                    + TIMESTAMP_LENGTH
                    + DATA_TYPE_VALUE_LENGTH
                    + RAW_VALUE_LENGTH],
                    timestamp,
                    NumericConverter.bytesToFloatByLSB(frame, CONTROL_ZONE_LENGTH
                            + TIMESTAMP_LENGTH
                            + DATA_TYPE_VALUE_LENGTH
                            + RAW_VALUE_LENGTH
                            + DATA_TYPE_VALUE_LENGTH));
            if ((frame[1] & FINAL_FRAME) != 0) {
                listener.onLastHistoryDataAnalyzed();
            }
        }
    }

    public interface OnDataAnalyzedListener {
        void onRealTimeDataAnalyzed(byte dataTypeValue, long timestamp, double rawValue);
        void onFirstHistoryDataAnalyzed();
        void onHistoryDataAnalyzed(byte dataTypeValue, long timestamp, double rawValue);
        void onLastHistoryDataAnalyzed();
    }
}
