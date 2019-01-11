package com.cjq.lib.weisi.iot.container;

import android.support.annotation.NonNull;

public abstract class ValueContainerWrapper<V extends Value> implements ValueContainer<V> {

    protected final ValueContainer<V> mHostContainer;

    public ValueContainerWrapper(@NonNull ValueContainer<V> hostContainer) {
        mHostContainer = hostContainer;
    }

    @Override
    public int addValue(long timestamp) {
        throw new UnsupportedOperationException("addValue may not be supported for ValueContainerWrapper");
    }

    @Override
    public int interpretAddResult(int logicalPosition) {
        return mHostContainer.interpretAddResult(logicalPosition);
    }

    @Override
    public int getPhysicalPositionByLogicalPosition(int logicalPosition) {
        return mHostContainer.getPhysicalPositionByLogicalPosition(logicalPosition);
    }

    @Override
    public int size() {
        return mHostContainer.size();
    }

    @Override
    public boolean empty() {
        return mHostContainer.empty();
    }

    protected abstract V wrapValue(V src);

    @Override
    public V getValue(int physicalPosition) {
        return wrapValue(mHostContainer.getValue(physicalPosition));
    }

    @Override
    public V getEarliestValue() {
        return wrapValue(mHostContainer.getEarliestValue());
    }

    @Override
    public V getLatestValue() {
        return wrapValue(mHostContainer.getLatestValue());
    }

    @Override
    public int findValuePosition(long timestamp) {
        return mHostContainer.findValuePosition(timestamp);
    }

    @Override
    public V findValue(long timestamp) {
        return mHostContainer.findValue(timestamp);
    }

    @Override
    public int findValuePosition(int possiblePosition, long timestamp) {
        return mHostContainer.findValuePosition(possiblePosition, timestamp);
    }

    @Override
    public V findValue(int possiblePosition, long timestamp) {
        return mHostContainer.findValue(possiblePosition, timestamp);
    }

    @Override
    public void detachSubValueContainer(ValueContainer subContainer) {
        if (subContainer instanceof ValueContainerWrapper) {
            mHostContainer.detachSubValueContainer(((ValueContainerWrapper) subContainer).mHostContainer);
        }
    }

    @Override
    public void registerOnValueAddListener(@NonNull OnValueAddListener listener) {
        mHostContainer.registerOnValueAddListener(listener);
    }

    @Override
    public void unregisterOnValueAddListener(@NonNull OnValueAddListener listener) {
        mHostContainer.unregisterOnValueAddListener(listener);
    }
}
