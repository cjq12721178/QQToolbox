package com.cjq.lib.weisi.sensor;

/**
 * Created by CJQ on 2017/8/7.
 */

public interface ValueBuilder {
    double build(byte[] src, int pos);
}
