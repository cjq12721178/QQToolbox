package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cjq.lib.weisi.iot.container.DynamicValueContainer;
import com.cjq.lib.weisi.iot.container.EmptyValueContainer;
import com.cjq.lib.weisi.iot.container.HistoryValueContainer;
import com.cjq.lib.weisi.iot.container.RealTimeValueContainer;
import com.cjq.lib.weisi.iot.container.ValueContainer;
import com.cjq.lib.weisi.iot.corrector.ValueCorrector;
import com.cjq.lib.weisi.iot.interpreter.DefaultInterpreter;
import com.cjq.lib.weisi.iot.interpreter.ValueInterpreter;

public class PracticalMeasurement extends DisplayMeasurement<DisplayMeasurement.Configuration> {

    private static final EmptyValueContainer<Value> EMPTY_VALUE_CONTAINER = new EmptyValueContainer<>();
    private final DataType mDataType;

    protected PracticalMeasurement(@NonNull ID id, @NonNull DataType dataType, String name, boolean hidden) {
        super(id, TextUtils.isEmpty(name) ? dataType.getName() : name, hidden);
        mDataType = dataType;
    }

    @Override
    public @NonNull String formatValue(double correctedValue, int index) {
        return mDataType.formatValue(correctedValue);
    }

    @Override
    public int getCurveType() {
        return mDataType.mCurveType;
    }

    @NonNull
    @Override
    protected ValueContainer<Value> onCreateDynamicValueContainer() {
        if (SensorManager.mode == SensorManager.USE_MODE_WEAR) {
            return new RealTimeValueContainerImpl();
        }
        return new DynamicValueContainerImpl();
    }

    @NonNull
    @Override
    protected ValueContainer<Value> onCreateHistoryValueContainer() {
        if (SensorManager.mode == SensorManager.USE_MODE_WEAR) {
            return EMPTY_VALUE_CONTAINER;
        }
        return new HistoryValueContainerImpl();
    }

    @NonNull
    @Override
    protected Configuration getEmptyConfiguration() {
        return EmptyConfiguration.INSTANCE;
    }

    public DataType getDataType() {
        return mDataType;
    }

    Value setValueContent(@NonNull ValueContainer<Value> container, int addMethodReturnValue, double rawValue) {
        Value value = getValueByContainerAddMethodReturnValue(container, addMethodReturnValue);
        if (value != null) {
            if (addMethodReturnValue >= 0 || Math.abs(value.getRawValue() - rawValue) > 0.00001) {
                value.setRawValue(rawValue);
                //return true;
            }
        }
        //return false;
        return value;
    }

    double correctRawValue(double value) {
        return mDataType.mCorrector != null
                ? mDataType.mCorrector.correct(value)
                : value;
    }

    public static class DataType {

        final byte mValue;
        private final int mCurveType;
        private final String mName;
        ValueCorrector mCorrector;
        private ValueInterpreter mInterpreter = DefaultInterpreter.getInstance();

        public DataType(byte value) {
            this(value, 0, null);
        }

        public DataType(byte value, int curveType, String name) {
            mValue = value;
            if (curveType <= CURVE_TYPE_INVALID) {
                mCurveType = getUnknownCurveType();
            } else {
                mCurveType = curveType;
            }
            mName = TextUtils.isEmpty(name) ? "未知测量量" : name;
        }

        public byte getValue() {
            return mValue;
        }

        public int getAbsValue() {
            return mValue & 0xff;
        }

        public String getFormattedValue() {
            return String.format("%02X", mValue);
        }

        public String getName() {
            return mName;
        }

        void setInterpreter(ValueInterpreter interpreter) {
            mInterpreter = interpreter != null ? interpreter : DefaultInterpreter.getInstance();
        }

        public ValueInterpreter getInterpreter() {
            return mInterpreter;
        }

        public @NonNull String formatValue(double rawValue) {
            return mInterpreter.interpret(rawValue);
        }
    }

    private static class DynamicValueContainerImpl extends DynamicValueContainer<Value> {
    }

    private static class HistoryValueContainerImpl extends HistoryValueContainer<Value> {
    }

    private static class RealTimeValueContainerImpl extends RealTimeValueContainer<Value> {
    }
}
