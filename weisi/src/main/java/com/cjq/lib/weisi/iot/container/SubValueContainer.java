package com.cjq.lib.weisi.iot.container;

import android.support.annotation.NonNull;

/**
 * Created by CJQ on 2018/3/20.
 */

public class SubValueContainer<V extends Value>
        implements ValueContainer<V>,
        ValueContainer.OnValueAddListener {

    private final ValueContainer<V> mParent;
    private final long mStartTime;
    private final long mEndTime;
    private int mOffset;
    private int mSize;

    public SubValueContainer(@NonNull ValueContainer<V> parent,
                             long startTime, long endTime) {
        if (startTime < 0) {
            throw new IllegalArgumentException("start time may not less than 0");
        }
        if (startTime > endTime) {
            throw new IllegalArgumentException("start time may less than end time");
        }
        parent.registerOnValueAddListener(this);
        mParent = parent;
        mStartTime = startTime;
        mEndTime = endTime;
        int startPos = mParent.findValuePosition(mStartTime);
        int endPos = mParent.findValuePosition(mEndTime);
        if (startPos < 0) {
            startPos = -startPos - 1;
        }
        if (endPos < 0) {
            endPos = -endPos - 1;
        }
        mOffset = startPos;
        mSize = endPos - startPos;
    }

    public long getStartTime() {
        return mStartTime;
    }

    public long getEndTime() {
        return mEndTime;
    }

    public boolean contains(long timestamp) {
        return mStartTime <= timestamp && timestamp < mEndTime;
    }

    @Override
    public int addValue(long timestamp) {
        return contains(timestamp)
                ? mParent.addValue(timestamp)
                : ADD_FAILED_RETURN_VALUE;
    }

    @Override
    public int interpretAddResult(int logicalPosition) {
        int addResult = mParent.interpretAddResult(logicalPosition);
        switch (addResult) {
            case NEW_VALUE_ADDED:
            case VALUE_UPDATED:
            case LOOP_VALUE_ADDED:
                if (contains(mParent.getValue(mParent.getPhysicalPositionByLogicalPosition(logicalPosition)).mTimestamp)) {
                    return addResult;
                } else {
                    return ADD_VALUE_FAILED;
                }
            default:
                return ADD_VALUE_FAILED;
        }
    }

    @Override
    public int getPhysicalPositionByLogicalPosition(int logicalPosition) {
        if (logicalPosition == ADD_FAILED_RETURN_VALUE) {
            return -1;
        }
        int position = getRealPositionByParentPosition(logicalPosition);
        return position >= 0
                ? position
                : -1;
    }

    @Override
    public int size() {
        return mSize;
    }

    @Override
    public boolean empty() {
        return mSize == 0;
    }

    @Override
    public V getValue(int physicalPosition) {
        return mParent.getValue(mOffset + physicalPosition);
    }

    @Override
    public V getEarliestValue() {
        return empty()
                ? null
                : getValue(0);
    }

    @Override
    public V getLatestValue() {
        return empty()
                ? null
                : getValue(mSize - 1);
    }

    @Override
    public int findValuePosition(long timestamp) {
        return getRealPositionByParentPosition(mParent.findValuePosition(timestamp));
    }

    @Override
    public V findValue(long timestamp) {
        return contains(timestamp)
                ? mParent.findValue(timestamp)
                : null;
    }

    @Override
    public int findValuePosition(int possiblePosition, long timestamp) {
        return getRealPositionByParentPosition(mParent.findValuePosition(possiblePosition, timestamp));
    }

    private int getRealPositionByParentPosition(int position) {
        int pos;
        if (position < 0) {
            pos = - position - 1;
            if (pos < mOffset) {
                return -1;
            }
            if (pos >= mOffset + mSize) {
                return -mSize - 1;
            }
            return -(pos - mOffset) - 1;
        } else {
            pos = position;
            if (position < mOffset) {
                return -1;
            }
            if (pos >= mOffset + mSize) {
                return -mSize - 1;
            }
            return pos - mOffset;
        }
    }

    @Override
    public V findValue(int possiblePosition, long timestamp) {
        return contains(timestamp)
                ? mParent.findValue(possiblePosition, timestamp)
                : null;
    }

    @Override
    public ValueContainer<V> applyForSubValueContainer(long startTime, long endTime) {
        return new SubValueContainer<>(mParent, startTime, endTime);
    }

    @Override
    public void detachSubValueContainer(ValueContainer subContainer) {
        mParent.detachSubValueContainer(subContainer);
    }

    @Override
    public void registerOnValueAddListener(@NonNull OnValueAddListener listener) {
        mParent.registerOnValueAddListener(listener);
    }

    @Override
    public void unregisterOnValueAddListener(@NonNull OnValueAddListener listener) {
        mParent.unregisterOnValueAddListener(listener);
    }

    @Override
    public void onValueAdd(int logicalPosition, long timestamp) {
        switch (mParent.interpretAddResult(logicalPosition)) {
            case NEW_VALUE_ADDED:
                if (timestamp < mStartTime) {
                    ++mOffset;
                } else if (timestamp < mEndTime) {
                    ++mSize;
                }
                break;
            case LOOP_VALUE_ADDED:
                if (timestamp >= mEndTime) {
                    if (mOffset > 0) {
                        --mOffset;
                    } else if (mSize > 0) {
                        --mSize;
                    }
                } else if (timestamp >= mStartTime) {
                    if (mOffset > 0) {
                        --mOffset;
                        ++mSize;
                    } else if (mSize != mParent.size()){
                        ++mSize;
                    }
                }
                break;
        }
    }
}
