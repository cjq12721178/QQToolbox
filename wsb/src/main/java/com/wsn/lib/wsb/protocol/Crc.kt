package com.wsn.lib.wsb.protocol

import com.wsn.lib.wsb.util.NumericConverter

/**
 * Created by CJQ on 2018/1/11.
 */

abstract class Crc protected constructor() {

    @JvmOverloads
    fun calc8(buffer: ByteArray, offset: Int = 0, len: Int = buffer.size): Byte {
        var crc8 = CRC8_START_BYTE
        for (j in offset until offset + len) {
            crc8 = crc8 xor (buffer[j].toInt() and CRC8_START_BYTE)
            for (i in 0..7) {
                if (crc8 and 1 != 0) {
                    crc8 = crc8 shr 1
                    crc8 = crc8 xor CRC8_GENERATOR_POLYNOMIAL
                } else {
                    crc8 = crc8 shr 1
                }
            }
        }
        return crc8.toByte()
    }

    //以下所有pos若无特殊注明均为所需计算CRC的buf长度
    fun calc16(buf: ByteArray, isMsb: Boolean): Int {
        return calc16(buf, 0, buf.size, isMsb)
    }

    fun calc16(buf: ByteArray, pos: Int, len: Int, isMsb: Boolean): Int {
        return calc16(buf, pos, len, CRC16_DEFAULT_START_VALUE, isMsb)
    }

    fun calc16(buf: ByteArray, pos: Int, len: Int, startCrc: Int, isMsb: Boolean): Int {
        return if (isMsb)
            calc16ByMsb(buf, pos, len, startCrc)
        else
            calc16ByLsb(buf, pos, len, startCrc)
    }

    @JvmOverloads
    fun calc16ByMsb(buf: ByteArray, pos: Int = 0, len: Int = buf.size): Int {
        return calc16ByMsb(buf, pos, len, CRC16_DEFAULT_START_VALUE)
    }

    abstract fun calc16ByMsb(buf: ByteArray, pos: Int, len: Int, startCrc: Int): Int

    @JvmOverloads
    fun calc16ByLsb(buf: ByteArray, pos: Int = 0, len: Int = buf.size): Int {
        return calc16ByLsb(buf, pos, len, CRC16_DEFAULT_START_VALUE)
    }

    abstract fun calc16ByLsb(buf: ByteArray, pos: Int, len: Int, startCrc: Int): Int

    fun isCorrect16WithCrcAppended(buf: ByteArray, isBufMsb: Boolean, isCrcMsb: Boolean): Boolean {
        return isCorrect16WithCrcAppended(buf, 0, buf.size - CRC16_LENGTH, CRC16_DEFAULT_START_VALUE, isBufMsb, isCrcMsb)
    }

    fun isCorrect16WithCrcAppended(buf: ByteArray, pos: Int, len: Int, isBufMsb: Boolean, isCrcMsb: Boolean): Boolean {
        return isCorrect16WithCrcAppended(buf, pos, len, CRC16_DEFAULT_START_VALUE, isBufMsb, isCrcMsb)
    }

    fun isCorrect16WithCrcAppended(buf: ByteArray, pos: Int, len: Int, startCrc: Int, isBufMsb: Boolean, isCrcMsb: Boolean): Boolean {
        return isCorrect16(buf, pos, len, startCrc, pos + len, isBufMsb, isCrcMsb)
    }

    fun isCorrect16(buf: ByteArray, srcPos: Int, len: Int, crcPos: Int, isBufMsb: Boolean, isCrcMsb: Boolean): Boolean {
        return isCorrect16(buf, crcPos, calc16(buf, srcPos, len, isBufMsb), isCrcMsb)
    }

    fun isCorrect16(buf: ByteArray, srcPos: Int, len: Int, startCrc: Int, crcPos: Int, isBufMsb: Boolean, isCrcMsb: Boolean): Boolean {
        return isCorrect16(buf, crcPos, calc16(buf, srcPos, len, startCrc, isBufMsb), isCrcMsb)
    }

    private fun isCorrect16(buf: ByteArray, crcPos: Int, calcCrc16: Int, isCrcMsb: Boolean): Boolean {
        return if (isCrcMsb)
            isCorrect16(buf[crcPos], buf[crcPos + 1], calcCrc16)
        else
            isCorrect16(buf[crcPos + 1], buf[crcPos], calcCrc16)
    }

    private fun isCorrect16(srcCrcHigh: Byte, srcCrcLow: Byte, calcCrc16: Int): Boolean {
        return NumericConverter.int8ToUInt16(srcCrcHigh, srcCrcLow) == calcCrc16
    }

    fun isCorrect16(buf: ByteArray, pos: Int, len: Int, isBufMsb: Boolean, srcCrcHigh: Byte, srcCrcLow: Byte): Boolean {
        return isCorrect16(srcCrcHigh, srcCrcLow, calc16(buf, pos, len, isBufMsb))
    }

