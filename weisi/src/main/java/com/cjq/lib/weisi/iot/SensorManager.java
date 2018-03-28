package com.cjq.lib.weisi.iot;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cjq.lib.weisi.protocol.EsbAnalyzer;
import com.cjq.lib.weisi.util.ExpandCollections;
import com.cjq.lib.weisi.util.ExpandComparator;
import com.cjq.lib.weisi.util.SimpleReflection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * Created by CJQ on 2017/8/31.
 */

public class SensorManager {

    private static final Map<Long, Sensor> SENSOR_MAP = new HashMap<>();
    private static final Map<Byte, LogicalSensor.DataType> BLE_DATA_TYPES = new HashMap<>();
    private static final Map<Byte, LogicalSensor.DataType> ESB_DATA_TYPES = new HashMap<>();
    private static final List<PhysicalSensor.Type> BLE_SENSOR_TYPES = new ArrayList<>();
    private static final List<PhysicalSensor.Type> ESB_SENSOR_TYPES = new ArrayList<>();
    private static SensorConfigurationProvider configurationProvider;

    private static final ExpandComparator<PhysicalSensor.Type, Integer> SENSOR_TYPE_SEARCH_COMPARATOR = new ExpandComparator<PhysicalSensor.Type, Integer>() {
        @Override
        public int compare(PhysicalSensor.Type type, Integer targetAddress) {
            if (type.mEndAddress < targetAddress) {
                return -1;
            }
            if (type.mStartAddress > targetAddress) {
                return 1;
            }
            return 0;
        }
    };

    private SensorManager() {
    }

    public static synchronized Sensor getSensor(@NonNull Sensor.ID id, boolean autoCreate) {
        Sensor sensor = SENSOR_MAP.get(id);
        if (sensor == null && autoCreate) {
            sensor = createSensor(id);
        }
        return sensor;
    }

    private static Sensor createSensor(long id) {
        return createSensor(new Sensor.ID(id));
    }

    private static Sensor createSensor(@NonNull Sensor.ID id) {
        Sensor sensor;
        if (id.isLogical()) {
            sensor = createLogicalSensor(id);
        } else {
            sensor = createPhysicalSensor(id);
        }
        return sensor;
    }

    public static synchronized Sensor getSensor(long id, boolean autoCreate) {
        Sensor sensor = SENSOR_MAP.get(id);
        if (sensor == null && autoCreate) {
            sensor = createSensor(id);
        }
        return sensor;
    }

    static LogicalSensor getLogicalSensor(int address,
                                          int dataTypeValueIndex,
                                          @NonNull PhysicalSensor.Type.MeasureParameter parameter) {
        return getLogicalSensor(address, parameter.mInvolvedDataType.mValue, dataTypeValueIndex, parameter);
    }

    static LogicalSensor getLogicalSensor(int address,
                                          byte dataType,
                                          int dataTypeValueIndex) {
        return getLogicalSensor(address, dataType, dataTypeValueIndex, null);
    }

    private static synchronized LogicalSensor getLogicalSensor(
            int address, byte dataTypeValue, int dataTypeValueIndex,
            @Nullable PhysicalSensor.Type.MeasureParameter parameter) {
        long id = Sensor.ID.getId(address, dataTypeValue, dataTypeValueIndex);
        LogicalSensor sensor = (LogicalSensor) SENSOR_MAP.get(id);
        if (sensor == null) {
            sensor = createLogicalSensor(new Sensor.ID(id), parameter);
        }
        return sensor;
    }

    public static synchronized LogicalSensor getLogicalSensor(
            long id, boolean autoCreate) {
        LogicalSensor sensor = (LogicalSensor) SENSOR_MAP.get(id);
        if (sensor == null && autoCreate) {
            sensor = createLogicalSensor(id);
        }
        return sensor;
    }

    public static synchronized LogicalSensor getLogicalSensor(
            @NonNull Sensor.ID id, boolean autoCreate) {
        LogicalSensor sensor = (LogicalSensor) SENSOR_MAP.get(id);
        if (sensor == null && autoCreate) {
            sensor = createLogicalSensor(id);
        }
        return sensor;
    }

