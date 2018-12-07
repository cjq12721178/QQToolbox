package com.cjq.lib.weisi.iot;

import com.wsn.lib.wsb.protocol.UdpSensorProtocol;
import com.wsn.lib.wsb.util.HexDump;

import org.junit.Test;

public class ProtocolTest {

    @Test
    public void getDataRequestFrame() {
        System.out.println(HexDump.dumpHexString(new UdpSensorProtocol().makeDataRequestFrame()));
    }
}
