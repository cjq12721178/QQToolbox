package com.cjq.lib.weisi.util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by CJQ on 2018/6/1.
 * 对没有实现Parcelable接口的类直接保存类名并使用反射获取该类，
 * 若要保存数据请实现Parcelable接口
 */

public class SimpleCustomClassParcel {

    private static final int VAL_NULL = -1;
    private static final int VAL_PARCELABLE = 0;
    private static final int VAL_CLASS_NAME = 1;

    private SimpleCustomClassParcel() {
    }

    public static void writeToParcel(Parcel dst, Object o, int flags) {
        if (o == null) {
            dst.writeInt(VAL_NULL);
        } else if (o instanceof Parcelable) {
            dst.writeInt(VAL_PARCELABLE);
            dst.writeParcelable((Parcelable) o, flags);
        } else {
            dst.writeInt(VAL_CLASS_NAME);
            dst.writeString(o.getClass().getName());
        }
    }

    public static <T> T readFromParcel(Parcel in, Object owner) {
        int val = in.readInt();
        if (val == VAL_NULL) {
            return null;
        } else if (val == VAL_PARCELABLE) {
            return in.readParcelable(owner.getClass().getClassLoader());
        } else {
            try {
                return (T) Class.forName(in.readString()).newInstance();
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static void writeToParcel(Parcel dst, List l, int flags) {
        int n = l != null ? l.size() : 0;
        dst.writeInt(n);
        for (int i = 0;i < n;++i) {
            writeToParcel(dst, l.get(i), flags);
        }
    }

    public static void readFromParcel(Parcel in, List l, Object owner) {
        int n = in.readInt();
        for (int i = 0;i < n;++i) {
            l.add(readFromParcel(in, owner));
        }
    }
}
