package com.cjq.lib.weisi.protocol;

/**
 * Created by CJQ on 2018/1/11.
 */

public abstract class Crc {

    public static final int CRC16_LENGTH = 2;
    private static final int CRC8_START_BYTE = 0xFF;
    private static final int CRC8_GENERATOR_POLYNOMIAL = 0x8C;
    private static final int CRC16_DEFAULT_START_VALUE = 0xFFFF;

    protected Crc() {
    }

    public static Crc getCcitt() {
        return CcittAscCrc.singleInstance;
    }

    public static Crc getWeisi() {
        return CcittDescCrc.singleInstance;
    }

    public byte calc8(byte[] buffer) {
        return calc8(buffer, 0, buffer.length);
    }

    public byte calc8(byte[] buffer, int offset, int len) {
        int crc8 = CRC8_START_BYTE;
        for (int j = offset; j < offset + len; j++) {
            crc8 ^= buffer[j] & CRC8_START_BYTE;
            for (int i = 0; i < 8; i++) {
                if ((crc8 & 1) != 0) {
                    crc8 >>= 1;
                    crc8 ^= CRC8_GENERATOR_POLYNOMIAL;
                } else {
                    crc8 >>= 1;
                }
            }
        }
        return (byte)crc8;
    }

    //以下所有pos若无特殊注明均为所需计算CRC的buf长度
    public int calc16(byte[] buf, boolean isMsb) {
        return calc16(buf, 0, buf.length, isMsb);
    }

    public int calc16(byte[] buf, int pos, int len, boolean isMsb) {
        return calc16(buf, pos, len, CRC16_DEFAULT_START_VALUE, isMsb);
    }

    public int calc16(byte[] buf, int pos, int len, int startCrc, boolean isMsb) {
        return isMsb
                ? calc16ByMsb(buf, pos, len, startCrc)
                : calc16ByLsb(buf, pos, len, startCrc);
    }

    public int calc16ByMsb(byte[] buf) {
        return calc16ByMsb(buf, 0, buf.length);
    }

    public int calc16ByMsb(byte[] buf, int pos, int len) {
        return calc16ByMsb(buf, pos, len, CRC16_DEFAULT_START_VALUE);
    }

    public abstract int calc16ByMsb(byte[] buf, int pos, int len, int startCrc);

    public int calc16ByLsb(byte[] buf) {
        return calc16ByLsb(buf, 0, buf.length);
    }

    public int calc16ByLsb(byte[] buf, int pos, int len) {
        return calc16ByLsb(buf, pos, len, CRC16_DEFAULT_START_VALUE);
    }

    public abstract int calc16ByLsb(byte[] buf, int pos, int len, int startCrc);

    public boolean isCorrect16WithCrcAppended(byte[] buf, boolean isBufMsb, boolean isCrcMsb) {
        return isCorrect16WithCrcAppended(buf, 0, buf.length - CRC16_LENGTH, CRC16_DEFAULT_START_VALUE, isBufMsb, isCrcMsb);
    }

    public boolean isCorrect16WithCrcAppended(byte[] buf, int pos, int len, boolean isBufMsb, boolean isCrcMsb) {
        return isCorrect16WithCrcAppended(buf, pos, len, CRC16_DEFAULT_START_VALUE, isBufMsb, isCrcMsb);
    }

    public boolean isCorrect16WithCrcAppended(byte[] buf, int pos, int len, int startCrc, boolean isBufMsb, boolean isCrcMsb) {
        return isCorrect16(buf, pos, len, startCrc, pos + len, isBufMsb, isCrcMsb);
    }

    public boolean isCorrect16(byte[] buf, int srcPos, int len, int crcPos, boolean isBufMsb, boolean isCrcMsb) {
        return isCorrect16(buf, crcPos, calc16(buf, srcPos, len, isBufMsb), isCrcMsb);
    }

    public boolean isCorrect16(byte[] buf, int srcPos, int len, int startCrc, int crcPos, boolean isBufMsb, boolean isCrcMsb) {
        return isCorrect16(buf, crcPos, calc16(buf, srcPos, len, startCrc, isBufMsb), isCrcMsb);
    }

    private boolean isCorrect16(byte[] buf, int crcPos, int calcCrc16, boolean isCrcMsb) {
        return isCrcMsb
                ? isCorrect16(buf[crcPos], buf[crcPos + 1], calcCrc16)
                : isCorrect16(buf[crcPos + 1], buf[crcPos], calcCrc16);
    }

