package com.cjq.lib.weisi.communicator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by KAT on 2016/6/14.
 */
public class UdpKit implements Communicator {

    private static final int MAX_BUFFER_LEN = 255;
    private static final int MIN_SEND_DATA_TIME_INTERVAL = 10;

    private boolean mLaunched;
    //private boolean mListening;
    private DatagramPacket mReceivePacket;
    private DatagramPacket mSendPacket;
    private DatagramSocket mSocket;
    //private OnCommunicatorErrorOccurredListener mErrorOccurredListener;
    //private Thread mSendThread;
    //private OnSendThreadExecutor mOnSendThreadExecutor;
    //private OnReceiveThreadExecutor mOnReceiveThreadExecutor;

    public boolean launch() {
        return launch(0);
    }

    public boolean launch(int localPort) {
        if (!mLaunched) {
            try {
                if (localPort <= 0 || localPort > 65536) {
                    mSocket = new DatagramSocket();
                } else {
                    mSocket = new DatagramSocket(localPort);
                }
                mSendPacket = new DatagramPacket(new byte[MAX_BUFFER_LEN], MAX_BUFFER_LEN);
                mReceivePacket = new DatagramPacket(new byte[MAX_BUFFER_LEN], MAX_BUFFER_LEN);
                mLaunched = true;
            } catch (Exception e) {
                close();
            }
        }
        return mLaunched;
    }

//    private void onErrorProcess(Exception e) {
//        if (mErrorOccurredListener != null) {
//            mErrorOccurredListener.onErrorOccurred(e);
//        }
//    }
//
//    public void setErrorOccurredListener(OnCommunicatorErrorOccurredListener listener) {
//        mErrorOccurredListener = listener;
//    }

    public void send(String targetIp, int targetPort, byte[] data) throws IOException {
        send(InetAddress.getByName(targetIp), targetPort, data);
    }

    public void send(InetAddress ip, int port, byte[] data) throws IOException {
        mSendPacket.setData(data);
        mSendPacket.setAddress(ip);
        mSendPacket.setPort(port);
        mSocket.send(mSendPacket);
    }

    //使用前需先使用方法setSendParameter
    public void send() throws IOException {
        mSocket.send(mSendPacket);
    }

    public void setSendParameter(String ip, int port, byte[] data) throws UnknownHostException {
        setSendParameter(InetAddress.getByName(ip), port, data);
    }

    public void setSendParameter(InetAddress ip, int port, byte[] data) {
        setSendIp(ip);
        setSendPort(port);
        setSendData(data);
    }

    public void setSendIp(InetAddress ip) {
        mSendPacket.setAddress(ip);
    }

    public void setSendIp(String ip) throws UnknownHostException {
        setSendIp(InetAddress.getByName(ip));
    }

    public void setSendPort(int port) {
        mSendPacket.setPort(port);
    }

    public void setSendData(byte[] data) {
        mSendPacket.setData(data);
    }
//    //以circulateTime为时间间隔循环发送数据
//    public void send(String targetIp, int targetPort, byte[] data, int circulateTime) {
//        if (!mLaunched
//                || targetIp == null
//                || data == null
//                || circulateTime <= MIN_SEND_DATA_TIME_INTERVAL) {
//            return;
//        }
//        if (mOnSendThreadExecutor == null) {
//            mOnSendThreadExecutor = new OnSendThreadExecutor();
//        }
//        try {
//            mOnSendThreadExecutor
//                    .setTargetIp(targetIp)
//                    .setTargetPort(targetPort)
//                    .setSendData(data)
//                    .setCirculateTime(circulateTime);
//            if (mSendThread == null || !mSendThread.isAlive()) {
//                mSendThread = new Thread(mOnSendThreadExecutor);
//                mSendThread.start();
//            }
//        } catch (Exception e) {
//            onErrorProcess(e);
//        }
//    }

    public int receive(byte[] dst) throws IOException {
        return receive(dst, 0, dst.length);
    }

