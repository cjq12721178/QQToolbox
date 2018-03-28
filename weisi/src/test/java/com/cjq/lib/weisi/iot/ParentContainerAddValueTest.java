package com.cjq.lib.weisi.iot;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by CJQ on 2018/3/23.
 */
@RunWith(Parameterized.class)
public class ParentContainerAddValueTest {

    private static ValueContainer container;
    private static ValueContainer subContainer;
    private static int parameterNo;
    private static int parameterGroupNo = 0;
    private static int initTestDataTimes = 0;
    private static ExpectValue expectContainerValue;
    private static ExpectValue expectSubContainerValue;

    private static class ExpectValue {

        public ExpectValue(int size, boolean empty, TestValue earliest,
                           TestValue latest, long[] findValueTimestamps,
                           int[] findValuePositions) {
            Size = size;
            Empty = empty;
            Earliest = earliest;
            Latest = latest;
            FindValueTimestamps = findValueTimestamps;
            FindValuePositions = findValuePositions;
        }

        public int Size;
        public boolean Empty;
        public TestValue Earliest;
        public TestValue Latest;
        public long[] FindValueTimestamps;
        public int[] FindValuePositions;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> initTestData() {
        switch (initTestDataTimes++) {
            case 0:
                return Arrays.asList(
                        new Object[][] {
                                //参数组编号，待添加数据，原数据容器期望值，子数据容器期望值
                                { 1, new Long[] { 3L }, new ExpectValue(
                                        1, false,
                                        new TestValue(3),
                                        new TestValue(3),
                                        new long[] { 3L },
                                        new int[] { 0 }
                                ), new ExpectValue(
                                        0, true,
                                        null,
                                        null,
                                        new long[] { 3L },
                                        new int[] { -1 }
                                )},
                                { 2, new Long[] { 5L }, new ExpectValue(
                                        2, false,
                                        new TestValue(3),
                                        new TestValue(5),
                                        new long[] { 3L, 5L },
                                        new int[] { 0, 1 }
                                ), new ExpectValue(
                                        1, false,
                                        new TestValue(5),
                                        new TestValue(5),
                                        new long[] { 3L, 5L },
                                        new int[] { -1, 0 }
                                )},
                                { 3, new Long[] { 14L, 17L }, new ExpectValue(
                                        4, false,
                                        new TestValue(3),
                                        new TestValue(17),
                                        new long[] { 3, 5, 14, 17 },
                                        new int[] { 0, 1, 2, 3 }
                                ), new ExpectValue(
                                        1, false,
                                        new TestValue(5),
                                        new TestValue(5),
                                        new long[] { 3L, 5L },
                                        new int[] { -1, 0 }
                                )},
                                { 4, new Long[] { 9L }, new ExpectValue(
                                        5, false,
                                        new TestValue(3),
                                        new TestValue(17),
                                        new long[] {  },
                                        new int[] {  }
                                ), new ExpectValue(
                                        2, false,
                                        new TestValue(5),
                                        new TestValue(9),
                                        new long[] { 3, 5, 9 },
                                        new int[] { -1, 0, 1 }
                                )},
                                { 5, new Long[] { 7L }, new ExpectValue(
                                        6, false,
                                        new TestValue(3),
                                        new TestValue(17),
                                        new long[] {  },
                                        new int[] {  }
                                ), new ExpectValue(
                                        3, false,
                                        new TestValue(5),
                                        new TestValue(9),
                                        new long[] { 3, 5, 7, 9, 11, 14, 17 },
                                        new int[] { -1, 0, 1, 2, -4, -4, -4 }
                                )},
                                { 6, new Long[] { 2L }, new ExpectValue(
                                        7, false,
                                        new TestValue(2),
                                        new TestValue(17),
                                        new long[] {  },
                                        new int[] {  }
                                ), new ExpectValue(
                                        3, false,
                                        new TestValue(5),
                                        new TestValue(9),
                                        new long[] { 2, 3, 5, 7, 9, 11, 14, 17 },
                                        new int[] { -1, -1, 0, 1, 2, -4, -4, -4 }
                                )}
                        }
                );
            case 1:
                return Arrays.asList(
                        new Object[][] {
                                //参数组编号，待添加数据，原数据容器期望值，子数据容器期望值
                                { 1, new Long[] {  }, new ExpectValue(
                                        50, false,
                                        new TestValue(10),
                                        new TestValue(108),
                                        new long[] { 40 },
                                        new int[] { 15 }
                                ), new ExpectValue(
                                        20, false,
                                        new TestValue(30),
                                        new TestValue(68),
                                        new long[] { 40 },
                                        new int[] { 5 }
                                )},
                                { 2, new Long[] { 13L }, new ExpectValue(
                                        50, false,
                                        new TestValue(12),
                                        new TestValue(108),
                                        new long[] { 13, 40 },
                                        new int[] { 1, 15 }
                                ), new ExpectValue(
                                        20, false,
                                        new TestValue(30),
                                        new TestValue(68),
                                        new long[] { 40 },
                                        new int[] { 5 }
                                )},
                                { 3, new Long[] { 40L }, new ExpectValue(
                                        50, false,
                                        new TestValue(12),
                                        new TestValue(108),
                                        new long[] { 13, 40 },
                                        new int[] { 1, 15 }
                                ), new ExpectValue(
                                        20, false,
                                        new TestValue(30),
                                        new TestValue(68),
                                        new long[] { 40 },
                                        new int[] { 5 }
                                )},
                                { 4, new Long[] { 41L }, new ExpectValue(
                                        50, false,
                                        new TestValue(13),
                                        new TestValue(108),
                                        new long[] { 13, 40, 41 },
                                        new int[] { 0, 14, 15 }
                                ), new ExpectValue(
                                        21, false,
                                        new TestValue(30),
                                        new TestValue(68),
                                        new long[] { 40, 41 },
                                        new int[] { 5, 6 }
                                )},
                                { 5, new Long[] { 71L }, new ExpectValue(
                                        50, false,
                                        new TestValue(14),
                                        new TestValue(108),
                                        new long[] { 13, 40, 41, 71 },
                                        new int[] { -1, 13, 14, 30 }
                                ), new ExpectValue(
                                        21, false,
                                        new TestValue(30),
                                        new TestValue(68),
                                        new long[] { 40, 41 },
                                        new int[] { 5, 6 }
                                )},
                                { 6, new Long[] { 81L }, new ExpectValue(
                                        50, false,
                                        new TestValue(16),
                                        new TestValue(108),
                                        new long[] { 13, 40, 41, 71, 81 },
                                        new int[] { -1, 12, 13, 29, 35 }
                                ), new ExpectValue(
                                        21, false,
                                        new TestValue(30),
                                        new TestValue(68),
                                        new long[] { 40, 41 },
                                        new int[] { 5, 6 }
                                )}
                        }
                );
            case 2:
                return Arrays.asList(
                        new Object[][] {
                                //参数组编号，待添加数据，原数据容器期望值，子数据容器期望值
                                { 1, new Long[] { 11L }, new ExpectValue(
                                        50, false,
                                        new TestValue(11),
                                        new TestValue(108),
                                        new long[] { 40 },
                                        new int[] { 15 }
                                ), new ExpectValue(
                                        31, false,
                                        new TestValue(11),
                                        new TestValue(70),
                                        new long[] { 40 },
                                        new int[] { 15 }
                                )},
                                { 2, new Long[] { 81L }, new ExpectValue(
                                        50, false,
                                        new TestValue(12),
                                        new TestValue(108),
                                        new long[] { 40 },
                                        new int[] { 14 }
                                ), new ExpectValue(
                                        30, false,
                                        new TestValue(12),
                                        new TestValue(70),
                                        new long[] { 40 },
                                        new int[] { 14 }
                                )},
                                { 3, new Long[] { 9L }, new ExpectValue(
                                        50, false,
                                        new TestValue(12),
                                        new TestValue(108),
                                        new long[] { 40 },
                                        new int[] { 14 }
                                ), new ExpectValue(
                                        30, false,
                                        new TestValue(12),
                                        new TestValue(70),
                                        new long[] { 40 },
                                        new int[] { 14 }
                                )}
                        }
                );
            case 3:
                return Arrays.asList(
                        new Object[][] {
                                //参数组编号，待添加数据，原数据容器期望值，子数据容器期望值
                                { 1, new Long[] {  }, new ExpectValue(
                                        50, false,
                                        new TestValue(10),
                                        new TestValue(108),
                                        new long[] { 60 },
                                        new int[] { 25 }
                                ), new ExpectValue(
                                        35, false,
                                        new TestValue(40),
                                        new TestValue(108),
                                        new long[] { 60 },
                                        new int[] { 10 }
                                )},
                                { 2, new Long[] { 41L }, new ExpectValue(
                                        50, false,
                                        new TestValue(12),
                                        new TestValue(108),
                                        new long[] { 60 },
                                        new int[] { 25 }
                                ), new ExpectValue(
                                        36, false,
                                        new TestValue(40),
                                        new TestValue(108),
                                        new long[] { 60 },
                                        new int[] { 11 }
                                )},
                                { 3, new Long[] { 31L }, new ExpectValue(
                                        50, false,
                                        new TestValue(14),
                                        new TestValue(108),
                                        new long[] { 60 },
                                        new int[] { 25 }
                                ), new ExpectValue(
                                        36, false,
                                        new TestValue(40),
                                        new TestValue(108),
                                        new long[] { 60 },
                                        new int[] { 11 }
                                )},
                                { 4, new Long[] { 120L }, new ExpectValue(
                                        50, false,
                                        new TestValue(16),
                                        new TestValue(120),
                                        new long[] { 60 },
                                        new int[] { 24 }
                                ), new ExpectValue(
                                        37, false,
                                        new TestValue(40),
                                        new TestValue(120),
                                        new long[] { 60 },
                                        new int[] { 11 }
                                )},
                                { 5, new Long[] { 140L }, new ExpectValue(
                                        50, false,
                                        new TestValue(18),
                                        new TestValue(140),
                                        new long[] { 60 },
                                        new int[] { 23 }
                                ), new ExpectValue(
                                        37, false,
                                        new TestValue(40),
                                        new TestValue(120),
                                        new long[] { 60 },
                                        new int[] { 11 }
                                )},
                        }
                );
        }
        return null;
    }

