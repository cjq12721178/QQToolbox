package com.cjq.lib.weisi.protocol;

import com.cjq.lib.weisi.sensor.ValueBuilder;

/**
 * Created by CJQ on 2017/8/30.
 */
public interface OnDataAnalyzedListener {
    void onDataAnalyzed(int sensorAddress,
                        byte dataTypeValue,
                        int dataTypeIndex,
                        long timestamp,
                        byte[] rawValue,
                        int rawValueIndex);
}