    private static LogicalSensor createLogicalSensor(long id) {
        return createLogicalSensor(new Sensor.ID(id));
    }

    private static LogicalSensor createLogicalSensor(@NonNull Sensor.ID id) {
        //生成逻辑传感器（即之前的测量量Measurement）
        PhysicalSensor.Type type = findSensorType(id.getAddress());
        PhysicalSensor.Type.MeasureParameter parameter = null;
        if (type != null) {
            for (int i = 0;i < type.mMeasureParameters.length;++i) {
                parameter = type.mMeasureParameters[i];
                if (parameter.mInvolvedDataType.mValue == id.getDataTypeValue()) {
                    for (int j = 0;j < id.getDataTypeValueIndex() && parameter != null;++j) {
                        parameter = parameter.mNext;
                    }
                    break;
                }
            }
        }
        return createLogicalSensor(id, parameter);
    }

    private static synchronized LogicalSensor createLogicalSensor(
            @NonNull Sensor.ID id,
            @Nullable PhysicalSensor.Type.MeasureParameter parameter) {
        LogicalSensor sensor;
        if (parameter != null) {
            sensor = new LogicalSensor(id,
                    parameter.mInvolvedDataType,
                    parameter.mDataTypeAccurateName);
        } else {
            sensor = new LogicalSensor(id, getDataType(id.getAddress(),
                    id.getDataTypeValue(), true));
        }
        putSensor(sensor);
        return sensor;
    }

    public static synchronized PhysicalSensor getPhysicalSensor(int address, boolean autoCreate) {
        PhysicalSensor sensor = (PhysicalSensor) SENSOR_MAP.get(Sensor.ID.getId(address));
        if (sensor == null && autoCreate) {
            sensor = createPhysicalSensor(address);
        }
        return sensor;
    }

    private static synchronized PhysicalSensor createPhysicalSensor(int address) {
        return createPhysicalSensor(new Sensor.ID(address));
    }

    private static synchronized PhysicalSensor createPhysicalSensor(@NonNull Sensor.ID id) {
        PhysicalSensor sensor = new PhysicalSensor(id);
        putSensor(sensor);
        return sensor;
    }

    private static synchronized void putSensor(@NonNull Sensor sensor) {
        SENSOR_MAP.put(sensor.getId().getId(), sensor);
    }

//    public static synchronized <S extends Sensor> List<S> getSensors(Class<S> sClass) {
//        if (sClass == Sensor.class) {
//            return (List<S>) new ArrayList<>(SENSOR_MAP.values());
//        }
//        List<S> sensors = new ArrayList<>();
//        for (Sensor sensor :
//                SENSOR_MAP.values()) {
//            if (sClass.isInstance(sensor)) {
//                sensors.add((S) sensor);
//            }
//        }
//        return sensors;
//    }

    public static synchronized <S extends Sensor> void getSensors(
            @NonNull List<S> sensorCarrier,
            Sensor.Filter<S> filter,
            @NonNull Class<S> sClass) {
        S s;
        for (Sensor sensor :
                SENSOR_MAP.values()) {
            if (sClass.isInstance(sensor)) {
                s = (S) sensor;
                if (filter == null || filter.isMatch(s)) {
                    sensorCarrier.add(s);
                }
            }
        }
    }

//    public static synchronized void getPhysicalSensors(@NonNull List<PhysicalSensor> sensorCarrier,
//                                                       Sensor.Filter<PhysicalSensor> filter) {
//        for (Sensor sensor :
//                SENSOR_MAP.values()) {
//            if (sensor instanceof PhysicalSensor
//                    && (filter == null || filter.isMatch((PhysicalSensor) sensor))) {
//                sensorCarrier.add((PhysicalSensor) sensor);
//            }
//        }
//    }
//
//    public static synchronized void getLogicalSensors(
//            @NonNull List<LogicalSensor> sensorCarrier,
//            Sensor.Filter<LogicalSensor> filter) {
//        for (Sensor sensor :
//                SENSOR_MAP.values()) {
//            if (sensor instanceof LogicalSensor
//                    && (filter == null || filter.isMatch((LogicalSensor) sensor))) {
//                sensorCarrier.add((LogicalSensor) sensor);
//            }
//        }
//    }

