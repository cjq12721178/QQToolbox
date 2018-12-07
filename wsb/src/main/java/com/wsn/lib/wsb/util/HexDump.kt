/*
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wsn.lib.wsb.util

import kotlin.experimental.and

/**
 * Clone of Android's HexDump class, for use in debugging. Cosmetic changes
 * only.
 */
object HexDump {
    private val HEX_DIGITS = charArrayOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F')

    @JvmStatic
    @JvmOverloads
    fun dumpHexString(array: ByteArray, offset: Int = 0, length: Int = array.size): String {
        val result = StringBuilder()

        val line = ByteArray(16)
        var lineIndex = 0

        result.append("\n0x")
        result.append(toHexString(offset))

        for (i in offset until offset + length) {
            if (lineIndex == 16) {
                result.append(" ")

                for (j in 0..15) {
                    if (line[j] > ' '.toByte() && line[j] < '~'.toByte()) {
                        result.append(String(line, j, 1))
                    } else {
                        result.append(".")
                    }
                }

                result.append("\n0x")
                result.append(toHexString(i))
                lineIndex = 0
            }

            val b = array[i]
            result.append(" ")
            result.append(HEX_DIGITS[b.toInt().ushr(4) and 0x0F])
            result.append(HEX_DIGITS[(b and 0x0F).toInt()])

            line[lineIndex++] = b
        }

        if (lineIndex != 16) {
            var count = (16 - lineIndex) * 3
            count++
            for (i in 0 until count) {
                result.append(" ")
            }

            for (i in 0 until lineIndex) {
                if (line[i] > ' '.toByte() && line[i] < '~'.toByte()) {
                    result.append(String(line, i, 1))
                } else {
                    result.append(".")
                }
            }
        }

        return result.toString()
    }

    @JvmStatic
    fun toHexString(b: Byte): String {
        return toHexString(toByteArray(b))
    }

    @JvmStatic
    @JvmOverloads
    fun toHexString(array: ByteArray, offset: Int = 0, length: Int = array.size): String {
        val buf = CharArray(length * 2)

        var bufIndex = 0
        for (i in offset until offset + length) {
            val b = array[i]
            buf[bufIndex++] = HEX_DIGITS[b.toInt().ushr(4) and 0x0F]
            buf[bufIndex++] = HEX_DIGITS[(b and 0x0F).toInt()]
        }

        return String(buf)
    }

    @JvmStatic
    fun toHexString(i: Int): String {
        return toHexString(toByteArray(i))
    }

    @JvmStatic
    fun toHexString(i: Short): String {
        return toHexString(toByteArray(i))
    }

    @JvmStatic
    fun toByteArray(b: Byte): ByteArray {
        val array = ByteArray(1)
        array[0] = b
        return array
    }

    @JvmStatic
    fun toByteArray(i: Int): ByteArray {
        val array = ByteArray(4)

        array[3] = (i and 0xFF).toByte()
        array[2] = (i shr 8 and 0xFF).toByte()
        array[1] = (i shr 16 and 0xFF).toByte()
        array[0] = (i shr 24 and 0xFF).toByte()

        return array
    }

    @JvmStatic
    fun toByteArray(i: Short): ByteArray {
        val array = ByteArray(2)

        array[1] = (i and 0xFF).toByte()
        array[0] = (i.toInt() shr 8 and 0xFF).toByte()

        return array
    }

    @JvmStatic
    private fun toByte(c: Char): Int {
        if (c >= '0' && c <= '9')
            return c - '0'
        if (c >= 'A' && c <= 'F')
            return c - 'A' + 10
        if (c >= 'a' && c <= 'f')
            return c - 'a' + 10

        throw RuntimeException("Invalid hex char '$c'")
    }

    @JvmStatic
    fun hexStringToByteArray(hexString: String): ByteArray {
        val length = hexString.length
        val buffer = ByteArray(length / 2)

        var i = 0
        while (i < length) {
            buffer[i / 2] = (toByte(hexString[i]) shl 4 or toByte(hexString[i + 1])).toByte()
            i += 2
        }

        return buffer
    }
}
