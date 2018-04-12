package com.cjq.lib.weisi.communicator.tcp;

import android.support.annotation.NonNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by CJQ on 2018/3/9.
 */

public class TcpServer extends Tcp {

    private ServerSocket mSocket;

    public boolean launch(int localPort) {
        if (!isLaunched()) {
            try {
                mSocket = new ServerSocket(localPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return isLaunched();
    }

    public boolean isLaunched() {
        return mSocket != null;
    }

    public void accept(@NonNull final OnClientAcceptListener listener) {
        if (listener != null && isLaunched() && mState != CONNECTING) {
            mState = CONNECTING;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isLaunched() && mState == CONNECTING) {
                        TcpSocket socket = null;
                        try {
                            Socket client = mSocket.accept();
                            socket = new TcpSocket(client);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            mState = socket != null ? CONNECTED : UNCONNECTED;
                            if (listener.onClientAccept(mState, socket)) {
                                mState = CONNECTING;
                            }
                        }
                    }
                }
            }).start();
        } else {
            listener.onClientAccept(mState, null);
        }
    }

    public void shutdown() {
        if (isLaunched()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mState = UNCONNECTED;
            }
        }
    }

    /**
     * Created by CJQ on 2018/3/12.
     */
    public interface OnClientAcceptListener {
        //返回true将继续等待客户端连接
        boolean onClientAccept(@ConnectState int state, TcpSocket socket);
    }
}
