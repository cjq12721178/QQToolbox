package com.cjq.tool.qqtoolbox

import com.wsn.lib.wsb.util.NumericConverter

import org.junit.Test

import org.junit.Assert.*

/**
 * Created by CJQ on 2017/7/12.
 */

class NumericConverterTest {

    @Test
    fun positiveByteToShortSameWithCast() {
        val b: Byte = 20
        val s1 = b.toShort()
        val s2 = NumericConverter.int8ToUInt8(b)
        assertEquals(s2.toLong(), s1.toLong())
    }

    @Test
    fun negativeByteToShortSameWithCast() {
        val b: Byte = -20
        val s1 = b.toShort()
        val s2 = NumericConverter.int8ToUInt8(b)
        assertEquals(s2.toLong(), s1.toLong())
    }

    @Test
    fun firstInt8ToUInt16ThenOrNotEqualsDirectInt8Or() {
        val high: Byte = -1
        val low: Byte = 2
        val expect = (high.toInt() shl 8) or low.toInt()
        val actual = NumericConverter.int8ToUInt16(high, low)
        assertEquals(expect.toLong(), actual.toLong())
    }

    @Test
    fun twoInt8ToInt16() {
        val high = 0xff.toByte()
        val low = 0xff.toByte()
        val expect = NumericConverter.int8ToInt16(high, low).toInt()
        val actual = 0xffff.toShort().toInt()
        assertEquals(expect.toLong(), actual.toLong())
    }

    @Test
    fun byteToInt() {
        val b: Byte = 0x80.toByte()
        val i: Int = b.toInt()
        System.out.println("b: $b, i: $i")
        assertEquals(i, 0x80)
    }

    @Test
    fun shrInt() {
        val src: Int = -10
        val dst: Int = src ushr 24
        System.out.println(String.format("X08", src))
        System.out.println("src: $src, dst: $dst")
        assertEquals(dst, 0xFF)
    }

    @Test
    fun floatToBytes() {
        val f: Float = 26f
        val mb = NumericConverter.floatToBytesByMSB(f)
        val lb = NumericConverter.floatToBytesByLSB(f)
        System.out.println("f: $f")
        System.out.println("floatToBytesByMSB: ${NumericConverter.bytesToHexDataString(mb)}")
        System.out.println("floatToBytesByLSB: ${NumericConverter.bytesToHexDataString(lb)}")
        val fmb = NumericConverter.bytesToFloatByMSB(mb, 0)
        val flb = NumericConverter.bytesToFloatByLSB(lb, 0)
        System.out.println("bytesToFloatByMSB: $fmb")
        System.out.println("bytesToFloatByLSB: $flb")
    }
}
