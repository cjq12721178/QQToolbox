package com.wsn.lib.wsb.communicator.receiver

import java.io.IOException

/**
 * Created by CJQ on 2018/3/12.
 */

class ReceiveException : IOException {

    constructor() : super() {}

    constructor(message: String) : super(message) {}

    constructor(message: String, cause: Throwable) : super(message, cause) {}

    constructor(cause: Throwable) : super(cause) {}

    companion object {

        private const val serialVersionUID = -1801596232075676570L
    }
}
