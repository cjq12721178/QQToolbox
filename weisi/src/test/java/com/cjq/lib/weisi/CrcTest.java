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

    @Test
    public void ccitt_isCorrect16WithCrcAppended() {
        byte[] data = {(byte) 0x1E, (byte) 0xFF, 0x06, 0x00, 0x01, 0x09, 0x20, 0x00, (byte) 0x1E, (byte) 0xE5, (byte) 0xE8, (byte) 0xAA, 0x5F, 0x78, 0x63, (byte) 0x8A, 0x44, (byte) 0x93, (byte) 0xCA, 0x4F, (byte) 0xDB, 0x2F, (byte) 0xCC, (byte) 0xD0, (byte) 0x9C, (byte) 0x87, (byte) 0x92, (byte) 0xDC, (byte) 0x90, (byte) 0xAF, (byte) 0xA9 };
        boolean expect = true;
        boolean actual = Crc.getCcitt().isCorrect16WithCrcAppended(data, 0, 29, 0xCE09, true, true);
        //System.out.print(String.format("%04X", actual));
        assertEquals(expect, actual);
    }

//    @Test
//    public void ccitt_isCorrect16() {
//        byte[] data = {(byte) 0x1E, (byte) 0xFF, 0x06, 0x00, 0x01, 0x09, 0x20, 0x00, (byte) 0x1E, (byte) 0xE5, (byte) 0xE8, (byte) 0xAA, 0x5F, 0x78, 0x63, (byte) 0x8A, 0x44, (byte) 0x93, (byte) 0xCA, 0x4F, (byte) 0xDB, 0x2F, (byte) 0xCC, (byte) 0xD0, (byte) 0x9C, (byte) 0x87, (byte) 0x92, (byte) 0xDC, (byte) 0x90, (byte) 0xAF, (byte) 0xA9 };
//        int expect = Crc.getCcitt().calc16(data, 0, 29, 0xCE09, true);
//        int actual = CrcClass.calc16AbnormalByMsb(0xCE09, data, 0, 29);
//        //System.out.print(String.format("%04X", actual));
//        assertEquals(expect, actual);
//    }
//
//    @Test
//    public void ccitt_isCorrect16ByLsb() {
//        byte[] data = { (byte) 0xC1, 0x62, (byte)0xFF, 0x04, 0x00, 0x01 };
//        int expect = Crc.getCcitt().calc16ByLsb(data);
//        int actual = CrcClass.calc16AbnormalByLSB(data);
//        //System.out.print(String.format("%04X", actual));
//        assertEquals(expect, actual);
//    }
}
