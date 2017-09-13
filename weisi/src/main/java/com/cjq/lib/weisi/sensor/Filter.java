package com.cjq.lib.weisi.sensor;

/**
 * Created by CJQ on 2017/9/13.
 */

public interface Filter {
    boolean isMatch(Sensor sensor);
}
