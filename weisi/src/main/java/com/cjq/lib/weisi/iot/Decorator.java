package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.container.Value;

/**
 * Created by CJQ on 2018/3/16.
 */
public interface Decorator<V extends Value> {
    String decorateName(String name);

    //para为保留参数，针对V有多种类型的值时进行区分
    String decorateValue(@NonNull V value, int para);
}
