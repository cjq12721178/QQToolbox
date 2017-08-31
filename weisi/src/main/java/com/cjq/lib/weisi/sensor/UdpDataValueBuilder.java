package com.cjq.lib.weisi.sensor;

import com.cjq.tool.qbox.util.NumericConverter;

/**
 * Created by CJQ on 2017/8/7.
 */

public class UdpDataValueBuilder implements ValueBuilder {

    //0：模拟量
    //1：状态量
    //2：计数量
    private final int mValueType;
    private final boolean mSigned;
    private final double mCoefficient;

    public UdpDataValueBuilder(int valueType, boolean signed, double coefficient) {
        mValueType = valueType;
        mSigned = signed;
        mCoefficient = coefficient;
    }

    @Override
    public double build(byte[] src, int pos) {
        byte h = src[pos], l = src[pos + 1];
        switch (mValueType) {
            case 0:
            default:
                return (mSigned
                        ? NumericConverter.int8ToInt32(h, l)
                        : NumericConverter.int8ToUInt16(h, l))
                        * mCoefficient;
            case 1:
                return l == 0x10 || l == 1 ? 1 : 0;
            case 2:
                return NumericConverter.int8ToUInt16(l);
        }
    }
}
