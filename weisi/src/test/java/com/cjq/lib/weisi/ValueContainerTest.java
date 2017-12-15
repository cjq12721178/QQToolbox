package com.cjq.lib.weisi;

import com.cjq.lib.weisi.sensor.ValueContainer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by CJQ on 2017/12/13.
 */

public class ValueContainerTest {

    @Test
    public void addDynamicValue_notOutOfMaxValueSize_isListStored() {
        int maxValueSize = 20;
        ValueContainerImpl valueContainer = new ValueContainerImpl(maxValueSize);
        for (int i = 0;i < maxValueSize;++i) {
            valueContainer.addDynamicValue(i, i);
            assertEquals(i, valueContainer.getDynamicValue(i).getTimestamp());
            assertEquals(i, valueContainer.getDynamicValue(i).getBatteryVoltage(), 0.01);
        }
    }

    @Test
    public void addDynamicValue_outOfMaxValueSize_isLoopStored() {
        int maxValueSize = 20;
        ValueContainerImpl valueContainer = new ValueContainerImpl(maxValueSize);
        for (int i = 0;i < 30;++i) {
            if (i >= maxValueSize) {
                for (int j = 0, k = i - maxValueSize;j < maxValueSize;++j, ++k) {
                    assertEquals(k, valueContainer.getDynamicValue(j).getTimestamp());
                    assertEquals(k, valueContainer.getDynamicValue(j).getBatteryVoltage(), 0.01);
                }
            }
            valueContainer.addDynamicValue(i, i);
        }
    }

    @Test
    public void addDynamicValue_descAdd_isLoopStored() {
        int maxValueSize = 20;
        ValueContainerImpl valueContainer = new ValueContainerImpl(maxValueSize);
        for (int sum = 50, i = sum;i > 0;--i) {
            valueContainer.addDynamicValue(i, i);
            for (int j = 0, n = valueContainer.getDynamicValueSize(), k = sum - n + 1;j < n;++j, ++k) {
                assertEquals(k, valueContainer.getDynamicValue(j).getTimestamp());
                assertEquals(k, valueContainer.getDynamicValue(j).getBatteryVoltage(), 0.01);
            }
        }
    }

    private static class ValueContainerImpl extends ValueContainer<ValueContainerImpl.Value> {

        public ValueContainerImpl(int maxDynamicValueSize) {
            super(maxDynamicValueSize);
        }

        @Override
        protected Value onCreateValue(long timestamp) {
            return new Value(timestamp, 0);
        }

        public int addDynamicValue(long timestamp, float batteryVoltage) {
            setRealTimeValue(timestamp, batteryVoltage);
            return setDynamicValueContent(addDynamicValue(timestamp), batteryVoltage);
        }

        private void setRealTimeValue(long timestamp, float batteryVoltage) {
            if (mRealTimeValue.getTimestamp() < timestamp) {
                mRealTimeValue.setTimestamp(timestamp);
                mRealTimeValue.mBatteryVoltage = batteryVoltage;
            }
        }

        private int setDynamicValueContent(int position, float batteryVoltage) {
            if (position < 0) {
                setValueContent(getDynamicValue(-position - 1), batteryVoltage);
            } else if (position < MAX_DYNAMIC_VALUE_SIZE) {
                setValueContent(getDynamicValue(position), batteryVoltage);
            }
            return position;
        }

        private void setValueContent(Value value, float batteryVoltage) {
            if (value != null) {
                value.mBatteryVoltage = batteryVoltage;
            }
        }

        public static class Value extends ValueContainer.Value {

            float mBatteryVoltage;

            public Value(long timeStamp, float batteryVoltage) {
                super(timeStamp);
                mBatteryVoltage = batteryVoltage;
            }

            @Override
            public void setTimestamp(long timeStamp) {
                super.setTimestamp(timeStamp);
            }

            public float getBatteryVoltage() {
                return mBatteryVoltage;
            }
        }
    }
}
