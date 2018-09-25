package com.cjq.lib.weisi.iot;


import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.container.ValueContainer;
import com.cjq.lib.weisi.iot.interpreter.ValueInterpreter;
import com.cjq.lib.weisi.util.ExpandCollections;
import com.cjq.lib.weisi.util.ExpandComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by CJQ on 2017/6/16.
 */

public class PhysicalSensor extends Sensor {

    private static final int MEASUREMENT_SEARCH_THRESHOLD = 3;
    private static final ExpandComparator<DisplayMeasurement, Long> MEASUREMENT_SEARCH_HELPER = new ExpandComparator<DisplayMeasurement, Long>() {
        @Override
        public int compare(DisplayMeasurement measurement, Long targetId) {
            long x = measurement.getId().getId();
            long y = targetId;
            return (x < y) ? -1 : ((x == y) ? 0 : 1);
        }
    };

    private final Type mType;
    private final List<DisplayMeasurement> mMeasurements;
    private List<DisplayMeasurement> mDisplayMeasurements;

    protected PhysicalSensor(@NonNull Info info, Type type, List<DisplayMeasurement> measurements) {
        super(info);
        mType = type != null ? type : UnknownType.SINGLETON;
        mMeasurements = measurements;
        sortMeasurements();
        generateDisplayMeasurements();
    }

    private void sortMeasurements() {
        if (mMeasurements.size() > 0) {
            Collections.sort(mMeasurements);
        }
    }

    private void generateDisplayMeasurements() {
        //从全部测量量中提取允许显示的测量量
        List<DisplayMeasurement> measurements = null;
        int i = 0, measurementSize = getMeasurementSize(), start = 0;
        for (;i < measurementSize;++i) {
            if (getMeasurementByPosition(i).isHidden()) {
                if (measurements == null) {
                    measurements = new ArrayList<>(measurementSize - 1);
                }
                measurements.addAll(mMeasurements.subList(start, i));
                start = i + 1;
            }
        }
        if (measurements == null) {
            measurements = mMeasurements;
        } else {
            if (start <= measurementSize) {
                measurements.addAll(mMeasurements.subList(start, measurementSize));
            }
        }
        //将虚拟测量量在显示测量量中的位置排于实际测量量之后
        int virtualMeasurementSize = getVirtualMeasurementSize(measurements, true);
        if (virtualMeasurementSize > 0) {
            int displayMeasurementSize = measurements.size();
            mDisplayMeasurements = new ArrayList<>(displayMeasurementSize);
            mDisplayMeasurements.addAll(measurements.subList(virtualMeasurementSize, displayMeasurementSize));
            mDisplayMeasurements.addAll(measurements.subList(0, virtualMeasurementSize));
        } else {
            mDisplayMeasurements = measurements;
        }
    }

    public Type getType() {
        return mType;
    }

    public boolean isUnknown() {
        return mType instanceof UnknownType;
    }

    //一般情况下传感器所拥有的测量量由配置文件读取，
    //对于未配置的传感器，可以采用该方法动态添加测量量
    //返回新添加的测量量
    private PracticalMeasurement addUnconfiguredPracticalMeasurement(int position,
                                                                     byte dataTypeValue,
                                                                     int dataTypeValueIndex) {
        PracticalMeasurement newMeasurement = SensorManager.getPracticalMeasurement(getRawAddress(), dataTypeValue, dataTypeValueIndex);
        synchronized (mMeasurements) {
            mMeasurements.add(position, newMeasurement);
            if (!newMeasurement.isHidden() && getMeasurementSize() != getDisplayMeasurementSize()) {
                generateDisplayMeasurements();
            }
        }
        return newMeasurement;
    }

    public int getRawAddress() {
        return getId().getAddress();
    }

    public String getFormatAddress() {
        return getId().getFormatAddress();
    }

    public DisplayMeasurement getVirtualMeasurementByIndex(int index) {
        int position = getDisplayMeasurementPosition((byte) 0, index);
        DisplayMeasurement measurement = getMeasurementByPositionSafely(position);
        return measurement != null && measurement.getId().isVirtualMeasurement()
                ? measurement
                : null;
    }

    public PracticalMeasurement getPracticalMeasurementByDataType(byte dataTypeValue) {
        return getPracticalMeasurementByDataType(dataTypeValue, 0);
    }

