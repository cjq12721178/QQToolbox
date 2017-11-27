package com.cjq.lib.weisi.communicator;

import android.util.Log;

import com.cjq.tool.qbox.util.ExceptionLog;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
    private boolean mListening;
    private OnCommunicatorErrorOccurredListener mErrorOccurredListener;

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
				/* Missing receive/send permission, trying to chmod the file */
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
                    processError(e);
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
        stopListen();
        if (mFd != null) {
            close();
            mFd = null;
        }
    }

    public void setErrorOccurredListener(OnCommunicatorErrorOccurredListener listener) {
        mErrorOccurredListener = listener;
    }

    public void processError(Exception e) {
        if (mErrorOccurredListener != null) {
            mErrorOccurredListener.onErrorOccurred(e);
        }
    }

    //若输入流未获取，返回-2
    public int receive() throws IOException {
        return mFileInputStream != null
                ? mFileInputStream.read()
                : -2;
    }

    public int receive(byte b[]) throws IOException {
        return receive(b, 0, b.length);
    }

    //若输入流未获取，返回-2
    public int receive(byte b[], int off, int len) throws IOException {
        return mFileInputStream != null
                ? mFileInputStream.read(b, off, len)
                : -2;
    }

    public void send(byte b[]) throws IOException {
        send(b, 0, b.length);
    }

    public void send(byte b[], int off, int len) throws IOException {
        if (mFileOutputStream != null) {
            mFileOutputStream.write(b, off, len);
        }
    }

    public void send(int b) throws IOException {
        send(new byte[] { (byte) b }, 0, 1);
    }

    public void startListen(final OnDataReceivedListener listener) {
        if (mFileInputStream == null
                || listener == null
                || mListening) {
            return;
        }
        Thread thread = new Thread() {

            @Override
            public void run() {
                int receivedLen;
                int handledLen;
                int offset = 0;
                mListening = true;
                byte[] data = new byte[2048];
                while (mListening && !isInterrupted()) {
                    try {
                        if (mFileInputStream != null) {
                            receivedLen = mFileInputStream.read(data, offset, data.length - offset);
                            handledLen = listener.onDataReceived(data, offset + receivedLen);
                            offset = saveUnhandledData(data, offset + receivedLen, handledLen);
                        }
                    } catch (IOException e) {
                        processError(e);
                    }
                }
                mListening = false;
            }
        };
        thread.start();
    }

    public void stopListen() {
        if (mListening) {
            mListening = false;
        }
    }

    private int saveUnhandledData(byte[] data, int receivedLen, int handledLen) {
        if (handledLen >= receivedLen) {
            return 0;
        } else if (handledLen > 0 && handledLen < receivedLen) {
            for (int i = handledLen; i < receivedLen; ++i) {
                data[i - handledLen] = data[i];
            }
            return receivedLen - handledLen;
        } else {
            return receivedLen;
        }
    }

    public interface OnDataReceivedListener {
        //返回已处理字节数
        int onDataReceived(byte[] data, int len);
    }
}
