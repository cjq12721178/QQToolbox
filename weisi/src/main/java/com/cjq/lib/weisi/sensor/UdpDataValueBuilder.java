package com.cjq.lib.weisi.sensor;

import com.cjq.tool.qbox.util.NumericConverter;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by CJQ on 2017/8/7.
 */

public class UdpDataValueBuilder implements ValueBuilder {

    private static final float VOLTAGE_UP_CONVERSION_VALUE = 307.2f;
    private static final float VOLTAGE_DOWN_CONVERSION_VALUE = 20.0f;
    private static final int VOLTAGE_DIVIDE_VALUE = 32768;
    private static final GregorianCalendar TIMESTAMP_BUILDER = new GregorianCalendar();
    private static int ADJUSTED_YEAR = TIMESTAMP_BUILDER.get(Calendar.YEAR);

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
    public long buildTimestamp(byte[] src, int timestampIndex) {
        long lastTime, currTime;
        int calendarPos = timestampIndex;
        lastTime = TIMESTAMP_BUILDER.getTimeInMillis();
        TIMESTAMP_BUILDER.set(ADJUSTED_YEAR,
                src[calendarPos] & 0x0f,
                src[++calendarPos],
                src[++calendarPos],
                src[++calendarPos],
                src[++calendarPos]);
        currTime = TIMESTAMP_BUILDER.getTimeInMillis();
        //跨年修正，属于那种基本不会发生的情况
        if (currTime < lastTime) {
            ADJUSTED_YEAR = new GregorianCalendar().get(Calendar.YEAR);
            TIMESTAMP_BUILDER.set(Calendar.YEAR, ADJUSTED_YEAR);
            currTime = TIMESTAMP_BUILDER.getTimeInMillis();
        }
        return currTime;
    }

    @Override
    public double buildRawValue(byte[] src, int rawValueIndex) {
        byte h = src[rawValueIndex], l = src[rawValueIndex + 1];
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

    @Override
    public float buildBatteryVoltage(byte[] src, int batteryVoltageIndex, int sensorAddress) {
        byte voltage = src[batteryVoltageIndex];
        return sensorAddress < VOLTAGE_DIVIDE_VALUE ? voltage / VOLTAGE_DOWN_CONVERSION_VALUE :
                (voltage != 0 ? VOLTAGE_UP_CONVERSION_VALUE / voltage : 0f);
    }
}
