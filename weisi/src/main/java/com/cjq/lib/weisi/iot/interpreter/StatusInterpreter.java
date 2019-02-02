package com.cjq.lib.weisi.iot.interpreter;

import android.support.annotation.NonNull;

/**
 * Created by KAT on 2016/11/23.
 */
public class StatusInterpreter implements ValueInterpreter {

    private final String on;
    private final String off;

    public StatusInterpreter(String on, String off) {
        this.on = on != null ? on : "";
        this.off = off != null ? off : "";
    }

    @Override
    public @NonNull String interpret(double value) {
        return value == 1 ? on : off;
    }
}
