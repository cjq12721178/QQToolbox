package com.cjq.lib.weisi.protocol;


import com.cjq.lib.weisi.util.NumericConverter;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by CJQ on 2018/1/10.
 */

public class EsbAnalyzer implements Analyzable {

    private static final GregorianCalendar TIMESTAMP_BUILDER = new GregorianCalendar();
    private static int ADJUSTED_YEAR = TIMESTAMP_BUILDER.get(Calendar.YEAR);
    private static final float VOLTAGE_UP_CONVERSION_VALUE = 307.2f;
    private static final int VOLTAGE_DIVIDE_VALUE = 32768;

    static final ValueBuilder[] VALUE_BUILDERS;
    static {
        final int size = 256;
        VALUE_BUILDERS = new ValueBuilder[size];
        Arrays.fill(VALUE_BUILDERS, new ValueBuilder(0, true, 3));
    }

    public static void setValueBuilder(byte dataTypeValue, int valueType, boolean signed, double coefficient) {
        VALUE_BUILDERS[dataTypeValue & 0xff] = new ValueBuilder(valueType, signed, coefficient);
    }

    @Override
    public long analyzeTimestamp(byte[] src, int position) {
        int calendarPos = position;
        //若基站或者其他网关设备没有打上时间戳，取本地系统时间
        if (src[calendarPos] == 0
                && src[calendarPos + 1] == 0
                && src[calendarPos + 2] == 0
                && src[calendarPos + 3] == 0
                && src[calendarPos + 4] == 0) {
            return System.currentTimeMillis();
        } else {
            long lastTime, currTime;
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
    }

    public double analyzeRawValue(byte[] src, int position, byte dataTypeValue) {
        return getValueBuilder(dataTypeValue).buildRawValue(src[position], src[position + 1]);
    }

    private ValueBuilder getValueBuilder(byte dataTypeValue) {
        return VALUE_BUILDERS[dataTypeValue & 0xff];
    }

    public float analyzeBatteryVoltage(byte voltage, int sensorAddress) {
        return sensorAddress < VOLTAGE_DIVIDE_VALUE
                ? voltage * BATTERY_VOLTAGE_COEFFICIENT
                : (voltage != 0
                ? VOLTAGE_UP_CONVERSION_VALUE / voltage
                : 0f);
    }

    static class ValueBuilder {

        //0：模拟量
        //1：状态量
        //2：计数量
        private final int mValueType;
        private final boolean mSigned;
        private final double mCoefficient;

        private ValueBuilder(int valueType, boolean signed, double coefficient) {
            mValueType = valueType;
            mSigned = signed;
            mCoefficient = coefficient;
        }

        public double buildRawValue(byte high, byte low) {
            switch (mValueType) {
                case 0:
                default:
                    return (mSigned
                            ? NumericConverter.int8ToInt32(high, low)
                            : NumericConverter.int8ToUInt16(high, low))
                            * mCoefficient;
                case 1:
                    return low == 0x10 || low == 1 ? 1 : 0;
                case 2:
                    return NumericConverter.int8ToUInt16(low);
            }
        }
    }
}
