package com.cjq.lib.weisi.sensor;

import com.cjq.tool.qbox.util.NumericConverter;

/**
 * Created by CJQ on 2017/8/30.
 */

public class BleDataValueBuilder implements ValueBuilder {

    private static final BleDataValueBuilder BLE_DATA_VALUE_BUILDER = new BleDataValueBuilder();

    private BleDataValueBuilder() {
    }

    public static BleDataValueBuilder getInstance() {
        return BLE_DATA_VALUE_BUILDER;
    }

    @Override
    public double build(byte[] src, int pos) {
        return NumericConverter.bytesToFloatByMSB(src, pos);
    }
}
