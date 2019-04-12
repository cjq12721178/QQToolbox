package com.cjq.lib.weisi.iot.container;

import android.support.annotation.NonNull;

public class EmptyValueContainer<V extends Value> implements ValueContainer<V> {

    @Override
    public int addValue(long timestamp) {
        throw new UnsupportedOperationException("EmptyValueContainer can not add value");
    }

    @Override
    public int interpretAddResult(int logicalPosition) {
        return ADD_VALUE_FAILED;
    }

    @Override
    public int getPhysicalPositionByLogicalPosition(int logicalPosition) {
        throw new UnsupportedOperationException("EmptyValueContainer can not get physical position by logical position");
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean empty() {
        return true;
    }

    @Override
    public V getValue(int physicalPosition) {
        return null;
    }

    @Override
    public V getEarliestValue() {
        return null;
    }

    @Override
    public V getLatestValue() {
        return null;
    }

    @Override
    public int findValuePosition(long timestamp) {
        return -1;
    }

    @Override
    public V findValue(long timestamp) {
        return null;
    }

    @Override
    public int findValuePosition(int possiblePosition, long timestamp) {
        return -1;
    }

    @Override
    public V findValue(int possiblePosition, long timestamp) {
        return null;
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
