package com.cjq.lib.weisi.iot;

import com.cjq.lib.weisi.iot.container.Value;

public interface Configuration<V extends Value> {
    Decorator<V> getDecorator();

    void setDecorator(Decorator<V> decorator);
}
