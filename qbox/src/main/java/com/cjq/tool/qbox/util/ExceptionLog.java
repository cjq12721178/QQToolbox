package com.cjq.tool.qbox.util;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.cjq.tool.qbox.BuildConfig;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by CJQ on 2017/10/31.
 */

public class ExceptionLog {

    public static final int LOG_TYPE_DEBUG = 1;
    public static final int LOG_TYPE_DISPLAY = 1 << 1;
    public static final int LOG_TYPE_RECORD = 1 << 2;

    private static final String LOG_TAG = "log_debug";

    private static Context context;
    private static String logFileDirectory;

    private ExceptionLog() {
    }

    public static void initialize(Context c, String logDirectory) {
        if (c == null) {
            throw new NullPointerException("context may not be null");
        }
        if (TextUtils.isEmpty(logDirectory)) {
            throw new IllegalArgumentException("current app logFileDirectory in sd card is necessary");
        }
        context = c;
        logFileDirectory = logDirectory;
    }

    public static void process(int logType, Exception e) {
        if (e == null) {
            return;
        }

        if ((logType & LOG_TYPE_DEBUG) != 0 && isDebuggable()) {
            Log.d(LOG_TAG, e.getMessage());
        }

        if ((logType & LOG_TYPE_DISPLAY) != 0) {
            SimpleCustomizeToast.show(context, e.getMessage());
        }

        if ((logType & LOG_TYPE_RECORD) != 0) {
            StringWriter stringWriter = new StringWriter();
            e.printStackTrace(new PrintWriter(stringWriter));
            saveInLocalFile(stringWriter.toString());
        }
    }

    private static boolean isDebuggable() {
        return BuildConfig.DEBUG;
    }

    private static void saveInLocalFile(String information) {
        try {
            File directory = new File(Environment.getExternalStorageDirectory() +
                    File.separator + logFileDirectory);
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    SimpleCustomizeToast.show(context, "日志文件目录创建失败，无法记录异常信息");
                    return;
                }
            }
            File file = new File(directory.getAbsolutePath() +
                    File.separator + "ErrorInfo.txt");
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    SimpleCustomizeToast.show(context, "日志文件创建失败，无法记录异常信息");
                    return;
                }
            }
            FileWriter writer = new FileWriter(file, true);
            try {
                writer.write(information);
            } catch (IOException ioe) {
                SimpleCustomizeToast.show(context, "异常信息记录失败");
            } finally {
                writer.close();
            }
        } catch (IOException ioe) {
            SimpleCustomizeToast.show(context, "日志文件创建失败，无法记录异常信息");
        }
    }

//    private static String decorateMessage(String msg) {
//        //目前暂时简单的表示为“时间戳-错误信息”
//        StringBuffer buffer = new StringBuffer();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        buffer.setLength(0);
//        buffer.append(dateFormat.format(new Date()))
//                .append(" —— ")
//                .append(msg)
//                .append('\n');
//        return buffer.toString();
//    }
}
