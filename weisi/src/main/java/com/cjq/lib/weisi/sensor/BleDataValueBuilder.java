package com.cjq.lib.weisi.sensor;

import com.cjq.tool.qbox.util.NumericConverter;

/**
 * Created by CJQ on 2017/8/30.
 */

public class BleDataValueBuilder implements ValueBuilder {

    private static final BleDataValueBuilder BLE_DATA_VALUE_BUILDER = new BleDataValueBuilder();
    private static final float BATTERY_VOLTAGE_COEFFICIENT = 0.05f;

    private BleDataValueBuilder() {
    }

    public static BleDataValueBuilder getInstance() {
        return BLE_DATA_VALUE_BUILDER;
    }

    @Override
    public long buildTimestamp(byte[] src, long timestampIndex) {
        return timestampIndex;
    }

    @Override
    public double buildRawValue(byte[] src, int rawValueIndex) {
        return NumericConverter.bytesToFloatByMSB(src, rawValueIndex);
    }

    @Override
    public float buildBatteryVoltage(byte[] src, int batteryVoltageIndex, int sensorAddress) {
        return NumericConverter.int8ToUInt16(src[batteryVoltageIndex])
                * BATTERY_VOLTAGE_COEFFICIENT;
    }
}
