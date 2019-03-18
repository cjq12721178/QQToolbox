package com.cjq.lib.weisi.iot;

import com.cjq.lib.weisi.iot.container.Corrector;

public interface Configuration {
    Decorator getDecorator();
    void setDecorator(Decorator decorator);
}