    private boolean isCorrect16(byte srcCrcHigh, byte srcCrcLow, int calcCrc16) {
        return (((srcCrcHigh & 0xff) << 8) | (srcCrcLow & 0xff)) == calcCrc16;
    }

    public boolean isCorrect16(byte[] buf, int pos, int len, boolean isBufMsb, byte srcCrcHigh, byte srcCrcLow) {
        return isCorrect16(srcCrcHigh, srcCrcLow, calc16(buf, pos, len, isBufMsb));
    }

    public boolean isCorrect16(byte[] buf, int pos, int len, int startCrc, boolean isBufMsb, byte srcCrcHigh, byte srcCrcLow) {
        return isCorrect16(srcCrcHigh, srcCrcLow, calc16(buf, pos, len, startCrc, isBufMsb));
    }

    public boolean isCorrect16(byte[] buf, int pos, int len, boolean isBufMsb, byte[] crcBuf, int crcPos, boolean isCrcMsb) {
        return isCorrect16(buf, pos, len, CRC16_DEFAULT_START_VALUE, isBufMsb, crcBuf, crcPos, isCrcMsb);
    }

    public boolean isCorrect16(byte[] buf, int pos, int len, int startCrc, boolean isBufMsb, byte[] crcBuf, int crcPos, boolean isCrcMsb) {
        return isCrcMsb
                ? isCorrect16(buf, pos, len, startCrc, isBufMsb, crcBuf[crcPos], crcBuf[crcPos + 1])
                : isCorrect16(buf, pos, len, startCrc, isBufMsb, crcBuf[crcPos + 1], crcBuf[crcPos]);
    }

    public boolean isCorrect16(byte[] buf, int pos, int len, boolean isBufMsb, int srcCrc, boolean isCrcMsb) {
        return isCorrect16(buf, pos, len, CRC16_DEFAULT_START_VALUE, isBufMsb, srcCrc, isCrcMsb);
    }

    public boolean isCorrect16(byte[] buf, int pos, int len, int startCrc, boolean isBufMsb, int srcCrc, boolean isCrcMsb) {
        return isCrcMsb
                ? calc16(buf, pos, len, startCrc, isBufMsb) == srcCrc
                : calc16(buf, pos, len, startCrc, isBufMsb) == ((srcCrc >> 8) | ((srcCrc & 0xff) << 8));
    }

    private abstract static class CcittCrc extends Crc {

        protected static final int[] REMAIN_TABLE_16 = new int[] {
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
                0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0
        };

        @Override
        public int calc16ByMsb(byte[] buf, int pos, int len, int startCrc) {
            int crc16 = startCrc;
            for(int i = pos, end = pos + len; i < end; i++) {
                crc16 = calc16PerByte(crc16, buf[i]);
            }
            return crc16 & 0xffff;
        }

        @Override
        public int calc16ByLsb(byte[] buf, int pos, int len, int startCrc) {
            int crc16 = startCrc;
            for(int i = pos + len - 1;i >= pos;--i) {
                crc16 = calc16PerByte(crc16, buf[i]);
            }
            return crc16 & 0xffff;
        }

        protected abstract int calc16PerByte(int crc, byte val);
    }

    //基于CRC16-CCITT，多项式为X16+X12+X5+1（0x1021），
    // 初始值为0xFFFF。表正序，算法正序。
    private static class CcittAscCrc extends CcittCrc {

        private static final CcittAscCrc singleInstance = new CcittAscCrc();

        @Override
        protected int calc16PerByte(int crc, byte val) {
            return (REMAIN_TABLE_16[((crc >> 8) ^ ((int)val & 0xff)) & 0xff] ^ (crc << 8)) & 0xffff;
        }
    }

    //基于CRC16-CCITT，多项式为X16+X12+X5+1（0x1021），
    // 初始值为0xFFFF。表正序，算法逆序。
    private static class CcittDescCrc extends CcittCrc {

        private static final CcittDescCrc singleInstance = new CcittDescCrc();

        @Override
        protected int calc16PerByte(int crc, byte val) {
            return (crc >> 8) ^ REMAIN_TABLE_16[(crc ^ val) & 0xff];
        }
    }
}
