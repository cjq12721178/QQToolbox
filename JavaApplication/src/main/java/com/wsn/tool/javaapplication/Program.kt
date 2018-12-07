package com.wsn.tool.javaapplication

import com.wsn.lib.wsb.communicator.UdpKit
import com.wsn.lib.wsb.communicator.receiver.DataReceiver

object Program {

    @JvmStatic
    fun main(args: Array<String>) {
        val udp = UdpKit()
        if (!udp.launch()) {
            println("udp launch failed")
            return
        }
        println("udp launch success")

        val receiver = DataReceiver(udp)
        if (!receiver.startListen(object : DataReceiver.Listener {
                    override fun onDataReceived(data: ByteArray, len: Int): Int {
                        println("data len = $len")
                        return len
                    }

                    override fun onErrorOccurred(e: Exception): Boolean {
                        println(e.message)
                        return false
                    }
                })) {
            println("start data receiver failed")
            return
        }
        println("start data receiver success")

        Thread(Runnable {
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            receiver.stopListen()
        }).start()
    }
}
