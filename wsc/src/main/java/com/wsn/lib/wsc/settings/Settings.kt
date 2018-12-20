package com.wsn.lib.wsc.settings

import com.wsn.lib.wsc.aquirer.EthernetBSSensorDataAcquirer

class Settings(override var localPort: Int, override var remoteIp: String, override var remotePort: Int, override var dataRequestCycle: Long) : EthernetBSSensorDataAcquirer.Settings {

}