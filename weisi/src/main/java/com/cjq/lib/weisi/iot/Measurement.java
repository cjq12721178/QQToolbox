package com.cjq.lib.weisi.iot;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cjq.lib.weisi.iot.container.SubValueContainer;
import com.cjq.lib.weisi.iot.container.Value;
import com.cjq.lib.weisi.iot.container.ValueContainer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class Measurement<V extends Value, C extends Configuration<V>> implements Comparable<Measurement<V, C>> {

    protected static final int CURVE_TYPE_INVALID = 0;
    protected static final int CURVE_TYPE_SENSOR_INFO = 0x10001;
    private static final int CURVE_PATTERN_BITS = 16;

    private static int unknownCurveType = 0x1FFFF;

    private final ID mId;
    private final String mDefaultName;
    private C mConfiguration;
    private ValueContainer<V> mDynamicValueContainer;
    private ValueContainer<V> mHistoryValueContainer;
    private ValueContainer<V> mUniteValueContainer;

    protected static int getUnknownCurveType() {
        return unknownCurveType--;
    }

    protected Measurement(@NonNull ID id, String name) {
        this(id, name, true);
    }

    protected Measurement(@NonNull ID id, String name, boolean autoInit) {
        mId = id;
        mDefaultName = TextUtils.isEmpty(name) ? "未知测量量" : name;
        if (autoInit) {
            init();
        }
    }

    protected void init() {
        mDynamicValueContainer = onCreateDynamicValueContainer();
        mHistoryValueContainer = onCreateHistoryValueContainer();
        resetConfiguration();
    }

    public ID getId() {
        return mId;
    }

    public abstract int getCurveType();

    @IntDef({CP_ANALOG,
            CP_STATUS,
            CP_COUNT})
    @Retention(RetentionPolicy.SOURCE)
    @interface CurvePattern {
    }

    public final static int CP_ANALOG = 1;
    public final static int CP_STATUS = 2;
    public final static int CP_COUNT = 3;

    public @CurvePattern int getCurvePattern() {
        return getCurveType() >> CURVE_PATTERN_BITS;
    }

    protected abstract @NonNull ValueContainer<V> onCreateDynamicValueContainer();

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

    public @NonNull String getDecoratedName() {
        Decorator<V> decorator = mConfiguration.getDecorator();
        return decorator != null ? decorator.decorateName(getDefaultName()) : "";
    }

    public @NonNull String getName() {
        String decoratedName = getDecoratedName();
        return decoratedName.isEmpty() ? getDefaultName() : decoratedName;
    }

    public @NonNull String getValueLabel(int para) {
        return getName();
    }

    public @NonNull String getValueLabel() {
        return getValueLabel(0);
    }

    public @NonNull String getFormattedRealTimeValue() {
        return formatValue(getRealTimeValue());
    }

    public @NonNull String formatValue(V v) {
        return formatValue(v, 0);
    }

    public @NonNull String formatValue(V v, int para) {
        if (v == null) {
            return "";
        }
        return formatValue(v.getRawValue(para), para);
    }

    public @NonNull String formatValue(double rawValue) {
        return formatValue(rawValue, 0);
    }

    public abstract @NonNull String formatValue(double rawValue, int para);

    public @NonNull String decorateValue(V v) {
        return decorateValue(v, 0);
    }

    public @NonNull String decorateValue(V v, int para) {
        if (v == null) {
            return "";
        }
        return decorateValue(v.getRawValue(para), para);
    }

    public @NonNull String decorateValue(double rawValue, int para) {
        Decorator<V> decorator = mConfiguration.getDecorator();
        if (decorator != null) {
            String result = decorator.decorateValue(rawValue, para);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return formatValue(rawValue, para);
    }

    public @NonNull String getDecoratedRealTimeValue() {
        return getDecoratedRealTimeValue(0);
    }

    public @NonNull String getDecoratedRealTimeValue(int para) {
        return decorateValue(getRealTimeValue(), para);
//        V v = mDynamicValueContainer.getLatestValue();
//        return v != null
//                ? decorateValue(v, para)
//                : null;
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
        int position = container.getPhysicalPositionByLogicalPosition(addMethodReturnValue);
        return position != -1
                ? container.getValue(position)
                : null;
//        if (addMethodReturnValue >= 0) {
//            return container.getValue(addMethodReturnValue);
//        } else if (addMethodReturnValue != ValueContainer.ADD_FAILED_RETURN_VALUE) {
//            return container.getValue(- addMethodReturnValue - 1);
//        }
//        return null;
    }

    public boolean hasRealTimeValue() {
        return !getDynamicValueContainer().empty();
    }

    public boolean hasHistoryValue() {
        return !getHistoryValueContainer().empty();
    }

    public boolean hasValue() {
        return hasRealTimeValue() || hasHistoryValue();
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

    public void setUniteValueContainer() {
        setUniteValueContainer(0L, 0L);
    }

    public void setUniteValueContainer(long startTime, long endTime) {
        clearUniteValueContainer();
        if (startTime < 0 || startTime >= endTime) {
            mUniteValueContainer = mDynamicValueContainer;
        } else {
            mUniteValueContainer = mHistoryValueContainer.applyForSubValueContainer(startTime, endTime);
        }
    }

    public ValueContainer<V> getUniteValueContainer() {
        return mUniteValueContainer;
    }

    public void clearUniteValueContainer() {
        mHistoryValueContainer.detachSubValueContainer(mUniteValueContainer);
        mUniteValueContainer = null;
    }

    @Override
    public String toString() {
        return getName();
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
