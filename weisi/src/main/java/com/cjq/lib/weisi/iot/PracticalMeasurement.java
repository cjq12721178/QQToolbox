package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.cjq.lib.weisi.iot.container.DynamicValueContainer;
import com.cjq.lib.weisi.iot.container.HistoryValueContainer;
import com.cjq.lib.weisi.iot.container.ValueContainer;
import com.cjq.lib.weisi.iot.corrector.ValueCorrector;
import com.cjq.lib.weisi.iot.interpreter.DefaultInterpreter;
import com.cjq.lib.weisi.iot.interpreter.ValueInterpreter;

public class PracticalMeasurement extends DisplayMeasurement<DisplayMeasurement.Configuration> {

    private static final Configuration EMPTY_CONFIGURATION = new DisplayMeasurement.EmptyConfiguration();
    private final DataType mDataType;

    protected PracticalMeasurement(@NonNull ID id, @NonNull DataType dataType, String name, boolean hidden) {
        super(id, TextUtils.isEmpty(name) ? dataType.getName() : name, hidden);
        mDataType = dataType;
    }

    @Override
    public String formatValue(double rawValue) {
        return mDataType.formatValue(rawValue);
    }

    @Override
    public int getCurveType() {
        return mDataType.mCurveType;
    }

    @NonNull
    @Override
    protected ValueContainer<Value> onCreateDynamicValueContainer() {
        return new DynamicValueContainerImpl();
    }

    @NonNull
    @Override
    protected ValueContainer<Value> onCreateHistoryValueContainer() {
        return new HistoryValueContainerImpl();
    }

    @NonNull
    @Override
    protected Configuration getEmptyConfiguration() {
        return EMPTY_CONFIGURATION;
    }

    public DataType getDataType() {
        return mDataType;
    }

//    int addDynamicValue(long timestamp, double rawValue) {
//        int result = getDynamicValueContainer().addValue(timestamp);
//        setValueContent(getValueByContainerAddMethodReturnValue(getDynamicValueContainer(), result), rawValue);
//        return result;
//    }
//
//    private void setValueContent(Value value, double rawValue) {
//        if (value != null) {
//            value.mRawValue = rawValue;
//        }
//    }
//
//    public int addHistoryValue(long timestamp, double rawValue) {
//        int result = getHistoryValueContainer().addValue(timestamp);
//        setValueContent(getValueByContainerAddMethodReturnValue(getHistoryValueContainer(), result), rawValue);
//        return result;
//    }

    boolean setValueContent(@NonNull ValueContainer<Value> container, int addMethodReturnValue, double rawValue) {
        Value value = getValueByContainerAddMethodReturnValue(container, addMethodReturnValue);
        if (value != null) {
            if (addMethodReturnValue >= 0 || Math.abs(value.mRawValue - rawValue) > 0.00001) {
                value.mRawValue = rawValue;
                return true;
            }
        }
        return false;
    }

    double correctRawValue(double value) {
        return mDataType.mCorrector != null
                ? mDataType.mCorrector.correct(value)
                : value;
    }

    public static class DataType {

        final byte mValue;
        private final int mCurveType;
        private String mName;
        ValueCorrector mCorrector;
        private ValueInterpreter mInterpreter = DefaultInterpreter.getInstance();

        public DataType(byte value) {
            this(value, 0);
        }

        public DataType(byte value, int curveType) {
            mValue = value;
            if (curveType <= CURVE_TYPE_INVALID) {
                mCurveType = getUnknownCurveType();
            } else {
                mCurveType = curveType;
            }
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

        void setName(String name) {
            mName = TextUtils.isEmpty(name) ? "未知测量量" : name;
        }

        public String getName() {
            return mName;
        }

//        public String getDefaultName() {
//            return TextUtils.isEmpty(mName) ? "未知测量量" : mName;
//        }

        void setInterpreter(ValueInterpreter interpreter) {
            mInterpreter = interpreter != null ? interpreter : DefaultInterpreter.getInstance();
        }

        public ValueInterpreter getInterpreter() {
            return mInterpreter;
        }

        //        public String getUnit() {
//            return mUnit;
//        }

        public String formatValue(Value value) {
            return value != null ? formatValue(value.mRawValue) : "";
        }

        public String formatValue(double rawValue) {
            return mInterpreter.interpret(rawValue);
        }

//        public String formatValueWithUnit(Value value) {
//            return value != null ? formatValueWithUnit(value.mRawValue) : "";
//        }
//
//        public String formatValueWithUnit(double rawValue) {
//            return mUnit != "" ?
//                    mInterpreter.interpret(rawValue) + mUnit :
//                    mInterpreter.interpret(rawValue);
//        }
    }

    private static class DynamicValueContainerImpl extends DynamicValueContainer<Value> {
    }

    private static class HistoryValueContainerImpl extends HistoryValueContainer<Value> {
    }
}
