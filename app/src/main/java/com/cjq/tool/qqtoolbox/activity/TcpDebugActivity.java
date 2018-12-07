package com.cjq.tool.qqtoolbox.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.wsn.lib.wsb.communicator.Communicator;
import com.wsn.lib.wsb.communicator.receiver.DataReceiver;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qqtoolbox.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpDebugActivity extends AppCompatActivity implements View.OnClickListener, Communicator, DataReceiver.Listener {

    private ServerSocket mLocalServer;
    //private Socket mClient;
    private Socket mRemoteServer;
    private InputStream mReader;
    private OutputStream mWriter;
    private EditText mEtLocalPort;
    private EditText mEtRemoteIp;
    private EditText mEtRemotePort;
    private Button mBtnLaunch;
    private Button mBtnConnect;
    private DataReceiver mReceiver;
    private TextView mTvReception;
    private TextView mTvEmission;
    private EditText mEtEmission;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tcp_debug);

        mEtLocalPort = findViewById(R.id.et_local_port);
        mEtRemoteIp = findViewById(R.id.et_server_ip);
        mEtRemotePort = findViewById(R.id.et_server_port);
        mBtnLaunch = findViewById(R.id.btn_launch_server);
        mBtnConnect = findViewById(R.id.btn_connect);
        mTvReception = findViewById(R.id.tv_reception);
        mTvEmission = findViewById(R.id.tv_emission);
        mEtEmission = findViewById(R.id.et_emission);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mReceiver != null) {
            mReceiver.stopListen();
        }
//        if (mClient != null) {
//            try {
//                mClient.close();
//            } catch (IOException e) {
//                SimpleCustomizeToast.show("关闭远程客户端失败");
//            }
//        }
        if (mLocalServer != null) {
            try {
                mLocalServer.close();
            } catch (IOException e) {
                SimpleCustomizeToast.show("关闭本地服务器失败");
            }
        }
        if (mRemoteServer != null) {
            try {
                mRemoteServer.close();
            } catch (IOException e) {
                SimpleCustomizeToast.show("断开远程服务器失败");
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_launch_server:
                if (mLocalServer == null) {
                    try {
                        mLocalServer = new ServerSocket(getLocalPort());
                        mEtRemoteIp.setEnabled(false);
                        mEtRemotePort.setEnabled(false);
                        mBtnConnect.setEnabled(false);
                        SimpleCustomizeToast.show("本地服务器启动成功");
                    } catch (IOException e) {
                        SimpleCustomizeToast.show("本地服务器启动失败");
                    } catch (NumberFormatException nfe) {
                        SimpleCustomizeToast.show("本地服务器端口输入有误");
                    }
                } else {
                    SimpleCustomizeToast.show("本地服务器启动成功");
                }
                break;
            case R.id.btn_accept:
                if (mLocalServer == null) {
                    SimpleCustomizeToast.show("本地服务器尚未启动");
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Socket client = mLocalServer.accept();
                            mReader = client.getInputStream();
                            mWriter = client.getOutputStream();
                            mReceiver = new DataReceiver(TcpDebugActivity.this);
                            mReceiver.startListen(TcpDebugActivity.this);
                            SimpleCustomizeToast.show("远程客户端连接成功");
                        } catch (IOException e) {
                            SimpleCustomizeToast.show("客户端连接失败");
                        }
                    }
                }).start();
                break;
            case R.id.btn_connect:
                if (mRemoteServer == null) {
                    mBtnLaunch.setEnabled(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                mRemoteServer = new Socket(getRemoteIp(), getRemotePort());
                                mReader = mRemoteServer.getInputStream();
                                mWriter = mRemoteServer.getOutputStream();
                                mReceiver = new DataReceiver(TcpDebugActivity.this);
                                mReceiver.startListen(TcpDebugActivity.this);
                                SimpleCustomizeToast.show("远程服务器连接成功");
                            } catch (IOException e) {
                                SimpleCustomizeToast.show("连接远程服务器失败");
                            } catch (NumberFormatException nfe) {
                                SimpleCustomizeToast.show("远程服务器端口输入有误");
                            }
                        }
                    }).start();
                } else {
                    SimpleCustomizeToast.show("远程服务器连接成功");
                }
                break;
            case R.id.btn_send:
                if (mWriter == null) {
                    SimpleCustomizeToast.show("请先启动服务器或连接服务器");
                } else {
                    try {
                        mWriter.write(getEmission());
                        mWriter.flush();
                        addEmission();
                    } catch (IOException e) {
                        SimpleCustomizeToast.show("发送数据失败");
                    }
                }
                break;
        }
    }

    private int getLocalPort() {
        return Integer.parseInt(mEtLocalPort.getText().toString());
    }

    private String getRemoteIp() {
        return mEtRemoteIp.getText().toString();
    }

    private int getRemotePort() {
        return Integer.parseInt(mEtRemotePort.getText().toString());
    }

    private byte[] getEmission() {
        return mEtEmission.getText().toString().getBytes();
    }

    private void addEmission() {
        mTvEmission.append(mEtEmission.getText());
        mEtEmission.setText(null);
    }

    @Override
    public int read(byte[] dst, int offset, int length) throws IOException {
        return mReader.read(dst, offset, length);
    }

    @Override
    public boolean canRead() {
        return mReader != null;
    }

    @Override
    public void stopRead() {
        if (mWriter != null) {
            try {
                mWriter.write(new byte[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int onDataReceived(final byte[] data, final int len) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvReception.append(new String(data, 0, len));
            }
        });
        return len;
    }

    @Override
    public boolean onErrorOccurred(Exception e) {
        SimpleCustomizeToast.show(e.getMessage());
        return true;
    }
}
