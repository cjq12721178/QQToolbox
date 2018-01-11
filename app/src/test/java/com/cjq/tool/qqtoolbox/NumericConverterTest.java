package com.cjq.tool.qqtoolbox;

import com.cjq.lib.weisi.util.NumericConverter;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by CJQ on 2017/7/12.
 */

public class NumericConverterTest {

    @Test
    public void positiveByteToShortSameWithCast() {
        byte b = 20;
        short s1 = b;
        short s2 = NumericConverter.int8ToUInt8(b);
        assertEquals(s2, s1);
    }

    @Test
    public void negativeByteToShortSameWithCast() {
        byte b = -20;
        short s1 = b;
        short s2 = NumericConverter.int8ToUInt8(b);
        assertEquals(s2, s1);
    }

    @Test
    public void firstInt8ToUInt16ThenOrNotEqualsDirectInt8Or() {
        byte high = -1;
        byte low = 2;
        int expect = (high << 8) | (low);
        int actual = NumericConverter.int8ToUInt16(high, low);
        assertEquals(expect, actual);
    }

    @Test
    public void twoInt8ToInt16() {
        byte high = (byte) 0xff;
        byte low = (byte) 0xff;
        int expect = NumericConverter.int8ToInt16(high, low);
        int actual = (short) 0xffff;
        assertEquals(expect, actual);
    }
}