    public PracticalMeasurement getPracticalMeasurementByDataType(byte dataTypeValue, int dataTypeValueIndex) {
        if (dataTypeValue == 0) {
            return null;
        }
        DisplayMeasurement measurement = getMeasurementByPositionSafely(getDisplayMeasurementPosition(dataTypeValue, dataTypeValueIndex));
        return measurement != null ? (PracticalMeasurement) measurement : null;
    }

    public DisplayMeasurement getMeasurementByPosition(int position) {
        return mMeasurements.get(position);
    }

    public DisplayMeasurement getMeasurementByPositionSafely(int position) {
        if (position < 0 || position >= mMeasurements.size()) {
            return null;
        }
        return getMeasurementByPosition(position);
    }

    public DisplayMeasurement getDisplayMeasurementByPosition(int position) {
        return mDisplayMeasurements.get(position);
    }

    public DisplayMeasurement getDisplayMeasurementByPositionSafely(int position) {
        if (position < 0 || position >= mDisplayMeasurements.size()) {
            return null;
        }
        return getDisplayMeasurementByPosition(position);
    }

    public int getMeasurementSize() {
        return mMeasurements.size();
    }

    public int getDisplayMeasurementSize() {
        return mDisplayMeasurements.size();
    }

    public int getPracticalMeasurementSize() {
        return getMeasurementSize() - getVirtualMeasurementSize(true);
    }

    public int getVirtualMeasurementSize(boolean containHidden) {
        return getVirtualMeasurementSize(mMeasurements, containHidden);
    }

    private int getVirtualMeasurementSize(@NonNull List<DisplayMeasurement> measurements, boolean containHidden) {
        int count = 0;
        DisplayMeasurement measurement;
        for (int i = 0, size = measurements.size();i < size;++i) {
            measurement = measurements.get(i);
            if (!measurement.getId().isVirtualMeasurement()) {
                break;
            }
            if (containHidden || !measurement.isHidden()) {
                ++count;
            }
        }
        return count;
    }

    public boolean hasVirtualMeasurement() {
        return getVirtualMeasurementSize(true) != 0;
    }

    private int getDisplayMeasurementPosition(byte dataTypeValue, int dataTypeValueIndex) {
        synchronized (mMeasurements) {
            int position, size = mMeasurements.size();
            if (size > MEASUREMENT_SEARCH_THRESHOLD) {
                return ExpandCollections.binarySearch(mMeasurements,
                        ID.getId(mInfo.getId().getAddress(), dataTypeValue, dataTypeValueIndex),
                        MEASUREMENT_SEARCH_HELPER);
            } else {
                ID currentId;
                int currentValue, targetValue = dataTypeValue & 0xff;
                int currentValueIndex, targetValueIndex = dataTypeValueIndex;
                for (position = 0;position < size;++position) {
                    currentId = mMeasurements.get(position).getId();
                    currentValue = currentId.getDataTypeAbsValue();
                    if (currentValue == targetValue) {
                        currentValueIndex = currentId.getDataTypeValueIndex();
                        if (currentValueIndex == targetValueIndex) {
                            return position;
                        } else if (targetValueIndex < currentValueIndex) {
                            break;
                        }
                    } else if (targetValue < currentValue) {
                        break;
                    }
                }
                return -(position + 1);
            }
        }
    }

    private PracticalMeasurement getPracticalMeasurementWithAutoCreate(byte dataTypeValue, int dataTypeValueIndex) {
        if (dataTypeValue == 0) {
            return null;
        }
        int position = getDisplayMeasurementPosition(dataTypeValue, dataTypeValueIndex);
        PracticalMeasurement measurement;
        if (position >= 0 && position < mMeasurements.size()) {
            measurement = (PracticalMeasurement) mMeasurements.get(position);
        } else if (position < 0) {
            measurement = addUnconfiguredPracticalMeasurement(- position - 1, dataTypeValue, dataTypeValueIndex);
        } else {
            return null;
        }
        return measurement;
    }

    public int addDynamicValue(byte dataTypeValue,
                               int dataTypeValueIndex,
                               long timestamp,
                               float batteryVoltage,
                               double rawValue) {
        //获取相应测量量
        PracticalMeasurement measurement = getPracticalMeasurementWithAutoCreate(dataTypeValue, dataTypeValueIndex);
        if (measurement == null) {
            return ValueContainer.ADD_FAILED_RETURN_VALUE;
        }
        //修正时间戳
        long correctedTimestamp = correctTimestamp(timestamp);
        //将传感器实时数据添加至实时数据缓存
        int result = mInfo.addDynamicValue(correctedTimestamp, batteryVoltage);
        //修正原始数据
        double correctedValue = measurement.correctRawValue(rawValue);
        //为测量量添加动态数据（包括实时数据及其缓存）
        measurement.addDynamicValue(correctedTimestamp, correctedValue);
        notifyDynamicValueCaptured(dataTypeValue, dataTypeValueIndex, batteryVoltage, correctedTimestamp, correctedValue);
        return result;
    }

