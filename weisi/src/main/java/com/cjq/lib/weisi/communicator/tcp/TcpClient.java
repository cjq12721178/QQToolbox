package com.cjq.lib.weisi.communicator.tcp;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Created by CJQ on 2018/3/12.
 */

public class TcpClient extends Tcp {

    private TcpSocket mSocket;

    public void connect(@NonNull final String serverIp, final int serverPort,
                        @NonNull final OnServerConnectListener listener) {
        connect(serverIp, serverPort, listener, 0);
    }

    public void connect(@NonNull final String serverIp, final int serverPort,
                        @NonNull final OnServerConnectListener listener,
                        final int timeout) {
        if (mSocket == null && mState != CONNECTING) {
            mState = CONNECTING;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Socket socket = new Socket();
                        socket.connect(new InetSocketAddress(serverIp, serverPort), timeout);
                        if (socket.isConnected()) {
                            mSocket = new TcpSocket(socket);
                            mState = CONNECTED;
                        }
                    } catch (IOException e) {
                        mSocket = null;
                        mState = UNCONNECTED;
                        e.printStackTrace();
                    } finally {
                        listener.onServerConnect(mState, mSocket);
                    }
                }
            }).start();
        } else {
            listener.onServerConnect(mState, mSocket);
        }
    }

    public TcpSocket getSocket() {
        return mSocket;
    }

    public void shutdown() {
        if (mSocket != null) {
            try {
                mSocket.close();
                mSocket = null;
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mState = UNCONNECTED;
            }
        }
    }

    public interface OnServerConnectListener {
        void onServerConnect(@ConnectState int state, TcpSocket socket);
    }
}
