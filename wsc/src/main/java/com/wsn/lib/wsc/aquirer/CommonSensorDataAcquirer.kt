package com.wsn.lib.wsc.aquirer

import com.wsn.lib.wsb.communicator.Communicator
import com.wsn.lib.wsb.communicator.receiver.DataReceiver
import com.wsn.lib.wsb.protocol.ControllableSensorProtocol
import com.wsn.lib.wsb.protocol.OnFrameAnalyzedListener
import java.io.IOException
import java.util.*


abstract class CommonSensorDataAcquirer<S : CommonSensorDataAcquirer.Settings>(protected val protocol: ControllableSensorProtocol<*>) : SensorDataAcquirer<S> {

    var running = false
        protected set
    private var hasTimeSynchronized = false
    private var dataRequestTask: DataRequestTask? = null
    private lateinit var dataReceiver: DataReceiver

    override fun start(settings: S, listener: SensorDataAcquirer.EventListener) {
        if (onStart(settings, object : OnCommunicationEstablishedListener {
                    override fun onCommunicationEstablished(c: Communicator) {
                        dataReceiver = DataReceiver(c)
                        if (dataReceiver.startListen(object : DataReceiver.Listener {
                                    override fun onDataReceived(data: ByteArray, len: Int): Int {
                                        return protocol.analyzeMultiplePackages(data, 0, len, object : OnFrameAnalyzedListener {
                                            override fun onTimeSynchronizationAnalyzed(timestamp: Long) {
                                                hasTimeSynchronized = true
                                                this@CommonSensorDataAcquirer.onTimeSynchronizationAnalyzed(timestamp)
                                            }

                                            override fun onSensorInfoAnalyzed(sensorAddress: Int, dataTypeValue: Byte, dataTypeIndex: Int, timestamp: Long, batteryVoltage: Float, rawValue: Double) {
                                                listener.onDataAchieved(sensorAddress, dataTypeValue, dataTypeIndex, timestamp, batteryVoltage, rawValue)
                                            }
                                        })
                                    }

                                    override fun onErrorOccurred(e: Exception): Boolean {
                                        e.printStackTrace()
                                        return false
                                    }
                                })) {
                            startDataRequestTask(settings.dataRequestCycle)
                            notifyStartSuccess(listener)
                        } else {
                            notifyStartFailed(listener, ERR_START_LISTEN_FAILED)
                        }
                    }

                    override fun onCommunicationUnestablished() {
                        notifyStartFailed(listener, ERR_LAUNCH_COMMUNICATOR_FAILED)
                    }
                })) {

        } else {
            notifyStartFailed(listener, ERR_LAUNCH_COMMUNICATOR_FAILED)
        }
    }

    override fun stop() {
        stopDataRequestTask()
        dataReceiver.stopListen()
        onStop()
    }

    private fun startDataRequestTask(cycle: Long) {
        stopDataRequestTask()
        if (dataRequestTask === null) {
            dataRequestTask = DataRequestTask()
        }
        dateRequestTimer.schedule(dataRequestTask, 0, cycle)
    }

    private fun stopDataRequestTask() {
        dataRequestTask?.run {
            cancel()
            dataRequestTask = null
            hasTimeSynchronized = false
        }
    }

    protected fun notifyStartSuccess(listener: SensorDataAcquirer.EventListener) {
        listener.onStartSuccess()
        running = true
    }

    protected fun notifyStartFailed(listener: SensorDataAcquirer.EventListener, cause: Int) {
        listener.onStartFailed(cause)
        running = false
    }

    fun timeSynchronize(): Boolean {
        try {
            onTimeSynchronize()
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    protected abstract fun onStart(settings: S, listener: OnCommunicationEstablishedListener): Boolean

    @Throws(IOException::class)
    protected abstract fun onSendDataRequestFrame()

    @Throws(IOException::class)
    protected abstract fun onTimeSynchronize()

    protected abstract fun onTimeSynchronizationAnalyzed(timestamp: Long)

    protected abstract fun onStop()

    companion object {
        const val ERR_LAUNCH_COMMUNICATOR_FAILED = 1
        const val ERR_START_LISTEN_FAILED = 2

        var dateRequestTimer = Timer()
    }

    interface Settings : SensorDataAcquirer.Settings {
        var dataRequestCycle: Long
    }

    private inner class DataRequestTask : TimerTask() {

        override fun run() {
            try {
                if (hasTimeSynchronized) {
                    onSendDataRequestFrame()
                } else {
                    onTimeSynchronize()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    interface OnCommunicationEstablishedListener {
        fun onCommunicationEstablished(c: Communicator)
        fun onCommunicationUnestablished()
    }
}