    @BeforeClass
    public static void init() {
        parameterNo = 0;
        switch (parameterGroupNo++) {
            case 0:
                container = new HistoryValueContainerImpl();
                subContainer = container.applyForSubValueContainer(5, 11);
                break;
            case 1:
                container = new DynamicValueContainerImpl();
                for (int i = 10;i < 110;i += 2) {
                    container.addValue(i);
                }
                subContainer = container.applyForSubValueContainer(30, 70);
                break;
            case 2:
                container = new DynamicValueContainerImpl();
                for (int i = 10;i < 110;i += 2) {
                    container.addValue(i);
                }
                subContainer = container.applyForSubValueContainer(10, 70);
                break;
            case 3:
                container = new DynamicValueContainerImpl();
                for (int i = 10;i < 110;i += 2) {
                    container.addValue(i);
                }
                subContainer = container.applyForSubValueContainer(40, 130);
                break;
        }
    }

    public ParentContainerAddValueTest(int no, Long[] addValues,
                                       ExpectValue containerValue,
                                       ExpectValue subContainerValue) {
        if (parameterNo != no) {
            parameterNo = no;
            for (long value : addValues) {
                container.addValue(value);
            }
            expectContainerValue = containerValue;
            expectSubContainerValue = subContainerValue;
        }
    }

