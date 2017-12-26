package com.cjq.tool.qqtoolbox.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.cjq.lib.weisi.communicator.receiver.DataReceiver;
import com.cjq.lib.weisi.communicator.UdpKit;
import com.cjq.lib.weisi.communicator.receiver.SyncDataReceiver;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qqtoolbox.R;

public class UdpDebugActivity
        extends AppCompatActivity
        implements View.OnClickListener,
        DataReceiver.Listener {

    private UdpKit mUdpKit;
    private SyncDataReceiver mDataReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_udp_debug);
        findViewById(R.id.btn_launch).setOnClickListener(this);
        findViewById(R.id.btn_start_listen).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
        findViewById(R.id.btn_stop_listen).setOnClickListener(this);
        findViewById(R.id.btn_shutdown).setOnClickListener(this);

//        mUdpKit = new UdpKit();
//        try {
//            mUdpKit.launch();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        //mUdpKit.startListen(true, this);
//        mDataReceiver = new DataReceiver(mUdpKit);
//        mDataReceiver.startListen(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUdpKit != null) {
            mUdpKit.close();
        }
        SyncDataReceiver.shutdown();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_launch:
                if (mUdpKit == null) {
                    mUdpKit = new UdpKit();
                }
                if (mUdpKit.launch()) {
                    SimpleCustomizeToast.show(this, "udp launch success");
                } else {
                    SimpleCustomizeToast.show(this, "udp launch failed");
                }
                break;
            case R.id.btn_start_listen:
                if (mUdpKit != null) {
                    if (mDataReceiver == null) {
                        mDataReceiver = new SyncDataReceiver(mUdpKit);
                    }
                    if (mDataReceiver.startListen(this)) {
                        SimpleCustomizeToast.show(this, "start udp listen");
                    } else {
                        SimpleCustomizeToast.show(this, "udp listen failed");
                    }
                }
                break;
            case R.id.btn_send:
                if (mUdpKit != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mUdpKit.send("192.168.1.18", 5000, new byte[] { (byte) 0xAA, (byte) 0xAA, (byte) 0x10, (byte) 0x15, 0x01, 0x35, (byte) 0xA6, 0x64, 0x55, 0x55 });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                break;
            case R.id.btn_stop_listen:
                if (mDataReceiver != null) {
                    mDataReceiver.stopListen();
                    mDataReceiver = null;
                }
                break;
            case R.id.btn_shutdown:
                if (mUdpKit != null) {
                    mUdpKit.close();
                    mUdpKit = null;
                }
                break;
        }

    }

    @Override
    public int onDataReceived(byte[] data, int len) {
        return len;
    }

    @Override
    public boolean onErrorOccurred(Exception e) {
        return false;
    }
}