    fun isCorrect16(buf: ByteArray, pos: Int, len: Int, startCrc: Int, isBufMsb: Boolean, srcCrcHigh: Byte, srcCrcLow: Byte): Boolean {
        return isCorrect16(srcCrcHigh, srcCrcLow, calc16(buf, pos, len, startCrc, isBufMsb))
    }

    fun isCorrect16(buf: ByteArray, pos: Int, len: Int, isBufMsb: Boolean, crcBuf: ByteArray, crcPos: Int, isCrcMsb: Boolean): Boolean {
        return isCorrect16(buf, pos, len, CRC16_DEFAULT_START_VALUE, isBufMsb, crcBuf, crcPos, isCrcMsb)
    }

    fun isCorrect16(buf: ByteArray, pos: Int, len: Int, startCrc: Int, isBufMsb: Boolean, crcBuf: ByteArray, crcPos: Int, isCrcMsb: Boolean): Boolean {
        return if (isCrcMsb)
            isCorrect16(buf, pos, len, startCrc, isBufMsb, crcBuf[crcPos], crcBuf[crcPos + 1])
        else
            isCorrect16(buf, pos, len, startCrc, isBufMsb, crcBuf[crcPos + 1], crcBuf[crcPos])
    }

    fun isCorrect16(buf: ByteArray, pos: Int, len: Int, isBufMsb: Boolean, srcCrc: Int, isCrcMsb: Boolean): Boolean {
        return isCorrect16(buf, pos, len, CRC16_DEFAULT_START_VALUE, isBufMsb, srcCrc, isCrcMsb)
    }

    fun isCorrect16(buf: ByteArray, pos: Int, len: Int, startCrc: Int, isBufMsb: Boolean, srcCrc: Int, isCrcMsb: Boolean): Boolean {
        return if (isCrcMsb)
            calc16(buf, pos, len, startCrc, isBufMsb) == srcCrc
        else
            calc16(buf, pos, len, startCrc, isBufMsb) == srcCrc shr 8 or (srcCrc and 0xff shl 8)
    }

    private abstract class CcittCrc : Crc() {

        override fun calc16ByMsb(buf: ByteArray, pos: Int, len: Int, startCrc: Int): Int {
            var crc16 = startCrc
            var i = pos
            val end = pos + len
            while (i < end) {
                crc16 = calc16PerByte(crc16, buf[i])
                i++
            }
            return crc16 and 0xffff
        }

        override fun calc16ByLsb(buf: ByteArray, pos: Int, len: Int, startCrc: Int): Int {
            var crc16 = startCrc
            for (i in pos + len - 1 downTo pos) {
                crc16 = calc16PerByte(crc16, buf[i])
            }
            return crc16 and 0xffff
        }

        protected abstract fun calc16PerByte(crc: Int, v: Byte): Int

        companion object {

            @JvmStatic
            protected val REMAIN_TABLE_16 = intArrayOf(
                    0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
                    0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
                    0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
                    0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
                    0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
                    0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
                    0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
                    0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
                    0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
                    0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
                    0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
                    0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
                    0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
                    0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
                    0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
                    0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
                    0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
                    0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
                    0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
                    0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
                    0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
                    0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
                    0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
                    0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
                    0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
                    0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
                    0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
                    0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
                    0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
                    0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
                    0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
                    0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0)
        }
    }

    //基于CRC16-CCITT，多项式为X16+X12+X5+1（0x1021），
    // 初始值为0xFFFF。表正序，算法正序。
    private class CcittAscCrc : CcittCrc() {

        override fun calc16PerByte(crc: Int, v: Byte): Int {
            return Crc.CcittCrc.REMAIN_TABLE_16[crc shr 8 xor (v.toInt() and 0xff) and 0xff] xor (crc shl 8) and 0xffff
        }
    }

    //基于CRC16-CCITT，多项式为X16+X12+X5+1（0x1021），
    // 初始值为0xFFFF。表正序，算法逆序。
    private class CcittDescCrc : CcittCrc() {

        override fun calc16PerByte(crc: Int, v: Byte): Int {
            return crc shr 8 xor Crc.CcittCrc.REMAIN_TABLE_16[crc xor NumericConverter.int8ToUInt16(v) and 0xff]
        }
    }

    companion object {

        const val CRC16_LENGTH = 2
        private const val CRC8_START_BYTE = 0xFF
        private const val CRC8_GENERATOR_POLYNOMIAL = 0x8C
        private const val CRC16_DEFAULT_START_VALUE = 0xFFFF

        @JvmStatic
        val ccitt: Crc by lazy {
            CcittAscCrc()
        }

        @JvmStatic
        val Weisi: Crc by lazy {
            CcittDescCrc()
        }
    }
}
