package com.cjq.lib.weisi.sensor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by CJQ on 2017/6/16.
 */

public class Sensor implements OnRawAddressComparer {

    private static final int MEASUREMENT_SEARCH_THRESHOLD = 3;
    private static final int BLE_ADDRESS_LENGTH = 6;
    private String mName;
    private final int mRawAddress;
    private final List<Measurement> mMeasurements;
    private final List<Measurement> mUnmodifiableMeasurements;

    public Sensor(int address) {
        this(address, null);
    }

    public Sensor(int address, String name) {
        mRawAddress = address & 0xffffff;
        Configuration configuration = ConfigurationManager.findConfiguration(isBle(), mRawAddress);
        if (configuration != null) {
            mName = name != null ? name : configuration.getSensorGeneralName();
            int size = configuration.mMeasureParameters.length;
            mMeasurements = new ArrayList<>(size);
            for (int i = 0;i < size;++i) {
                mMeasurements.add(new Measurement(configuration.mMeasureParameters[i].mInvolvedDataType,
                        configuration.mMeasureParameters[i].mDataTypeAccurateName));
            }
        } else {
            mName = name != null ? name : "未知传感器";
            mMeasurements = new ArrayList<>();
        }
        mUnmodifiableMeasurements = Collections.unmodifiableList(mMeasurements);
    }

    boolean isBle() {
        return (mRawAddress & 0xff0000) != 0;
    }

    //一般情况下传感器所拥有的测量量由配置文件读取，
    //对于未配置的传感器，可以采用该方法动态添加测量量
    //注：传感器阵列测量量需要依次添加
    //返回新添加的测量量
    public Measurement addMeasurement(byte dataTypeValue, String name) {
        int position = getMeasurementPosition(dataTypeValue);
        Measurement newMeasurement = new Measurement(ConfigurationManager.
                getDataType(isBle(), dataTypeValue), name);
        if (position >= 0) {
            mMeasurements.get(position)
                    .getLastSameDataTypeMeasurement()
                    .setSameDataTypeMeasurement(newMeasurement);
        } else {
            mMeasurements.add(-position - 1, newMeasurement);
        }
        return newMeasurement;
    }

    public String getName() {
        return mName;
    }

    @Override
    public int getRawAddress() {
        return mRawAddress;
    }

    public String getAddress() {
        if (isBle()) {
            return String.format("%06X", mRawAddress);
        } else {
            return String.format("%04X", mRawAddress);
        }
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
