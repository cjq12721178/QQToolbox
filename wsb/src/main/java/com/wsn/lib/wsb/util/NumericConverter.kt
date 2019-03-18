package com.wsn.lib.wsb.util

import kotlin.experimental.and


/**
 * Created by KAT on 2016/6/28.
 */
object NumericConverter {

    private const val FLOAT_BYTES = 4

    @JvmStatic
    fun int8ToUInt8(b: Byte): Short {
        return int8ToUInt16(b).toShort()
    }

    @JvmStatic
    fun int8ToUInt16(b: Byte): Int {
        return b.toInt() and 0xff
    }

    @JvmStatic
    fun int16ToUInt16(s: Short): Int {
        return s.toInt() and 0xffff
    }

    @JvmStatic
    fun int8ToUInt32(b: Byte): Long {
        return b.toLong() and 0xffL
    }

    fun int16ToUInt32(s: Short): Long {
        return s.toLong() and 0xffffL
    }

    @JvmStatic
    fun int32ToUInt32(i: Int): Long {
        return i.toLong() and 0xffffffffL
    }

    @JvmStatic
    fun int8ToUInt16(high: Byte, low: Byte): Int {
        return (int8ToUInt16(high) shl 8) or int8ToUInt16(low)
    }

    @JvmStatic
    fun int8ToInt16(h: Byte, l: Byte): Short {
        return int8ToInt32(h, l).toShort()
    }

    @JvmStatic
    fun int8ToInt32(h: Byte, l: Byte): Int {
        return (h.toInt() shl 8) or int8ToUInt16(l)
    }

    //高位在前，低位在后
    @JvmStatic
    fun int8ToUInt32(b1: Byte, b2: Byte, b3: Byte, b4: Byte): Long {
        return (int8ToUInt32(b1) shl 24) or
                (int8ToUInt32(b2) shl 16) or
                (int8ToUInt32(b3) shl 8) or
                int8ToUInt32(b4)
    }

    @JvmStatic
    fun int8toUInt32ByMSB(bytes: ByteArray, pos: Int): Long {
        return int8ToUInt32(bytes[pos], bytes[pos + 1], bytes[pos + 2], bytes[pos + 3])
    }

    @JvmStatic
    fun int8toUInt32ByLSB(bytes: ByteArray, pos: Int): Long {
        return int8ToUInt32(bytes[pos + 3], bytes[pos + 2], bytes[pos + 1], bytes[pos])
    }

    //高位在前，低位在后
    @JvmStatic
    fun int8ToInt32(b1: Byte, b2: Byte, b3: Byte, b4: Byte): Int {
        return (b1.toInt() shl 24) or
                (int8ToUInt16(b2) shl 16) or
                (int8ToUInt16(b3) shl 8) or
                int8ToUInt16(b4)
    }

    @JvmStatic
    fun int8toInt32ByMSB(bytes: ByteArray, pos: Int): Int {
        return int8ToInt32(bytes[pos], bytes[pos + 1], bytes[pos + 2], bytes[pos + 3])
    }

    @JvmStatic
    fun int8toInt32ByLSB(bytes: ByteArray, pos: Int): Int {
        return int8ToInt32(bytes[pos + 3], bytes[pos + 2], bytes[pos + 1], bytes[pos])
    }

    //高位在前，低位在后
    @JvmStatic
    fun bytesToFloat(b1: Byte, b2: Byte, b3: Byte, b4: Byte): Float {
        return java.lang.Float.intBitsToFloat(int8ToInt32(b1, b2, b3, b4))
    }

    @JvmStatic
    fun bytesToFloatByMSB(bytes: ByteArray, pos: Int): Float {
        return java.lang.Float.intBitsToFloat(int8toInt32ByMSB(bytes, pos))
    }

    @JvmStatic
    fun bytesToFloatByLSB(bytes: ByteArray, pos: Int): Float {
        return java.lang.Float.intBitsToFloat(int8toInt32ByLSB(bytes, pos))
    }

    @JvmStatic
    fun floatToBytesByMSB(f: Float, bytes: ByteArray, pos: Int) {
        val intBits = java.lang.Float.floatToIntBits(f)
        var offset = pos
        bytes[offset++] = (intBits ushr 24).toByte()
        bytes[offset++] = ((intBits and 0x00FF0000) ushr 16).toByte()
        bytes[offset++] = ((intBits and 0x0000FF00) ushr 8).toByte()
        bytes[offset] = (intBits and 0xFF).toByte()
//        var i = 0
//        var mask = -0x1000000
//        while (i < FLOAT_BYTES) {
//            bytes[pos + i] = (intBits and mask).toByte()
//            ++i
//            mask = mask ushr 8
//        }
    }

    @JvmStatic
    fun floatToBytesByMSB(f: Float): ByteArray {
        val bytes = ByteArray(FLOAT_BYTES)
        floatToBytesByMSB(f, bytes, 0)
        return bytes
    }

    @JvmStatic
    fun floatToBytesByLSB(f: Float, bytes: ByteArray, pos: Int) {
        val intBits = java.lang.Float.floatToIntBits(f)
        var offset = pos
        bytes[offset++] = (intBits and 0xFF).toByte()
        bytes[offset++] = ((intBits and 0x0000FF00) ushr 8).toByte()
        bytes[offset++] = ((intBits and 0x00FF0000) ushr 16).toByte()
        bytes[offset] = (intBits ushr 24).toByte()
//        var i = 0
//        var mask = 0xff
//        while (i < FLOAT_BYTES) {
//            bytes[pos + i] = (intBits and mask).toByte()
//            ++i
//            mask = mask shl 8
//        }
    }

    @JvmStatic
    fun floatToBytesByLSB(f: Float): ByteArray {
        val bytes = ByteArray(FLOAT_BYTES)
        floatToBytesByLSB(f, bytes, 0)
        return bytes
    }

    @JvmStatic
    fun hexDataStringToBytes(s: String): ByteArray? {
        val ss = s.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val length = ss.size
        val data = ByteArray(length)
        try {
            for (i in 0 until length) {
                if (ss[i].length > 2) {
                    return null
                }
                data[i] = Integer.parseInt(ss[i], 16).toByte()
            }
        } catch (nfe: NumberFormatException) {
            return null
        }
        return data
    }

    @JvmOverloads
    @JvmStatic
    fun bytesToHexDataString(data: ByteArray, offset: Int = 0, len: Int = data.size): String {
        val builder = StringBuilder(len * 3)
        var i = offset
        val n = offset + len
        while (i < n) {
            builder.append(String.format("%02X ", int8ToUInt16(data[i])))
            ++i
        }
        return builder.toString()
    }
}
