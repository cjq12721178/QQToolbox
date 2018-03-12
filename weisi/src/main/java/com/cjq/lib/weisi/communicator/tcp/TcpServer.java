package com.cjq.lib.weisi.communicator.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by CJQ on 2018/3/9.
 */

public class TcpServer {

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

    public void accept(final OnClientAcceptListener listener) {
        if (listener == null) {
            return;
        }
        if (!isLaunched()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isLaunched()) {
                    TcpSocket socket = null;
                    try {
                        Socket client = mSocket.accept();
                        socket = new TcpSocket(client);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (!listener.onClientAccept(socket)) {
                            break;
                        }
                    }
                }
            }
        }).start();
    }

    public void shutdown() {
        if (isLaunched()) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Created by CJQ on 2018/3/12.
     */
    public interface OnClientAcceptListener {
        //返回true将继续等待客户端连接
        boolean onClientAccept(TcpSocket socket);
    }
}
