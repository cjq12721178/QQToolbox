package com.cjq.lib.weisi.iot;

import com.cjq.lib.weisi.util.ExpandComparator;

import java.lang.reflect.Constructor;


/**
 * Created by CJQ on 2018/3/16.
 */
public class Value implements Cloneable {

    long mTimestamp;

    public Value(long timestamp) {
        mTimestamp = timestamp;
    }

    public Value copy(long timestamp) {
        Value v;
        try {
            v = (Value) clone();
            v.mTimestamp = timestamp;
        } catch (CloneNotSupportedException e) {
            try {
                Constructor c = getClass().getConstructor(long.class);
                if (!c.isAccessible()) {
                    c.setAccessible(true);
                }
                v = (Value) c.newInstance(timestamp);
                v.mTimestamp = timestamp;
            } catch (Exception e1) {
                v = null;
            }
        }
        return v;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    protected void setTimestamp(long timeStamp) {
        mTimestamp = timeStamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Value) {
            return mTimestamp == ((Value) obj).mTimestamp;
        }
        return false;
    }

    public static final ExpandComparator<Value, Long> SEARCH_HELPER = new ExpandComparator<Value, Long>() {
        @Override
        public int compare(Value value, Long targetTimestamp) {
            return (value.mTimestamp < targetTimestamp)
                    ? -1
                    : ((value.mTimestamp == targetTimestamp)
                    ? 0
                    : 1);
        }
    };
}