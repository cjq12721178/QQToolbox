package com.cjq.lib.weisi.iot.interpreter;

import android.support.annotation.NonNull;

/**
 * Created by KAT on 2016/11/21.
 */
public interface ValueInterpreter {
    @NonNull String interpret(double value);
}
