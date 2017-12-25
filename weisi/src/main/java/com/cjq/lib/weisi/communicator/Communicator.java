package com.cjq.lib.weisi.communicator;

import java.io.IOException;

/**
 * Created by CJQ on 2017/12/22.
 */

public interface Communicator {

    /**
     * Reads as many bytes as possible into the destination buffer.
     *
     * @param dst the destination byte buffer
     * @param offset the index of the first byte in the buffer to receive
     * @param length the length of the data to receive
     * @return the actual number of bytes read
     * @throws IOException if an error occurred during reading
     */
    int read(final byte[] dst, int offset, int length) throws IOException;

    boolean canRead();

    //有些读取方法会阻塞线程，通过stopRead方法实现停止读取，如果是异步可以不实现该方法
    void stopRead() throws IOException;
}
