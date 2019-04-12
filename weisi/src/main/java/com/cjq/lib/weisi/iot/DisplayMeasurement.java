package com.cjq.lib.weisi.iot;

import android.os.Parcel;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.config.Corrector;
import com.cjq.lib.weisi.iot.config.Decorator;
import com.cjq.lib.weisi.iot.config.Warner;

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

        @Override
        public double getRawValue() {
            return mRawValue;
        }

        @Override
        public double getRawValue(int index) {
            return mRawValue;
        }
    }

    public static class Configuration extends com.cjq.lib.weisi.iot.config.Configuration {

        private Corrector mCorrector;
        private Warner<Value> mWarner;

        public Configuration() {
            super();
        }

        protected Configuration(Parcel in) {
            super(in);
            mCorrector = in.readParcelable(Corrector.class.getClassLoader());
            mWarner = in.readParcelable(Warner.class.getClassLoader());
        }

        public Corrector getCorrector() {
            return mCorrector;
        }

        public void setCorrector(Corrector corrector) {
            mCorrector = corrector;
        }

        public Warner<Value> getWarner() {
            return mWarner;
        }

        public void setWarner(Warner<Value> warner) {
            mWarner = warner;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeParcelable(mCorrector, flags);
            dest.writeParcelable(mWarner, flags);
        }

        public static final Creator<Configuration> CREATOR = new Creator<Configuration>() {
            @Override
            public Configuration createFromParcel(Parcel in) {
                return new Configuration(in);
            }

            @Override
            public Configuration[] newArray(int size) {
                return new Configuration[size];
            }
        };
    }

    protected static class EmptyConfiguration
            extends Configuration {

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

        @Override
        public void setCorrector(Corrector corrector) {
            throw new UnsupportedOperationException("inner configuration can not set corrector");
        }

        @Override
        public void setWarner(Warner<Value> warner) {
            throw new UnsupportedOperationException("inner configuration can not set warner");
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

    public static class SingleRangeWarner implements Warner<Value> {

        private final double mHighLimit;
        private final double mLowLimit;

        public SingleRangeWarner(double highLimit, double lowLimit) {
            if (highLimit < lowLimit) {
                throw new IllegalArgumentException("high limit: " + highLimit + " less than low limit: " + lowLimit);
            }
            mHighLimit = highLimit;
            mLowLimit = lowLimit;
        }

        protected SingleRangeWarner(Parcel in) {
            mHighLimit = in.readDouble();
            mLowLimit = in.readDouble();
        }

        @Override
        @Result
        public int test(@NonNull Value value, Corrector corrector) {
            double testingValue = Warner.getTestingValue(value, corrector);
            if (testingValue > mHighLimit) {
                return RESULT_ABOVE_HIGH_LIMIT;
            } else if (testingValue < mLowLimit) {
                return RESULT_BELOW_LOW_LIMIT;
            }
            return RESULT_NORMAL;
        }

        public double getHighLimit() {
            return mHighLimit;
        }

        public double getLowLimit() {
            return mLowLimit;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(mHighLimit);
            dest.writeDouble(mLowLimit);
        }

        public static final Creator<SingleRangeWarner> CREATOR = new Creator<SingleRangeWarner>() {
            @Override
            public SingleRangeWarner createFromParcel(Parcel in) {
                return new SingleRangeWarner(in);
            }

            @Override
            public SingleRangeWarner[] newArray(int size) {
                return new SingleRangeWarner[size];
            }
        };
    }

    public static class SwitchWarner implements Warner<Value> {

        private final double mAbnormalValue;

        public SwitchWarner(double abnormalValue) {
            mAbnormalValue = abnormalValue;
        }

        protected SwitchWarner(Parcel in) {
            mAbnormalValue = in.readDouble();
        }

        @Override
        @Result
        public int test(@NonNull Value value, Corrector corrector) {
            if (Warner.getTestingValue(value, corrector)
                    == mAbnormalValue) {
                return RESULT_ABNORMAL;
            }
            return RESULT_NORMAL;
        }

        public double getAbnormalValue() {
            return mAbnormalValue;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeDouble(mAbnormalValue);
        }

        public static final Creator<SwitchWarner> CREATOR = new Creator<SwitchWarner>() {
            @Override
            public SwitchWarner createFromParcel(Parcel in) {
                return new SwitchWarner(in);
            }

            @Override
            public SwitchWarner[] newArray(int size) {
                return new SwitchWarner[size];
            }
        };
    }
}
