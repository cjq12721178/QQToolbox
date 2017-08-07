package com.cjq.lib.weisi.sensor;

/**
 * Created by KAT on 2016/11/23.
 */
public class DefaultInterpreter implements ValueInterpreter {

    private static final DefaultInterpreter INTERPRETER = new DefaultInterpreter();

    private DefaultInterpreter() {
    }

    public static DefaultInterpreter getInstance() {
        return INTERPRETER;
    }

    @Override
    public String interpret(double value) {
        return String.valueOf(value);
    }
}
