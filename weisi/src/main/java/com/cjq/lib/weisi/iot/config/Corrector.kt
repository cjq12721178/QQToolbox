package com.cjq.lib.weisi.iot.config

import android.os.Parcelable
import com.wsn.lib.wsb.util.NumericConverter

interface Corrector : ByteSerializable, Parcelable {
    fun getType(): Int
    fun correctValue(rawValue: Double): Double

    companion object {
        const val TYPE_NONE = 0
        const val TYPE_LINEAR_FITTING = 1

        /**
         * 这种生成方式相当于解析协议，具体内容如下：
         * 第一个字节表示[Corrector]类型，第二个字节以后按各个校准器的格式解析，其中：
         * 1 - 线性拟合校准器[LinearFittingCorrector]
         * 具体解析见[buildLinearFittingCorrector]
         */
        fun buildCorrector(data: ByteArray): Corrector? {
            if (data.isEmpty()) {
                return null
            }
            return when (NumericConverter.int8ToUInt16(data[0])) {
                TYPE_LINEAR_FITTING -> buildLinearFittingCorrector(data)
                else -> null
            }
        }

        fun buildCorrector(type: Int): Corrector? {
            return when (type) {
                TYPE_LINEAR_FITTING -> LinearFittingCorrector(FloatArray(0), FloatArray(0), "kN", "")
                else -> null
            }
        }

        /**
         *   第二字节表示校准值单位长度(cn)，
         *   其后的cn个字节可转换为[LinearFittingCorrector.correctedValueUnit]
         *   第(2 + cn)个字节表示采样值单位长度(sn)，
         *   其后的sn个字节可转换为[LinearFittingCorrector.samplingValueUnit]
         *   第(2 + cn + 1 + sn)个字节表示校准值[LinearFittingCorrector.correctedValueUnit]
         *   和采样值[LinearFittingCorrector.samplingValueUnit]
         *   的对数(n)，其中校准值为4字节，采样值为4字节，
         *   即之后应有n * (4 + 4)个字节
         */
        private fun buildLinearFittingCorrector(data: ByteArray): LinearFittingCorrector? {
            var offset = 1
            if (data.size <= offset) {
                return null
            }
            val correctValueUnitLength = NumericConverter.int8ToUInt16(data[offset++])
            if (data.size <= offset + correctValueUnitLength) {
                return null
            }
            val correctValueUnit = String(data, offset, correctValueUnitLength)
            offset += correctValueUnitLength
            val samplingValueUnitLength = NumericConverter.int8ToUInt16(data[offset++])
            if (data.size <= offset + samplingValueUnitLength) {
                return null
            }
            val samplingValueUnit = String(data, offset, samplingValueUnitLength)
            offset += samplingValueUnitLength
            val groupCount = NumericConverter.int8ToUInt16(data[offset++])
            if (data.size != offset + groupCount * 2 * 4) {
                return null
            }
            val correctedValues = FloatArray(groupCount) { i ->
                NumericConverter.bytesToFloatByMSB(data, i * 4 + offset)
            }

            offset += groupCount * 4
            val samplingValues = FloatArray(groupCount) { i ->
                NumericConverter.bytesToFloatByMSB(data, i * 4 + offset)
            }
            return LinearFittingCorrector(correctedValues, samplingValues,
                    correctValueUnit, samplingValueUnit)
        }

        fun toByteArray(corrector: Corrector?): ByteArray {
            return corrector?.toByteArray() ?: ByteArray(0)
        }
    }
}
