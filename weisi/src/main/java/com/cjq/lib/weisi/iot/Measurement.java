package com.cjq.lib.weisi.iot;

import android.os.Parcel;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cjq.lib.weisi.iot.config.Configuration;
import com.cjq.lib.weisi.iot.config.Decorator;
import com.cjq.lib.weisi.iot.config.Corrector;
import com.cjq.lib.weisi.iot.container.Value;
import com.cjq.lib.weisi.iot.container.ValueContainer;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class Measurement<V extends Value, C extends Configuration> implements Comparable<Measurement<V, C>> {

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
        Decorator decorator = mConfiguration.getDecorator();
        return decorator != null ? decorator.decorateName() : "";
    }

    public @NonNull String getName() {
        String decoratedName = getDecoratedName();
        return decoratedName.isEmpty() ? getDefaultName() : decoratedName;
    }

    public @NonNull String getValueLabel(int index) {
        return getName();
    }

    public @NonNull String getValueLabel() {
        return getValueLabel(0);
    }

    public @NonNull String getFormattedRealTimeValue() {
        return formatValue(getRealTimeValue());
    }

    public Corrector getCorrector() {
        return getCorrector(0);
    }

    public Corrector getCorrector(int index) {
        return null;
    }

    public double getCorrectedValue(@NonNull V v) {
        return getCorrectedValue(v, 0);
    }

    public double getCorrectedValue(@NonNull V v, int index) {
        return v.getCorrectedValue(getCorrector(index), index);
    }

    public @NonNull String formatValue(V v) {
        return formatValue(v, 0);
    }

    public @NonNull String formatValue(V v, int index) {
        if (v == null) {
            return "";
        }
        return formatValue(v.getCorrectedValue(getCorrector(index), index), index);
    }

    public @NonNull String formatValue(double correctedValue) {
        return formatValue(correctedValue, 0);
    }

    public abstract @NonNull String formatValue(double correctedValue, int index);

    public @NonNull String decorateValue(V v) {
        return decorateValue(v, 0);
    }

    public @NonNull String decorateValue(V v, int index) {
        if (v == null) {
            return "";
        }
        //同formatValue
        return decorateValue(v.getCorrectedValue(getCorrector(index), index), index);
    }

    public @NonNull String decorateValue(double correctedValue) {
        return decorateValue(correctedValue, 0);
    }

    public @NonNull String decorateValue(double correctedValue, int index) {
        Decorator decorator = mConfiguration.getDecorator();
        if (decorator != null) {
            String result = decorator.decorateValue(correctedValue, index);
            if (!result.isEmpty()) {
                return result;
            }
        }
        return formatValue(correctedValue, index);
    }

    public @NonNull String getDecoratedRealTimeValue() {
        return getDecoratedRealTimeValue(0);
    }

    public @NonNull String getDecoratedRealTimeValue(int index) {
        return decorateValue(getRealTimeValue(), index);
//        V v = mDynamicValueContainer.getLatestValue();
//        return v != null
//                ? decorateValue(v, index)
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

    protected static class EmptyConfiguration extends Configuration {

        static final EmptyConfiguration INSTANCE = new EmptyConfiguration();

        public EmptyConfiguration() {
            super();
        }

        protected EmptyConfiguration(Parcel in) {
            super(in);
        }

        @Override
        public void setDecorator(Decorator decorator) {
            throw new UnsupportedOperationException("inner configuration can not set decorator");
        }

        public static final Creator<EmptyConfiguration> CREATOR = new Creator<EmptyConfiguration>() {
            @Override
            public EmptyConfiguration createFromParcel(Parcel in) {
                return new EmptyConfiguration(in);
            }

            @Override
            public EmptyConfiguration[] newArray(int size) {
                return new EmptyConfiguration[size];
            }
        };
    }
}
