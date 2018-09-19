package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.container.Value;
import com.cjq.lib.weisi.iot.container.ValueContainer;

public abstract class Measurement<V extends Value, C extends Configuration<V>> implements Comparable<Measurement<V, C>> {

    private final ID mId;
    private final String mDefaultName;
    private C mConfiguration;
    private ValueContainer<V> mDynamicValueContainer;
    private ValueContainer<V> mHistoryValueContainer;

    protected Measurement(@NonNull ID id, String name) {
        mId = id;
        mDefaultName = name != null ? name : "";
        mDynamicValueContainer = onCreateDynamicValueContainer();
        mHistoryValueContainer = onCreateHistoryValueContainer();
        resetConfiguration();
    }

    public ID getId() {
        return mId;
    }

    protected abstract @NonNull
    ValueContainer<V> onCreateDynamicValueContainer();

    protected abstract @NonNull ValueContainer<V> onCreateHistoryValueContainer();

    protected abstract @NonNull C getEmptyConfiguration();

    public boolean setConfiguration(C configuration) {
        boolean changed = false;
        if (configuration != null) {
            if (mConfiguration != configuration) {
                mConfiguration = configuration;
                changed = true;
            }
        } else {
            if (mConfiguration == null) {
                mConfiguration = getEmptyConfiguration();
            } else if (mConfiguration != getEmptyConfiguration()) {
                mConfiguration = getEmptyConfiguration();
                changed = true;
            }
        }
        if (changed) {
            onConfigurationChanged();
        }
        return changed;
    }

    protected void onConfigurationChanged() {
    }

    public @NonNull C getConfiguration() {
        return mConfiguration;
    }

    public @NonNull String getDefaultName() {
        return mDefaultName;
    }

    public String getDecoratedName() {
        Decorator<V> decorator = mConfiguration.getDecorator();
        return decorator != null ? decorator.decorateName(getDefaultName()) : null;
    }

    public @NonNull String getName() {
        String decoratedName = getDecoratedName();
        return decoratedName != null ? decoratedName : getDefaultName();
    }

    public String decorateValue(@NonNull V v) {
        return decorateValue(v, 0);
    }

    public String decorateValue(V v, int para) {
        Decorator<V> decorator = mConfiguration.getDecorator();
        return decorator != null
                ? decorator.decorateValue(v, para)
                : null;
    }

    public String getDecoratedRealTimeValue() {
        return getDecoratedRealTimeValue(0);
    }

    public String getDecoratedRealTimeValue(int para) {
        V v = mDynamicValueContainer.getLatestValue();
        return v != null
                ? decorateValue(v, para)
                : null;
    }

    public V getRealTimeValue() {
        return mDynamicValueContainer.getLatestValue();
    }

    public ValueContainer<V> getDynamicValueContainer() {
        return mDynamicValueContainer;
    }

    public ValueContainer<V> getHistoryValueContainer() {
        return mHistoryValueContainer;
    }

    public V getValueByContainerAddMethodReturnValue(@NonNull ValueContainer<V> container, int addMethodReturnValue) {
        if (addMethodReturnValue >= 0) {
            return container.getValue(addMethodReturnValue);
        } else if (addMethodReturnValue != ValueContainer.ADD_FAILED_RETURN_VALUE) {
            return container.getValue(- addMethodReturnValue - 1);
        }
        return null;
    }

    public boolean hasRealTimeValue() {
        return !getDynamicValueContainer().empty();
    }

    public boolean hasHistoryValue() {
        return !getHistoryValueContainer().empty();
    }

    public boolean resetConfiguration() {
        SensorManager.MeasurementConfigurationProvider provider = SensorManager.getMeasurementConfigurationProvider();
        if (provider == null) {
            return setConfiguration(null);
        } else {
            C configuration = provider.getConfiguration(mId);
            return setConfiguration(configuration);
        }
    }

    @Override
    public int compareTo(@NonNull Measurement<V, C> o) {
        return mId.compareTo(o.mId);
    }

    protected static class EmptyConfiguration<V extends Value> implements Configuration<V> {

        @Override
        public Decorator getDecorator() {
            return null;
        }

        @Override
        public void setDecorator(Decorator decorator) {
            throw new UnsupportedOperationException("inner configuration can not set decorator");
        }
    }
}
