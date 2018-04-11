package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.util.SimpleReflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CJQ on 2018/3/19.
 */

public abstract class BaseValueContainer<V extends Value>
        implements ValueContainer<V> {

    private List<OnValueAddListener> mAddListeners;

    @Override
    public int addValue(long timestamp) {
        if (timestamp < 0) {
            return ADD_FAILED_RETURN_VALUE;
        }
        int result = onAddValue(timestamp);
        if (mAddListeners != null && result != ADD_FAILED_RETURN_VALUE) {
            for (int i = 0, n = mAddListeners.size();i < n;++i) {
                mAddListeners.get(0).onValueAdd(result, timestamp);
            }
        }
        return result;
    }

    protected abstract int onAddValue(long timestamp);

    @Override
    public void registerOnValueAddListener(@NonNull OnValueAddListener listener) {
        if (mAddListeners == null) {
            mAddListeners = new ArrayList<>();
        }
        if (!mAddListeners.contains(listener)) {
            mAddListeners.add(listener);
        }
    }

    @Override
    public void unregisterOnValueAddListener(@NonNull OnValueAddListener listener) {
        if (mAddListeners == null) {
            return;
        }
        mAddListeners.remove(listener);
    }

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
            Constructor constructor = ((Class<V>) SimpleReflection.getClassParameterizedType(this, BaseValueContainer.class, 0)).getConstructor(long.class);
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            return (V) constructor.newInstance(timestamp);
        } catch (Exception e) {
        }
        return null;
    }

    @Override
    public V findValue(long timestamp) {
        int position = findValuePosition(timestamp);
        return position >= 0 ? getValue(position) : null;
    }

    @Override
    public V findValue(int possiblePosition, long timestamp) {
        int position = findValuePosition(possiblePosition, timestamp);
        return position >= 0 ? getValue(position) : null;
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
    public int findValuePosition(int possiblePosition, long timestamp) {
        synchronized (this) {
            int size = size();
            int position = getPhysicalPositionByLogicalPosition(possiblePosition);
            if (position >= 0 && position < size) {
                V value;
                int currentPosition = position;
                int lastPosition = currentPosition;
                for (; currentPosition < size && currentPosition >= 0;) {
                    value = getValue(currentPosition);
                    long valueTimestamp = value.mTimestamp;
                    if (valueTimestamp == timestamp) {
                        return currentPosition;
                    } else if (valueTimestamp > timestamp) {
                        if (currentPosition > lastPosition) {
                            return encodePosition(currentPosition);
                        }
                        lastPosition = currentPosition--;
                    } else {
                        if (currentPosition < lastPosition) {
                            return encodePosition(lastPosition);
                        }
                        lastPosition = currentPosition++;
                    }
                }
                return encodePosition((currentPosition > lastPosition
                        ? currentPosition
                        : lastPosition));
            } else {
                return findValuePosition(timestamp);
            }
        }
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

    @Override
    public ValueContainer<V> applyForSubValueContainer(long startTime, long endTime) {
        synchronized (this) {
            return new SubValueContainer<>(this, startTime, endTime);
        }
    }

    @Override
    public void detachSubValueContainer(ValueContainer<V> subContainer) {
        if (subContainer instanceof OnValueAddListener) {
            unregisterOnValueAddListener((OnValueAddListener) subContainer);
        }
    }
}
