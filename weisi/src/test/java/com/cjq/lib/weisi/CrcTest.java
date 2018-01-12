package com.cjq.lib.weisi;

import com.cjq.lib.weisi.protocol.Crc;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by CJQ on 2018/1/11.
 */

public class CrcTest {

    @Test
    public void ccitt_calc16ByMsb() {
        byte[] data = {(byte) 0xFF, (byte) 0xFF, 0x01, 0x6C};
        int expect = 0x9E1B;
        int actual = Crc.getCcitt().calc16ByMsb(data);
        System.out.print(String.format("%04X", actual));
        assertEquals(expect, actual);
    }

    @Test
    public void ccitt_isCorrect16_bufMsb_srcCrc_crcMsb() {
        byte[] data = {(byte) 0xFF, (byte) 0xFF, 0x01, 0x6C};
        boolean expect = true;
        boolean actual = Crc.getCcitt().isCorrect16(data, 0, data.length, true, 0x9E1B, true);
        //System.out.print(String.format("%04X", actual));
        assertEquals(expect, actual);
    }
}
