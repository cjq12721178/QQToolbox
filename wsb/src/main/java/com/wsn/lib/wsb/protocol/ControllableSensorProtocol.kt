package com.wsn.lib.wsb.protocol

import com.wsn.lib.wsb.util.NumericConverter


/**
 * Created by CJQ on 2018/1/9.
 */

abstract class ControllableSensorProtocol<A : Analyzable> protected constructor(analyzer: A) : BaseSensorProtocol<A>(analyzer) {

    abstract val dataRequestCommandCode: Byte

    abstract val timeSynchronizationCommandCode: Byte

    abstract val timeSynchronizationFrameBuilder: TimeSynchronizationFrameBuilder

    fun analyze(data: ByteArray, listener: OnFrameAnalyzedListener) {
        analyze(data, 0, data.size, listener)
    }

    open fun analyze(data: ByteArray, offset: Int, length: Int, listener: OnFrameAnalyzedListener) {
        //判断数据是否为空，以及数据长度是否大于最小数据长度
        if (offset < 0
                || offset + length > data.size
                || length < MIN_FRAME_LENGTH) {
            return
        }

        //记录实际数据域长度（除去命令码长度之后的长度）
        val realDataZoneLength = NumericConverter.int8ToUInt16(data[offset + START_CHARACTER.size + BASE_STATION_ADDRESS_LENGTH]) - COMMAND_CODE_LENGTH
        //计算实际数据长度
        val realDataLength = MIN_FRAME_LENGTH + realDataZoneLength
        //检查起始符和结束符
        if (data[offset] != START_CHARACTER[0]
                || data[offset + 1] != START_CHARACTER[1]
                || data[offset + realDataLength - 1] != END_CHARACTER[1]) {
            return
        }/* || data[offset + realDataLength - 2] != END_CHARACTER[0] */

        //计算CRC16并校验
        if (!crc.isCorrect16WithCrcAppended(
                        data,
                        offset + START_CHARACTER.size,
                        BASE_STATION_ADDRESS_LENGTH
                                + DATA_ZONE_LENGTH_LENGTH
                                + COMMAND_CODE_LENGTH
                                + realDataZoneLength,
                        true,
                        isCrcMsb)) {
            return
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

        analyzeDataZone(data, offset + DATA_ZONE_POSITION, realDataZoneLength, listener)
    }

    private fun analyzeDataZone(data: ByteArray, dataZoneStart: Int, realDataZoneLength: Int, listener: OnFrameAnalyzedListener) {
        //获取并校验命令码
        val commandCode = (data[dataZoneStart] - FIXED_DIFFERENCE_FROM_COMMAND_TO_RESPONSE).toByte().toInt()

        //目前只需要这两个命令
        if (commandCode == dataRequestCommandCode.toInt()) {
            //解析数据帧
            onDataAnalyzed(data, dataZoneStart + COMMAND_CODE_LENGTH, realDataZoneLength, listener)
        } else if (commandCode == timeSynchronizationCommandCode.toInt()) {
            onTimeSynchronizationAnalyzed(data, dataZoneStart + COMMAND_CODE_LENGTH, realDataZoneLength, listener)
        }
    }

    open fun analyzeMultiplePackages(data: ByteArray, offset: Int, length: Int, listener: OnFrameAnalyzedListener): Int {
        if (offset < 0 || offset + length > data.size) {
            return 0
        }

        var start = offset
        val end = offset + length
        var realDataZoneLength: Int
        var realDataLength: Int
        var handledPosition = 0
        while (start < end) {
            //查找起始字符位置
            while (start < end) {
                while (data[start++] != START_CHARACTER[0] && start < end);
                if (start >= end) {
                    break
                }
                if (data[start++] == START_CHARACTER[1]) {
                    start -= START_CHARACTER.size
                    break
                }
            }
            if (start >= end) {
                break
            }
            //判断数据长度是否大于最小数据长度
            if (start + MIN_FRAME_LENGTH > end) {
                break
            }
            //记录实际数据域长度（除去命令码长度之后的长度）
            realDataZoneLength = NumericConverter.int8ToUInt16(data[start + START_CHARACTER.size + BASE_STATION_ADDRESS_LENGTH]) - COMMAND_CODE_LENGTH
            //计算实际数据长度
            realDataLength = MIN_FRAME_LENGTH + realDataZoneLength
            //检查结束符
            //本来还应该检查udpData[start + realDataLength - 2] != END_CHARACTER[0]
            //可惜部分硬件很坑爹，结束符前会多一个字节。。
            if (data[start + realDataLength - 1] != END_CHARACTER[1]) {
                start += realDataLength
                continue
            }
            //计算CRC16并校验
            if (!crc.isCorrect16WithCrcAppended(
                            data,
                            start + START_CHARACTER.size,
                            BASE_STATION_ADDRESS_LENGTH
                                    + DATA_ZONE_LENGTH_LENGTH
                                    + COMMAND_CODE_LENGTH
                                    + realDataZoneLength,
                            true,
                            isCrcMsb)) {
                start += realDataLength
                continue
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

            analyzeDataZone(data, start + DATA_ZONE_POSITION, realDataZoneLength, listener)
            start += realDataLength
            handledPosition = start
        }
        return handledPosition - offset
    }

    protected abstract fun onDataAnalyzed(data: ByteArray,
                                          realDataZoneStart: Int,
                                          realDataZoneLength: Int,
                                          listener: OnFrameAnalyzedListener)

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

    protected abstract fun onTimeSynchronizationAnalyzed(data: ByteArray,
                                                         realDataZoneStart: Int,
                                                         realDataZoneLength: Int,
                                                         listener: OnFrameAnalyzedListener)

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

    fun makeGeneralCommandFrame(builder: FrameBuilder): ByteArray {
        return builder.build()
    }

    fun makeDataRequestFrame(): ByteArray {
        return makeGeneralCommandFrame(getEmptyDataZoneFrameBuilder(dataRequestCommandCode))
    }

    fun getEmptyDataZoneFrameBuilder(commandCode: Byte): EmptyDataZoneFrameBuilder {
        return EmptyDataZoneFrameBuilder(commandCode)
    }

    fun makeTimeSynchronizationFrame(): ByteArray {
        return makeGeneralCommandFrame(timeSynchronizationFrameBuilder)
    }

    abstract inner class FrameBuilder protected constructor(private val commandCode: Byte) {
        private var baseStationAddressHigh: Byte = 0
        private var baseStationAddressLow: Byte = 0

        //返回除命令码之外的数据域长度
        protected abstract val dataZoneLength: Int

        init {
            baseStationAddressHigh = DEFAULT_BASE_STATION_ADDRESS_HIGH
            baseStationAddressLow = DEFAULT_BASE_STATION_ADDRESS_LOW
        }

        fun build(): ByteArray {
            val dataZoneLength = dataZoneLength
            val frame = ByteArray(MIN_FRAME_LENGTH + dataZoneLength)
            var offset = 0
            frame[offset] = START_CHARACTER[0]
            frame[++offset] = START_CHARACTER[1]
            frame[++offset] = baseStationAddressHigh
            frame[++offset] = baseStationAddressLow
            //frame[++offset] = DEFAULT_BASE_STATION_ADDRESS_HIGH;
            //frame[++offset] = DEFAULT_BASE_STATION_ADDRESS_LOW;
            frame[++offset] = (dataZoneLength + COMMAND_CODE_LENGTH).toByte()
            frame[++offset] = commandCode
            fillDataZone(frame, ++offset)
            offset += dataZoneLength
            val crc16 = crc.calc16ByMsb(frame, START_CHARACTER.size, offset - START_CHARACTER.size)
            //int crc16 = CrcClass.calc16WeisiByMsb(frame, START_CHARACTER.length, offset - START_CHARACTER.length);
            if (isCrcMsb) {
                frame[offset] = (crc16 shr 8).toByte()
                frame[++offset] = (crc16 and 0xff).toByte()
            } else {
                frame[offset] = (crc16 and 0xff).toByte()
                frame[++offset] = (crc16 shr 8).toByte()
            }
            frame[++offset] = END_CHARACTER[0]
            frame[++offset] = END_CHARACTER[1]
            return frame
        }

        fun setBaseStationAddress(high: Byte, low: Byte): FrameBuilder {
            baseStationAddressHigh = high
            baseStationAddressLow = low
            return this
        }

        protected abstract fun fillDataZone(frame: ByteArray, offset: Int)
    }

    inner class EmptyDataZoneFrameBuilder(commandCode: Byte) : FrameBuilder(commandCode) {

        override val dataZoneLength: Int
            get() = 0

        override fun fillDataZone(frame: ByteArray, offset: Int) {}
    }

    abstract inner class TimeSynchronizationFrameBuilder protected constructor(commandCode: Byte) : FrameBuilder(commandCode)

    companion object {

        private const val DEFAULT_BASE_STATION_ADDRESS_HIGH = 0xFF.toByte()
        private const val DEFAULT_BASE_STATION_ADDRESS_LOW = 0xFF.toByte()
        private const val BASE_STATION_ADDRESS_LENGTH = 2
        protected const val DATA_ZONE_LENGTH_LENGTH = 1
        private const val COMMAND_CODE_LENGTH = 1
        private const val FIXED_DIFFERENCE_FROM_COMMAND_TO_RESPONSE = 0x80.toByte()
        private val START_CHARACTER = byteArrayOf(0xAA.toByte(), 0xAA.toByte())
        private val END_CHARACTER = byteArrayOf(0x55, 0x55)
        private val DATA_ZONE_POSITION = (START_CHARACTER.size
                + BASE_STATION_ADDRESS_LENGTH
                + DATA_ZONE_LENGTH_LENGTH)
        private val MIN_FRAME_LENGTH = (DATA_ZONE_POSITION
                + COMMAND_CODE_LENGTH
                + BaseSensorProtocol.CRC16_LENGTH
                + END_CHARACTER.size)
    }

}
