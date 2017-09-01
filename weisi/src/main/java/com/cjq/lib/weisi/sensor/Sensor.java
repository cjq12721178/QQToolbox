package com.cjq.lib.weisi.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by CJQ on 2017/6/16.
 */

public class Sensor implements OnRawAddressComparer {

    private static final int MEASUREMENT_SEARCH_THRESHOLD = 3;

    private String mName;
    private final int mRawAddress;
    private final String mFormatAddress;
    private final List<Measurement> mMeasurements;
    private final List<Measurement> mUnmodifiableMeasurements;
    private SensorDecorator mDecorator;

    Sensor(int address) {
        this(address, null);
    }

    Sensor(int address, SensorDecorator decorator) {
        //设置地址
        mRawAddress = address & 0xffffff;
        mFormatAddress = ConfigurationManager.isBleSensor(address)
                ? String.format("%06X", mRawAddress)
                : String.format("%04X", mRawAddress);
        //根据配置生成测量参数列表
        Configuration configuration = ConfigurationManager.findConfiguration(mRawAddress);
        if (configuration != null) {
            mName = configuration.getSensorGeneralName();
            mMeasurements = new ArrayList<>(configuration.mMeasureParameters.length);
            for (Configuration.MeasureParameter parameter :
                    configuration.mMeasureParameters) {
                mMeasurements.add(new Measurement(parameter));
            }
        } else {
            mName = "未知传感器";
            mMeasurements = new ArrayList<>();
        }
        mUnmodifiableMeasurements = Collections.unmodifiableList(mMeasurements);
        setDecorator(decorator);
    }

    public void setDecorator(SensorDecorator decorator) {
        if (decorator == mDecorator) {
            return;
        }
        mDecorator = decorator;
        if (decorator == null) {
            for (Measurement measurement :
                    mMeasurements) {
                measurement.setDecorator(null);
            }
        } else {
            Iterator<Measurement> measurementIterator = mMeasurements.iterator();
            Measurement measurement = measurementIterator.next();
            MeasurementDecorator[] measurementDecorators = decorator.getMeasurementDecorators();
            for (int measurementDecoratorIndex = 0;
                 measurementDecoratorIndex < measurementDecorators.length
                    && measurement != null;) {
                if (measurement.getDataType().mValue
                        == measurementDecorators[measurementDecoratorIndex].getDataTypeValue()) {
                    measurement.setDecorator(measurementDecorators[measurementDecoratorIndex]);
                    measurement = measurement.getNextSameDataTypeMeasurement();
                    if (measurement == null) {
                        measurement = measurementIterator.next();
                    }
                    ++measurementDecoratorIndex;
                } else {
                    measurement = measurementIterator.next();
                }
            }
        }
    }

    //一般情况下传感器所拥有的测量量由配置文件读取，
    //对于未配置的传感器，可以采用该方法动态添加测量量
    //注：传感器阵列测量量需要依次添加
    //返回新添加的测量量
    public Measurement addMeasurement(byte dataTypeValue, MeasurementDecorator decorator) {
        int position = getMeasurementPosition(dataTypeValue);
        Measurement newMeasurement = new Measurement(
                ConfigurationManager.getDataType(mRawAddress, dataTypeValue),
                decorator);
        if (position >= 0) {
            mMeasurements.get(position)
                    .getLastSameDataTypeMeasurement()
                    .setSameDataTypeMeasurement(newMeasurement);
        } else {
            mMeasurements.add(-position - 1, newMeasurement);
        }
        return newMeasurement;
    }

    //返回传感器通用名称
    public String getGeneralName() {
        return mName;
    }

    //返回传感器名称（可以经过SensorDecorator修饰）
    public String getName() {
        return mDecorator != null ? mDecorator.getName() : mName;
    }

    @Override
    public int getRawAddress() {
        return mRawAddress;
    }

    public String getFormatAddress() {
        return mFormatAddress;
    }

    public Measurement getMeasurementByDataTypeValue(byte dataTypeValue) {
        return getMeasurementByDataTypeValue(dataTypeValue, 0);
    }

    public Measurement getMeasurementByDataTypeValue(byte dataTypeValue, int index) {
        return getMeasurementByPosition(getMeasurementPosition(dataTypeValue), index);
    }

    public Measurement getMeasurementByPosition(int position) {
        return getMeasurementByPosition(position, 0);
    }

    public Measurement getMeasurementByPosition(int position, int index) {
        if (position >= 0 && position < mMeasurements.size()) {
            Measurement result = mMeasurements.get(position);
            for (int i = 0;
                 i < index && (result = result.getNextSameDataTypeMeasurement()) != null;
                 ++i);
            return result;
        } else {
            return null;
        }
    }

    public int getMeasurementPosition(byte dataTypeValue) {
        int position;
        if (mMeasurements.size() > MEASUREMENT_SEARCH_THRESHOLD) {
            MODIFIABLE_DATA_TYPE.setValue(dataTypeValue);
            position = Collections.binarySearch(mMeasurements,
                    MEASUREMENT_GET_COMPARER,
                    MEASUREMENT_GET_COMPARATOR);
        } else {
            for (position = 0;position < MEASUREMENT_SEARCH_THRESHOLD;++position) {
                if (mMeasurements.get(position).getDataType().getValue() == dataTypeValue) {
                    break;
                }
            }
            if (position == MEASUREMENT_SEARCH_THRESHOLD) {
                position = -(position + 1);
            }
        }
        return position;
    }

    public List<Measurement> getMeasurements() {
        return mUnmodifiableMeasurements;
    }

    public int getMeasurementSize() {
        int size = 0;
        for (Measurement measurement :
                mMeasurements) {
            do {
                ++size;
            } while ((measurement = measurement.getNextSameDataTypeMeasurement()) != null);
        }
        return size;
    }

    private static final Comparator<Measurement> MEASUREMENT_GET_COMPARATOR = new Comparator<Measurement>() {
        @Override
        public int compare(Measurement m1, Measurement m2) {
            return m1.getDataType().getValue() - m2.getDataType().getValue();
        }
    };

    private static final ModifiableDataType MODIFIABLE_DATA_TYPE = new ModifiableDataType((byte)0);
    private static final Measurement MEASUREMENT_GET_COMPARER = new Measurement(MODIFIABLE_DATA_TYPE);

    private static class ModifiableDataType extends DataType {

        private byte mModifiableValue;

        public ModifiableDataType(byte value) {
            super(value);
        }

        @Override
        public byte getValue() {
            return mModifiableValue;
        }

        public void setValue(byte value) {
            mModifiableValue = value;
        }
    }
}