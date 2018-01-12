package com.cjq.lib.weisi.protocol;

/**
 * Created by CJQ on 2018/1/11.
 */

public abstract class BaseSensorProtocol<A extends Analyzable> {

    protected static final int CRC16_LENGTH = Crc.CRC16_LENGTH;
    protected static final int DATA_TYPE_VALUE_LENGTH = 1;
    protected static final int SENSOR_BATTERY_VOLTAGE_LENGTH = 1;
    protected static final int RSSI_LENGTH = 1;

    protected final A mAnalyzer;

    protected BaseSensorProtocol(A analyzer) {
        mAnalyzer = analyzer;
    }

    public abstract Crc getCrc();

    public abstract boolean isCrcMsb();
}
