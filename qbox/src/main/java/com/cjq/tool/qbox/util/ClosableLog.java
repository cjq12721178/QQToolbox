package com.cjq.tool.qbox.util;

import android.util.Log;

/**
 * Created by CJQ on 2017/7/5.
 */

public final class ClosableLog {

    private static boolean enablePrint;

    private ClosableLog() {
    }

    public static void setEnablePrint(boolean enabled) {
        enablePrint = enabled;
    }

    public static int v(String tag, String msg) {
        return enablePrint ? Log.v(tag, msg) : -1;
    }

    public static int v(String tag, String msg, Throwable tr) {
        return enablePrint ? Log.v(tag, msg, tr) : -1;
    }

    public static int d(String tag, String msg) {
        return enablePrint ? Log.d(tag, msg) : -1;
    }

    public static int d(String tag, String msg, Throwable tr) {
        return enablePrint ? Log.d(tag, msg, tr) : -1;
    }

    public static int i(String tag, String msg) {
        return enablePrint ? Log.i(tag, msg) : -1;
    }

    public static int i(String tag, String msg, Throwable tr) {
        return enablePrint ? Log.i(tag, msg, tr) : -1;
    }

    public static int w(String tag, String msg) {
        return enablePrint ? Log.w(tag, msg) : -1;
    }

    public static int w(String tag, String msg, Throwable tr) {
        return enablePrint ? Log.w(tag, msg, tr) : -1;
    }

    public static int w(String tag, Throwable tr) {
        return enablePrint ? Log.w(tag, tr) : -1;
    }

    public static int e(String tag, String msg) {
        return enablePrint ? Log.e(tag, msg) : -1;
    }

    public static int e(String tag, String msg, Throwable tr) {
        return enablePrint ? Log.e(tag, msg, tr) : -1;
    }

    public static String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }
}
