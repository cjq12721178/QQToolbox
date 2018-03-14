package com.cjq.lib.weisi.node;

import android.content.Context;

import com.cjq.lib.weisi.protocol.EsbAnalyzer;
import com.cjq.lib.weisi.util.ExpandCollections;
import com.cjq.lib.weisi.util.ExpandComparator;

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

    private static final int DEFAULT_DYNAMIC_SENSOR_MAX_VALUE_SIZE = 50;
    private static final Map<Integer, Sensor> SENSOR_MAP = new HashMap<>();
    private static final Map<Byte, Sensor.Measurement.DataType> BLE_DATA_TYPES = new HashMap<>();
    private static final Map<Byte, Sensor.Measurement.DataType> ESB_DATA_TYPES = new HashMap<>();
    private static final List<Sensor.Type> BLE_SENSOR_TYPES = new ArrayList<>();
    private static final List<Sensor.Type> ESB_SENSOR_TYPES = new ArrayList<>();
    private static ValueContainerConfigurationProvider configurationProvider;
//    static {
//        setValueContainerConfigurationProvider(null);
//    }

    private static final ExpandComparator<Sensor.Type, Integer> SENSOR_TYPE_SEARCH_COMPARATOR = new ExpandComparator<Sensor.Type, Integer>() {
        @Override
        public int compare(Sensor.Type type, Integer targetAddress) {
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

//    public static Sensor getSensor(int address, boolean autoCreate) {
//        return getSensor(address, null, autoCreate);
//    }

//    public static Sensor createSensor(int address, Sensor.Decorator decorator) {
//        return getSensor(address, decorator, true);
//    }

    public static synchronized Sensor getSensor(int address, boolean autoCreate) {
        Sensor sensor = SENSOR_MAP.get(address);
        if (autoCreate && sensor == null) {
            sensor = new Sensor(address, DEFAULT_DYNAMIC_SENSOR_MAX_VALUE_SIZE);
            SENSOR_MAP.put(address, sensor);
//            if (sensor == null) {
//
//            } else {
//                sensor.setDecorator(decorator);
//            }
        }
        return sensor;
    }

    public static synchronized void getSensors(List<Sensor> sensorCarrier, Sensor.Filter filter) {
        if (sensorCarrier == null) {
            return;
        }
        if (filter == null) {
            sensorCarrier.addAll(SENSOR_MAP.values());
        } else {
            for (Sensor sensor :
                    SENSOR_MAP.values()) {
                if (filter.isMatch(sensor)) {
                    sensorCarrier.add(sensor);
                }
            }
        }
    }

    public static synchronized int getSensorWithHistoryValuesCount() {
        int count = 0;
        for (Sensor sensor :
                SENSOR_MAP.values()) {
            if (sensor.hasHistoryValue()) {
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

    private static boolean importConfiguration(Map<Byte, Sensor.Measurement.DataType> dataTypes,
                                               List<Sensor.Type> types,
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

    public static Sensor.Type findSensorType(int address) {
        List<Sensor.Type> types = getSensorTypes(address);
        if (types == null)
            return null;
        int position = ExpandCollections.binarySearch(types,
                address,
                SENSOR_TYPE_SEARCH_COMPARATOR);
        return position >= 0 ? types.get(position) : null;
    }

    public static Sensor.Measurement.DataType getDataType(int address, byte dataTypeValue, boolean autoCreate) {
        Map<Byte, Sensor.Measurement.DataType> dataTypeMap = getDataTypes(address);
        Sensor.Measurement.DataType dataType = dataTypeMap.get(dataTypeValue);
        if (autoCreate && dataType == null) {
            dataType = new Sensor.Measurement.DataType(dataTypeValue);
            dataTypeMap.put(dataTypeValue, dataType);
        }
        return dataType;
    }

    public static List<Sensor.Type> getBleSensorTypes() {
        return Collections.unmodifiableList(BLE_SENSOR_TYPES);
    }

    public static List<Sensor.Type> getEsbSensorTypes() {
        return Collections.unmodifiableList(ESB_SENSOR_TYPES);
    }

    private static List<Sensor.Type> getSensorTypes(int address) {
        return Sensor.isBleProtocolFamily(address) ? BLE_SENSOR_TYPES : ESB_SENSOR_TYPES;
    }

    private static Map<Byte, Sensor.Measurement.DataType> getDataTypes(int address) {
        return Sensor.isBleProtocolFamily(address) ? BLE_DATA_TYPES : ESB_DATA_TYPES;
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

    public static synchronized void setValueContainerConfigurationProvider(ValueContainerConfigurationProvider provider, boolean isResetConfigurations) {
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

    static ValueContainerConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    public interface ValueContainerConfigurationProvider {
        Sensor.Configuration getSensorConfiguration(int address);
        Sensor.Measurement.Configuration getMeasurementConfiguration(int address, byte dataTypeValue, int dataTypeValueIndex);
    }

//    private static class EmptyValueContainerConfigurationProvider implements ValueContainerConfigurationProvider {
//
//        @Override
//        public Sensor.Configuration getSensorConfiguration(int address) {
//            return null;
//        }
//
//        @Override
//        public Sensor.Measurement.Configuration getMeasurementConfiguration(long id) {
//            return null;
//        }
//    }

    private static class ConfigurationImporter extends DefaultHandler {

        private static final String DATA_TYPE = "DataType";
        private static final String PARAPHRASES = "paraphrases";
        private static final String SENSOR_TYPE = "SensorType";
        private static final String DATA_TYPE_CUSTOM_NAME = "DataTypeCustomName";

        private Map<Byte, Sensor.Measurement.DataType> mDataTypeMap;
        private Sensor.Measurement.DataType mDataType;
        private StringBuilder mBuilder;
        private Map<Double, String> mParaphrases;
        private Double mNumber;
        private String mText;
        private String mOn;
        private String mOff;
        private List<Sensor.Type> mTypes;
        private Sensor.Type mType;
        private List<Sensor.Type.MeasureParameter> mMeasureParameters;
        private Sensor.Type.MeasureParameter mMeasureParameter;
        private int mIndex;
        private byte mDataTypeValue;
        private String mDataTypeCustomName;
        private int mCustomDataTypeNameType;
        private int mValueType = -1;
        private boolean mSigned;
        private double mCoefficient;
        private ScriptValueCorrector.Builder mScriptValueCorrectorBuilder;
        private String mLabel;

        public Map<Byte, Sensor.Measurement.DataType> getDataTypeMap() {
            return mDataTypeMap;
        }

        public List<Sensor.Type> getTypes() {
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
                mType = new Sensor.Type();
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
                    mDataType = new Sensor.Measurement.DataType((byte)Integer.parseInt(mBuilder.toString(), 16));
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
                        mDataType = new Sensor.Measurement.DataType(mDataTypeValue);
                        mDataTypeMap.put(mDataTypeValue, mDataType);
                    }
                    //生成测量参数
                    mMeasureParameter = new Sensor.Type.MeasureParameter(mDataType,
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
                    mType.mMeasureParameters = new Sensor.Type.MeasureParameter[mMeasureParameters.size()];
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
                    Collections.sort(mTypes, new Comparator<Sensor.Type>() {
                        @Override
                        public int compare(Sensor.Type c1, Sensor.Type c2) {
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
                List<Sensor.Type.MeasureParameter> measureParameters,
                Sensor.Type.MeasureParameter parameterGetter) {
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

        private static final Comparator<Sensor.Type.MeasureParameter> MEASURE_PARAMETER_COMPARATOR = new Comparator<Sensor.Type.MeasureParameter>() {
            @Override
            public int compare(Sensor.Type.MeasureParameter mp1, Sensor.Type.MeasureParameter mp2) {
                return mp1.mInvolvedDataType.mValue - mp2.mInvolvedDataType.mValue;
            }
        };
    }
}
