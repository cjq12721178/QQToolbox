package com.cjq.lib.weisi.iot;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.container.Corrector;

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

    @Override
    public Corrector getCorrector(int index) {
        return getConfiguration().getCorrector();
    }

    public int testValue(@NonNull Value value) {
        Warner<Value> warner = getConfiguration().getWarner();
        return warner != null ? testValue(warner, value) : Warner.RESULT_NORMAL;
    }

    public int testValue(@NonNull Warner<Value> warner, @NonNull Value value) {
        return testValue(warner, value, getCorrector(0));
    }

    public int testValue(@NonNull Warner<Value> warner, @NonNull Value value, Corrector corrector) {
        return warner.test(value, corrector);
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
        public double getRawValue(int index) {
            return mRawValue;
        }
    }

    public interface Configuration extends com.cjq.lib.weisi.iot.Configuration {
        Corrector getCorrector();
        void setCorrector(Corrector corrector);
        Warner<Value> getWarner();
        void setWarner(Warner<Value> warner);
    }

    protected static class EmptyConfiguration
            extends Measurement.EmptyConfiguration
            implements Configuration {

        static final EmptyConfiguration INSTANCE = new EmptyConfiguration();

        @Override
        public Corrector getCorrector() {
            return null;
        }

        @Override
        public void setCorrector(Corrector corrector) {
            throw new UnsupportedOperationException("inner configuration can not set corrector");
        }

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
        int test(@NonNull Value value, Corrector corrector);
    }

    public interface SwitchWarner extends Warner<Value> {
        @IntDef({RESULT_NORMAL, RESULT_ABNORMAL})
        @Retention(RetentionPolicy.SOURCE)
        @interface Result {
        }

        int RESULT_ABNORMAL = 3;

        @Override
        @SwitchWarner.Result
        int test(@NonNull Value value, Corrector corrector);
    }
}
