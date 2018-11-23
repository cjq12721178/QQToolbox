package com.cjq.lib.weisi.iot;

import com.cjq.lib.weisi.protocol.UdpSensorProtocol;
import com.cjq.lib.weisi.util.HexDump;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProtocolTest {

    @Test
    public void getDataRequestFrame() {
        System.out.println(HexDump.dumpHexString(new UdpSensorProtocol().makeDataRequestFrame()));
    }
}
