package com.cjq.lib.weisi.iot.config;

import android.os.Parcel;
import android.os.Parcelable;

public class Configuration implements Parcelable {

    protected Decorator mDecorator;

    public Configuration() {
    }

    protected Configuration(Parcel in) {
        mDecorator = in.readParcelable(Decorator.class.getClassLoader());
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

    public Decorator getDecorator() {
        return mDecorator;
    }

    public void setDecorator(Decorator decorator) {
        mDecorator = decorator;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDecorator, flags);
    }
}
