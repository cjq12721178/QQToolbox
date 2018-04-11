package com.cjq.tool.qqtoolbox.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Size;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.cjq.lib.weisi.communicator.BleKit;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.util.DebugTag;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class BleDebugActivity
        extends AppCompatActivity
        implements View.OnClickListener,
        BluetoothAdapter.LeScanCallback,
        EasyPermissions.PermissionCallbacks {

    private static final int RC_BLE_PERM = 1;
    private static final String[] BLE_PERMS = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private BleKit mBleKit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_debug);

        findViewById(R.id.btn_open_and_scan).setOnClickListener(this);
        findViewById(R.id.btn_close).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_open_and_scan:
                requestBlePermission();
                break;
            case R.id.btn_close:
                closeBle();
                break;
        }
    }

    private void closeBle() {
        if (mBleKit != null) {
            mBleKit.stopScan();
        }
    }

    @AfterPermissionGranted(RC_BLE_PERM)
    public void requestBlePermission() {
        if (EasyPermissions.hasPermissions(this, BLE_PERMS)) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                SimpleCustomizeToast.show("current device not support ble");
                return;
            }
            if (bluetoothAdapter.isEnabled()) {
                openAndScanBle();
            } else {
                // 请求打开 Bluetooth
                Intent requestBluetoothOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 设置 Bluetooth 设备可以被其它 Bluetooth 设备扫描到
                requestBluetoothOn.setAction(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                // 设置 Bluetooth 设备可见时间
                requestBluetoothOn.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                // 请求开启 Bluetooth
                startActivityForResult(requestBluetoothOn, RC_BLE_PERM);
            }
        } else {
            EasyPermissions.requestPermissions(this,
                    "需要BLE权限",
                    RC_BLE_PERM,
                    BLE_PERMS);
        }
    }

    private void openAndScanBle() {
        if (mBleKit == null) {
            mBleKit = new BleKit();
        }
        if (mBleKit.launch(this)) {
            SimpleCustomizeToast.show("ble launch success");
            mBleKit.startScan(this, 5000, 10000);
        } else {
            SimpleCustomizeToast.show("ble launch failed");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        Log.d(DebugTag.GENERAL_LOG_TAG, "address = " + device.getAddress() + ", rssi = " + rssi);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
//        if (requestCode == RC_BLE_PERM) {
//            if (perms.equals(BLE_PERMS)) {
//                openAndScanBle();
//            } else {
//                SimpleCustomizeToast.show(this, "部分BLE权限未通过");
//            }
//        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (requestCode == RC_BLE_PERM) {
            if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
                new AppSettingsDialog.Builder(this).build().show();
            } else {
                SimpleCustomizeToast.show("部分BLE权限未通过");
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == RC_BLE_PERM) {
            if (resultCode == RESULT_CANCELED) {
                SimpleCustomizeToast.show("ble not enabled");
            } else {
                openAndScanBle();
            }
        }

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
//            String yes = getString(R.string.yes);
//            String no = getString(R.string.no);
//
//            // Do something after user returned from app settings screen, like showing a Toast.
//            Toast.makeText(
//                    this,
//                    getString(R.string.returned_from_app_settings_to_activity,
//                            hasCameraPermission() ? yes : no,
//                            hasLocationAndContactsPermissions() ? yes : no,
//                            hasSmsPermission() ? yes : no),
//                    Toast.LENGTH_LONG)
//                    .show();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

//    private static abstract class PermissionsRequester implements EasyPermissions.PermissionCallbacks {
//
//        private OnRequestResultListener mListener;
//        private final int REQUEST_CODE;
//        private final String[] PERMISSIONS;
//
//        protected PermissionsRequester(int request_code,
//                                       String[] permissions) {
//            REQUEST_CODE = request_code;
//            PERMISSIONS = permissions;
//        }
//
//        public void requestPermissions(OnRequestResultListener listener) {
//            if (listener == null) {
//                throw new NullPointerException("listener may not be null");
//            }
//            mListener = listener;
//            if (hasPermissions()) {
//                mListener.onPermissionsGranted(mContext);
//            } else {
//                onRequestPermissions();
//            }
//        }
//
//        protected boolean hasPermissions() {
//            return EasyPermissions.hasPermissions(mContext, PERMISSIONS);
//        }
//
//        protected abstract void onRequestPermissions();
//
//        @Override
//        public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
//            if (requestCode == REQUEST_CODE) {
//
//            }
//        }
//
//        public interface OnRequestResultListener {
//            void onPermissionsGranted(Context context);
//        }
//    }
}
