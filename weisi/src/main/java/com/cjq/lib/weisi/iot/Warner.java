package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

/**
 * Created by CJQ on 2018/3/16.
 */
public interface Warner<V extends Value> {
    int RESULT_NORMAL = 0;

    int test(@NonNull V value);
}
