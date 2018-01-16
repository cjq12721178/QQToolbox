package com.cjq.lib.weisi.communicator.receiver;

import com.cjq.lib.weisi.communicator.Communicator;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by CJQ on 2017/12/22.
 */

public class DataReceiver {

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    private final Communicator mCommunicator;
    //private volatile boolean mListening;
    private State mState;
    private Listener mListener;

    public DataReceiver(Communicator communicator) {
        if (communicator == null) {
            throw new NullPointerException("communicator may not be null");
        }
        mCommunicator = communicator;
        mState = State.STOPPED;
    }

    public boolean startListen(Listener listener) {
        return startListen(listener, DEFAULT_BUFFER_SIZE);
    }

    public synchronized boolean startListen(Listener listener, final int bufferSize) {
        if (mState == State.STOPPED) {
            if (listener == null || bufferSize <= 0 || !mCommunicator.canRead()) {
                return false;
            }
            mListener = listener;
            mState = State.RUNNING;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int receivedLen = 0;
                    int handledLen;
                    byte[] data = new byte[bufferSize];
                    while (isListening() && mCommunicator.canRead()) {
                        try {
                            receivedLen += mCommunicator.read(data, receivedLen, data.length - receivedLen);
                            if (receivedLen > 0) {
                                handledLen = mListener.onDataReceived(data, receivedLen);
                                receivedLen = saveUnhandledData(data, receivedLen, handledLen);
                            }
                        } catch (IOException ioe) {
                            if (mListener.onErrorOccurred(ioe)) {
                                mState = State.STOPPING;
                            }
                        }
                    }
                    mState = State.STOPPED;
                    mListener = null;
                }
            }).start();
        }
        return isListening();
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

    public boolean isListening() {
        return mState == State.RUNNING;
    }

    public synchronized void stopListen() {
        if (isListening()) {
            mState = State.STOPPING;
            onStopListen(mCommunicator);
        }
    }

    protected void onStopListen(Communicator communicator) {
    }

    protected State getState() {
        return mState;
    }

    protected enum State {
        STOPPED,
        RUNNING,
        STOPPING
    }

    public interface Listener {
        //len为实际接收数据长度
        //返回已处理字节数
        int onDataReceived(byte[] data, int len);
        //返回true表示停止监听，返回false表示继续监听
        boolean onErrorOccurred(Exception e);
    }
}
