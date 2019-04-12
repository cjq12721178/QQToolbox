package com.cjq.lib.weisi.iot.config;

import android.os.Parcel;
import android.support.annotation.NonNull;

public class MeasurementDecorator extends Decorator {

    private @NonNull final String mCustomUnit;
    private final int mDecimals;

    public MeasurementDecorator(@NonNull String customName,
                                @NonNull String customUnit,
                                int savedDecimals) {
        super(customName);
        mCustomUnit = customUnit;
        mDecimals = correctDecimals(savedDecimals);
    }

    protected MeasurementDecorator(Parcel in) {
        super(in);
        mCustomUnit = in.readString();
        mDecimals = correctDecimals(in.readInt());
    }

    private int correctDecimals(int savedDecimals) {
        if (savedDecimals < 0 || savedDecimals > 9) {
            return -1;
        } else {
            return savedDecimals;
        }
    }

    @NonNull
    public String getCustomUnit() {
        return mCustomUnit;
    }

    public int getOriginDecimals() {
        return mDecimals;
    }

    public @NonNull String getOriginDecimalsLabel() {
        if (mDecimals == -1) {
            return "";
        }
        return String.valueOf(mDecimals);
    }

    public int getRefinedDecimals() {
        if (mDecimals == -1) {
            return 3;
        }
        return mDecimals;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mCustomUnit);
        dest.writeInt(mDecimals);
    }

    public static final Creator<MeasurementDecorator> CREATOR = new Creator<MeasurementDecorator>() {
        @Override
        public MeasurementDecorator createFromParcel(Parcel in) {
            return new MeasurementDecorator(in);
        }

        @Override
        public MeasurementDecorator[] newArray(int size) {
            return new MeasurementDecorator[size];
        }
    };
}
