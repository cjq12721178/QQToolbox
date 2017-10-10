package com.cjq.tool.qbox.util;

/**
 * Created by CJQ on 2017/10/10.
 */

public class CodeRunTimeCatcher {

    private static long startTime;

    private CodeRunTimeCatcher() {
    }

    public static void start() {
        startTime = System.nanoTime();
    }

    public static long end() {
        return System.nanoTime() - startTime;
    }
}
