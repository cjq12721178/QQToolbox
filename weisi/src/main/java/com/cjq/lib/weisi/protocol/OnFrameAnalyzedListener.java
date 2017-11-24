package com.cjq.lib.weisi.protocol;

import com.cjq.lib.weisi.sensor.ValueBuildDelegator;

/**
 * Created by CJQ on 2017/8/30.
 */
public interface OnFrameAnalyzedListener {
    void onDataAnalyzed(int sensorAddress,
                        byte dataTypeValue,
                        int dataTypeIndex,
                        ValueBuildDelegator valueBuildDelegator);
    void onTimeSynchronizationFinished(int year,
                                       int month,
                                       int day,
                                       int hour,
                                       int minute,
                                       int second);
}
