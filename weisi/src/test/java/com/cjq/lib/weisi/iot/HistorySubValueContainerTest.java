package com.cjq.lib.weisi.iot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;

/**
 * Created by CJQ on 2018/3/21.
 */
@RunWith(Parameterized.class)
public class HistorySubValueContainerTest {

    private static int parameterGroupNo;
    private static HistoryValueContainer container;
    private static ValueContainer subContainer;

    private static List<Long> srcTimestamps;
    private static List<Long> expectTimestamps;
    private static List<Long> addTimestamps;
    private static long startTime;
    private static long endTime;

    @Parameterized.Parameters
    public static Collection<Object[]> initTestData() {
        List<Long> commonSrcTimestamps = Arrays.asList(2L, 4L, 7L, 8L, 9L, 12L, 14L, 15L, 18L);
        return Arrays.asList(
                new Object[][] {
                        { 1, commonSrcTimestamps, null, 4, 14 },
                        { 2, commonSrcTimestamps, null, 4, 27 },
                        { 3, commonSrcTimestamps, null, 0, 14 },
                        { 4, commonSrcTimestamps, null, 0, 25 },
                        { 5, commonSrcTimestamps, null, 6, 6 },
                        { 6, commonSrcTimestamps, null, 8, 15 },
                        { 7, commonSrcTimestamps, null, 11, 15 }
                }
        );
    }

    public HistorySubValueContainerTest(int parameterGroupNo,
                                        List<Long> timestamps,
                                        List<Long> addTimestamps,
                                        long startTime,
                                        long endTime) {
        if (parameterGroupNo != HistorySubValueContainerTest.parameterGroupNo) {
            HistorySubValueContainerTest.parameterGroupNo = parameterGroupNo;

            //初始化expect参数
            HistorySubValueContainerTest.addTimestamps = addTimestamps;
            HistorySubValueContainerTest.startTime = startTime;
            HistorySubValueContainerTest.endTime = endTime;
            srcTimestamps = new ArrayList<>(new HashSet<>(timestamps));
            Collections.sort(srcTimestamps);
            int startPos = Collections.binarySearch(srcTimestamps, HistorySubValueContainerTest.startTime);
            if (startPos < 0) {
                startPos = - startPos - 1;
            }
            int endPos = Collections.binarySearch(srcTimestamps, HistorySubValueContainerTest.endTime);
            if (endPos < 0) {
                endPos = - endPos - 1;
            }
            expectTimestamps = new ArrayList<>(srcTimestamps.subList(startPos, endPos));

            //初始化actual参数
            container = new HistoryValueContainerImpl();
            for (int i = 0;i < timestamps.size();++i) {
                container.addValue(timestamps.get(i));
            }
            subContainer = container.applyForSubValueContainer(startTime, endTime);
        }
    }

    @Test
    public void addValue() throws Exception {
    }

    @Test
    public void interpretAddResult() throws Exception {
    }

    @Test
    public void size() throws Exception {
        assertEquals(expectTimestamps.size(), subContainer.size());
    }

    @Test
    public void empty() throws Exception {
        assertEquals(expectTimestamps.isEmpty(), subContainer.empty());
    }

    @Test
    public void getValue() throws Exception {
        if (!expectTimestamps.isEmpty()) {
            assertEquals(expectTimestamps.get(0).longValue(), subContainer.getValue(0).getTimestamp());
            assertEquals(expectTimestamps.get(expectTimestamps.size() - 1).longValue(), subContainer.getValue(expectTimestamps.size() - 1).getTimestamp());
            if (expectTimestamps.size() > 2) {
                int pos = getRandomMidPosition();
                assertEquals(expectTimestamps.get(pos).longValue(), subContainer.getValue(pos).getTimestamp());
            }
        }
    }

    private int getRandomMidPosition() {
        return new Random().nextInt(expectTimestamps.size() - 2) + 1;
//        int pos = ((new Random().nextInt()) % (expectTimestamps.size() - 2)) + 1;
//        System.out.println("random position = " + pos);
//        return pos;
    }

    private int getRandomPosition() {
        return new Random().nextInt(expectTimestamps.size());
    }

    @Test
    public void getEarliestValue() throws Exception {
        if (expectTimestamps.isEmpty()) {
            assertEquals(null, subContainer.getEarliestValue());
        } else {
            assertEquals(expectTimestamps.get(0).longValue(), subContainer.getEarliestValue().getTimestamp());
        }
    }

