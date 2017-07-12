package com.cjq.tool.qbox.util;

/**
 * Created by KAT on 2016/6/28.
 */
public class NumericConverter {
    public static short int8ToUInt8(byte b) {
        return (short) (b & 0xff);
    }

    public static int int8ToUInt16(byte b) {
        return b & 0xff;
    }

    public static int int16ToUInt16(short s) {
        return s & 0xffff;
    }

    public static long int8ToUInt32(byte b) {
        return b & 0xffl;
    }

    public static long int16ToUInt32(short s) {
        return s & 0xffffl;
    }

    public static long int32ToUInt32(int i) {
        return i & 0xffffffffl;
    }

    public static int int8ToUInt16(byte high, byte low) {
        return (int8ToUInt16(high) << 8) | int8ToUInt16(low);
    }

    public static short int8ToInt16(byte h, byte l) {
        return (short) ((((short)h) << 8) | int8ToUInt8(l));
    }

    public static int int8ToInt32(byte h, byte l) {
        return (((int)h) << 8) | int8ToUInt16(l);
    }

    //高位在前，低位在后
    public static long int8ToUInt32(byte b1, byte b2, byte b3, byte b4) {
        return (int8ToUInt32(b1) << 24) |
                (int8ToUInt32(b2) << 16) |
                (int8ToUInt32(b3) << 8) |
                int8ToUInt32(b4);
    }

    public static long int8toUInt32ByMSB(byte[] bytes, int pos) {
        return int8ToUInt32(bytes[pos], bytes[pos + 1], bytes[pos + 2], bytes[pos + 3]);
    }

    public static long int8toUInt32ByLSB(byte[] bytes, int pos) {
        return int8ToUInt32(bytes[pos + 3], bytes[pos + 2], bytes[pos + 1], bytes[pos]);
    }

    //高位在前，低位在后
    public static int int8ToInt32(byte b1, byte b2, byte b3, byte b4) {
        return (((int)b1) << 24) |
                (int8ToUInt16(b2) << 16) |
                (int8ToUInt16(b3) << 8) |
                int8ToUInt16(b4);
    }

    public static int int8toInt32ByMSB(byte[] bytes, int pos) {
        return int8ToInt32(bytes[pos], bytes[pos + 1], bytes[pos + 2], bytes[pos + 3]);
    }

    public static int int8toInt32ByLSB(byte[] bytes, int pos) {
        return int8ToInt32(bytes[pos + 3], bytes[pos + 2], bytes[pos + 1], bytes[pos]);
    }

    //高位在前，低位在后
    public static float bytesToFloat(byte b1, byte b2, byte b3, byte b4) {
        return Float.intBitsToFloat(int8ToInt32(b1, b2, b3, b4));
    }

    public static float bytesToFloatByMSB(byte[] bytes, int pos) {
        return Float.intBitsToFloat(int8toInt32ByMSB(bytes, pos));
    }

    public static float bytesToFloatByLSB(byte[] bytes, int pos) {
        return Float.intBitsToFloat(int8toInt32ByLSB(bytes, pos));
    }
}
