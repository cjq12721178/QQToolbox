package com.cjq.lib.weisi.iot.interpreter;

import android.support.annotation.NonNull;

/**
 * Created by CJQ on 2017/8/8.
 */

public class GroundLeadInterpreter implements ValueInterpreter {

    private static final String NO_STUB_BUT_LINE = "未检测到接地桩，接地线地址：%02X";
    private static final String NO_LINE_BUT_STUB = "接地桩地址：%02X，未检测到接地线";
    private static final String BOTH_CHECKED = "接地桩地址：%02X，接地线地址：%02X";
    private static final String BOTH_NONE = "未检测到接地桩和接地线";

    private static GroundLeadInterpreter groundLeadInterpreter;

    public static GroundLeadInterpreter getInstance() {
        if (groundLeadInterpreter == null) {
            groundLeadInterpreter = new GroundLeadInterpreter();
        }
        return groundLeadInterpreter;
    }

    private GroundLeadInterpreter() {
    }

    @Override
    public @NonNull String interpret(double value) {
        int address = (int) value;
        int stubAddress = address & 0xff00;
        int lineAddress = address & 0xff;
        return stubAddress != 0
                ? (lineAddress != 0
                    ? String.format(BOTH_CHECKED, stubAddress, lineAddress)
                    : String.format(NO_LINE_BUT_STUB, stubAddress))
                : (lineAddress != 0
                    ? String.format(NO_STUB_BUT_LINE, lineAddress)
                    : BOTH_NONE);
    }
}
