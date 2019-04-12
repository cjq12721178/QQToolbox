package com.cjq.lib.weisi.iot.config

import android.os.Parcel
import android.os.Parcelable
import com.wsn.lib.wsb.util.NumericConverter

class LinearFittingCorrector(private val correctedValues: FloatArray,
                             private val samplingValues: FloatArray,
                             val correctedValueUnit: String,
                             val samplingValueUnit: String) : Corrector {

    private val gradients: FloatArray
    private val offsets: FloatArray

    constructor(parcel: Parcel) : this(
            parcel.createFloatArray() ?: floatArrayOf(),
            parcel.createFloatArray() ?: floatArrayOf(),
            parcel.readString() ?: "",
            parcel.readString() ?: "") {
    }

    init {
        if (correctedValues.size != samplingValues.size) {
            throw IllegalArgumentException("the count of calibrated pressure group is not equal to the count of sampling values")
        }
        correctedValues.sort()
        samplingValues.sort()
        gradients = when (groupCount()) {
            0, 1 -> FloatArray(0)
            else -> FloatArray(correctedValues.size - 1) { i ->
                (correctedValues[i] - correctedValues[i + 1]) / (samplingValues[i] - samplingValues[i + 1])
            }
        }
        offsets = when (groupCount()) {
            0, 1 -> FloatArray(0)
            else -> FloatArray(gradients.size) { i ->
                correctedValues[i] - gradients[i] * samplingValues[i]
            }
        }
    }

    constructor(correctedValueList: List<Float>,
                samplingValueList: List<Float>,
                correctedValueUnit: String,
                samplingValueUnit: String)
            : this(correctedValueList.toFloatArray(),
            samplingValueList.toFloatArray(),
            correctedValueUnit, samplingValueUnit)

    override fun correctValue(rawValue: Double): Double {
        return if (groupCount() <= 1) {
            rawValue
        } else {
            val pos = samplingValues.binarySearch(rawValue.toFloat())
            if (pos < 0) {
                val index = - pos - 1
                val key = when {
                    index <= 1 -> 0
                    index >= gradients.size -> gradients.size - 1
                    else -> index - 1
                }
                gradients[key] * rawValue + offsets[key]//correctedValues[key]
            } else {
                correctedValues[pos].toDouble()
            }
        }
    }

    override fun getType(): Int {
        return Corrector.TYPE_LINEAR_FITTING
    }

    fun groupCount(): Int {
        return correctedValues.size
    }

    fun getCorrectedValue(position: Int): Float {
        return correctedValues[position]
    }

    fun getSamplingValue(position: Int): Float {
        return samplingValues[position]
    }

    fun getFormatCorrectedValue(position: Int): String {
        return "${correctedValues[position]}$correctedValueUnit"
    }

    fun getFormatSamplingValue(position: Int): String {
        return "${samplingValues[position]}$samplingValueUnit"
    }

    override fun toByteArray(): ByteArray {
        val correctedValueUnitBytes = correctedValueUnit.toByteArray()
        val samplingValueUnitBytes = samplingValueUnit.toByteArray()
        val result = ByteArray(1 +
                1 + correctedValueUnitBytes.size +
                1 + samplingValueUnitBytes.size +
                1 + groupCount() * 2 * 4)
        var offset = 0
        result[offset++] = getType().toByte()
        result[offset++] = correctedValueUnitBytes.size.toByte()
        correctedValueUnitBytes.copyInto(result, offset)
        offset += correctedValueUnitBytes.size
        result[offset++] = samplingValueUnitBytes.size.toByte()
        samplingValueUnitBytes.copyInto(result, offset)
        offset += samplingValueUnitBytes.size
        result[offset++] = groupCount().toByte()
        correctedValues.forEach {
            NumericConverter.floatToBytesByMSB(it, result, offset)
            offset += 4
        }
        samplingValues.forEach {
            NumericConverter.floatToBytesByMSB(it, result, offset)
            offset += 4
        }
        return result
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeFloatArray(correctedValues)
        parcel.writeFloatArray(samplingValues)
        parcel.writeString(correctedValueUnit)
        parcel.writeString(samplingValueUnit)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LinearFittingCorrector> {
        override fun createFromParcel(parcel: Parcel): LinearFittingCorrector {
            return LinearFittingCorrector(parcel)
        }

        override fun newArray(size: Int): Array<LinearFittingCorrector?> {
            return arrayOfNulls(size)
        }
    }
}