package com.cjq.tool.qqtoolbox.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.cjq.lib.weisi.communicator.receiver.DataReceiver;
import com.cjq.lib.weisi.communicator.receiver.ReceiveException;
import com.cjq.lib.weisi.communicator.tcp.TcpClient;
import com.cjq.lib.weisi.communicator.tcp.TcpServer;
import com.cjq.lib.weisi.communicator.tcp.TcpSocket;
import com.cjq.tool.qbox.ui.toast.SimpleCustomizeToast;
import com.cjq.tool.qqtoolbox.R;

import java.io.IOException;

public class TcpDebugActivity2
        extends AppCompatActivity
        implements View.OnClickListener,
        DataReceiver.Listener,
        TcpServer.OnClientAcceptListener, TcpClient.OnServerConnectListener {

    private TcpServer mServer;
    private TcpSocket mSocket;
    private TcpClient mClient;
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
        setContentView(R.layout.activity_tcp_debug2);

        mEtLocalPort = findViewById(R.id.et_local_port);
        mEtRemoteIp = findViewById(R.id.et_server_ip);
        mEtRemotePort = findViewById(R.id.et_server_port);
        mBtnLaunch = findViewById(R.id.btn_launch_server);
        mBtnConnect = findViewById(R.id.btn_connect);
        mTvReception = findViewById(R.id.tv_reception);
        mTvEmission = findViewById(R.id.tv_emission);
        mEtEmission = findViewById(R.id.et_emission);
        findViewById(R.id.btn_launch_server).setOnClickListener(this);
        findViewById(R.id.btn_accept).setOnClickListener(this);
        findViewById(R.id.btn_connect).setOnClickListener(this);
        findViewById(R.id.btn_send).setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                SimpleCustomizeToast.show("关闭远程客户端连接失败");
            }
        }
        if (mReceiver != null) {
            mReceiver.stopListen();
        }
        if (mServer != null) {
            mServer.shutdown();
        }
        if (mClient != null) {
            mClient.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_launch_server:
                if (mServer == null) {
                    mServer = new TcpServer();
                    if (mServer.launch(getLocalPort())) {
                        mEtRemoteIp.setEnabled(false);
                        mEtRemotePort.setEnabled(false);
                        mBtnConnect.setEnabled(false);
                        SimpleCustomizeToast.show("本地服务器启动成功");
                    } else {
                        SimpleCustomizeToast.show("本地服务器启动失败");
                    }
                } else {
                    SimpleCustomizeToast.show("本地服务器启动成功");
                }
                break;
            case R.id.btn_accept:
                if (mServer == null) {
                    SimpleCustomizeToast.show("本地服务器尚未启动");
                    return;
                }
                mServer.accept(this);
                break;
            case R.id.btn_connect:
                if (mClient == null) {
                    mBtnLaunch.setEnabled(false);
                    mClient = new TcpClient();
                    mClient.connect(getRemoteIp(), getRemotePort(), this);
                } else {
                    SimpleCustomizeToast.show("远程服务器连接成功");
                }
                break;
            case R.id.btn_send:
                if (mSocket == null) {
                    SimpleCustomizeToast.show("请先启动服务器或连接服务器");
                } else {
                    try {
                        mSocket.write(getEmission());
                        //mWriter.flush();
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

    @Override
    public boolean onClientAccept(TcpSocket socket) {
        if (socket != null) {
            mSocket = socket;
            mReceiver = new DataReceiver(socket);
            mReceiver.startListen(this);
            SimpleCustomizeToast.show("远程客户端连接成功");
            return false;
        } else {
            SimpleCustomizeToast.show("客户端连接失败");
            return true;
        }
    }

    @Override
    public void onServerConnect(TcpSocket socket) {
        if (socket != null) {
            mSocket = socket;
            mReceiver = new DataReceiver(socket);
            mReceiver.startListen(this);
            SimpleCustomizeToast.show("远程服务器连接成功");
        } else {
            SimpleCustomizeToast.show("连接远程服务器失败");
        }
    }
}
