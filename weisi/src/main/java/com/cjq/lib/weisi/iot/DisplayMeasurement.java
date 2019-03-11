package com.cjq.lib.weisi.iot;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class DisplayMeasurement<C extends DisplayMeasurement.Configuration> extends Measurement<DisplayMeasurement.Value, C> {

    private final boolean mHidden;

    protected DisplayMeasurement(@NonNull ID id, String name, boolean hidden) {
        this(id, name, hidden, true);
    }

    protected DisplayMeasurement(@NonNull ID id, String name, boolean hidden, boolean autoInit) {
        super(id, name, autoInit);
        mHidden = hidden | (id.getDataTypeAbsValue() == 0xF1);
    }

    public boolean isHidden() {
        return mHidden;
    }

    public int testValue(@NonNull Warner<Value> warner, @NonNull Value value) {
        return warner.test(value);
    }

    public int testValue(@NonNull Value value) {
        Warner<Value> warner = getConfiguration().getWarner();
        return warner != null ? warner.test(value) : Warner.RESULT_NORMAL;
    }

    public int testRealTimeValue() {
        Value v = getRealTimeValue();
        return v != null ? testValue(v) : Warner.RESULT_NORMAL;
    }

    public static class Value extends com.cjq.lib.weisi.iot.container.Value {

        private double mRawValue;

        public Value(long timeStamp) {
            super(timeStamp);
        }

        @Override
        protected void setTimestamp(long timeStamp) {
            mTimestamp = timeStamp;
        }

        void setRawValue(double rawValue) {
            mRawValue = rawValue;
        }

        public double getRawValue() {
            return mRawValue;
        }

        @Override
        public double getRawValue(int para) {
            return mRawValue;
        }
    }

    public interface Configuration extends com.cjq.lib.weisi.iot.container.Configuration<Value> {

        Warner<Value> getWarner();

        void setWarner(Warner<Value> warner);
    }

    protected static class EmptyConfiguration
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
        @IntDef({RESULT_NORMAL, RESULT_ABNORMAL})
        @Retention(RetentionPolicy.SOURCE)
        @interface Result {
        }

        int RESULT_ABNORMAL = 3;

        @Override
        @SwitchWarner.Result
        int test(Value value);
    }
}
