package com.cjq.lib.weisi.protocol;


import com.cjq.lib.weisi.util.NumericConverter;

/**
 * Created by CJQ on 2018/1/10.
 */

public class BleAnalyzer implements Analyzable {

    @Override
    public long analyzeTimestamp(byte[] src, int position) {
        return NumericConverter.int8toUInt32ByMSB(src,
                position) * 1000;
    }

    public float analyzeBatteryVoltage(byte voltage) {
        return voltage < 0
                ? voltage
                : voltage * BATTERY_VOLTAGE_COEFFICIENT;
    }

    public double analyzeRawValue(byte[] src, int position) {
        return NumericConverter.bytesToFloatByMSB(src, position);
    }
}
