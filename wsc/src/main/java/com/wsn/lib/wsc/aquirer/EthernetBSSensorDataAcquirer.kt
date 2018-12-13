package com.wsn.lib.wsc.aquirer

import com.wsn.lib.wsb.communicator.UdpKit
import com.wsn.lib.wsb.protocol.ControllableSensorProtocol
import com.wsn.lib.wsb.protocol.UdpSensorProtocol

class EthernetBSSensorDataAcquirer<S : EthernetBSSensorDataAcquirer.Settings> : CommonSensorDataAcquirer<S>(UdpSensorProtocol()) {

    private val udpKit = UdpKit()
    private val dataRequestFrame = protocol.makeDataRequestFrame()

    override fun onStart(settings: S, listener: OnCommunicationEstablishedListener): Boolean {
        return if (udpKit.launch(settings.localPort)) {
            udpKit.setSendIp(settings.remoteIp)
            udpKit.setSendPort(settings.remotePort)
            listener.onCommunicationEstablished(udpKit)
            true
        } else {
            listener.onCommunicationUnestablished()
            false
        }
    }

    override fun onSendDataRequestFrame() {
        udpKit.send()
    }

    override fun onTimeSynchronize() {
        udpKit.setSendData(protocol.makeTimeSynchronizationFrame())
        udpKit.send()
    }

    override fun onTimeSynchronizationAnalyzed(timestamp: Long) {
        udpKit.setSendData(dataRequestFrame)
    }

    override fun onStop() {
        udpKit.close()
    }

    interface Settings : CommonSensorDataAcquirer.Settings {
        var localPort: Int
        var remoteIp: String
        var remotePort: Int
    }
}