package com.cjq.lib.weisi.protocol;


/**
 * Created by CJQ on 2017/8/30.
 */
public interface OnFrameAnalyzedListener extends OnSensorInfoAnalyzeListener {
    void onTimeSynchronizationAnalyzed(long timestamp);
}
