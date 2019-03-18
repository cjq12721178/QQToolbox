package com.cjq.lib.weisi.iot.container;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.container.Value;

public interface Corrector {
    double correctValue(double rawValue);
}
