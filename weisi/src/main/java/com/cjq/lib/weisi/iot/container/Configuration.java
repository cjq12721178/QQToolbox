package com.cjq.lib.weisi.iot.container;

public interface Configuration<V extends Value> {
    Decorator<V> getDecorator();
    void setDecorator(Decorator<V> decorator);
    Corrector getCorrector();
    void setCorrector(Corrector corrector);
}
