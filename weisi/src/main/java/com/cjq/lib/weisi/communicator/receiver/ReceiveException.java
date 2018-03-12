package com.cjq.lib.weisi.communicator.receiver;

import java.io.IOException;

/**
 * Created by CJQ on 2018/3/12.
 */

public class ReceiveException extends IOException {

    private static final long serialVersionUID = -1801596232075676570L;

    public ReceiveException() {
        super();
    }

    public ReceiveException(String message) {
        super(message);
    }

    public ReceiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ReceiveException(Throwable cause) {
        super(cause);
    }
}
