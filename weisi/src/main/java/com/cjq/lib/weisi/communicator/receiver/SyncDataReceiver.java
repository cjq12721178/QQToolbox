package com.cjq.lib.weisi.communicator.receiver;

import com.cjq.lib.weisi.communicator.Communicator;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by CJQ on 2017/12/25.
 */

public class SyncDataReceiver extends DataReceiver {

    private static ExecutorService stopReadExecutor;

    public SyncDataReceiver(Communicator communicator) {
        super(communicator);
    }

    @Override
    protected void onStopListen(final Communicator communicator) {
        if (!waitForStopped()) {
            if (stopReadExecutor == null) {
                stopReadExecutor = Executors.newSingleThreadExecutor();
            }
            if (!stopReadExecutor.isShutdown()) {
                stopReadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            communicator.stopRead();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }

    private boolean waitForStopped() {
        int count = 0;
        while (getState() != State.STOPPED && count++ < 5) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return count <= 5;
    }

    public static void shutdown() {
        if (stopReadExecutor != null) {
            stopReadExecutor.shutdown();
        }
    }
}
