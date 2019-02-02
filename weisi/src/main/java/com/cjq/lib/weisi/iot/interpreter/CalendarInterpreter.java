package com.cjq.lib.weisi.iot.interpreter;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by KAT on 2016/11/23.
 */
public class CalendarInterpreter implements ValueInterpreter {

    private static final Map<String, CalendarInterpreter> INTERPRETER_MAP;
    private static final Date DATE = new Date();
    private static final long TIME_DIFFERENCE = Calendar.getInstance().getTimeZone().getRawOffset();

    static {
        INTERPRETER_MAP = new HashMap<>();
    }

    private SimpleDateFormat dateFormat;

    private CalendarInterpreter() {
    }

    public static CalendarInterpreter from(String pattern) {
        String calendarPattern = TextUtils.isEmpty(pattern) ? "yyyy-MM-dd HH:mm:ss" : pattern;
        CalendarInterpreter interpreter = INTERPRETER_MAP.get(pattern);
        if (interpreter == null) {
            interpreter = new CalendarInterpreter();
            interpreter.dateFormat = new SimpleDateFormat(calendarPattern);
            INTERPRETER_MAP.put(calendarPattern, interpreter);
        }
        return interpreter;
    }

    @Override
    public @NonNull String interpret(double value) {
        //原始数据为秒，且为int，后为了与其他传感器统一，转为float
        DATE.setTime((long)Float.floatToIntBits((float)value) * 1000 - TIME_DIFFERENCE);
        return dateFormat.format(DATE);
    }
}
