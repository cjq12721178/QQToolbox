package com.wsn.lib.wsb.communicator.tcp


/**
 * Created by CJQ on 2018/4/12.
 */

abstract class Tcp {
    //    public static final int UNCONNECTED = 1;
    //    public static final int CONNECTING = 2;
    //    public static final int CONNECTED = 3;

    var state = ConnectState.UNCONNECTED
        protected set

    //    @IntDef({ UNCONNECTED, CONNECTING, CONNECTED })
    //    @Retention(RetentionPolicy.SOURCE)
    //    @interface ConnectState {
    //    }

    enum class ConnectState {
        UNCONNECTED,
        CONNECTING,
        CONNECTED
    }
}
