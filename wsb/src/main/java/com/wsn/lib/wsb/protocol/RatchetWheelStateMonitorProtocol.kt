package com.wsn.lib.wsb.protocol


import com.wsn.lib.wsb.util.NumericConverter
import kotlin.experimental.and

/**
 * Created by CJQ on 2017/7/10.
 */

class RatchetWheelStateMonitorProtocol {

    private val queryRealTimeDataFrame: ByteArray
    private val queryHistoryDataFrame: ByteArray

    init {
        queryRealTimeDataFrame = ByteArray(CONTROL_ZONE_LENGTH + CRC16_LENGTH)
        queryHistoryDataFrame = ByteArray(CONTROL_ZONE_LENGTH + TIME_LENGTH + TIME_LENGTH + CRC16_LENGTH)
    }

    fun makeQueryRealTimeDataFrame(frameSerialNumber: Int): ByteArray {
        setControlZone(queryRealTimeDataFrame,
                FUNCTION_CODE_QUERY_REAL_TIME_DATA,
                frameSerialNumber)
        setCrc(queryRealTimeDataFrame)
        return queryRealTimeDataFrame
    }

    fun makeQueryHistoryDataFrame(frameSerialNumber: Int, startTime: Long, endTime: Long): ByteArray {
        setControlZone(queryHistoryDataFrame,
                FUNCTION_CODE_QUERY_HISTORY_DATA,
                frameSerialNumber)
        setTimeSpan(queryHistoryDataFrame, startTime, endTime)
        setCrc(queryHistoryDataFrame)
        return queryHistoryDataFrame
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
    private fun setControlZone(frame: ByteArray, functionCode: Int, frameSerialNumber: Int) {
        frame[0] = (frameSerialNumber shl 5 or frame.size - CONTROL_ZONE_LENGTH - CRC16_LENGTH).toByte()
        frame[1] = (functionCode or DIRECTION_DOWN or FIRST_FRAME or FINAL_FRAME).toByte()
    }

    private fun setTimeSpan(frame: ByteArray, startTime: Long, endTime: Long) {
        NumericConverter.floatToBytesByLSB((startTime / 1000).toFloat(), frame, CONTROL_ZONE_LENGTH)
        NumericConverter.floatToBytesByLSB((endTime / 1000).toFloat(), frame, CONTROL_ZONE_LENGTH + TIME_LENGTH)
    }

    private fun setCrc(frame: ByteArray) {
        val crcPos = frame.size - CRC16_LENGTH
        val crc16 = Crc.ccitt.calc16ByMsb(frame, 0, crcPos)
        //int crc16 = CrcClass.calc16CcittByMsb(frame, 0, crcPos);
        frame[crcPos] = (crc16 and 0xff).toByte()
        frame[crcPos + 1] = (crc16 and 0xff00).toByte()
    }

    fun analyze(frame: ByteArray, listener: OnDataAnalyzedListener?) {
        if (listener == null) {
            return
        }
        if (!Crc.ccitt.isCorrect16WithCrcAppended(
                        frame,
                        true,
                        false)) {
            return
        }
        //        if (!CrcClass.isCorrect16CcittAppendCrc(frame, true, false)) {
        //            return;
        //        }
        val dataZoneLength = frame[0] and 0x1F
        if (dataZoneLength + CONTROL_ZONE_LENGTH + CRC16_LENGTH != frame.size) {
            return
        }
        if ((NumericConverter.int8ToUInt16(frame[1]) and DIRECTION_DOWN) != 0) {
            return
        }
        val functionCode = NumericConverter.int8ToUInt16(frame[1]) and 0xF8
        if (functionCode == FUNCTION_CODE_QUERY_REAL_TIME_DATA) {
            listener.onRealTimeDataAnalyzed(frame[CONTROL_ZONE_LENGTH],
                    NumericConverter.int8toUInt32ByLSB(frame,
                            CONTROL_ZONE_LENGTH + ADDRESS_LENGTH) * 1000,
                    NumericConverter.bytesToFloatByLSB(frame,
                            CONTROL_ZONE_LENGTH + ADDRESS_LENGTH + TIMESTAMP_LENGTH).toDouble())
        } else if (functionCode == FUNCTION_CODE_QUERY_HISTORY_DATA) {
            if (NumericConverter.int8ToUInt16(frame[1]) and FIRST_FRAME != 0) {
                listener.onFirstHistoryDataAnalyzed()
            }
            val timestamp = NumericConverter.int8toUInt32ByLSB(frame,
                    CONTROL_ZONE_LENGTH) * 1000
            listener.onHistoryDataAnalyzed(frame[CONTROL_ZONE_LENGTH + TIMESTAMP_LENGTH],
                    timestamp,
                    NumericConverter.bytesToFloatByLSB(frame, CONTROL_ZONE_LENGTH
                            + TIMESTAMP_LENGTH
                            + DATA_TYPE_VALUE_LENGTH).toDouble())
            listener.onHistoryDataAnalyzed(frame[CONTROL_ZONE_LENGTH
                    + TIMESTAMP_LENGTH
                    + DATA_TYPE_VALUE_LENGTH
                    + RAW_VALUE_LENGTH],
                    timestamp,
                    NumericConverter.bytesToFloatByLSB(frame, CONTROL_ZONE_LENGTH
                            + TIMESTAMP_LENGTH
                            + DATA_TYPE_VALUE_LENGTH
                            + RAW_VALUE_LENGTH
                            + DATA_TYPE_VALUE_LENGTH).toDouble())
            if (NumericConverter.int8ToUInt16(frame[1]) and FINAL_FRAME != 0) {
                listener.onLastHistoryDataAnalyzed()
            }
        }
    }

    interface OnDataAnalyzedListener {
        fun onRealTimeDataAnalyzed(dataTypeValue: Byte, timestamp: Long, rawValue: Double)
        fun onFirstHistoryDataAnalyzed()
        fun onHistoryDataAnalyzed(dataTypeValue: Byte, timestamp: Long, rawValue: Double)
        fun onLastHistoryDataAnalyzed()
    }

    companion object {

        private const val CONTROL_ZONE_LENGTH = 2
        private const val TIME_LENGTH = 4
        private const val ADDRESS_LENGTH = 4
        private const val TIMESTAMP_LENGTH = 4
        private const val RAW_VALUE_LENGTH = 4
        private const val FUNCTION_CODE_QUERY_REAL_TIME_DATA = 0x10 shl 3
        private const val FUNCTION_CODE_QUERY_HISTORY_DATA = 0x11 shl 3
        private const val DIRECTION_UP = 0 shl 2
        private const val DIRECTION_DOWN = 1 shl 2
        private const val FIRST_FRAME = 1 shl 1
        private const val FINAL_FRAME = 1
        private const val MAX_FRAME_SERIAL_NUMBER = 7 shl 5
        private const val CRC16_LENGTH = Crc.CRC16_LENGTH
        private const val DATA_TYPE_VALUE_LENGTH = 1
    }
}
