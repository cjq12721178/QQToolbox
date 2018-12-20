package com.wsn.lib.wsc.data

data class UdpSensorData(var sensorAddress: Int,
                         var dataTypeValue: Byte,
                         var timestamp: Long,
                         var batteryVoltage: Float,
                         var rawValue: Double)