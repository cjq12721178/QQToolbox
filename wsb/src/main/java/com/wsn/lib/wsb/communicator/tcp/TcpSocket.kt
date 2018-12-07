package com.wsn.lib.wsb.communicator.tcp


import com.wsn.lib.wsb.communicator.Communicator

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

/**
 * Created by CJQ on 2018/3/9.
 */

class TcpSocket @Throws(IOException::class)
constructor(private val socket: Socket) : Communicator {
    private val reader = socket.getInputStream()
    private val writer = socket.getOutputStream()

    fun isConnected() = socket.isConnected

    @Throws(IOException::class)
    @JvmOverloads
    fun write(src: ByteArray, offset: Int = 0, length: Int = src.size) {
        writer.write(src, offset, length)
    }

    @Throws(IOException::class)
    fun receive(dst: ByteArray, offset: Int, length: Int): Int {
        return reader.read(dst, offset, length)
    }

    @Throws(IOException::class)
    override fun read(dst: ByteArray, offset: Int, length: Int): Int {
        val expectLen = reader.available()
        return if (expectLen > 0)
            receive(dst, offset, length)
        else
            expectLen
    }

    override fun canRead(): Boolean {
        return isConnected()
    }

    @Throws(IOException::class)
    override fun stopRead() {
    }

    @Throws(IOException::class)
    fun close() {
        if (!socket.isClosed) {
            reader.close()
            writer.close()
            socket.close()
        }
    }
}
