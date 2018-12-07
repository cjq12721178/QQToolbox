package com.wsn.lib.wsb.protocol


/**
 * Created by CJQ on 2017/8/30.
 */
interface OnFrameAnalyzedListener : OnSensorInfoAnalyzeListener {
    fun onTimeSynchronizationAnalyzed(timestamp: Long)
}
