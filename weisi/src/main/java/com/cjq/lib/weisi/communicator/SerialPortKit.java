package com.cjq.lib.weisi.communicator;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by CJQ on 2017/11/24.
 */

public class SerialPortKit {

    private static final String TAG = "SerialPort";

    /*
     * Do not remove or rename the field mFd: it is used by native method close();
     */
    private FileDescriptor mFd;
    private FileInputStream mFileInputStream;
    private FileOutputStream mFileOutputStream;

    // JNI
    private native static FileDescriptor open(String path, int baudRate, int flags);
    private native void close();
    static {
        System.loadLibrary("serial_port");
    }

    public boolean launch(String deviceName, int baudRate, int flags) {
        return launch(new File("/dev/" + deviceName), baudRate, flags);
    }

    public boolean launch(File device, int baudRate, int flags) {
        if (!isLaunch()) {
            /* Check access permission */
            if (!device.canRead() || !device.canWrite()) {
                try {
				/* Missing read/write permission, trying to chmod the file */
                    Process su;
                    //su = Runtime.getRuntime().exec("su");
                    su = Runtime.getRuntime().exec("/system/bin/su");
                    String cmd = "chmod 777 " + device.getAbsolutePath() + "\n"
                            + "exit\n";
                    su.getOutputStream().write(cmd.getBytes());
                    if ((su.waitFor() != 0) || !device.canRead()
                            || !device.canWrite()) {
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }

            mFd = open(device.getAbsolutePath(), baudRate, flags);
            if (mFd == null) {
                Log.e(TAG, "native open returns null");
            } else {
                mFileInputStream = new FileInputStream(mFd);
                mFileOutputStream = new FileOutputStream(mFd);
            }
        }
        return isLaunch();
    }

    public boolean isLaunch() {
        return mFd != null;
    }

    public void shutdown() {
        if (mFd != null) {
            close();
            mFd = null;
        }
    }

    // Getters and setters
    public InputStream getInputStream() {
        return mFileInputStream;
    }

    public OutputStream getOutputStream() {
        return mFileOutputStream;
    }
}
