package com.wsn.lib.wsb.protocol;

import com.wsn.lib.wsb.util.NumericConverter;

import java.util.Arrays;

/**
 * Created by CJQ on 2017/7/13.
 */

public class BleSensorProtocol extends BaseSensorProtocol<BleAnalyzer> {

    public static final int BROADCAST_ADDRESS_LENGTH = 6;
    private static final int DATA_ZONE_LENGTH_LENGTH = 1;
    private static final int SENSOR_VALUE_LENGTH = 4;

    //private static final int BATTERY_INFO_LENGTH = 1;
    private static final int SENSOR_DATA_LENGTH = BaseSensorProtocol.DATA_TYPE_VALUE_LENGTH
            + SENSOR_VALUE_LENGTH;
    private static final int MIN_DATA_ZONE_LENGTH = BaseSensorProtocol.SENSOR_BATTERY_VOLTAGE_LENGTH
            + BaseSensorProtocol.RSSI_LENGTH
            + SENSOR_DATA_LENGTH;
    private static final int MIN_FRAME_LENGTH = DATA_ZONE_LENGTH_LENGTH
            + MIN_DATA_ZONE_LENGTH
            + BaseSensorProtocol.CRC16_LENGTH;

    private final byte[] mDataTypeArray = new byte[255];
    private final byte[] mTmpBroadcastAddress = new byte[BROADCAST_ADDRESS_LENGTH];

    public BleSensorProtocol() {
        super(new BleAnalyzer());
    }

    public void analyze(String broadcastAddress, byte[] broadcastData, OnSensorInfoAnalyzeListener listener) {
        if (broadcastAddress == null
                || broadcastData == null
                || broadcastData.length < MIN_FRAME_LENGTH
                || listener == null) {
            return;
        }
        if (!broadcastAddressStringToBytes(mTmpBroadcastAddress, broadcastAddress)) {
            return;
        }
        int dataZoneLength = NumericConverter.INSTANCE.int8ToUInt16(broadcastData[0]) - BaseSensorProtocol.CRC16_LENGTH;
        if (dataZoneLength < MIN_DATA_ZONE_LENGTH
                || !getCrc().isCorrect16WithCrcAppended(
                        broadcastData,
                        0,
                        DATA_ZONE_LENGTH_LENGTH + dataZoneLength,
                        getCrc().calc16ByLsb(mTmpBroadcastAddress),
                        true,
                        isCrcMsb())
                /* || CrcClass.calc16CcittByMsb(CrcClass.calc16CcittByLSB(mTmpBroadcastAddress), broadcastData, 0, DATA_ZONE_LENGTH_LENGTH + dataZoneLength) !=
                NumericConverter.int8ToUInt16(broadcastData[DATA_ZONE_LENGTH_LENGTH + dataZoneLength],
                        broadcastData[DATA_ZONE_LENGTH_LENGTH + dataZoneLength + 1]) */) {
            return;
        }
        //是否为阵列传感器
        //暂时没有应用
//        boolean isArraySensor = NumericConverter.int8ToUInt16(mTmpBroadcastAddress[4]) != 0;
//        if (isArraySensor) {
//            Arrays.fill(mDataTypeArray, (byte) 0);
//        }
        Arrays.fill(mDataTypeArray, (byte) 0);
        int sensorAddress = broadcastAddressToRawAddress(mTmpBroadcastAddress);
        long timestamp = System.currentTimeMillis();
        float voltage = getAnalyzer().analyzeBatteryVoltage(broadcastData[broadcastData.length
                - BaseSensorProtocol.CRC16_LENGTH
                - BaseSensorProtocol.RSSI_LENGTH
                - BaseSensorProtocol.SENSOR_BATTERY_VOLTAGE_LENGTH]);
        for (int start = DATA_ZONE_LENGTH_LENGTH,
             end = start + (dataZoneLength - BaseSensorProtocol.RSSI_LENGTH - BaseSensorProtocol.SENSOR_BATTERY_VOLTAGE_LENGTH) / SENSOR_DATA_LENGTH * SENSOR_DATA_LENGTH;
             start < end;
             start += SENSOR_DATA_LENGTH) {
            listener.onSensorInfoAnalyzed(sensorAddress,
                    broadcastData[start],
//                    isArraySensor
//                            ? mDataTypeArray[NumericConverter.int8ToUInt16(broadcastData[start])]++
//                            : 0,
                    mDataTypeArray[NumericConverter.INSTANCE.int8ToUInt16(broadcastData[start])]++,
                    timestamp,
                    voltage,
                    getAnalyzer().analyzeRawValue(broadcastData,
                            start + BaseSensorProtocol.DATA_TYPE_VALUE_LENGTH));
        }
    }

    public byte[] broadcastAddressStringToBytes(String src) {
        byte[] dst = new byte[BROADCAST_ADDRESS_LENGTH];
        return broadcastAddressStringToBytes(dst, src) ? dst : null;
    }

    public boolean broadcastAddressStringToBytes(byte[] dst, String src) {
        try {
            String[] tmp = src.split(":");
            int len = tmp.length;
            if (len == BROADCAST_ADDRESS_LENGTH) {
                for (int i = 0;i < len;++i) {
                    dst[i] = Short.valueOf(tmp[i], 16).byteValue();
                }
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }

    public int broadcastAddressToRawAddress(String broadcastAddressString, byte[] tmpBroadcastAddressBytes) {
        broadcastAddressStringToBytes(tmpBroadcastAddressBytes, broadcastAddressString);
        return broadcastAddressToRawAddress(tmpBroadcastAddressBytes);
    }

    public int broadcastAddressToRawAddress(String broadcastAddress) {
        return broadcastAddressToRawAddress(broadcastAddressStringToBytes(broadcastAddress));
    }

    public int broadcastAddressToRawAddress(byte[] broadcastAddress) {
        return broadcastAddress == null
                || broadcastAddress.length != BROADCAST_ADDRESS_LENGTH
                ? 0
                : NumericConverter.INSTANCE.int8ToInt32((byte) 0,
                broadcastAddress[1],
                broadcastAddress[2],
                broadcastAddress[3]);
    }

    @Override
    public Crc getCrc() {
        return Crc.Companion.getCcitt();
    }

    @Override
    public boolean isCrcMsb() {
        return true;
    }
}
