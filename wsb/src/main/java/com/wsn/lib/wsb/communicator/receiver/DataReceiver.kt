package com.wsn.lib.wsb.communicator.receiver


import com.wsn.lib.wsb.communicator.Communicator

import java.io.IOException

/**
 * Created by CJQ on 2017/12/22.
 */

open class DataReceiver(private val communicator: Communicator) {

    protected var state: State = State.STOPPED
        private set
    //private var listener: Listener? = null

    val isListening: Boolean
        get() = state === State.RUNNING

    fun startListen(listener: Listener): Boolean {
        return startListen(listener, DEFAULT_BUFFER_SIZE)
    }

    @Synchronized
    fun startListen(listener: Listener, bufferSize: Int): Boolean {
        if (state == State.STOPPED) {
            if (bufferSize <= 0 || !communicator.canRead()) {
                return false
            }
            //listener = l
            state = State.RUNNING
            Thread(Runnable {
                var receivedLen: Int
                var handledLen: Int
                var bufferLen = 0
                val data = ByteArray(bufferSize)
                while (isListening && communicator.canRead()) {
                    try {
                        receivedLen = communicator.read(data, bufferLen, data.size - bufferLen)
                        if (receivedLen > 0) {
                            bufferLen += receivedLen
                            handledLen = listener.onDataReceived(data, bufferLen)
                            bufferLen = saveUnhandledData(data, bufferLen, handledLen)
                        } else if (receivedLen == -1) {
                            throw ReceiveException("recv len = 1")
                        }
                    } catch (ioe: IOException) {
                        if (listener.onErrorOccurred(ioe)) {
                            state = State.STOPPING
                        }
                    }
                }
                state = State.STOPPED
            }).start()
        }
        return isListening
    }

    private fun saveUnhandledData(data: ByteArray, receivedLen: Int, handledLen: Int): Int {
        if (handledLen >= receivedLen) {
            return 0
        } else if (handledLen > 0 && handledLen < receivedLen) {
            for (i in handledLen until receivedLen) {
                data[i - handledLen] = data[i]
            }
            return receivedLen - handledLen
        } else {
            return receivedLen
        }
    }

    @Synchronized
    fun stopListen() {
        if (isListening) {
            state = State.STOPPING
            communicator.stopRead()
        }
    }

    //protected open fun onStopListen(communicator: Communicator) {}

    protected enum class State {
        STOPPED,
        RUNNING,
        STOPPING
    }

    interface Listener {
        //len为实际接收数据长度
        //返回已处理字节数
        fun onDataReceived(data: ByteArray, len: Int): Int

        //返回true表示停止监听，返回false表示继续监听
        fun onErrorOccurred(e: Exception): Boolean
    }

    companion object {

        private const val DEFAULT_BUFFER_SIZE = 2048
    }
}
