package com.cjq.tool.qqtoolbox.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.data.FilterCollection;
import com.cjq.lib.weisi.data.Storage;
import com.cjq.lib.weisi.iot.PhysicalSensor;

import java.util.List;

/**
 * Created by CJQ on 2018/5/31.
 */

public class PhysicalSensorProvider implements Storage.ElementsProvider<PhysicalSensor>, Parcelable {
    @Override
    public void onProvideElements(@NonNull List<PhysicalSensor> elements, FilterCollection<PhysicalSensor> filters) {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public PhysicalSensorProvider() {
    }

    protected PhysicalSensorProvider(Parcel in) {
    }

    public static final Creator<PhysicalSensorProvider> CREATOR = new Creator<PhysicalSensorProvider>() {
        @Override
        public PhysicalSensorProvider createFromParcel(Parcel source) {
            return new PhysicalSensorProvider(source);
        }

        @Override
        public PhysicalSensorProvider[] newArray(int size) {
            return new PhysicalSensorProvider[size];
        }
    };
}