    @Test
    public void getLatestValue() throws Exception {
        if (expectTimestamps.isEmpty()) {
            assertEquals(null, subContainer.getLatestValue());
        } else {
            assertEquals(expectTimestamps.get(expectTimestamps.size() - 1).longValue(), subContainer.getLatestValue().getTimestamp());
        }
    }

    @Test

    public void findValuePosition() throws Exception {
        if (!srcTimestamps.isEmpty() && srcTimestamps.get(0).longValue() + 1 < startTime) {
            assertEquals(-1, subContainer.findValuePosition(srcTimestamps.get(0).longValue() + 1));
        }
        //小于mStartTime
        if (startTime > 0) {
            assertEquals(-1, subContainer.findValuePosition(startTime - 1));
        }
        //等于mStartTime
        if (expectTimestamps.isEmpty()) {
            assertEquals(-1, subContainer.findValuePosition(startTime));
        } else {
            if (expectTimestamps.get(0).longValue() == startTime) {
                assertEquals(0, subContainer.findValuePosition(startTime));
            } else {
                assertEquals(-1, subContainer.findValuePosition(startTime));
            }
        }
        //大于mStartTime，小于firstValue，取存在值
        if (!expectTimestamps.isEmpty()) {
            for (int i = 0;i < srcTimestamps.size();++i) {
                long timestamp = srcTimestamps.get(i);
                if (timestamp > startTime) {
                    if (timestamp < expectTimestamps.get(0).longValue()) {
                        assertEquals(-1, subContainer.findValuePosition(timestamp));
                    }
                    break;
                }
            }
        }
        //大于mStartTime，小于firstValue，取不存在值
        if (!expectTimestamps.isEmpty() && startTime + 1 < expectTimestamps.get(0).longValue()) {
            assertEquals(-1, subContainer.findValuePosition(startTime + 1));
        }
        //等于firstValue
        if (!expectTimestamps.isEmpty()) {
            assertEquals(0, subContainer.findValuePosition(expectTimestamps.get(0).longValue()));
        }
        //大于firstValue，小于lastValue，取随机存在值
        if (expectTimestamps.size() > 2) {
            int pos = getRandomMidPosition();
            assertEquals(pos, subContainer.findValuePosition(expectTimestamps.get(pos).longValue()));
        }
        //大于firstValue，小于lastValue，取不存在值
        if (expectTimestamps.size() > 2) {
            long prevTimestamp = expectTimestamps.get(0);
            long currTimestamp;
            for (int i = 1; i < expectTimestamps.size() - 1; ++i) {
                currTimestamp = expectTimestamps.get(i);
                if (currTimestamp - prevTimestamp > 1) {
                    assertEquals(-i - 1, subContainer.findValuePosition(currTimestamp - 1));
                    break;
                }
                prevTimestamp = currTimestamp;
            }
        }
        //等于lastValue
        if (!expectTimestamps.isEmpty()) {
            assertEquals(expectTimestamps.size() - 1, subContainer.findValuePosition(expectTimestamps.get(expectTimestamps.size() - 1).longValue()));
        }
        //大于lastValue，小于endTime，取存在值
        if (!expectTimestamps.isEmpty()) {
            for (int i = srcTimestamps.size() - 1;i >= 0;--i) {
                long timestamp = srcTimestamps.get(i);
                if (timestamp < endTime) {
                    if (timestamp > expectTimestamps.get(expectTimestamps.size() - 1).longValue()) {
                        assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(timestamp));
                    }
                    break;
                }
            }
        }
        //大于lastValue，小于endTime，取不存在值
        if (!expectTimestamps.isEmpty() && expectTimestamps.get(expectTimestamps.size() - 1).longValue() + 1 < endTime) {
            assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(endTime - 1));
        }
        //等于mEndTime
        if (endTime >= 0) {
            assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(endTime));
        }
        //大于mEndTime
        assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(endTime + 1));
    }

    @Test
    public void findValue() throws Exception {
        //小于mStartTime
        if (startTime > 0) {
            assertEquals(null, subContainer.findValue(startTime - 1));
        }
        //等于mStartTime
        if (expectTimestamps.isEmpty()) {
            assertEquals(null, subContainer.findValue(startTime));
        } else {
            if (expectTimestamps.get(0).longValue() == startTime) {
                assertEquals(startTime, subContainer.findValue(startTime).getTimestamp());
            } else {
                assertEquals(null, subContainer.findValue(startTime));
            }
        }
        //大于mStartTime，小于firstValue，取存在值
        if (!expectTimestamps.isEmpty()) {
            for (int i = 0;i < srcTimestamps.size();++i) {
                long timestamp = srcTimestamps.get(i);
                if (timestamp > startTime) {
                    if (timestamp < expectTimestamps.get(0).longValue()) {
                        assertEquals(null, subContainer.findValue(timestamp));
                    }
                    break;
                }
            }
        }
        //大于mStartTime，小于firstValue，取不存在值
        if (!expectTimestamps.isEmpty() && startTime + 1 < expectTimestamps.get(0).longValue()) {
            assertEquals(null, subContainer.findValue(startTime + 1));
        }
        //等于firstValue
        if (!expectTimestamps.isEmpty()) {
            assertEquals(expectTimestamps.get(0).longValue(), subContainer.findValue(expectTimestamps.get(0).longValue()).getTimestamp());
        }
        //大于firstValue，小于lastValue，取随机存在值
        if (expectTimestamps.size() > 2) {
            long timestamp = expectTimestamps.get(getRandomMidPosition()).longValue();
            assertEquals(timestamp, subContainer.findValue(timestamp).getTimestamp());
        }
        //大于firstValue，小于lastValue，取不存在值
        if (expectTimestamps.size() > 2) {
            long prevTimestamp = expectTimestamps.get(0);
            long currTimestamp;
            for (int i = 1; i < expectTimestamps.size() - 1; ++i) {
                currTimestamp = expectTimestamps.get(i);
                if (currTimestamp - prevTimestamp > 1) {
                    assertEquals(null, subContainer.findValue(currTimestamp - 1));
                    break;
                }
                prevTimestamp = currTimestamp;
            }
        }
        //等于lastValue
        if (!expectTimestamps.isEmpty()) {
            long timestamp = expectTimestamps.get(expectTimestamps.size() - 1).longValue();
            assertEquals(timestamp, subContainer.findValue(timestamp).getTimestamp());
        }
        //大于lastValue，小于endTime，取存在值
        if (!expectTimestamps.isEmpty()) {
            for (int i = srcTimestamps.size() - 1;i >= 0;--i) {
                long timestamp = srcTimestamps.get(i);
                if (timestamp < endTime) {
                    if (timestamp > expectTimestamps.get(expectTimestamps.size() - 1).longValue()) {
                        assertEquals(null, subContainer.findValue(timestamp));
                    }
                    break;
                }
            }
        }
        //大于lastValue，小于endTime，取不存在值
        if (!expectTimestamps.isEmpty() && expectTimestamps.get(expectTimestamps.size() - 1).longValue() + 1 < endTime) {
            assertEquals(null, subContainer.findValue(endTime - 1));
        }
        //等于mEndTime
        if (endTime >= 0) {
            assertEquals(null, subContainer.findValue(endTime));
        }
        //大于mEndTime
        assertEquals(null, subContainer.findValue(endTime + 1));
    }

    @Test
    public void findValuePosition_startIn() throws Exception {
        if (!expectTimestamps.isEmpty()) {
            int start = getRandomPosition();
            //小于mStartTime
            if (startTime > 0) {
                assertEquals(-1, subContainer.findValuePosition(start, startTime - 1));
            }
            //等于mStartTime
            if (expectTimestamps.isEmpty()) {
                assertEquals(-1, subContainer.findValuePosition(start, startTime));
            } else {
                if (expectTimestamps.get(0).longValue() == startTime) {
                    assertEquals(0, subContainer.findValuePosition(start, startTime));
                } else {
                    assertEquals(-1, subContainer.findValuePosition(start, startTime));
                }
            }
            //大于mStartTime，小于firstValue，取存在值
            if (!expectTimestamps.isEmpty()) {
                for (int i = 0;i < srcTimestamps.size();++i) {
                    long timestamp = srcTimestamps.get(i);
                    if (timestamp > startTime) {
                        if (timestamp < expectTimestamps.get(0).longValue()) {
                            assertEquals(-1, subContainer.findValuePosition(start, timestamp));
                        }
                        break;
                    }
                }
            }
            //大于mStartTime，小于firstValue，取不存在值
            if (!expectTimestamps.isEmpty() && startTime + 1 < expectTimestamps.get(0).longValue()) {
                assertEquals(-1, subContainer.findValuePosition(start, startTime + 1));
            }
            //等于firstValue
            if (!expectTimestamps.isEmpty()) {
                assertEquals(0, subContainer.findValuePosition(start, expectTimestamps.get(0).longValue()));
            }
            //大于firstValue，小于lastValue，取随机存在值
            if (expectTimestamps.size() > 2) {
                int pos = getRandomMidPosition();
                assertEquals(pos, subContainer.findValuePosition(start, expectTimestamps.get(pos).longValue()));
            }
            //大于firstValue，小于lastValue，取不存在值
            if (expectTimestamps.size() > 2) {
                long prevTimestamp = expectTimestamps.get(0);
                long currTimestamp;
                for (int i = 1; i < expectTimestamps.size() - 1; ++i) {
                    currTimestamp = expectTimestamps.get(i);
                    if (currTimestamp - prevTimestamp > 1) {
                        assertEquals(-i - 1, subContainer.findValuePosition(start, currTimestamp - 1));
                        break;
                    }
                    prevTimestamp = currTimestamp;
                }
            }
            //等于lastValue
            if (!expectTimestamps.isEmpty()) {
                assertEquals(expectTimestamps.size() - 1, subContainer.findValuePosition(start, expectTimestamps.get(expectTimestamps.size() - 1).longValue()));
            }
            //大于lastValue，小于endTime，取存在值
            if (!expectTimestamps.isEmpty()) {
                for (int i = srcTimestamps.size() - 1;i >= 0;--i) {
                    long timestamp = srcTimestamps.get(i);
                    if (timestamp < endTime) {
                        if (timestamp > expectTimestamps.get(expectTimestamps.size() - 1).longValue()) {
                            assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(start, timestamp));
                        }
                        break;
                    }
                }
            }
            //大于lastValue，小于endTime，取不存在值
            if (!expectTimestamps.isEmpty() && expectTimestamps.get(expectTimestamps.size() - 1).longValue() + 1 < endTime) {
                assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(start, endTime - 1));
            }
            //等于mEndTime
            if (endTime >= 0) {
                assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(start, endTime));
            }
            //大于mEndTime
            assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(start, endTime + 1));
        }
    }

    @Test
    public void findValuePosition_startOut() throws Exception {
        int start = expectTimestamps.size();
        //小于mStartTime
        if (startTime > 0) {
            assertEquals(-1, subContainer.findValuePosition(start, startTime - 1));
        }
        //等于mStartTime
        if (expectTimestamps.isEmpty()) {
            assertEquals(-1, subContainer.findValuePosition(start, startTime));
        } else {
            if (expectTimestamps.get(0).longValue() == startTime) {
                assertEquals(0, subContainer.findValuePosition(start, startTime));
            } else {
                assertEquals(-1, subContainer.findValuePosition(start, startTime));
            }
        }
        //大于mStartTime，小于firstValue，取存在值
        if (!expectTimestamps.isEmpty()) {
            for (int i = 0;i < srcTimestamps.size();++i) {
                long timestamp = srcTimestamps.get(i);
                if (timestamp > startTime) {
                    if (timestamp < expectTimestamps.get(0).longValue()) {
                        assertEquals(-1, subContainer.findValuePosition(start, timestamp));
                    }
                    break;
                }
            }
        }
        //大于mStartTime，小于firstValue，取不存在值
        if (!expectTimestamps.isEmpty() && startTime + 1 < expectTimestamps.get(0).longValue()) {
            assertEquals(-1, subContainer.findValuePosition(start, startTime + 1));
        }
        //等于firstValue
        if (!expectTimestamps.isEmpty()) {
            assertEquals(0, subContainer.findValuePosition(start, expectTimestamps.get(0).longValue()));
        }
        //大于firstValue，小于lastValue，取随机存在值
        if (expectTimestamps.size() > 2) {
            int pos = getRandomMidPosition();
            assertEquals(pos, subContainer.findValuePosition(start, expectTimestamps.get(pos).longValue()));
        }
        //大于firstValue，小于lastValue，取不存在值
        if (expectTimestamps.size() > 2) {
            long prevTimestamp = expectTimestamps.get(0);
            long currTimestamp;
            for (int i = 1; i < expectTimestamps.size() - 1; ++i) {
                currTimestamp = expectTimestamps.get(i);
                if (currTimestamp - prevTimestamp > 1) {
                    assertEquals(-i - 1, subContainer.findValuePosition(start, currTimestamp - 1));
                    break;
                }
                prevTimestamp = currTimestamp;
            }
        }
        //等于lastValue
        if (!expectTimestamps.isEmpty()) {
            assertEquals(expectTimestamps.size() - 1, subContainer.findValuePosition(start, expectTimestamps.get(expectTimestamps.size() - 1).longValue()));
        }
        //大于lastValue，小于endTime，取存在值
        if (!expectTimestamps.isEmpty()) {
            for (int i = srcTimestamps.size() - 1;i >= 0;--i) {
                long timestamp = srcTimestamps.get(i);
                if (timestamp < endTime) {
                    if (timestamp > expectTimestamps.get(expectTimestamps.size() - 1).longValue()) {
                        assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(start, timestamp));
                    }
                    break;
                }
            }
        }
        //大于lastValue，小于endTime，取不存在值
        if (!expectTimestamps.isEmpty() && expectTimestamps.get(expectTimestamps.size() - 1).longValue() + 1 < endTime) {
            assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(start, endTime - 1));
        }
        //等于mEndTime
        if (endTime >= 0) {
            assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(start, endTime));
        }
        //大于mEndTime
        assertEquals(- expectTimestamps.size() - 1, subContainer.findValuePosition(start, endTime + 1));
    }

    @Test
    public void findValue_startIn() throws Exception {
        if (!expectTimestamps.isEmpty()) {
            int start = getRandomPosition();
            //小于mStartTime
            if (startTime > 0) {
                assertEquals(null, subContainer.findValue(start, startTime - 1));
            }
            //等于mStartTime
            if (expectTimestamps.isEmpty()) {
                assertEquals(null, subContainer.findValue(start, startTime));
            } else {
                if (expectTimestamps.get(0).longValue() == startTime) {
                    assertEquals(startTime, subContainer.findValue(start, startTime).getTimestamp());
                } else {
                    assertEquals(null, subContainer.findValue(start, startTime));
                }
            }
            //大于mStartTime，小于firstValue，取存在值
            if (!expectTimestamps.isEmpty()) {
                for (int i = 0;i < srcTimestamps.size();++i) {
                    long timestamp = srcTimestamps.get(i);
                    if (timestamp > startTime) {
                        if (timestamp < expectTimestamps.get(0).longValue()) {
                            assertEquals(null, subContainer.findValue(start, timestamp));
                        }
                        break;
                    }
                }
            }
            //大于mStartTime，小于firstValue，取不存在值
            if (!expectTimestamps.isEmpty() && startTime + 1 < expectTimestamps.get(0).longValue()) {
                assertEquals(null, subContainer.findValue(start, startTime + 1));
            }
            //等于firstValue
            if (!expectTimestamps.isEmpty()) {
                assertEquals(expectTimestamps.get(0).longValue(), subContainer.findValue(start, expectTimestamps.get(0).longValue()).getTimestamp());
            }
            //大于firstValue，小于lastValue，取随机存在值
            if (expectTimestamps.size() > 2) {
                int pos = getRandomMidPosition();
                long timestamp = expectTimestamps.get(pos).longValue();
                assertEquals(timestamp, subContainer.findValue(start, timestamp).getTimestamp());
            }
            //大于firstValue，小于lastValue，取不存在值
            if (expectTimestamps.size() > 2) {
                long prevTimestamp = expectTimestamps.get(0);
                long currTimestamp;
                for (int i = 1; i < expectTimestamps.size() - 1; ++i) {
                    currTimestamp = expectTimestamps.get(i);
                    if (currTimestamp - prevTimestamp > 1) {
                        assertEquals(null, subContainer.findValue(start, currTimestamp - 1));
                        break;
                    }
                    prevTimestamp = currTimestamp;
                }
            }
            //等于lastValue
            if (!expectTimestamps.isEmpty()) {
                long timestamp = expectTimestamps.get(expectTimestamps.size() - 1).longValue();
                assertEquals(timestamp, subContainer.findValue(start, timestamp).getTimestamp());
            }
            //大于lastValue，小于endTime，取存在值
            if (!expectTimestamps.isEmpty()) {
                for (int i = srcTimestamps.size() - 1;i >= 0;--i) {
                    long timestamp = srcTimestamps.get(i);
                    if (timestamp < endTime) {
                        if (timestamp > expectTimestamps.get(expectTimestamps.size() - 1).longValue()) {
                            assertEquals(null, subContainer.findValue(start, timestamp));
                        }
                        break;
                    }
                }
            }
            //大于lastValue，小于endTime，取不存在值
            if (!expectTimestamps.isEmpty() && expectTimestamps.get(expectTimestamps.size() - 1).longValue() + 1 < endTime) {
                assertEquals(null, subContainer.findValue(start, endTime - 1));
            }
            //等于mEndTime
            if (endTime >= 0) {
                assertEquals(null, subContainer.findValue(start, endTime));
            }
            //大于mEndTime
            assertEquals(null, subContainer.findValue(start, endTime + 1));
        }
    }

    @Test
    public void findValue_startOut() throws Exception {
        int start = expectTimestamps.size();
        //小于mStartTime
        if (startTime > 0) {
            assertEquals(null, subContainer.findValue(start, startTime - 1));
        }
        //等于mStartTime
        if (expectTimestamps.isEmpty()) {
            assertEquals(null, subContainer.findValue(start, startTime));
        } else {
            if (expectTimestamps.get(0).longValue() == startTime) {
                assertEquals(startTime, subContainer.findValue(start, startTime).getTimestamp());
            } else {
                assertEquals(null, subContainer.findValue(start, startTime));
            }
        }
        //大于mStartTime，小于firstValue，取存在值
        if (!expectTimestamps.isEmpty()) {
            for (int i = 0;i < srcTimestamps.size();++i) {
                long timestamp = srcTimestamps.get(i);
                if (timestamp > startTime) {
                    if (timestamp < expectTimestamps.get(0).longValue()) {
                        assertEquals(null, subContainer.findValue(start, timestamp));
                    }
                    break;
                }
            }
        }
        //大于mStartTime，小于firstValue，取不存在值
        if (!expectTimestamps.isEmpty() && startTime + 1 < expectTimestamps.get(0).longValue()) {
            assertEquals(null, subContainer.findValue(start, startTime + 1));
        }
        //等于firstValue
        if (!expectTimestamps.isEmpty()) {
            assertEquals(expectTimestamps.get(0).longValue(), subContainer.findValue(start, expectTimestamps.get(0).longValue()).getTimestamp());
        }
        //大于firstValue，小于lastValue，取随机存在值
        if (expectTimestamps.size() > 2) {
            long timestamp = expectTimestamps.get(getRandomMidPosition()).longValue();
            assertEquals(timestamp, subContainer.findValue(start, timestamp).getTimestamp());
        }
        //大于firstValue，小于lastValue，取不存在值
        if (expectTimestamps.size() > 2) {
            long prevTimestamp = expectTimestamps.get(0);
            long currTimestamp;
            for (int i = 1; i < expectTimestamps.size() - 1; ++i) {
                currTimestamp = expectTimestamps.get(i);
                if (currTimestamp - prevTimestamp > 1) {
                    assertEquals(null, subContainer.findValue(start, currTimestamp - 1));
                    break;
                }
                prevTimestamp = currTimestamp;
            }
        }
        //等于lastValue
        if (!expectTimestamps.isEmpty()) {
            long timestamp = expectTimestamps.get(expectTimestamps.size() - 1).longValue();
            assertEquals(timestamp, subContainer.findValue(start, timestamp).getTimestamp());
        }
        //大于lastValue，小于endTime，取存在值
        if (!expectTimestamps.isEmpty()) {
            for (int i = srcTimestamps.size() - 1;i >= 0;--i) {
                long timestamp = srcTimestamps.get(i);
                if (timestamp < endTime) {
                    if (timestamp > expectTimestamps.get(expectTimestamps.size() - 1).longValue()) {
                        assertEquals(null, subContainer.findValue(start, timestamp));
                    }
                    break;
                }
            }
        }
        //大于lastValue，小于endTime，取不存在值
        if (!expectTimestamps.isEmpty() && expectTimestamps.get(expectTimestamps.size() - 1).longValue() + 1 < endTime) {
            assertEquals(null, subContainer.findValue(start, endTime - 1));
        }
        //等于mEndTime
        if (endTime >= 0) {
            assertEquals(null, subContainer.findValue(start, endTime));
        }
        //大于mEndTime
        assertEquals(null, subContainer.findValue(start, endTime + 1));
    }

    @Test
    public void subValueContainer() throws Exception {
    }

    @Test
    public void onValueAdd() throws Exception {
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
}