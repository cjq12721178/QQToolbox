package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.DisplayMeasurement;
import com.cjq.lib.weisi.iot.ID;
import com.cjq.lib.weisi.iot.interpreter.DefaultInterpreter;
import com.cjq.lib.weisi.iot.interpreter.ValueInterpreter;

public abstract class VirtualMeasurement<C extends DisplayMeasurement.Configuration> extends DisplayMeasurement<C> {

    private final ValueInterpreter mValueInterpreter;

    protected VirtualMeasurement(@NonNull ID id, String name, ValueInterpreter valueInterpreter, boolean hidden) {
        this(id, name, valueInterpreter, hidden, true);
    }

    protected VirtualMeasurement(@NonNull ID id, String name, ValueInterpreter valueInterpreter, boolean hidden, boolean autoInit) {
        super(id, name, hidden, autoInit);
        mValueInterpreter = valueInterpreter != null ? valueInterpreter : DefaultInterpreter.getInstance();
    }

    @Override
    public String formatValue(double rawValue) {
        return mValueInterpreter.interpret(rawValue);
    }
}
