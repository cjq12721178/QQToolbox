package com.cjq.lib.weisi;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import com.cjq.lib.weisi.node.ValueContainer;

import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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

    private static class ValueContainerImpl extends ValueContainer<ValueContainerImpl.Value, ValueContainerImpl.Configuration> {

        public ValueContainerImpl(int maxDynamicValueSize) {
            super(maxDynamicValueSize);
        }

        @Override
        protected Value onCreateValue(long timestamp) {
            return new Value(timestamp, 0);
        }

        @Override
        protected Configuration getEmptyConfiguration() {
            return null;
        }

        @Override
        public String getDefaultName() {
            return null;
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

        public static class Configuration extends ValueContainer.Configuration<Value> {

        }
    }

//    @Test
//    public void warnerResult() {
//        WarnerImpl warner = new WarnerImpl();
//        warner.setResult(5);
//        //warner.setDescription(null);
//        warner.setValue(123);
//        assertEquals(5, warner.getResult());
//        assertEquals(3, warner.test(new ValueContainerImpl.Value(2, 2)));
//    }
//
//    private static class WarnerImpl implements ValueContainer.Warner<ValueContainerImpl.Value> {
//
//        private @Result int mResult;
//
//        @Override
//        public int test(ValueContainerImpl.Value value) {
//            return 3;
//        }
//
//        public void setResult(@Result int result) {
//            mResult = result;
//        }
//
//        public int getResult() {
//            return mResult;
//        }
//
//        public void setDescription(@NonNull String description) {
//
//        }
//
//        public void setValue(@Size(min=1,max=10) int value) {
//
//        }
//    }
}