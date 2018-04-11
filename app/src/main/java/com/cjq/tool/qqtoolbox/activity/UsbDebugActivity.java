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

import com.cjq.lib.weisi.communicator.receiver.DataReceiver;
import com.cjq.lib.weisi.communicator.usb.UsbSerialDriver;
import com.cjq.lib.weisi.communicator.usb.UsbSerialPort;
import com.cjq.lib.weisi.communicator.usb.UsbSerialProber;
import com.cjq.lib.weisi.protocol.UdpSensorProtocol;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.cjq.lib.weisi.util.NumericConverter;
import com.cjq.tool.qqtoolbox.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UsbDebugActivity
        extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener,
        View.OnClickListener, DataReceiver.Listener {

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
    //private Thread mListenThread;
    private DataReceiver mUsbDataReceiver;

    private BroadcastReceiver mUsbDeviceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                final UsbDevice deviceFound = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAvailableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(mUsbManager);
                        List<String> devicesName = new ArrayList<>();
                        for (UsbSerialDriver driver :
                                mAvailableDrivers) {
                            devicesName.add(driver.getDevice().getDeviceName());
                        }
                        int selectedPosition = devicesName.indexOf(deviceFound.getDeviceName());
                        mSpnSelectDevice.setAdapter(new ArrayAdapter(
                                context,
                                android.R.layout.simple_spinner_dropdown_item,
                                devicesName));
                        mSpnSelectDevice.setSelection(selectedPosition, true);
                    }
                });

//                Message message = Message.obtain();
//                message.what = MSG_USB_INSERTED;
//                message.obj = deviceFound.toString();
//                handler.sendMessage(message);
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                closeDevice();
                //UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                Message message = Message.obtain();
//                message.what = MSG_USB_EXIT;
//                message.obj = device.toString();
//                handler.sendMessage(message);
//				Toast.makeText(MainActivity.this,
//						"ACTION_USB_DEVICE_DETACHED: \n" + device.toString(),
//						Toast.LENGTH_LONG).show();
//				handler.sendEmptyMessage(MSG_USB_EXIT);
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    openDeviceAndSetParameter(device);
                } else {
                    SimpleCustomizeToast.show("用户不允许访问USB设备！");
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
        findViewById(R.id.btn_close_device).setOnClickListener(this);
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
            openDeviceAndSetParameter(device);
        } else {
            mUsbManager.requestPermission(device,
                    PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));
        }
    }

    private void openDeviceAndSetParameter(UsbDevice device) {
        //mUsbSerialDriver = UsbSerialProber.getDefaultProber().probeDevice(device);

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
            SimpleCustomizeToast.show(e.getMessage());
        }
    }

    private void startListen() {
//        if (mListenThread == null) {
//            mListenThread = new Thread(mUsbSerialPortListener);
//        }
//        mListenThread.start();
        mUsbDataReceiver = new DataReceiver(mUsbSerialPort);
        mUsbDataReceiver.startListen(this);
    }

    public void stopListen() {
//        if (mListenThread != null && mListenThread.isAlive()) {
//            mListenThread.interrupt();
//            mListenThread = null;
//        }
        if (mUsbDataReceiver != null) {
            mUsbDataReceiver.stopListen();
        }
    }

//    private Runnable mUsbSerialPortListener = new Runnable() {
//        @Override
//        public void run() {
//            final byte[] data = new byte[2048];
//            data[0] = 0x37;
//            while (!mListenThread.isInterrupted()) {
//                try {
//                    final int receivedLen = ((Cp21xxSerialDriver.Cp21xxSerialPort) mUsbSerialPort).read(data, 1, data.length - 1, 5000);
//                    if (receivedLen > 0) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                mTvReceive.append(HexDump.dumpHexString(data, 0, receivedLen));
//                            }
//                        });
//                    }
//                } catch (IOException e) {
//                }
//            }
//        }
//    };

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
                SimpleCustomizeToast.show("usb serial port close failed");
            } finally {
                stopListen();
            }
        }
    }

    private IntentFilter makeUsbDeviceIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(ACTION_USB_PERMISSION);

        return intentFilter;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                if (mUsbSerialPort != null) {
                    try {
                        //mUsbSerialPort.write(NumericConverter.hexDataStringToBytes(mEtSend.getText().toString()), 5000);
                        byte[] commandFrame = new UdpSensorProtocol().makeDataRequestFrame();
                        mUsbSerialPort.write(commandFrame, 5000);
                        //mUsbSerialPort.write(new byte[] { 0x12, (byte) 0xAA, (byte) 0xAA, (byte) 0xFF, (byte) 0xFF, 0x01, 0x6C, (byte) 0x9E, 0x1B, 0x55, 0x55 }, 1, 10, 5000);
                    } catch (IOException e) {
                        SimpleCustomizeToast.show(e.getMessage());
                    }
                }
                break;
            case R.id.btn_open_device:
                int position = mSpnSelectDevice.getSelectedItemPosition();
                if (position >= 0) {
                    mUsbSerialDriver = mAvailableDrivers.get(position);
                    UsbDevice device = mUsbSerialDriver.getDevice();
                    if (mUsbManager.hasPermission(device)) {
                        openDeviceAndSetParameter(device);
                    } else {
                        mUsbManager.requestPermission(device,
                                PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0));
                    }
                }
                break;
            case R.id.btn_close_device:
                closeDevice();
                break;
        }
    }

    @Override
    public int onDataReceived(final byte[] data, final int len) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvReceive.append(NumericConverter.bytesToHexDataString(data, 0, len));
                mTvReceive.append("\n");
            }
        });
        return len;
    }

    @Override
    public boolean onErrorOccurred(Exception e) {
        ExceptionLog.debug(e);
        return false;
    }
}
