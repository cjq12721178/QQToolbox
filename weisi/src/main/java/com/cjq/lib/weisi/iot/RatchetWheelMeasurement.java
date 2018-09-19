package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.container.ValueContainer;
import com.cjq.lib.weisi.iot.container.ValueContainerWrapper;
import com.cjq.lib.weisi.iot.interpreter.ValueInterpreter;

public abstract class RatchetWheelMeasurement extends VirtualMeasurement<RatchetWheelMeasurement.Configuration> {

    private static final Configuration EMPTY_CONFIGURATION = new EmptyConfiguration();
    protected final PracticalMeasurement mDistanceRecorder;

    protected RatchetWheelMeasurement(@NonNull ID id,
                                      String name,
                                      ValueInterpreter valueInterpreter,
                                      boolean hidden,
                                      PracticalMeasurement distanceRecorder) {
        super(id, name, valueInterpreter, hidden);
        mDistanceRecorder = distanceRecorder;
    }

    @NonNull
    @Override
    protected Configuration getEmptyConfiguration() {
        return EMPTY_CONFIGURATION;
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

    public interface Configuration extends DisplayMeasurement.Configuration {
        double getInitialDistance();
        double getInitialValue();
    }

    protected static class EmptyConfiguration
            extends DisplayMeasurement.EmptyConfiguration
            implements Configuration {

        @Override
        public double getInitialDistance() {
            return 0;
        }

        @Override
        public double getInitialValue() {
            return 0;
        }
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