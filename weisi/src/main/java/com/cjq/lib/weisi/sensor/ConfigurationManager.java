package com.cjq.lib.weisi.sensor;

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
 * Created by CJQ mOn 2017/6/16.
 */

public class ConfigurationManager {

    private static final int BLE_ADDRESS_LENGTH = 6;
    private static final Map<Byte, DataType> BLE_DATA_TYPES = new HashMap<>();
    private static final Map<Byte, DataType> ETHERNET_DATA_TYPES = new HashMap<>();
    private static final List<Configuration> BLE_CONFIGURATIONS = new ArrayList<>();
    private static final List<Configuration> ETHERNET_CONFIGURATIONS = new ArrayList<>();

    public static boolean importBleConfiguration(Context context) {
        return importConfiguration(BLE_DATA_TYPES,
                BLE_CONFIGURATIONS,
                context,
                "BleSensorConfiguration.xml");
    }

    public static boolean importEthernetConfiguration(Context context) {
        return importConfiguration(ETHERNET_DATA_TYPES,
                ETHERNET_CONFIGURATIONS,
                context,
                "EthernetSensorConfiguration.xml");
    }

    private static boolean importConfiguration(Map<Byte, DataType> dataTypes,
                                               List<Configuration> configurations,
                                               Context context,
                                               String configFileName) {
        ConfigurationImporter importer = getConfigurationImporter(context, configFileName);
        if (importer == null) {
            return false;
        }
        dataTypes.clear();
        configurations.clear();
        dataTypes.putAll(importer.getDataTypeMap());
        configurations.addAll(importer.getConfigurations());
        return true;
    }

    public static Configuration findConfiguration(int address) {
        List<Configuration> configurations = getConfigurations(address);
        if (configurations == null)
            return null;
        int position = ExpandCollections.binarySearch(configurations,
                address,
                CONFIGURATION_SEARCH_COMPARATOR);
        return position >= 0 ? configurations.get(position) : null;
//        synchronized (CONFIGURATION_SEARCHER) {
//            CONFIGURATION_SEARCHER.mStartAddress = address;
//            int index = Collections.binarySearch(configurations,
//                    CONFIGURATION_SEARCHER,
//                    CONFIGURATION_SEARCH_COMPARATOR);
//            return index >= 0 ? configurations.get(index) : null;
//        }
    }

    public static boolean isBleSensor(int address) {
        return (address & 0xff0000) != 0;
    }

    public static boolean isBleSensor(String address) {
        return address.length() == BLE_ADDRESS_LENGTH;
    }

    public static DataType getDataType(int address, byte dataTypeValue, boolean autoCreate) {
        Map<Byte, DataType> dataTypeMap = getDataTypes(address);
        DataType dataType = dataTypeMap.get(dataTypeValue);
        if (autoCreate && dataType == null) {
            dataType = new DataType(dataTypeValue);
//            dataType.mBuilder = isBleSensor(address)
//                    ? BleDataValueBuilder.getInstance()
//                    : new UdpDataValueBuilder(0, true, 3);
            dataTypeMap.put(dataTypeValue, dataType);
        }
        return dataType;
    }

//    private static final Configuration CONFIGURATION_SEARCHER = new Configuration();
//    private static final Comparator<Configuration> CONFIGURATION_SEARCH_COMPARATOR = new Comparator<Configuration>() {
//        @Override
//        public int compare(Configuration c1, Configuration c2) {
//            if (c1.mEndAddress < c2.mStartAddress) {
//                return -1;
//            }
//            if (c1.mStartAddress > c2.mStartAddress) {
//                return 1;
//            }
//            return 0;
//        }
//    };

    private static final ExpandComparator<Configuration, Integer> CONFIGURATION_SEARCH_COMPARATOR = new ExpandComparator<Configuration, Integer>() {
        @Override
        public int compare(Configuration configuration, Integer targetAddress) {
            if (configuration.mEndAddress < targetAddress) {
                return -1;
            }
            if (configuration.mStartAddress > targetAddress) {
                return 1;
            }
            return 0;
        }
    };

    private static List<Configuration> getConfigurations(int address) {
        return isBleSensor(address) ? BLE_CONFIGURATIONS : ETHERNET_CONFIGURATIONS;
    }

