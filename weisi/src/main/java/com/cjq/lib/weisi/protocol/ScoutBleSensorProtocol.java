package com.cjq.lib.weisi.protocol;

import com.cjq.tool.qbox.util.NumericConverter;

import java.util.Arrays;

/**
 * Created by CJQ on 2017/7/13.
 */

public class ScoutBleSensorProtocol implements Constant {

    public static final int BROADCAST_ADDRESS_LENGTH = 6;
    private static final int DATA_ZONE_LENGTH_LENGTH = 1;
    private static final int BATTERY_INFO_LENGTH = 1;
    private static final int RSSI_LENGTH = 1;
    private static final int MIN_FRAME_LENGTH = DATA_ZONE_LENGTH_LENGTH
            + BATTERY_INFO_LENGTH
            + RSSI_LENGTH
            + CRC16_LENGTH;
    private static final int SENSOR_DATA_LENGTH = DATA_TYPE_VALUE_LENGTH
            + RAW_VALUE_LENGTH;
    private static final int MIN_DATA_ZONE_LENGTH = BATTERY_INFO_LENGTH
            + RSSI_LENGTH
            + SENSOR_DATA_LENGTH;

    private final byte[] mDataTypeArray = new byte[255];

    public void analyze(byte[] broadcastAddress, byte[] broadcastData, OnDataAnalyzedListener listener) {
        if (broadcastAddress == null
                || broadcastAddress.length != BROADCAST_ADDRESS_LENGTH
                || broadcastData == null
                || broadcastData.length < MIN_FRAME_LENGTH
                || listener == null) {
            return;
        }
        int dataZoneLength = NumericConverter.int8ToUInt16(broadcastData[0]) - CRC16_LENGTH;
        if (dataZoneLength < MIN_DATA_ZONE_LENGTH
                || Crc.calc16ByMSB(Crc.calc16ByLSB(broadcastAddress), broadcastData, 0, DATA_ZONE_LENGTH_LENGTH + dataZoneLength) !=
                NumericConverter.int8ToUInt16(broadcastData[DATA_ZONE_LENGTH_LENGTH + dataZoneLength],
                        broadcastData[DATA_ZONE_LENGTH_LENGTH + dataZoneLength + 1])) {
            return;
        }
        //是否为阵列传感器
        boolean isArraySensor = NumericConverter.int8ToUInt16(broadcastAddress[4]) != 0;
        if (isArraySensor) {
            Arrays.fill(mDataTypeArray, (byte) 0);
        }
        for (int start = 0, end = dataZoneLength / SENSOR_DATA_LENGTH * SENSOR_DATA_LENGTH;
             start < end; start += SENSOR_DATA_LENGTH) {
            listener.onDataAnalyzed(broadcastData[start],
                    isArraySensor
                            ? mDataTypeArray[NumericConverter.int8ToUInt16(broadcastData[start])]++
                            : 0,
                    System.currentTimeMillis(),
                    NumericConverter.bytesToFloatByMSB(broadcastData, start + DATA_TYPE_VALUE_LENGTH));
        }
    }

    public interface OnDataAnalyzedListener {
        void onDataAnalyzed(byte dataTypeValue, int dataTypeIndex, long timestamp, double rawValue);
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
                : NumericConverter.int8ToInt32((byte) 0,
                broadcastAddress[1],
                broadcastAddress[2],
                broadcastAddress[3]);
    }
}
