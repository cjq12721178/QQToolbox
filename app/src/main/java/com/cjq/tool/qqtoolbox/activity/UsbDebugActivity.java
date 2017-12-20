package com.cjq.tool.qqtoolbox.activity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.cjq.lib.weisi.communicator.usb.UsbSerialDriver;
import com.cjq.lib.weisi.communicator.usb.UsbSerialPort;
import com.cjq.lib.weisi.communicator.usb.UsbSerialProber;
import com.cjq.lib.weisi.util.HexDump;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qqtoolbox.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UsbDebugActivity
        extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        View.OnClickListener {

    private static final String ACTION_USB_PERMISSION = "com.example.usbdemo.action.ACTION_USB_PERMISSION";

    private UsbManager mUsbManager;
    private UsbSerialDriver mUsbSerialDriver;
    private UsbSerialPort mUsbSerialPort;
    private Spinner mSpnSelectDevice;
    private Spinner mSpnBaudRate;
    private Spinner mSpnDataBits;
    private Spinner mSpnStopBits;
    private Spinner mSpnParity;
    private EditText mEtSend;
    private TextView mTvReceive;
    private List<UsbSerialDriver> mAvailableDrivers;
    private Thread mListenThread;

    private BroadcastReceiver mUsbDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_USB_PERMISSION.equals(intent.getAction())) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device == mUsbSerialDriver.getDevice()) {
                        openDeviceAndSetParameter();
                    }
                } else {
                    SimpleCustomizeToast.show(context, "用户不允许访问USB设备！");
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usb_debug);

        mUsbManager = (UsbManager) getSystemService(USB_SERVICE);
        mAvailableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
        List<String> devicesName = new ArrayList<>();
        for (UsbSerialDriver driver :
                mAvailableDrivers) {
            devicesName.add(driver.getDevice().getDeviceName());
        }
        mSpnSelectDevice = (Spinner) findViewById(R.id.spn_select_device);
        mSpnSelectDevice.setAdapter(new ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                devicesName));
        mSpnSelectDevice.setSelection(-1, false);
        //mSpnSelectDevice.setOnItemSelectedListener(this);
        mSpnBaudRate = (Spinner) findViewById(R.id.spn_baud_rate);
        mSpnBaudRate.setSelection(16, false);
        mSpnDataBits = (Spinner) findViewById(R.id.spn_data_bits);
        mSpnDataBits.setSelection(3, false);
        mSpnStopBits = (Spinner) findViewById(R.id.spn_stop_bits);
        mSpnStopBits.setSelection(0, false);
        mSpnParity = (Spinner) findViewById(R.id.spn_parity);
        mSpnParity.setSelection(0, false);
        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.btn_open_device).setOnClickListener(this);
        mEtSend = (EditText) findViewById(R.id.et_send);
        mTvReceive = (TextView) findViewById(R.id.tv_receive_content);

        registerReceiver(mUsbDeviceReceiver, makeUsbDeviceIntentFilter());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeDevice();
        unregisterReceiver(mUsbDeviceReceiver);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        closeDevice();

        mUsbSerialDriver = mAvailableDrivers.get(position);
        UsbDevice device = mUsbSerialDriver.getDevice();
        if (mUsbManager.hasPermission(device)) {
            openDeviceAndSetParameter();
        } else {
            mUsbManager.requestPermission(device,
                    PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));
        }
    }

    private void openDeviceAndSetParameter() {
        UsbDeviceConnection connection = mUsbManager.openDevice(mUsbSerialDriver.getDevice());
        mUsbSerialPort = mUsbSerialDriver.getPorts().get(0);
        try {
            mUsbSerialPort.open(connection);
            mUsbSerialPort.setParameters(Integer.parseInt(mSpnBaudRate.getSelectedItem().toString()),
                    Integer.parseInt(mSpnDataBits.getSelectedItem().toString()),
                    getStopBits(),
                    mSpnParity.getSelectedItemPosition());
            startListen();
        } catch (Exception e) {
            SimpleCustomizeToast.show(this, e.getMessage());
        }
    }

    private void startListen() {
        if (mListenThread == null) {
            mListenThread = new Thread(mUsbSerialPortListener);
        }
        mListenThread.start();
    }

    public void stopListen() {
        if (mListenThread != null && mListenThread.isAlive()) {
            mListenThread.interrupt();
            mListenThread = null;
        }
    }

    private Runnable mUsbSerialPortListener = new Runnable() {
        @Override
        public void run() {
            final byte[] data = new byte[2048];
            while (!mListenThread.isInterrupted()) {
                try {
                    final int receivedLen = mUsbSerialPort.read(data, 5000);
                    if (receivedLen > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTvReceive.append(HexDump.dumpHexString(data, 0, receivedLen));
                            }
                        });
                    }
                } catch (IOException e) {
                }
            }
        }
    };

    private int getStopBits() {
        switch (mSpnStopBits.getSelectedItemPosition()) {
            case 0:
                return UsbSerialPort.STOPBITS_1;
            case 1:
                return UsbSerialPort.STOPBITS_1_5;
            case 2:
                return UsbSerialPort.STOPBITS_2;
        }
        throw new IndexOutOfBoundsException("no stop bits selected");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        closeDevice();
    }

    private void closeDevice() {
        if (mUsbSerialPort != null) {
            try {
                mUsbSerialPort.close();
                mUsbSerialPort = null;
                mUsbSerialDriver = null;
            } catch (IOException e) {
                SimpleCustomizeToast.show(this, "usb serial port close failed");
            } finally {
                stopListen();
            }
        }
    }

    private IntentFilter makeUsbDeviceIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        //intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        //intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(ACTION_USB_PERMISSION);
        return intentFilter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                if (mUsbSerialPort != null) {
                    try {
                        mUsbSerialPort.write(HexDump.hexStringToByteArray(mEtSend.getText().toString()), 5000);
                    } catch (IOException e) {
                        SimpleCustomizeToast.show(this, e.getMessage());
                    }
                }
                break;
            case R.id.btn_open_device:
                int position = mSpnSelectDevice.getSelectedItemPosition();
                if (position >= 0) {
                    closeDevice();
                    mUsbSerialDriver = mAvailableDrivers.get(position);
                    UsbDevice device = mUsbSerialDriver.getDevice();
                    if (mUsbManager.hasPermission(device)) {
                        openDeviceAndSetParameter();
                    } else {
                        mUsbManager.requestPermission(device,
                                PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));
                    }
                }
                break;
        }
    }
}
