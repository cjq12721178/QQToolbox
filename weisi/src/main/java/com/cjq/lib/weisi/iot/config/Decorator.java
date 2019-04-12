package com.cjq.lib.weisi.iot.config;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by CJQ on 2018/3/16.
 */
public class Decorator implements Parcelable {

    private final @NonNull String mCustomName;

    public Decorator(@NonNull String customName) {
        mCustomName = customName;
    }

    protected Decorator(Parcel in) {
        mCustomName = in.readString();
    }

    public static final Creator<Decorator> CREATOR = new Creator<Decorator>() {
        @Override
        public Decorator createFromParcel(Parcel in) {
            return new Decorator(in);
        }

        @Override
        public Decorator[] newArray(int size) {
            return new Decorator[size];
        }
    };

    public @NonNull String decorateName() {
        return mCustomName;
    }

    public @NonNull String decorateValue(double value, int para) {
        return "";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mCustomName);
    }
}