    public int addHistoryValue(byte dataTypeValue, int dataTypeValueIndex, long timestamp, float batteryVoltage, double rawValue) {
        return addInfoHistoryValue(timestamp, batteryVoltage);
    }

    public int addMeasurementHistoryValue(byte dataTypeValue, int dataTypeValueIndex, long timestamp, double rawValue) {
        PracticalMeasurement measurement = getPracticalMeasurementWithAutoCreate(dataTypeValue, dataTypeValueIndex);
        if (measurement != null) {
            return measurement.addHistoryValue(timestamp, rawValue);
        } else {
            return ValueContainer.ADD_FAILED_RETURN_VALUE;
        }
    }

    @Override
    public Sensor.Info getMainMeasurement() {
        return mInfo;
    }

    @Override
    public void resetConfiguration() {
        super.resetConfiguration();
        for (int i = 0, size = getDisplayMeasurementSize();i < size;++i) {
            getDisplayMeasurementByPosition(i).resetConfiguration();
        }
    }

    /**
     * Created by CJQ on 2017/6/16.
     */

    public static class Type {

        String mSensorGeneralName;
        int mStartAddress;
        int mEndAddress;
        PracticalMeasurementParameter[] mPracticalMeasurementParameters;
        List<VirtualMeasurementParameter> mVirtualMeasurementParameters;

        Type() {
        }

        public static abstract class MeasurementParameter {
            final boolean mHideMeasurement;

            protected MeasurementParameter(boolean hideMeasurement) {
                mHideMeasurement = hideMeasurement;
            }
        }

        public static class PracticalMeasurementParameter extends MeasurementParameter {

            final String mDataTypeAccurateName;
            final PracticalMeasurement.DataType mInvolvedDataType;
            PracticalMeasurementParameter mNext;

            public PracticalMeasurementParameter(PracticalMeasurement.DataType involvedDataType,
                                                 String dataTypeAccurateName,
                                                 boolean hideMeasurement) {
                super(hideMeasurement);
                mDataTypeAccurateName = dataTypeAccurateName;
                mInvolvedDataType = involvedDataType;
            }

            public PracticalMeasurementParameter getLast() {
                PracticalMeasurementParameter result = this;
                while (result.mNext != null) {
                    result = result.mNext;
                }
                return result;
            }
        }

        public static class VirtualMeasurementParameter extends MeasurementParameter {

            final String mMeasurementName;
            final String mMeasurementType;
            final ValueInterpreter mValueInterpreter;

            public VirtualMeasurementParameter(String measurementName,
                                               String measurementType,
                                               ValueInterpreter valueInterpreter,
                                               boolean hideMeasurement) {
                super(hideMeasurement);
                mMeasurementName = measurementName;
                mMeasurementType = measurementType;
                mValueInterpreter = valueInterpreter;
            }
        }

        public String getSensorGeneralName() {
            return mSensorGeneralName;
        }

        public int getStartAddress() {
            return mStartAddress;
        }

        public int getEndAddress() {
            return mEndAddress;
        }

        public PracticalMeasurementParameter[] getPracticalMeasurementParameters() {
            return mPracticalMeasurementParameters;
        }

        public int getPracticalMeasurementParameterSize() {
            int count = 0;
            int size = mPracticalMeasurementParameters.length;
            PracticalMeasurementParameter parameter;
            for (int i = 0;i < size;++i) {
                parameter = mPracticalMeasurementParameters[i];
                do {
                    ++count;
                    parameter = parameter.mNext;
                } while (parameter != null);
            }
            return count;
        }

        public int getVirtualMeasurementParameterSize() {
            return mVirtualMeasurementParameters != null
                    ? mVirtualMeasurementParameters.size()
                    : 0;
        }

        public int getMeasurementParameterSize() {
            return getPracticalMeasurementParameterSize()
                    + getVirtualMeasurementParameterSize();
        }
    }

    private static class UnknownType extends Type {

        private static final UnknownType SINGLETON;
        static {
            SINGLETON = new UnknownType();
            SINGLETON.mSensorGeneralName = "未知传感器";
        }
    }
}