    private static Map<Byte, DataType> getDataTypes(int address) {
        return isBleSensor(address) ? BLE_DATA_TYPES : ETHERNET_DATA_TYPES;
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

    private static class ConfigurationImporter extends DefaultHandler {

        private static final String DATA_TYPE = "DataType";
        private static final String PARAPHRASES = "paraphrases";
        private static final String CONFIGURATION = "configuration";
        private static final String DATA_TYPE_CUSTOM_NAME = "DataTypeCustomName";

        private Map<Byte, DataType> mDataTypeMap;
        private DataType mDataType;
        private StringBuilder mBuilder;
        private Map<Double, String> mParaphrases;
        private Double mNumber;
        private String mText;
        private String mOn;
        private String mOff;
        private List<Configuration> mConfigurations;
        private Configuration mConfiguration;
        private List<Configuration.MeasureParameter> mMeasureParameters;
        private Configuration.MeasureParameter mMeasureParameter;
        private int mIndex;
        private byte mDataTypeValue;
        private String mDataTypeCustomName;
        private int mCustomDataTypeNameType;
        private int mValueType = -1;
        private boolean mSigned;
        private double mCoefficient;

        public Map<Byte, DataType> getDataTypeMap() {
            return mDataTypeMap;
        }

        public List<Configuration> getConfigurations() {
            return mConfigurations;
        }

        @Override
        public void startDocument() throws SAXException {
            mBuilder = new StringBuilder();
            mDataTypeMap = new HashMap<>();
            mConfigurations = new ArrayList<>();
            mMeasureParameters = new ArrayList<>();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.equals(CONFIGURATION)) {
                mConfiguration = new Configuration();
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
                    mDataType = new DataType((byte)Integer.parseInt(mBuilder.toString(), 16));
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
                        //mDataType.mBuilder = new UdpDataValueBuilder(mValueType, mSigned, mCoefficient);
                        EsbAnalyzer.setValueBuilder(mDataType.mValue, mValueType, mSigned, mCoefficient);
                        mValueType = -1;
                        mCoefficient = 1;
                    }
//                    else {
//                        mDataType.mBuilder = BleDataValueBuilder.getInstance();
//                    }
                    mDataTypeMap.put(mDataType.mValue, mDataType);
                    break;
                case "SensorName":
                    mConfiguration.mSensorGeneralName = mBuilder.toString();
                    break;
                case "start":
                    mConfiguration.mStartAddress = Integer.parseInt(mBuilder.toString(), 16);
                    break;
                case "end":
                    mConfiguration.mEndAddress = Integer.parseInt(mBuilder.toString(), 16);
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
                        mDataType = new DataType(mDataTypeValue);
                        mDataTypeMap.put(mDataTypeValue, mDataType);
                    }
//                    //若为Ethernet，则为DataType额外生成ValueBuilder
//                    if (mValueType != -1) {
//                        mDataType.mBuilder = new UdpDataValueBuilder(mValueType, mSigned, mCoefficient);
//                        mValueType = -1;
//                        mCoefficient = 1;
//                    } else {
//                        mDataType.mBuilder = BleDataValueBuilder.getInstance();
//                    }
                    //生成测量参数
                    mMeasureParameter = new Configuration.MeasureParameter(mDataType,
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
                    mConfiguration.mMeasureParameters = new Configuration.MeasureParameter[mMeasureParameters.size()];
                    mMeasureParameters.toArray(mConfiguration.mMeasureParameters);
                    mMeasureParameters.clear();
                    break;
                case "configuration":
                    mConfigurations.add(mConfiguration);
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
                case "configurations":
                    Collections.sort(mConfigurations, new Comparator<Configuration>() {
                        @Override
                        public int compare(Configuration c1, Configuration c2) {
                            return c1.mStartAddress - c2.mStartAddress;
                        }
                    });
                    break;
                default:
                    break;
            }
        }

        private int findMeasureParameter(
                List<Configuration.MeasureParameter> measureParameters,
                Configuration.MeasureParameter parameterGetter) {
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

        private static final Comparator<Configuration.MeasureParameter> MEASURE_PARAMETER_COMPARATOR = new Comparator<Configuration.MeasureParameter>() {
            @Override
            public int compare(Configuration.MeasureParameter mp1, Configuration.MeasureParameter mp2) {
                return mp1.mInvolvedDataType.mValue - mp2.mInvolvedDataType.mValue;
            }
        };
    }
}
