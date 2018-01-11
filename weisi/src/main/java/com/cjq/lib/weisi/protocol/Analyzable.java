package com.cjq.lib.weisi.protocol;

/**
 * Created by CJQ on 2018/1/10.
 */

public interface Analyzable {

    float BATTERY_VOLTAGE_COEFFICIENT = 0.05f;

    long analyzeTimestamp(byte[] src, int position);

    //关于电压的解析，返回值类型为float，
    //若为正，则表示为电压，若为负，表示为百分比
    //因为解析格式不同，就不在这个接口统一定义了
    //原始值同上
}
