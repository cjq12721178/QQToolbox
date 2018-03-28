package com.cjq.lib.weisi.iot;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by CJQ on 2018/3/26.
 */

public class CreateValueTest {

    @Test
    public void addValue() {
        ValueContainer container = new DynamicValueContainer<TestValue>() {
        };
        container.addValue(2);
        assertEquals(true, container.getLatestValue() instanceof TestValue);
    }

    private static class TestValue extends Value {

        public TestValue(long timestamp) {
            super(timestamp);
        }
    }
}
