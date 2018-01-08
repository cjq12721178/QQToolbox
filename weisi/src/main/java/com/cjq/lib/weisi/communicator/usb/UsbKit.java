package com.cjq.lib.weisi.communicator.usb;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;

import java.io.IOException;

/**
 * Created by CJQ on 2017/12/26.
 */

public class UsbKit {

    private static final String ACTION_USB_PERMISSION = UsbKit.class.getName() + ".ACTION_USB_PERMISSION";

    private static UsbManager usbManager;
    private static OnUsbSerialPortStateChangeListener onUsbSerialPortStateChangeListener;
    private static BroadcastReceiver deviceReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
                launch(context, device);
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                close(device);
            } else if (ACTION_USB_PERMISSION.equals(action)) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    open(null, device);
                } else {
                    open(null,null);
                }
            }
        }
    };

    public static void register(Context context, OnUsbSerialPortStateChangeListener listener) {
        if (context == null) {
            throw new NullPointerException("context may not be null");
        }
        if (listener == null) {
            throw new NullPointerException("listener may not be null");
        }
        if (onUsbSerialPortStateChangeListener != listener) {
            onUsbSerialPortStateChangeListener = listener;
        }
        if (isRegistered()) {
            return;
        }
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        intentFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        intentFilter.addAction(ACTION_USB_PERMISSION);
        context.registerReceiver(deviceReceiver, intentFilter);
    }

    public static boolean isRegistered() {
        return usbManager != null;
    }

    public static void unregister(Context context) {
        if (isRegistered()) {
            context.unregisterReceiver(deviceReceiver);
            onUsbSerialPortStateChangeListener = null;
            usbManager = null;
        }
    }

    public static boolean launch(Context context) {
        return launch(context, 0, 0);
    }

    public static boolean launch(Context context, int vendorId, int productId) {
        if (vendorId == 0 && productId == 0) {
            UsbSerialProber prober = UsbSerialProber.getDefaultProber();
            for (UsbSerialDriver driver :
                    prober.findAllDrivers(usbManager)) {
                if (!launch(context, prober, driver.getDevice())) {
                    return false;
                }
            }
            return true;
        } else {
            return launch(context,
                    findDeviceByVendorIdAndProductId(vendorId, productId));
        }
    }

    private static UsbDevice findDeviceByVendorIdAndProductId(int vendorId, int productId) {
        if (usbManager == null) {
            return null;
        }
        for (UsbDevice device : usbManager.getDeviceList().values()) {
            if (device.getVendorId() == vendorId
                    && device.getProductId() == productId) {
                return device;
            }
        }
        return null;
    }

    public static boolean launch(Context context, UsbDevice device) {
        return launch(context, null, device);
    }

    private static boolean launch(Context context, UsbSerialProber prober, UsbDevice device) {
        if (usbManager == null || context == null || device == null) {
            return false;
        }
        if (usbManager.hasPermission(device)) {
            return open(prober, device);
        } else {
            usbManager.requestPermission(device,
                    PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0));
        }
        return true;
    }

    private static boolean open(UsbSerialProber prober, UsbDevice device) {
        if (device == null) {
            return false;
        }
        UsbSerialDriver driver = (prober != null ? prober : UsbSerialProber.getDefaultProber()).probeDevice(device);
        if (driver != null) {
            UsbDeviceConnection connection = usbManager.openDevice(device);
            UsbSerialPort port = driver.getPorts().get(0);
            try {
                port.open(connection);
            } catch (IOException e) {
                try {
                    port.close();
                } catch (IOException e1) {
                }
                port = null;
            }
            onUsbSerialPortStateChangeListener.onUsbSerialPortOpen(port);
            return port != null;
        }
        return false;
    }

    private static void close(UsbDevice device) {
        onUsbSerialPortStateChangeListener.onUsbSerialPortClose(device);
    }

    public interface OnUsbSerialPortStateChangeListener {
        void onUsbSerialPortOpen(UsbSerialPort port);
        void onUsbSerialPortClose(UsbDevice device);
    }
}
