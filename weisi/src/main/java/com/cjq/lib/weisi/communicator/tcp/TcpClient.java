package com.cjq.lib.weisi.communicator.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * Created by CJQ on 2018/3/12.
 */

public class TcpClient {

    private TcpSocket mSocket;

    public void connect(final String serverIp, final int serverPort,
                        final OnServerConnectListener listener) {
        connect(serverIp, serverPort, listener, 0);
    }

    public void connect(final String serverIp, final int serverPort,
                        final OnServerConnectListener listener,
                        final int timeout) {
        if (mSocket == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(serverIp, serverPort), timeout);
                        if (socket.isConnected()) {
                            mSocket = new TcpSocket(socket);
                        }
                    } catch (IOException e) {
                        mSocket = null;
                        e.printStackTrace();
                    } finally {
                        listener.onServerConnect(mSocket);
                    }
                }
            }).start();
        }
    }

    public TcpSocket getSocket() {
        return mSocket;
    }

    public void shutdown() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public interface OnServerConnectListener {
        void onServerConnect(TcpSocket socket);
    }
}
