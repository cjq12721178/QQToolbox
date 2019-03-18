package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.container.Value;

/**
 * Created by CJQ on 2018/3/16.
 */
public interface Decorator {
    @NonNull String decorateName(String name);
    @NonNull String decorateValue(double value, int para);
}
