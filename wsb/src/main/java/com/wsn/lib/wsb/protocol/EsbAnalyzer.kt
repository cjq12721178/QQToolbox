package com.wsn.lib.wsb.protocol


import com.wsn.lib.wsb.util.NumericConverter

import java.util.Arrays
import java.util.Calendar
import java.util.GregorianCalendar
import kotlin.experimental.and

/**
 * Created by CJQ on 2018/1/10.
 */

open class EsbAnalyzer : Analyzable {

    override fun analyzeTimestamp(src: ByteArray, position: Int): Long {
        var calendarPos = position
        //若基站或者其他网关设备没有打上时间戳，取本地系统时间
        //        src[calendarPos] == 0
        //                && src[calendarPos + 1] == 0
        //                && src[calendarPos + 2] == 0
        //                && src[calendarPos + 3] == 0
        //                && src[calendarPos + 4] == 0
        if (src[calendarPos].toInt() == 0 || src[calendarPos + 1].toInt() == 0) {
            return System.currentTimeMillis()
        } else {
            val lastTime = TIMESTAMP_BUILDER.timeInMillis
            var currTime: Long
            TIMESTAMP_BUILDER.set(ADJUSTED_YEAR,
                    (src[calendarPos] and 0x0f) - 1,
                    src[++calendarPos].toInt(),
                    src[++calendarPos].toInt(),
                    src[++calendarPos].toInt(),
                    src[++calendarPos].toInt())
            currTime = TIMESTAMP_BUILDER.timeInMillis
            //跨年修正，属于那种基本不会发生的情况
            if (currTime < lastTime) {
                ADJUSTED_YEAR = GregorianCalendar().get(Calendar.YEAR)
                TIMESTAMP_BUILDER.set(Calendar.YEAR, ADJUSTED_YEAR)
                currTime = TIMESTAMP_BUILDER.timeInMillis
            }
            return currTime
        }
    }

    fun analyzeRawValue(src: ByteArray, position: Int, dataTypeValue: Byte): Double {
        return getValueBuilder(dataTypeValue).buildRawValue(src[position], src[position + 1])
    }

    private fun getValueBuilder(dataTypeValue: Byte): ValueBuilder {
        return VALUE_BUILDERS[NumericConverter.int8ToUInt16(dataTypeValue)]
    }

    open fun analyzeBatteryVoltage(voltage: Byte, sensorAddress: Int): Float {
        return when {
            sensorAddress >= VOLTAGE_DIVIDE_VALUE -> voltage * Analyzable.BATTERY_VOLTAGE_COEFFICIENT
            voltage > 0 -> VOLTAGE_UP_CONVERSION_VALUE / voltage
            else -> 0f
        }
    }

    internal class ValueBuilder internal constructor(//0：模拟量
            //1：状态量
            //2：计数量
            private val mValueType: Int, private val mSigned: Boolean, private val mCoefficient: Double) {

        fun buildRawValue(high: Byte, low: Byte): Double {
            return when (mValueType) {
                0 -> (if (mSigned)
                    NumericConverter.int8ToInt32(high, low)
                else
                    NumericConverter.int8ToUInt16(high, low)) * mCoefficient
                1 -> (if (low.toInt() == 0x10 || low.toInt() == 1) 1 else 0).toDouble()
                2 -> NumericConverter.int8ToUInt16(low).toDouble()
                else -> (if (mSigned)
                    NumericConverter.int8ToInt32(high, low)
                else
                    NumericConverter.int8ToUInt16(high, low)) * mCoefficient
            }
        }
    }

    companion object {

        private val TIMESTAMP_BUILDER = GregorianCalendar()
        private var ADJUSTED_YEAR = TIMESTAMP_BUILDER.get(Calendar.YEAR)
        private const val VOLTAGE_UP_CONVERSION_VALUE = 307.2f
        private const val VOLTAGE_DIVIDE_VALUE = 32768

        internal val VALUE_BUILDERS: Array<ValueBuilder>

        init {
            val size = 256
            val builder = ValueBuilder(0, true, 3.0)
            VALUE_BUILDERS = Array(size) {
                builder
            }
        }

        @JvmStatic
        fun setValueBuilder(dataTypeValue: Byte, valueType: Int, signed: Boolean, coefficient: Double) {
            VALUE_BUILDERS[NumericConverter.int8ToUInt16(dataTypeValue)] = ValueBuilder(valueType, signed, coefficient)
        }
    }
}
