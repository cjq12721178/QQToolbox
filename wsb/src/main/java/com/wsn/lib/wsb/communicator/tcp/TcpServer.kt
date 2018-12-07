package com.wsn.lib.wsb.communicator.tcp

import java.io.IOException
import java.net.ServerSocket

import com.wsn.lib.wsb.communicator.tcp.Tcp.ConnectState.CONNECTED
import com.wsn.lib.wsb.communicator.tcp.Tcp.ConnectState.CONNECTING
import com.wsn.lib.wsb.communicator.tcp.Tcp.ConnectState.UNCONNECTED

/**
 * Created by CJQ on 2018/3/9.
 */

class TcpServer : Tcp() {

    private var serverSocket: ServerSocket? = null

    val isLaunched: Boolean
        get() = serverSocket != null

    fun launch(localPort: Int): Boolean {
        if (!isLaunched) {
            try {
                serverSocket = ServerSocket(localPort)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return isLaunched
    }

    fun accept(listener: OnClientAcceptListener) {
        if (isLaunched && state != CONNECTING) {
            state = CONNECTING
            Thread(Runnable {
                while (isLaunched && state == CONNECTING) {
                    var tcpSocket: TcpSocket? = null
                    try {
                        val client = serverSocket!!.accept()
                        tcpSocket = TcpSocket(client)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        state = if (tcpSocket != null) CONNECTED else UNCONNECTED
                        if (listener.onClientAccept(state, tcpSocket)) {
                            state = CONNECTING
                        }
                    }
                }
            }).start()
        } else {
            listener.onClientAccept(state, null)
        }
    }

    fun shutdown() {
        if (isLaunched) {
            try {
                if (serverSocket?.isClosed == false) {
                    serverSocket?.close()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                state = UNCONNECTED
                serverSocket = null
            }
        }
    }

    /**
     * Created by CJQ on 2018/3/12.
     */
    interface OnClientAcceptListener {
        //返回true将继续等待客户端连接
        fun onClientAccept(state: Tcp.ConnectState, socket: TcpSocket?): Boolean
    }
}
