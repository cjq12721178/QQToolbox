package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.container.ValueContainer;
import com.cjq.lib.weisi.iot.interpreter.ValueInterpreter;

public class RatchetWheelMeasurementA extends RatchetWheelMeasurement {

    protected RatchetWheelMeasurementA(@NonNull ID id, String name, int curveType, ValueInterpreter valueInterpreter, boolean hidden, PracticalMeasurement distanceRecorder) {
        super(id, name, curveType, valueInterpreter, hidden, distanceRecorder);
    }

    @NonNull
    @Override
    protected ValueContainer onCreateDynamicValueContainer() {
        return new ValueContainerImpl(mDistanceRecorder.getDynamicValueContainer());
    }

    @NonNull
    @Override
    protected ValueContainer onCreateHistoryValueContainer() {
        return new ValueContainerImpl(mDistanceRecorder.getHistoryValueContainer());
    }

    private static class ValueContainerImpl extends RatchetWheelMeasurement.ValueContainerImpl {

        public ValueContainerImpl(@NonNull ValueContainer<Value> hostContainer) {
            super(hostContainer);
        }

        @Override
        protected Value wrapValue(Value src) {
            if (src == null) {
                return null;
            }
            mValue.setTimestamp(src.getTimestamp());
            mValue.mRawValue = mInitialValue + src.mRawValue - mInitialDistance;
            return mValue;
        }

        @Override
        public ValueContainer<Value> applyForSubValueContainer(long startTime, long endTime) {
            return new ValueContainerImpl(mHostContainer.applyForSubValueContainer(startTime, endTime));
        }
    }
}
