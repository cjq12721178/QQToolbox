package com.cjq.tool.qqtoolbox.bean;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.cjq.tool.qqtoolbox.util.DebugTag;

/**
 * Created by CJQ on 2018/6/1.
 */

public class Person implements Parcelable {

    public Person(String name, int age) {
        mName = name;
        mAge = age;
    }

    private String mName;
    private int mAge;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getAge() {
        return mAge;
    }

    public void setAge(int age) {
        mAge = age;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.d(DebugTag.GENERAL_LOG_TAG, "writeToParcel person");
        dest.writeString(this.mName);
        dest.writeInt(this.mAge);
    }

    public Person() {
    }

    protected Person(Parcel in) {
        Log.d(DebugTag.GENERAL_LOG_TAG, "parcel in person");
        this.mName = in.readString();
        this.mAge = 17;
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {
        @Override
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }

        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }
    };
}
