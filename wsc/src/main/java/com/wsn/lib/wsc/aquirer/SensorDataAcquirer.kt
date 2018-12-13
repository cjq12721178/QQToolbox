package com.wsn.lib.wsc.aquirer

interface SensorDataAcquirer<S : SensorDataAcquirer.Settings> {

    fun start(settings: S, listener: EventListener)
    fun stop()
    fun restart(settings: S, listener: EventListener) {
        stop()
        start(settings, listener)
    }

    interface Settings {
    }

    interface EventListener {
        fun onStartSuccess()
        fun onStartFailed(cause: Int)
        fun onDataAchieved(sensorAddress: Int,
                           dataTypeValue: Byte,
                           dataTypeValueIndex: Int,
                           timestamp: Long,
                           batteryVoltage: Float,
                           rawValue: Double,
                           rssi: Float? = null)
    }
}