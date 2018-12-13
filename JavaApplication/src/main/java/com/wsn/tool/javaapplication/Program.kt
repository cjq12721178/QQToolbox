package com.wsn.tool.javaapplication


import com.wsn.lib.wsb.communicator.UdpKit
import com.wsn.lib.wsb.communicator.receiver.DataReceiver
import com.wsn.lib.wsb.config.ConfigurationImporter
import com.wsn.lib.wsc.aquirer.EthernetBSSensorDataAcquirer
import com.wsn.lib.wsc.aquirer.SensorDataAcquirer

object Program {

    @JvmStatic
    fun main(args: Array<String>) {
        testAcquireEthernetBaseStationSensorData()
    }

    private fun testAcquireEthernetBaseStationSensorData() {
        if (!ConfigurationImporter().leadIn("D:\\cjq\\PersonalProjects\\QQToolbox\\weisi\\src\\main\\assets\\EsbSensorConfiguration.xml")) {
            println("import configurations.xml failed")
            return
        }

        val acquirer = EthernetBSSensorDataAcquirer<Settings>()
        acquirer.start(Settings(0, "192.168.1.18", 5000, 500), object : SensorDataAcquirer.EventListener {
            override fun onStartSuccess() {
                System.out.print("ethernet acquirer start success")
            }

            override fun onStartFailed(cause: Int) {
                System.out.print("ethernet acquirer start failed")
                acquirer.stop()
            }

            override fun onDataAchieved(sensorAddress: Int, dataTypeValue: Byte, dataTypeValueIndex: Int, timestamp: Long, batteryVoltage: Float, rawValue: Double, rssi: Float?) {
                System.out.println("address = %04X, type = %02X, index = %d, timestamp = %d, voltage = %.2f, value = %.3f".format(sensorAddress, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue));
            }
        })

        System.`in`.read()
        acquirer.stop()
    }

    private class Settings(override var localPort: Int, override var remoteIp: String, override var remotePort: Int, override var dataRequestCycle: Long) : EthernetBSSensorDataAcquirer.Settings {

    }

    private fun testStopDataReceiver() {
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