    public int receive(byte[] dst, int offset, int length) throws IOException {
        mReceivePacket.setData(dst, offset, length);
        mSocket.receive(mReceivePacket);
        return mReceivePacket.getLength();
    }

    @Override
    public int read(byte[] dst, int offset, int length) throws IOException {
        return receive(dst, offset, length);
    }

    @Override
    public boolean canRead() {
        return mLaunched;
    }

    @Override
    public void stopRead() throws IOException {
        if (mSocket != null && !mSocket.isClosed()) {
            mSocket.send(new DatagramPacket(new byte[0], 0, mSocket.getLocalAddress(), mSocket.getLocalPort()));
        }
    }

//    private class OnSendThreadExecutor implements Runnable {
//
//        private long mCirculateTime;
//        private InetAddress mTargetAddress;
//        private int mTargetPort;
//        private byte[] mSendData;
//
//        public OnSendThreadExecutor setCirculateTime(long circulateTime) {
//            mCirculateTime = circulateTime;
//            return this;
//        }
//
//        public OnSendThreadExecutor setTargetIp(InetAddress targetAddress) {
//            mTargetAddress = targetAddress;
//            return this;
//        }
//
//        public OnSendThreadExecutor setTargetIp(String targetAddress)
//                throws UnknownHostException {
//            mTargetAddress = InetAddress.getByName(targetAddress);
//            return this;
//        }
//
//        public OnSendThreadExecutor setTargetPort(int targetPort) {
//            mTargetPort = targetPort;
//            return this;
//        }
//
//        public OnSendThreadExecutor setSendData(byte[] send) {
//            mSendData = send;
//            return this;
//        }
//
//        @Override
//        public void run() {
//            while (mLaunched) {
//                try {
//                    send(mTargetAddress, mTargetPort, mSendData);
//                    Thread.sleep(mCirculateTime);
//                } catch (Exception e) {
//                    onErrorProcess(e);
//                }
//            }
//        }
//    }

//    public void startListen(boolean isAsynchronous, OnDataReceivedListener listener) {
//        if (mLaunched && !mListening && listener != null) {
//            if (mOnReceiveThreadExecutor == null) {
//                mOnReceiveThreadExecutor = new OnReceiveThreadExecutor();
//            }
//            mOnReceiveThreadExecutor.setOnDataReceivedListener(listener);
//            if (isAsynchronous) {
//                Thread receiveDataThread = new Thread(mOnReceiveThreadExecutor);
//                receiveDataThread.start();
//            } else {
//                mOnReceiveThreadExecutor.run();
//            }
//        }
//    }

//    private class OnReceiveThreadExecutor implements Runnable {
//
//        private OnDataReceivedListener mOnDataReceivedListener;
//
//        public void setOnDataReceivedListener(OnDataReceivedListener listener) {
//            mOnDataReceivedListener = listener;
//        }
//
//        @Override
//        public void run() {
//            mListening = true;
//            byte[] receiveData;
//            while (mListening) {
//                try {
//                    mSocket.receive(mReceivePacket);
//                    receiveData = mReceivePacket.getData();
//                    if (receiveData != null) {
//                        mOnDataReceivedListener.onDataReceived(receiveData);
//                    }
//                } catch (Exception e) {
//                    onErrorProcess(e);
//                }
//            }
//        }
//    }
//
//    public interface OnDataReceivedListener {
//        void onDataReceived(byte[] data);
//    }

    public void close() {
        mLaunched = false;
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
        mSendPacket = null;
        mReceivePacket = null;
//        try {
//            stopListen();
//            mLaunched = false;
//            if (mOnSendThreadExecutor != null) {
//                mOnSendThreadExecutor = null;
//            }
//            if (mSocket != null) {
//                mSocket.close();
//            }
//            setErrorOccurredListener(null);
//        } catch (Exception e) {
//            onErrorProcess(e);
//        }
    }

//    public void stopListen() {
//        mListening = false;
//        if (mOnReceiveThreadExecutor != null) {
//            mOnReceiveThreadExecutor.mOnDataReceivedListener = null;
//            mOnReceiveThreadExecutor = null;
//        }
//    }
}