    @Test
    public void size() throws Exception {
        assertEquals(expectContainerValue.Size, container.size());
        assertEquals(expectSubContainerValue.Size, subContainer.size());
    }

    @Test
    public void empty() throws Exception {
        assertEquals(expectContainerValue.Empty, container.empty());
        assertEquals(expectSubContainerValue.Empty, subContainer.empty());
    }

    @Test
    public void getEarliestValue() throws Exception {
        assertEquals(expectContainerValue.Earliest, container.getEarliestValue());
        assertEquals(expectSubContainerValue.Earliest, subContainer.getEarliestValue());
    }

    @Test
    public void getLatestValue() throws Exception {
        assertEquals(expectContainerValue.Latest, container.getLatestValue());
        assertEquals(expectSubContainerValue.Latest, subContainer.getLatestValue());
    }

    @Test
    public void findValuePosition() {
        for (int i = 0;i < expectContainerValue.FindValueTimestamps.length;++i) {
            assertEquals(expectContainerValue.FindValuePositions[i], container.findValuePosition(expectContainerValue.FindValueTimestamps[i]));
        }
        for (int i = 0;i < expectSubContainerValue.FindValueTimestamps.length;++i) {
            assertEquals(expectSubContainerValue.FindValuePositions[i], subContainer.findValuePosition(expectSubContainerValue.FindValueTimestamps[i]));
        }
    }

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

    private static class HistoryValueContainerImpl extends HistoryValueContainer<TestValue> {
    }

    private static class DynamicValueContainerImpl extends DynamicValueContainer<TestValue> {

    }
}
