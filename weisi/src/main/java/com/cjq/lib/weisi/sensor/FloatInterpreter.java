package com.cjq.lib.weisi.sensor;

/**
 * Created by KAT on 2016/11/23.
 */
public class FloatInterpreter implements ValueInterpreter {

    private static final int MAX_DECIMALS = 9;
    private static final String[] FORMATS;
    static {
        FORMATS = new String[MAX_DECIMALS + 1];
        for (int i = 0;i <= MAX_DECIMALS;++i) {
            FORMATS[i] = "%." + i + "f";
        }
    }

    private String mFormat;

    public FloatInterpreter(int decimals) {
        mFormat = FORMATS[Math.min(MAX_DECIMALS, Math.max(decimals, 0))];
    }

    @Override
    public String interpret(double value) {
        return String.format(mFormat, value);
    }
}
