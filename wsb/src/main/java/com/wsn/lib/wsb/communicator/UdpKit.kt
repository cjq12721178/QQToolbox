package com.wsn.lib.wsb.communicator

import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * Created by KAT on 2016/6/14.
 */
class UdpKit : Communicator {

    var launched: Boolean = false
        private set
    private lateinit var receivePacket: DatagramPacket
    private lateinit var sendPacket: DatagramPacket
    private lateinit var socket: DatagramSocket

    @JvmOverloads
    fun launch(localPort: Int = 0): Boolean {
        if (!launched) {
            try {
                socket = if (localPort <= 0 || localPort > 65536) {
                    DatagramSocket()
                } else {
                    DatagramSocket(localPort)
                }
                sendPacket = DatagramPacket(ByteArray(MAX_BUFFER_LEN), MAX_BUFFER_LEN)
                receivePacket = DatagramPacket(ByteArray(MAX_BUFFER_LEN), MAX_BUFFER_LEN)
                //socket.soTimeout = 200
                launched = true
            } catch (e: Exception) {
                close()
            }
        }
        return launched
    }

    @Throws(IOException::class)
    fun send(targetIp: String, targetPort: Int, data: ByteArray) {
        send(InetAddress.getByName(targetIp), targetPort, data)
    }

    @Throws(IOException::class)
    fun send(ip: InetAddress, port: Int, data: ByteArray) {
        if (launched) {
            sendPacket.data = data
            sendPacket.address = ip
            sendPacket.port = port
            socket.send(sendPacket)
        }
    }

    //使用前需先使用方法setSendParameter
    @Throws(IOException::class)
    fun send() {
        if (launched) {
            socket.send(sendPacket)
        }
    }

    @Throws(UnknownHostException::class)
    fun setSendParameter(ip: String, port: Int, data: ByteArray) {
        setSendParameter(InetAddress.getByName(ip), port, data)
    }

    fun setSendParameter(ip: InetAddress, port: Int, data: ByteArray) {
        if (launched) {
            setSendIp(ip)
            setSendPort(port)
            setSendData(data)
        }
    }

    fun setSendIp(ip: InetAddress) {
        if (launched) {
            sendPacket.address = ip
        }
    }

    @Throws(UnknownHostException::class)
    fun setSendIp(ip: String) {
        setSendIp(InetAddress.getByName(ip))
    }

    fun setSendPort(port: Int) {
        if (launched) {
            sendPacket.port = port
        }
    }

    fun setSendData(data: ByteArray) {
        if (launched) {
            sendPacket.data = data
        }
    }

    @Throws(IOException::class)
    @JvmOverloads
    fun receive(dst: ByteArray, offset: Int = 0, length: Int = dst.size): Int {
        receivePacket.setData(dst, offset, length)
        socket.receive(receivePacket)
        return receivePacket.length
    }

    @Throws(IOException::class)
    override fun read(dst: ByteArray, offset: Int, length: Int): Int {
        return receive(dst, offset, length)
    }

    override fun canRead(): Boolean {
        return launched
    }

    override fun stopRead() {
        if (launched && !socket.isClosed) {
            try {
                sendStopReadFrame()
            } catch (ioe: IOException) {
                ioe.printStackTrace()
            } catch (e: java.lang.Exception) {
                close()
            }
        }
    }

    private fun sendStopReadFrame() {
        socket.send(DatagramPacket(ByteArray(0), 0, InetAddress.getByName("127.0.0.1"), socket.localPort))
    }

    fun close() {
        if (launched) {
            launched = false
            socket.close()
        }
    }

    companion object {
        private const val MAX_BUFFER_LEN = 255
    }
}
