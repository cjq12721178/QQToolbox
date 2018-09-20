package com.cjq.lib.weisi.iot.interpreter;

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

    private final String mFormat;

    public FloatInterpreter(int decimals, String unit) {
        mFormat = FORMATS[Math.min(MAX_DECIMALS, Math.max(decimals, 0))]
                + makeSafeUnit(unit);
    }

    private String makeSafeUnit(String unit) {
        if (unit == null) {
            return "";
        }
        if (!unit.contains("%")) {
            return unit;
        }
        int length = unit.length();
        StringBuilder builder = new StringBuilder(length * 2);
        char c;
        for (int i = 0;i < length;++i) {
            c = unit.charAt(i);
            builder.append(c);
            if (c == '%') {
                builder.append(c);
            }
        }
        return builder.toString();
    }

    @Override
    public String interpret(double value) {
        return String.format(mFormat, value);
    }
}
