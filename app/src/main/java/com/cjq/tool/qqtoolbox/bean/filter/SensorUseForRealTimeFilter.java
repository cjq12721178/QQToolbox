package com.cjq.tool.qqtoolbox.bean.filter;

import android.os.Parcel;
import android.os.Parcelable;

import com.cjq.lib.weisi.data.Filter;
import com.cjq.lib.weisi.iot.Sensor;

/**
 * Created by CJQ on 2017/9/19.
 */

public class SensorUseForRealTimeFilter<S extends Sensor> implements Filter<S>, Parcelable {

    public int getType() {
        return mType;
    }

    public SensorUseForRealTimeFilter setType(int type) {
        mType = type;
        return this;
    }

    private int mType;

    @Override
    public boolean match(S sensor) {
        return sensor.hasRealTimeValue();
    }

    public SensorUseForRealTimeFilter() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
    }

    protected SensorUseForRealTimeFilter(Parcel in) {
        this.mType = in.readInt();
    }

    public static final Creator<SensorUseForRealTimeFilter> CREATOR = new Creator<SensorUseForRealTimeFilter>() {
        @Override
        public SensorUseForRealTimeFilter createFromParcel(Parcel source) {
            return new SensorUseForRealTimeFilter(source);
        }

        @Override
        public SensorUseForRealTimeFilter[] newArray(int size) {
            return new SensorUseForRealTimeFilter[size];
        }
    };
}