    public static synchronized <S extends Sensor> int getSensorWithHistoryValuesCount(Class<S> sClass) {
        int count = 0;
        for (Sensor sensor :
                SENSOR_MAP.values()) {
            if (sClass.isInstance(sensor) && sensor.hasHistoryValue()) {
                ++count;
            }
        }
        return count;
    }

    public static boolean importBleConfiguration(Context context) {
        return importConfiguration(BLE_DATA_TYPES,
                BLE_SENSOR_TYPES,
                context,
                "BleSensorConfiguration.xml");
    }

    public static boolean importEsbConfiguration(Context context) {
        return importConfiguration(ESB_DATA_TYPES,
                ESB_SENSOR_TYPES,
                context,
                "EsbSensorConfiguration.xml");
    }

    private static boolean importConfiguration(Map<Byte, LogicalSensor.DataType> dataTypes,
                                               List<PhysicalSensor.Type> types,
                                               Context context,
                                               String configFileName) {
        ConfigurationImporter importer = getConfigurationImporter(context, configFileName);
        if (importer == null) {
            return false;
        }
        dataTypes.clear();
        types.clear();
        dataTypes.putAll(importer.getDataTypeMap());
        types.addAll(importer.getTypes());
        return true;
    }

    public static PhysicalSensor.Type findSensorType(int address) {
        List<PhysicalSensor.Type> types = getSensorTypes(address);
        if (types == null)
            return null;
        int position = ExpandCollections.binarySearch(types,
                address,
                SENSOR_TYPE_SEARCH_COMPARATOR);
        return position >= 0 ? types.get(position) : null;
    }

    public static LogicalSensor.DataType getDataType(int address, byte dataTypeValue, boolean autoCreate) {
        Map<Byte, LogicalSensor.DataType> dataTypeMap = getDataTypes(address);
        LogicalSensor.DataType dataType = dataTypeMap.get(dataTypeValue);
        if (autoCreate && dataType == null) {
            dataType = new LogicalSensor.DataType(dataTypeValue);
            dataTypeMap.put(dataTypeValue, dataType);
        }
        return dataType;
    }

    public static List<PhysicalSensor.Type> getBleSensorTypes() {
        return Collections.unmodifiableList(BLE_SENSOR_TYPES);
    }

    public static List<PhysicalSensor.Type> getEsbSensorTypes() {
        return Collections.unmodifiableList(ESB_SENSOR_TYPES);
    }

    private static List<PhysicalSensor.Type> getSensorTypes(int address) {
        return Sensor.ID.isBleProtocolFamily(address) ? BLE_SENSOR_TYPES : ESB_SENSOR_TYPES;
    }

    private static Map<Byte, LogicalSensor.DataType> getDataTypes(int address) {
        return Sensor.ID.isBleProtocolFamily(address) ? BLE_DATA_TYPES : ESB_DATA_TYPES;
    }

