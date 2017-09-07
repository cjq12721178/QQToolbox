package com.cjq.lib.weisi.communicator;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by CJQ on 2017/6/13.
 */

public class BleKit {

    private long mIntervalTime = -1;
    private long mDurationTime = 10000;
    private Handler mHandler = new Handler();
    private BluetoothAdapter.LeScanCallback mLeScanCallback;
    private BluetoothAdapter mBluetoothAdapter;
    private Map<String, BluetoothGatt> mBluetoothGattMap = new HashMap<>();

    public boolean launch(Context context) {
        if (mBluetoothAdapter == null) {
            if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                return false;
            }

            final BluetoothManager bluetoothManager = (BluetoothManager)context.getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetoothManager == null) {
                return false;
            }

            mBluetoothAdapter = bluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                return false;
            }

            if (!mBluetoothAdapter.enable()) {
                mBluetoothAdapter = null;
                return false;
            }
        }
        return true;
    }

    //intervalTime = 0，不间断扫描，此时durationTime无意义
    //intervalTime > 0，间隔intervalTime时间段扫描，持续durationTime时间段
    //intervalTime < 0，进行单次扫描
    public void startScan(BluetoothAdapter.LeScanCallback leScanCallback,
                          long intervalTime,
                          long durationTime) {
        stopScan();
        if (leScanCallback != null &&
                mBluetoothAdapter != null &&
                !mBluetoothAdapter.isDiscovering() &&
                durationTime > 0) {
            mLeScanCallback = leScanCallback;
            mIntervalTime = intervalTime;
            mDurationTime = durationTime;
            mOnStartScan.run();
        }
    }

    private Runnable mOnStartScan = new Runnable() {
        @Override
        public void run() {
            if (mIntervalTime != 0) {
                mHandler.postDelayed(mOnStopScan, mDurationTime);
            }
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        }
    };

    private Runnable mOnStopScan = new Runnable() {
        @Override
        public void run() {
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
            if (mIntervalTime > 0) {
                mHandler.postDelayed(mOnStartScan, mIntervalTime);
            }
        }
    };

    public void stopScan() {
        if (mBluetoothAdapter != null && mLeScanCallback != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                if (mIntervalTime != 0) {
                    mHandler.removeCallbacks(mOnStopScan);
                }
                mIntervalTime = -1;
                mOnStopScan.run();
            } else {
                if (mIntervalTime > 0) {
                    mHandler.removeCallbacks(mOnStartScan);
                }
            }
        }
    }

    public boolean connect(Context context, String address, BluetoothGattCallback gattCallback) {
        if (context == null || mBluetoothGattMap == null) {
            return false;
        }

        // Previously connected device.  Try to reconnect.
        BluetoothGatt bluetoothGatt = getBluetoothGatt(address);
        if (bluetoothGatt != null) {
            if (bluetoothGatt.connect()) {
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
        if (bluetoothGatt == null) {
            return false;
        }
        mBluetoothGattMap.put(address, bluetoothGatt);
        return true;
    }

    public void disconnect(String address) {
        disconnect(getBluetoothGatt(address));
    }

    public void disconnect(BluetoothGatt bluetoothGatt) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.disconnect();
    }

    public void close(String address) {
        close(getBluetoothGatt(address));
    }

    private BluetoothGatt getBluetoothGatt(String address) {
        return mBluetoothGattMap.get(address);
    }

    public void close(BluetoothGatt bluetoothGatt) {
        if (mBluetoothAdapter == null || bluetoothGatt == null) {
            return;
        }
        bluetoothGatt.close();
        mBluetoothGattMap.remove(bluetoothGatt.getDevice().getAddress());
    }

    public void readCharacteristic(String address, UUID serviceUuid, UUID characteristicUuid) {
        readCharacteristic(getBluetoothGatt(address), serviceUuid, characteristicUuid);
    }

    public void readCharacteristic(BluetoothGatt bluetoothGatt, UUID serviceUuid, UUID characteristicUuid) {
        if (bluetoothGatt == null) {
            return;
        }
        BluetoothGattService service = bluetoothGatt.getService(serviceUuid);
        if (service == null) {
            return;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);
        if (characteristic == null) {
            return;
        }
        int charaProp = characteristic.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PERMISSION_READ) > 0) {
            bluetoothGatt.readCharacteristic(characteristic);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            bluetoothGatt.setCharacteristicNotification(characteristic, true);
        }
    }
}
