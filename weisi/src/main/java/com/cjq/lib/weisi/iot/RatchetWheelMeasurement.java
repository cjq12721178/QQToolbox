package com.cjq.lib.weisi.iot;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.container.ValueContainer;
import com.cjq.lib.weisi.iot.container.ValueContainerWrapper;
import com.cjq.lib.weisi.iot.interpreter.ValueInterpreter;

public abstract class RatchetWheelMeasurement extends VirtualMeasurement<RatchetWheelMeasurement.Configuration> {

    protected final PracticalMeasurement mDistanceRecorder;

    protected RatchetWheelMeasurement(@NonNull ID id,
                                      String name,
                                      int curveType,
                                      ValueInterpreter valueInterpreter,
                                      boolean hidden,
                                      @NonNull PracticalMeasurement distanceRecorder) {
        super(id, name, curveType, valueInterpreter != null ? valueInterpreter : distanceRecorder.getDataType().getInterpreter(), hidden, false);
        mDistanceRecorder = distanceRecorder;
        init();
    }

    @NonNull
    @Override
    protected Configuration getEmptyConfiguration() {
        return EmptyConfiguration.INSTANCE;
    }

    @Override
    protected void onConfigurationChanged() {
        Configuration configuration = getConfiguration();
        ValueContainerImpl dynamicalValueContainer = (ValueContainerImpl) getDynamicValueContainer();
        ValueContainerImpl historyValueContainer = (ValueContainerImpl) getHistoryValueContainer();
        if (configuration != getEmptyConfiguration()) {
            dynamicalValueContainer.setInitialValue(configuration.getInitialValue());
            dynamicalValueContainer.setInitialDistance(configuration.getInitialDistance());
            historyValueContainer.setInitialValue(configuration.getInitialValue());
            historyValueContainer.setInitialDistance(configuration.getInitialDistance());
        } else {
            dynamicalValueContainer.setInitialValue(0);
            dynamicalValueContainer.setInitialDistance(0);
            historyValueContainer.setInitialValue(0);
            historyValueContainer.setInitialDistance(0);
        }
    }

    public static class Configuration extends DisplayMeasurement.Configuration {

        private final double mInitialDistance;
        private final double mInitialValue;

        public Configuration(double initialDistance, double initialValue) {
            mInitialDistance = initialDistance;
            mInitialValue = initialValue;
        }

        protected Configuration(Parcel in) {
            super(in);
            mInitialDistance = in.readDouble();
            mInitialValue = in.readDouble();
        }

        public double getInitialDistance() {
            return mInitialDistance;
        }

        public double getInitialValue() {
            return mInitialValue;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeDouble(mInitialDistance);
            dest.writeDouble(mInitialValue);
        }
    }

    protected static class EmptyConfiguration
            extends Configuration {

        static final EmptyConfiguration INSTANCE = new EmptyConfiguration(0.0, 0.0);

        public EmptyConfiguration(double initialDistance, double initialValue) {
            super(initialDistance, initialValue);
        }

        protected EmptyConfiguration(Parcel in) {
            super(in);
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

    protected static abstract class ValueContainerImpl extends ValueContainerWrapper<DisplayMeasurement.Value> {

        protected double mInitialValue;
        protected double mInitialDistance;
        protected final Value mValue = new Value(0L);

        public ValueContainerImpl(@NonNull ValueContainer<Value> hostContainer) {
            super(hostContainer);
        }

        public void setInitialValue(double initialValue) {
            mInitialValue = initialValue;
        }

        public void setInitialDistance(double initialDistance) {
            mInitialDistance = initialDistance;
        }
    }
}
