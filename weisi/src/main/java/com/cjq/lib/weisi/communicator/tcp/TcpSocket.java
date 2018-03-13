package com.cjq.lib.weisi.communicator.tcp;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.communicator.Communicator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by CJQ on 2018/3/9.
 */

public class TcpSocket implements Communicator {

    private Socket mSocket;
    private InputStream mReader;
    private OutputStream mWriter;

    public TcpSocket(@NonNull Socket socket) throws IOException {
        mSocket = socket;
        mReader = mSocket.getInputStream();
        mWriter = mSocket.getOutputStream();
    }

    public void write(byte[] src) throws IOException {
        write(src, 0, src.length);
    }

    public void write(byte[] src, int offset, int length) throws IOException {
        mWriter.write(src, offset, length);
    }

    public int receive(byte[] dst, int offset, int length) throws IOException {
        return mReader.read(dst, offset, length);
    }

    @Override
    public int read(byte[] dst, int offset, int length) throws IOException {
        int expectLen = mReader.available();
        return expectLen > 0
                ? receive(dst, offset, length)
                : expectLen;
    }

    @Override
    public boolean canRead() {
        return mSocket.isConnected();
    }

    @Override
    public void stopRead() throws IOException {
    }

    public void close() throws IOException {
        if (mSocket != null && !mSocket.isClosed()) {
            mReader.close();
            mWriter.close();
            mSocket.close();
        }
    }
}
