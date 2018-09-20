package com.cjq.lib.weisi.iot.interpreter;

import android.support.annotation.IntRange;
import android.text.TextUtils;

import com.cjq.lib.weisi.iot.SensorManager;

public class ErrorStateInterpreter implements ValueInterpreter {

    private final String[] mStatesDescription = new String[Byte.SIZE];

    public ErrorStateInterpreter setState(@IntRange(from=0, to=7) int pos, String description) {
        mStatesDescription[pos] = description;
        return this;
    }

    @Override
    public String interpret(double value) {
        int rawValue = (int) value;
        if ((rawValue & 0xff) == 0) {
            return "正常";
        }
        byte dataType = (byte) (rawValue & 0xff00);
        StringBuilder builder = new StringBuilder(100);
        builder.append("测量量-")
                .append(SensorManager.getDataType(0xABCD, dataType, true).getName())
                .append("存在");
        boolean hasErrorDsp = false;
        for (int i = 0, j = 1;i < Byte.SIZE;++i, j <<= 1) {
            if ((rawValue & j) != 0 && !TextUtils.isEmpty(mStatesDescription[i])) {
                if (hasErrorDsp) {
                    builder.append('、');
                }
                builder.append(mStatesDescription[i]);
                hasErrorDsp = true;
            }
        }
        builder.append(hasErrorDsp ? "等问题" : "未知异常");
        return builder.toString();
    }
}
