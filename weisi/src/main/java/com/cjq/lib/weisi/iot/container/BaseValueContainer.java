package com.cjq.lib.weisi.iot.container;

import com.wsn.lib.wsb.util.SimpleReflection;

import java.lang.reflect.Constructor;

public abstract class BaseValueContainer<V extends Value> implements ValueContainer<V> {


    public V createValue(long timestamp) {
        V v;
        if (!empty()) {
            v = (V) getValue(0).copy(timestamp);
            if (v != null) {
                return v;
            }
        }
        return onCreateValue(timestamp);
    }

    protected V onCreateValue(long timestamp) {
        try {
            Constructor constructor = ((Class<V>) SimpleReflection.INSTANCE.getClassParameterizedType(this, 0)).getConstructor(long.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return (V) constructor.newInstance(timestamp);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 请确保存储的数据按时间戳从小到大排列，否则请在子类中重写
     */
    @Override
    public V getEarliestValue() {
        return empty() ? null : getValue(0);
    }

    /**
     * 请确保存储的数据按时间戳从小到大排列，否则请在子类中重写
     */
    @Override
    public V getLatestValue() {
        return empty() ? null : getValue(size() - 1);
    }

    @Override
    public int getPhysicalPositionByLogicalPosition(int logicalPosition) {
        if (logicalPosition >= 0) {
            return logicalPosition;
        }
        if (logicalPosition == ADD_FAILED_RETURN_VALUE) {
            return -1;
        }
        return decodePosition(logicalPosition);
    }

    protected int decodePosition(int src) {
        return - src - 1;
    }

    protected int encodePosition(int src) {
        return - src - 1;
    }

}