    private static ConfigurationImporter getConfigurationImporter(Context context, String fileName) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ConfigurationImporter importer = new ConfigurationImporter();
            parser.parse(context.getAssets().open(fileName), importer);
            return importer;
        } catch (Exception e) {
            return null;
        }
    }

    public static synchronized void setValueContainerConfigurationProvider(SensorConfigurationProvider provider, boolean isResetConfigurations) {
        if (configurationProvider != provider) {
            configurationProvider = provider;
            if (isResetConfigurations) {
                for (Sensor sensor
                        : SENSOR_MAP.values()) {
                    sensor.resetConfiguration();
                }
            }
        }
    }

    static SensorConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    public interface SensorConfigurationProvider {
        <C extends Sensor.Configuration> C getSensorConfiguration(Sensor.ID id);
//        PhysicalSensor.Configuration getPhysicalSensorConfiguration(Sensor.ID id);
//        LogicalSensor.Configuration getLogicalConfiguration(Sensor.ID id);
    }

    private static class ConfigurationImporter extends DefaultHandler {

        private static final String DATA_TYPE = "DataType";
        private static final String PARAPHRASES = "paraphrases";
        private static final String SENSOR_TYPE = "SensorType";
        private static final String DATA_TYPE_CUSTOM_NAME = "DataTypeCustomName";

        private Map<Byte, LogicalSensor.DataType> mDataTypeMap;
        private LogicalSensor.DataType mDataType;
        private StringBuilder mBuilder;
        private Map<Double, String> mParaphrases;
        private Double mNumber;
        private String mText;
        private String mOn;
        private String mOff;
        private List<PhysicalSensor.Type> mTypes;
        private PhysicalSensor.Type mType;
        private List<PhysicalSensor.Type.MeasureParameter> mMeasureParameters;
        private PhysicalSensor.Type.MeasureParameter mMeasureParameter;
        private int mIndex;
        private byte mDataTypeValue;
        private String mDataTypeCustomName;
        private int mCustomDataTypeNameType;
        private int mValueType = -1;
        private boolean mSigned;
        private double mCoefficient;
        private ScriptValueCorrector.Builder mScriptValueCorrectorBuilder;
        private String mLabel;

        public Map<Byte, LogicalSensor.DataType> getDataTypeMap() {
            return mDataTypeMap;
        }

        public List<PhysicalSensor.Type> getTypes() {
            return mTypes;
        }

        @Override
        public void startDocument() throws SAXException {
            mBuilder = new StringBuilder();
            mDataTypeMap = new HashMap<>();
            mTypes = new ArrayList<>();
            mMeasureParameters = new ArrayList<>();
            mScriptValueCorrectorBuilder = new ScriptValueCorrector.Builder();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.equals(SENSOR_TYPE)) {
                mType = new PhysicalSensor.Type();
            } else if (localName.equals(PARAPHRASES)) {
                mParaphrases = new HashMap<>();
            } else if (localName.equals(DATA_TYPE_CUSTOM_NAME)) {
                mCustomDataTypeNameType = Integer.parseInt(attributes.getValue("type"));
            }
            mBuilder.setLength(0);
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            mBuilder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            switch (localName) {
                case "value":
                    mDataType = new LogicalSensor.DataType((byte)Integer.parseInt(mBuilder.toString(), 16));
                    break;
                case "name":
                    mDataType.mName = mBuilder.toString();
                    break;
                case "decimal":
                    mDataType.mInterpreter = new FloatInterpreter(Integer.parseInt(mBuilder.toString()));
                    break;
                case "unit":
                    mDataType.mUnit = mBuilder.toString();
                    break;
                case "type":
                    mValueType = Integer.parseInt(mBuilder.toString());
                    break;
                case "signed":
                    mSigned = Boolean.parseBoolean(mBuilder.toString());
                    break;
                case "coefficient":
                    mCoefficient = Double.parseDouble(mBuilder.toString());
                    break;
                case DATA_TYPE:
                    //根据mValueType为DataType配备不同的ValueBuilder
                    if (mValueType != -1) {
                        EsbAnalyzer.setValueBuilder(mDataType.mValue, mValueType, mSigned, mCoefficient);
                        mValueType = -1;
                        mCoefficient = 1;
                    }
                    mDataTypeMap.put(mDataType.mValue, mDataType);
                    break;
                case "SensorName":
                    mType.mSensorGeneralName = mBuilder.toString();
                    break;
                case "start":
                    mType.mStartAddress = Integer.parseInt(mBuilder.toString(), 16);
                    break;
                case "end":
                    mType.mEndAddress = Integer.parseInt(mBuilder.toString(), 16);
                    break;
                case "DataTypeValue":
                    mDataTypeValue = (byte)Integer.parseInt(mBuilder.toString(), 16);
                    break;
                case DATA_TYPE_CUSTOM_NAME:
                    mDataTypeCustomName = mBuilder.toString();
                    break;
                case "measurement":
                    //获取数据类型
                    mDataType = mDataTypeMap.get(mDataTypeValue);
                    if (mDataType == null) {
                        mDataType = new LogicalSensor.DataType(mDataTypeValue);
                        mDataTypeMap.put(mDataTypeValue, mDataType);
                    }
                    //生成测量参数
                    mMeasureParameter = new PhysicalSensor.Type.MeasureParameter(mDataType,
                            mDataTypeCustomName != null
                                    ? (mCustomDataTypeNameType == 0
                                        ? mDataType.getName() + mDataTypeCustomName
                                        : mDataTypeCustomName)
                                    : null);
                    mDataTypeCustomName = null;
                    //若存在相同数据类型，则为阵列传感器，使用链式附加，否则按数据类型升序排列
                    mIndex = findMeasureParameter(mMeasureParameters, mMeasureParameter);
                    if (mIndex >= 0) {
                        mMeasureParameters.get(mIndex).getLast().mNext = mMeasureParameter;
                    } else {
                        mMeasureParameters.add(-mIndex-1, mMeasureParameter);
                    }
                    break;
                case "measurements":
                    mType.mMeasureParameters = new PhysicalSensor.Type.MeasureParameter[mMeasureParameters.size()];
                    mMeasureParameters.toArray(mType.mMeasureParameters);
                    mMeasureParameters.clear();
                    break;
                case SENSOR_TYPE:
                    mTypes.add(mType);
                    break;
                case "on":
                    mOn = mBuilder.toString();
                    break;
                case "off":
                    mOff = mBuilder.toString();
                    break;
                case "status":
                    mDataType.mInterpreter = new StatusInterpreter(mOn, mOff);
                    break;
                case "number":
                    mNumber = Double.parseDouble(mBuilder.toString());
                    break;
                case "text":
                    mText = mBuilder.toString();
                    break;
                case "paraphrase":
                    mParaphrases.put(mNumber, mText);
                    break;
                case PARAPHRASES:
                    mDataType.mInterpreter = new ParaphraseInterpreter(mParaphrases);
                    break;
                case "calendar":
                    mDataType.mInterpreter = CalendarInterpreter.from(mBuilder.toString());
                    break;
                case "interpreter":
                    switch (mBuilder.toString()) {
                        case "ground":mDataType.mInterpreter = GroundLeadInterpreter.getInstance();
                            break;
                    }
                    break;
                case "SensorTypes":
                    Collections.sort(mTypes, new Comparator<PhysicalSensor.Type>() {
                        @Override
                        public int compare(PhysicalSensor.Type c1, PhysicalSensor.Type c2) {
                            return c1.mStartAddress - c2.mStartAddress;
                        }
                    });
                    break;
                case "label":
                    mLabel = mBuilder.toString();
                    break;
                case "function":
                    mScriptValueCorrectorBuilder.putScript(mLabel, mBuilder.toString());
                    break;
                case "ScriptValueCorrectorLabel":
                    mDataType.mCorrector = mScriptValueCorrectorBuilder.getCorrector(mBuilder.toString());
                    break;
                default:
                    break;
            }
        }

        private int findMeasureParameter(
                List<PhysicalSensor.Type.MeasureParameter> measureParameters,
                PhysicalSensor.Type.MeasureParameter parameterGetter) {
            int index, size = measureParameters.size();
            final int threshold = 3;
            if (size > threshold) {
                index = Collections.binarySearch(measureParameters,
                        parameterGetter,
                        MEASURE_PARAMETER_COMPARATOR);
            } else {
                byte dataTypeValue = parameterGetter.mInvolvedDataType.mValue;
                for (index = 0;index < size;++index) {
                    if (measureParameters.get(index).mInvolvedDataType.mValue == dataTypeValue) {
                        break;
                    }
                }
                if (index == size) {
                    index = -(index + 1);
                }
            }
            return index;
        }

        private static final Comparator<PhysicalSensor.Type.MeasureParameter> MEASURE_PARAMETER_COMPARATOR = new Comparator<PhysicalSensor.Type.MeasureParameter>() {
            @Override
            public int compare(PhysicalSensor.Type.MeasureParameter mp1, PhysicalSensor.Type.MeasureParameter mp2) {
                return mp1.mInvolvedDataType.mValue - mp2.mInvolvedDataType.mValue;
            }
        };
    }
}
