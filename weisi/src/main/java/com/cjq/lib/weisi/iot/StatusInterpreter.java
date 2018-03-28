package com.cjq.lib.weisi.iot;

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
    public String interpret(double value) {
        return value == 1 ? on : off;
    }
}
