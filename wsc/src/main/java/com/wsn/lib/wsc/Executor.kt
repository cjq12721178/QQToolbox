package com.wsn.lib.wsc

import com.google.gson.GsonBuilder
import com.wsn.lib.wsb.config.ConfigurationImporter
import com.wsn.lib.wsc.aquirer.EthernetBSSensorDataAcquirer
import com.wsn.lib.wsc.aquirer.SensorDataAcquirer
import com.wsn.lib.wsc.data.UdpSensorData
import com.wsn.lib.wsc.settings.Settings

class Executor {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            if (args.size != 5) {
                System.err.println("input argument size(${args.size}) exception")
                return
            }
            if (!ConfigurationImporter().leadIn(args[0])) {
                System.err.println("import configurations.xml failed")
                return
            }

            val gson = GsonBuilder().serializeNulls().create();
            val data = UdpSensorData(0, 0, 0, 0f, 0.0);
            val acquirer = EthernetBSSensorDataAcquirer<Settings>()
            acquirer.start(Settings(args[1].toInt(), args[2], args[3].toInt(), args[4].toLong()), object : SensorDataAcquirer.EventListener {
                override fun onStartSuccess() {
                }

                override fun onStartFailed(cause: Int) {
                    System.err.println("ethernet acquirer start failed")
                    acquirer.stop()
                }

                override fun onDataAchieved(sensorAddress: Int, dataTypeValue: Byte, dataTypeValueIndex: Int, timestamp: Long, batteryVoltage: Float, rawValue: Double, rssi: Float?) {
                    data.sensorAddress = sensorAddress
                    data.dataTypeValue = dataTypeValue
                    data.timestamp = timestamp
                    data.rawValue = rawValue
                    data.batteryVoltage = batteryVoltage
                    System.out.print(gson.toJson(data))
                }
            })

            while (System.`in`.read() != 's'.toInt());
            acquirer.stop()
            System.err.println("acquire stop")
        }
    }
}