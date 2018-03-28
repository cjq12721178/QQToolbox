package com.cjq.lib.weisi.iot;

import org.junit.Test;

import static com.cjq.lib.weisi.iot.ValueContainer.*;
import static org.junit.Assert.*;

/**
 * Created by CJQ on 2018/3/19.
 */

public class DynamicValueContainerTest {

    @Test
    public void addValue() {
        //未添加任何数据
        DynamicValueContainer container = new DynamicValueContainerImpl();
        assertEquals(0, container.size());
        assertEquals(true, container.empty());
        assertEquals(null, container.getEarliestValue());
        assertEquals(null, container.getLatestValue());

        //添加第一条数据，timestamp = 9
        int addResult = container.addValue(9);
        assertEquals(0, addResult);
        assertEquals(NEW_VALUE_ADDED, container.interpretAddResult(addResult));
        assertEquals(1, container.size());
        assertEquals(false, container.empty());
        assertEquals(9, container.getEarliestValue().getTimestamp());
        assertEquals(9, container.getLatestValue().getTimestamp());
        assertEquals(9, container.getValue(0).getTimestamp());

        //升序添加第二条数据，timestamp = 11
        addResult = container.addValue(11);
        assertEquals(1, addResult);
        assertEquals(NEW_VALUE_ADDED, container.interpretAddResult(addResult));
        assertEquals(2, container.size());
        assertEquals(false, container.empty());
        assertEquals(9, container.getEarliestValue().getTimestamp());
        assertEquals(11, container.getLatestValue().getTimestamp());
        assertArrayEquals(new long[] {9, 11}, new long[] {
                container.getValue(0).getTimestamp(),
                container.getValue(1).getTimestamp()
        });

        //添加第三条重复数据，timestamp = 9
        addResult = container.addValue(9);
        assertEquals(-1, addResult);
        assertEquals(VALUE_UPDATED, container.interpretAddResult(addResult));
        assertEquals(2, container.size());
        assertEquals(false, container.empty());
        assertEquals(9, container.getEarliestValue().getTimestamp());
        assertEquals(11, container.getLatestValue().getTimestamp());
        assertArrayEquals(new long[] {9, 11}, new long[] {
                container.getValue(0).getTimestamp(),
                container.getValue(1).getTimestamp()
        });

        //降序添加新数据，timestamp = 10
        addResult = container.addValue(10);
        assertEquals(1, addResult);
        assertEquals(NEW_VALUE_ADDED, container.interpretAddResult(addResult));
        assertEquals(3, container.size());
        assertEquals(false, container.empty());
        assertEquals(9, container.getEarliestValue().getTimestamp());
        assertEquals(11, container.getLatestValue().getTimestamp());
        assertArrayEquals(new long[] {9, 10, 11}, new long[] {
                container.getValue(0).getTimestamp(),
                container.getValue(1).getTimestamp(),
                container.getValue(2).getTimestamp()
        });

        //添加错误数据，timestamp < 0
        addResult = container.addValue(-1);
        assertEquals(ADD_FAILED_RETURN_VALUE, addResult);
        assertEquals(ADD_VALUE_FAILED, container.interpretAddResult(addResult));
        assertEquals(3, container.size());
        assertEquals(false, container.empty());
        assertEquals(9, container.getEarliestValue().getTimestamp());
        assertEquals(11, container.getLatestValue().getTimestamp());
        assertArrayEquals(new long[] {9, 10, 11}, new long[] {
                container.getValue(0).getTimestamp(),
                container.getValue(1).getTimestamp(),
                container.getValue(2).getTimestamp()
        });

        //升序添加数据至最大数据存储量50
        for (int i = 3;i < 50;++i) {
            long timestamp = i * 3 + 14;
            addResult = container.addValue(timestamp);
            assertEquals(i, addResult);
            assertEquals(NEW_VALUE_ADDED, container.interpretAddResult(addResult));
            assertEquals(i + 1, container.size());
            assertEquals(false, container.empty());
            assertEquals(9, container.getEarliestValue().getTimestamp());
            assertEquals(timestamp, container.getLatestValue().getTimestamp());
            assertArrayEquals(new long[] {9, 10, 11}, new long[] {
                    container.getValue(0).getTimestamp(),
                    container.getValue(1).getTimestamp(),
                    container.getValue(2).getTimestamp()
            });
            for (int j = 3;j <= i;++j) {
                assertEquals(j * 3 + 14, container.getValue(j).getTimestamp());
            }
        }

        //超出最大数据存储量升序添加数据，timestamp = 166
        addResult = container.addValue(166);
        assertEquals(49, addResult);
        assertEquals(LOOP_VALUE_ADDED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(10, container.getEarliestValue().getTimestamp());
        assertEquals(166, container.getLatestValue().getTimestamp());
        assertArrayEquals(new long[] { 10, 11,166 }, new long[] {
                container.getValue(0).getTimestamp(),
                container.getValue(1).getTimestamp(),
                container.getValue(49).getTimestamp()
        });
        for (int i = 2;i < 49;++i) {
            assertEquals(i * 3 + 17, container.getValue(i).getTimestamp());
        }

        //超出最大数据存储量添加最小数据，timestamp = 7
        addResult = container.addValue(7);
        assertEquals(ADD_FAILED_RETURN_VALUE, addResult);
        assertEquals(ADD_VALUE_FAILED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(10, container.getEarliestValue().getTimestamp());
        assertEquals(166, container.getLatestValue().getTimestamp());
        assertArrayEquals(new long[] { 10, 11,166 }, new long[] {
                container.getValue(0).getTimestamp(),
                container.getValue(1).getTimestamp(),
                container.getValue(49).getTimestamp()
        });
        for (int i = 2;i < 49;++i) {
            assertEquals(i * 3 + 17, container.getValue(i).getTimestamp());
        }

        //超出最大数据存储量添加中间重复数据，timestamp = 149
        addResult = container.addValue(149);
        assertEquals(-45, addResult);
        assertEquals(VALUE_UPDATED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(10, container.getEarliestValue().getTimestamp());
        assertEquals(166, container.getLatestValue().getTimestamp());
        assertArrayEquals(new long[] { 10, 11,166 }, new long[] {
                container.getValue(0).getTimestamp(),
                container.getValue(1).getTimestamp(),
                container.getValue(49).getTimestamp()
        });
        for (int i = 2;i < 49;++i) {
            assertEquals(i * 3 + 17, container.getValue(i).getTimestamp());
        }

        for (int i = 0;i < 50;++i) {
            System.out.println(container.getValue(i).getTimestamp());
        }

        //超出最大数据存储量添加中间不重复数据，timestamp = 118
        addResult = container.addValue(118);
        assertEquals(33, addResult);
        assertEquals(LOOP_VALUE_ADDED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(11, container.getEarliestValue().getTimestamp());
        assertEquals(166, container.getLatestValue().getTimestamp());
        assertArrayEquals(new long[] { 11, 118, 166 }, new long[] {
                container.getValue(0).getTimestamp(),
                container.getValue(33).getTimestamp(),
                container.getValue(49).getTimestamp()
        });
        for (int i = 2;i < 33;++i) {
            assertEquals(i * 3 + 20, container.getValue(i).getTimestamp());
        }
        for (int i = 34;i < 49;++i) {
            assertEquals(i * 3 + 17, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void addValue2() {
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }

        //超出最大数据存储量添加最大数据，timestamp = 115
        int addResult = container.addValue(115);
        assertEquals(49, addResult);
        assertEquals(LOOP_VALUE_ADDED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(12, container.getEarliestValue().getTimestamp());
        assertEquals(115, container.getLatestValue().getTimestamp());
        for (int i = 0;i < 49;++i) {
            assertEquals(i * 2 + 12, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void addValue3() {
        //刚好添加至满值
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }

        //添加小于最小值，timestamp = 9
        int addResult = container.addValue(9);
        assertEquals(ADD_FAILED_RETURN_VALUE, addResult);
        assertEquals(ADD_VALUE_FAILED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(10, container.getEarliestValue().getTimestamp());
        assertEquals(108, container.getLatestValue().getTimestamp());
        for (int i = 0;i < 50;++i) {
            assertEquals(i * 2 + 10, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void addValue4() {
        //刚好添加至满值
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }

        //添加中间值，不重复，timestamp = 11
        int addResult = container.addValue(11);
        assertEquals(0, addResult);
        assertEquals(LOOP_VALUE_ADDED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(11, container.getEarliestValue().getTimestamp());
        assertEquals(108, container.getLatestValue().getTimestamp());
        assertEquals(11, container.getValue(0).getTimestamp());
        for (int i = 1;i < 50;++i) {
            assertEquals(i * 2 + 10, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void addValue5() {
        //刚好添加至满值
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }

        //添加中间值，重复，timestamp = 12
        int addResult = container.addValue(12);
        assertEquals(-2, addResult);
        assertEquals(VALUE_UPDATED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(10, container.getEarliestValue().getTimestamp());
        assertEquals(108, container.getLatestValue().getTimestamp());
        for (int i = 0;i < 50;++i) {
            assertEquals(i * 2 + 10, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void addValue6() {
        //添加至满值后再加一个数据
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }
        container.addValue(110);

        //添加小于最小值，timestamp = 9
        int addResult = container.addValue(9);
        assertEquals(ADD_FAILED_RETURN_VALUE, addResult);
        assertEquals(ADD_VALUE_FAILED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(12, container.getEarliestValue().getTimestamp());
        assertEquals(110, container.getLatestValue().getTimestamp());
        for (int i = 0;i < 50;++i) {
            assertEquals(i * 2 + 12, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void addValue7() {
        //添加至满值后再加一个数据
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }
        container.addValue(110);

        //添加大于最大值，timestamp = 112
        int addResult = container.addValue(112);
        assertEquals(49, addResult);
        assertEquals(LOOP_VALUE_ADDED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(14, container.getEarliestValue().getTimestamp());
        assertEquals(112, container.getLatestValue().getTimestamp());
        for (int i = 0;i < 50;++i) {
            assertEquals(i * 2 + 14, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void addValue8() {
        //添加至满值后再加一个数据
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }
        container.addValue(110);

        //添加等于最大值，timestamp = 110
        int addResult = container.addValue(110);
        assertEquals(-50, addResult);
        assertEquals(VALUE_UPDATED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(12, container.getEarliestValue().getTimestamp());
        assertEquals(110, container.getLatestValue().getTimestamp());
        for (int i = 0;i < 50;++i) {
            assertEquals(i * 2 + 12, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void addValue9() {
        //添加至满值后再加一个数据
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }
        container.addValue(110);

        //添加等于最小值，timestamp = 12
        int addResult = container.addValue(12);
        assertEquals(-1, addResult);
        assertEquals(VALUE_UPDATED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(12, container.getEarliestValue().getTimestamp());
        assertEquals(110, container.getLatestValue().getTimestamp());
        for (int i = 0;i < 50;++i) {
            assertEquals(i * 2 + 12, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void addValue10() {
        //添加至满值后再加一个数据
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }
        container.addValue(110);

        //添加大于最小值，小于mValueHead，timestamp = 11
        int addResult = container.addValue(11);
        assertEquals(ADD_FAILED_RETURN_VALUE, addResult);
        assertEquals(ADD_VALUE_FAILED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(12, container.getEarliestValue().getTimestamp());
        assertEquals(110, container.getLatestValue().getTimestamp());
        for (int i = 0;i < 50;++i) {
            assertEquals(i * 2 + 12, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void addValue11() {
        //添加至满值后再加2个数据
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }
        container.addValue(110);
        container.addValue(112);

        //添加第二大值，timestamp = 111
        int addResult = container.addValue(111);
        assertEquals(48, addResult);
        assertEquals(LOOP_VALUE_ADDED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(16, container.getEarliestValue().getTimestamp());
        assertEquals(112, container.getLatestValue().getTimestamp());
        for (int i = 0;i < 48;++i) {
            assertEquals(i * 2 + 16, container.getValue(i).getTimestamp());
        }
        assertEquals(111, container.getValue(48).getTimestamp());
        assertEquals(112, container.getValue(49).getTimestamp());
    }

    @Test
    public void addValue12() {
        //加满两轮
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 210;i += 2) {
            container.addValue(i);
        }

        //添加第二大值，timestamp = 111
        //int addResult = container.addValue(111);
        //assertEquals(48, addResult);
        //assertEquals(LOOP_VALUE_ADDED, container.interpretAddResult(addResult));
        assertEquals(50, container.size());
        assertEquals(false, container.empty());
        assertEquals(110, container.getEarliestValue().getTimestamp());
        assertEquals(208, container.getLatestValue().getTimestamp());
        for (int i = 0;i < 50;++i) {
            assertEquals(i * 2 + 110, container.getValue(i).getTimestamp());
        }
    }

    @Test
    public void findValuePosition() {
        //size未满
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 100;i += 2) {
            container.addValue(i);
        }

        //找小于最小值
        assertEquals(-1, container.findValuePosition(9));

        //等于最小值
        assertEquals(0, container.findValuePosition(10));

        //大于最大值
        assertEquals(-46, container.findValuePosition(100));

        //等于最大值
        assertEquals(44, container.findValuePosition(98));

        //中间值，存在
        assertEquals(19, container.findValuePosition(48));

        //中间值，不存在
        assertEquals(-21, container.findValuePosition(49));
    }

    @Test
    public void findValuePosition2() {
        //size满
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 110;i += 2) {
            container.addValue(i);
        }

        //找小于最小值
        assertEquals(-1, container.findValuePosition(9));

        //等于最小值
        assertEquals(0, container.findValuePosition(10));

        //大于最大值
        assertEquals(-51, container.findValuePosition(120));

        //等于最大值
        assertEquals(49, container.findValuePosition(108));

        //中间值，存在
        assertEquals(19, container.findValuePosition(48));

        //中间值，不存在
        assertEquals(-21, container.findValuePosition(49));
    }

    @Test
    public void findValuePosition3() {
        //size满了1.5轮
        DynamicValueContainer container = new DynamicValueContainerImpl();
        for (int i = 10;i < 160;i += 2) {
            container.addValue(i);
        }

        //找小于最小值
        assertEquals(-1, container.findValuePosition(50));

        //等于最小值
        assertEquals(0, container.findValuePosition(60));

        //大于最大值
        assertEquals(-51, container.findValuePosition(160));

        //等于最大值
        assertEquals(49, container.findValuePosition(158));

        //中间值，存在
        assertEquals(4, container.findValuePosition(68));

        //中间值，不存在
        assertEquals(-6, container.findValuePosition(69));
    }

//    @Test
//    public void test_CreateValue() {
//        DynamicValueContainer container = new DynamicValueContainerImpl();
//        container.addValue(1);
//        boolean actual = container.getValue(0) instanceof TestValue;
//        assertEquals(true, actual);
//    }
//
//    @Test
//    public void test_findValuePosition_start() {
//        DynamicValueContainer container = new DynamicValueContainerImpl();
//        for (int i = 0;i < 60;++i) {
//            container.addValue(i);
//        }
//        int actual = container.findValuePosition(12, 9);
//        assertEquals(-1, actual);
//    }
//
//    @Test
//    public void test_findValuePosition_size60() {
//        DynamicValueContainer container = new DynamicValueContainerImpl();
//        for (int i = 0;i < 60;++i) {
//            container.addValue(i);
//        }
//        int actual = container.findValuePosition(9);
//        assertEquals(-1, actual);
//        actual = container.findValuePosition(10);
//        assertEquals(0, actual);
//        assertEquals(10, container.getValue(actual).getTimestamp());
//        actual = container.findValuePosition(49);
//        assertEquals(39, actual);
//        assertEquals(49, container.getValue(actual).getTimestamp());
//        actual = container.findValuePosition(59);
//        assertEquals(49, actual);
//        assertEquals(59, container.getValue(actual).getTimestamp());
//        actual = container.findValuePosition(50);
//        assertEquals(40, actual);
//        assertEquals(50, container.getValue(actual).getTimestamp());
//        actual = container.findValuePosition(70);
//        assertEquals(-51, actual);
//    }
//
//    @Test
//    public void test_findValuePosition_size30() {
//        DynamicValueContainer container = new DynamicValueContainerImpl();
//        for (int i = 1;i <= 30;++i) {
//            container.addValue(i);
//        }
//        int actual = container.findValuePosition(0);
//        assertEquals(-1, actual);
//        actual = container.findValuePosition(1);
//        assertEquals(0, actual);
//        assertEquals(1, container.getValue(actual).getTimestamp());
//        actual = container.findValuePosition(15);
//        assertEquals(14, actual);
//        assertEquals(15, container.getValue(actual).getTimestamp());
//        actual = container.findValuePosition(30);
//        assertEquals(29, actual);
//        assertEquals(30, container.getValue(actual).getTimestamp());
//        actual = container.findValuePosition(40);
//        assertEquals(-31, actual);
//    }

    private static class TestValue extends Value {

        private double mRawValue;

        public TestValue(long timestamp) {
            super(timestamp);
            mRawValue = 3.4;
        }

        public double getRawValue() {
            return mRawValue;
        }
    }

    private static class DynamicValueContainerImpl extends DynamicValueContainer<TestValue> {
    }
}