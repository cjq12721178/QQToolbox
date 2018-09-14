package com.cjq.lib.weisi.iot;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class DisplayMeasurement extends Measurement<DisplayMeasurement.Value, DisplayMeasurement.Configuration> {

    private static final Configuration EMPTY_CONFIGURATION = new EmptyConfiguration();

    protected DisplayMeasurement(int address, byte dataTypeValue, int dataTypeValueIndex, String name) {
        super(address, dataTypeValue, dataTypeValueIndex, name);
    }

    protected DisplayMeasurement(long id, String name) {
        super(id, name);
    }

    protected DisplayMeasurement(@NonNull ID id, String name) {
        super(id, name);
    }

    @NonNull
    @Override
    protected Configuration getEmptyConfiguration() {
        return EMPTY_CONFIGURATION;
    }

    public int testValue(@NonNull Warner<Value> warner, @NonNull Value value) {
        return warner.test(value);
    }

    public int testValue(@NonNull Value value) {
        Warner<Value> warner = getConfiguration().getWarner();
        return warner != null ? warner.test(value) : Warner.RESULT_NORMAL;
    }

    public int testRealTimeValue() {
        return testValue(getRealTimeValue());
    }

    public String getFormattedRealTimeValue() {
        return formatValue(getRealTimeValue());
    }

    public String getFormattedRealTimeValueWithUnit() {
        return formatValueWithUnit(getRealTimeValue());
    }

    public String formatValue(Value v) {
        return formatValue(v.getRawValue());
    }

    public abstract String formatValue(double rawValue);

    public String formatValueWithUnit(@NonNull Value v) {
        return formatValueWithUnit(v.getRawValue());
    }

    public abstract String formatValueWithUnit(double rawValue);

    public static class Value extends com.cjq.lib.weisi.iot.Value {

        double mRawValue;

        public Value(long timeStamp) {
            super(timeStamp);
        }

        public double getRawValue() {
            return mRawValue;
        }
    }

    public interface Configuration extends com.cjq.lib.weisi.iot.Configuration<Value> {

        Warner<Value> getWarner();

        void setWarner(Warner<Value> warner);
    }

    private static class EmptyConfiguration
            extends Measurement.EmptyConfiguration<Value>
            implements Configuration {

        @Override
        public Warner<Value> getWarner() {
            return null;
        }

        @Override
        public void setWarner(Warner<Value> warner) {
            throw new UnsupportedOperationException("inner configuration can not set warner");
        }
    }

    public interface SingleRangeWarner extends Warner<Value> {
        @IntDef({RESULT_NORMAL,
                RESULT_ABOVE_HIGH_LIMIT,
                RESULT_BELOW_LOW_LIMIT})
        @Retention(RetentionPolicy.SOURCE)
        @interface Result {
        }

        int RESULT_ABOVE_HIGH_LIMIT = 1;
        int RESULT_BELOW_LOW_LIMIT = 2;

        @Override
        @SingleRangeWarner.Result
        int test(Value value);
    }

    public interface SwitchWarner extends Warner<Value> {
        @IntDef({RESULT_IN_NORMAL_STATE, RESULT_IN_ABNORMAL_STATE})
        @Retention(RetentionPolicy.SOURCE)
        @interface Result {
        }

        int RESULT_IN_NORMAL_STATE = RESULT_NORMAL;
        int RESULT_IN_ABNORMAL_STATE = 1;

        @Override
        @SwitchWarner.Result
        int test(Value value);
    }
}
