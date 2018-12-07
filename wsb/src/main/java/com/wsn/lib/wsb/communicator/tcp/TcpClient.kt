package com.wsn.lib.wsb.communicator.tcp

import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket

import com.wsn.lib.wsb.communicator.tcp.Tcp.ConnectState.CONNECTED
import com.wsn.lib.wsb.communicator.tcp.Tcp.ConnectState.CONNECTING
import com.wsn.lib.wsb.communicator.tcp.Tcp.ConnectState.UNCONNECTED

/**
 * Created by CJQ on 2018/3/12.
 */

class TcpClient : Tcp() {

    lateinit var tcpSocket: TcpSocket
        private set

    @JvmOverloads
    fun connect(serverIp: String, serverPort: Int,
                listener: OnServerConnectListener,
                timeout: Int = 0) {
        if (state == UNCONNECTED) {
            state = CONNECTING
            Thread(Runnable {
                try {
                    val socket = Socket()
                    socket.connect(InetSocketAddress(serverIp, serverPort), timeout)
                    if (socket.isConnected) {
                        tcpSocket = TcpSocket(socket)
                        state = CONNECTED
                    }
                } catch (e: IOException) {
                    shutdown()
                    state = UNCONNECTED
                    e.printStackTrace()
                } finally {
                    notifyListener(listener)
                }
            }).start()
        } else {
            notifyListener(listener)
        }
    }

    private fun notifyListener(listener: OnServerConnectListener) {
        listener.onServerConnect(state, if (state == CONNECTED) {
            tcpSocket
        } else {
            null
        })
    }

    fun shutdown() {
        if (state == CONNECTED) {
            try {
                tcpSocket.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                state = UNCONNECTED
            }
        }
    }

    interface OnServerConnectListener {
        fun onServerConnect(state: Tcp.ConnectState, socket: TcpSocket?)
    }
}
