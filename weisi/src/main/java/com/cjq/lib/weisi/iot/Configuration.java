package com.cjq.lib.weisi.iot;

public interface Configuration<V extends Value> {
    Decorator<V> getDecorator();

    void setDecorator(Decorator<V> decorator);
}
