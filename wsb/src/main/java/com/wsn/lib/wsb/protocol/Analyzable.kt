package com.wsn.lib.wsb.protocol

/**
 * Created by CJQ on 2018/1/10.
 */

interface Analyzable {

    fun analyzeTimestamp(src: ByteArray, position: Int): Long

    companion object {
        //关于电压的解析，返回值类型为float，
        //若为正，则表示为电压，若为负，表示为百分比
        //因为解析格式不同，就不在这个接口统一定义了
        //原始值同上
        const val BATTERY_VOLTAGE_COEFFICIENT = 0.05f
    }
}
