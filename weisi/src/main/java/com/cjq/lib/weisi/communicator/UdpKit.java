package com.cjq.lib.weisi.communicator;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by KAT on 2016/6/14.
 */
public class UdpKit implements Communicator {

    private static final int MAX_BUFFER_LEN = 255;

    private boolean mLaunched;
    private DatagramPacket mReceivePacket;
    private DatagramPacket mSendPacket;
    private DatagramSocket mSocket;

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

    public void close() {
        mLaunched = false;
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
        mSendPacket = null;
        mReceivePacket = null;
    }
}
