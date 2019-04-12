package com.cjq.lib.weisi.iot.container;

import android.support.annotation.NonNull;

public abstract class RealTimeValueContainer<V extends Value> extends BaseValueContainer<V> {

    private V mValue;

    @Override
    public int addValue(long timestamp) {
        if (timestamp < 0) {
            return ADD_FAILED_RETURN_VALUE;
        }
        if (mValue != null) {
            if (timestamp < mValue.getTimestamp()) {
                return ADD_FAILED_RETURN_VALUE;
            }
            mValue.setTimestamp(timestamp);
            return -1;
        } else {
            mValue = createValue(timestamp);
            return 0;
        }
    }

    @Override
    public int interpretAddResult(int logicalPosition) {
        if (logicalPosition == 0) {
            return NEW_VALUE_ADDED;
        } else if (logicalPosition == -1) {
            return VALUE_UPDATED;
        } else {
            return ADD_VALUE_FAILED;
        }
    }

    @Override
    public int size() {
        return empty() ? 0 : 1;
    }

    @Override
    public boolean empty() {
        return mValue == null;
    }

    @Override
    public V getValue(int physicalPosition) {
        if (physicalPosition == 0) {
            return mValue;
        }
        return null;
    }

    @Override
    public int findValuePosition(long timestamp) {
        if (empty()) {
            return -1;
        }
        if (timestamp <= mValue.getTimestamp()) {
            return 0;
        }
        return -1;
    }

    @Override
    public V findValue(long timestamp) {
        if (empty()) {
            return null;
        }
        if (timestamp == mValue.getTimestamp()) {
            return mValue;
        }
        return null;
    }

    @Override
    public int findValuePosition(int possiblePosition, long timestamp) {
        return findValuePosition(timestamp);
    }

    @Override
    public V findValue(int possiblePosition, long timestamp) {
        return findValue(timestamp);
    }

    @Override
    public ValueContainer<V> applyForSubValueContainer(long startTime, long endTime) {
        return this;
    }

    @Override
    public void detachSubValueContainer(ValueContainer subContainer) {
    }

    @Override
    public void registerOnValueAddListener(@NonNull OnValueAddListener listener) {
    }

    @Override
    public void unregisterOnValueAddListener(@NonNull OnValueAddListener listener) {
    }
}
