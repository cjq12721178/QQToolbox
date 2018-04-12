package com.cjq.lib.weisi.communicator.tcp;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by CJQ on 2018/4/12.
 */

public abstract class Tcp {

    @IntDef({ UNCONNECTED, CONNECTING, CONNECTED })
    @Retention(RetentionPolicy.SOURCE)
    @interface ConnectState {
    }

    public static final int UNCONNECTED = 1;
    public static final int CONNECTING = 2;
    public static final int CONNECTED = 3;

    protected  @ConnectState int mState = UNCONNECTED;

    public @ConnectState int getState() {
        return mState;
    }
}
