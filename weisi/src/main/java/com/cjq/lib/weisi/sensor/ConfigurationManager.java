package com.cjq.lib.weisi.sensor;

import android.content.Context;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.InputStream;
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

    private static Map<Byte, DataType> mBleDataTypes;
    private static Map<Byte, DataType> mEthernetDataTypes;
    private static List<Configuration> mBleConfiguration;
    private static List<Configuration> mEthernetConfiguration;

    public static boolean importBleConfiguration(Context context) {
        try {
            return importBleConfiguration(context.getAssets().open("BleSensorConfiguration.xml"));
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean importBleConfiguration(InputStream is) {
        ConfigurationImporter importer = getConfigurationImporter(is);
        if (importer == null) {
            return false;
        }
        mBleDataTypes = importer.getDataTypeMap();
        mBleConfiguration = importer.getConfigurations();
        return true;
    }

    public static boolean importEthernetConfiguration(Context context) {
        try {
            return importBleConfiguration(context.getAssets().open("EthernetSensorConfiguration.xml"));
        } catch (Exception e) {
        }
        return false;
    }

    public static boolean importEthernetConfiguration(InputStream is) {
        ConfigurationImporter importer = getConfigurationImporter(is);
        if (importer == null) {
            return false;
        }
        mEthernetDataTypes = importer.getDataTypeMap();
        mEthernetConfiguration = importer.getConfigurations();
        return true;
    }

    static Configuration findConfiguration(boolean isBle, int address) {
        CONFIGURATION_SEARCHER.mStartAddress = address;
        List<Configuration> configurations = getConfigurations(isBle);
        if (configurations == null)
            return null;
        int index = Collections.binarySearch(configurations,
                CONFIGURATION_SEARCHER,
                CONFIGURATION_SEARCH_COMPARATOR);
        return index >= 0 ? configurations.get(index) : null;
    }

    static DataType getDataType(boolean isBle, byte dataTypeValue) {
        Map<Byte, DataType> dataTypeMap = isBle ? mBleDataTypes : mEthernetDataTypes;
        DataType dataType = dataTypeMap.get(dataTypeValue);
        if (dataType == null) {
            dataType = new DataType(dataTypeValue);
            dataTypeMap.put(dataTypeValue, dataType);
        }
        return dataType;
    }

    private static final Configuration CONFIGURATION_SEARCHER = new Configuration();
    private static final Comparator<Configuration> CONFIGURATION_SEARCH_COMPARATOR = new Comparator<Configuration>() {
        @Override
        public int compare(Configuration c1, Configuration c2) {
            if (c1.mEndAddress < c2.mStartAddress) {
                return -1;
            } else if (c1.mStartAddress > c2.mStartAddress) {
                return 1;
            } else {
                return 0;
            }
        }
    };

    private static List<Configuration> getConfigurations(boolean isBle) {
        return isBle ? mBleConfiguration : mEthernetConfiguration;
    }

    private static ConfigurationImporter getConfigurationImporter(InputStream is) {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            ConfigurationImporter importer = new ConfigurationImporter();
            parser.parse(is, importer);
            return importer;
        } catch (Exception e) {
            return null;
        }
    }

    private static class ConfigurationImporter extends DefaultHandler {

        private static final String DATA_TYPE = "DataType";
        private static final String PARAPHRASES = "paraphrases";
        private static final String CONFIGURATION = "configurations";

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
        private String mAppendLabel;

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
                case DATA_TYPE:
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
                case "AppendLabel":
                    mAppendLabel = mBuilder.toString();
                    break;
                case "measurement":
                    //获取数据类型
                    mDataType = mDataTypeMap.get(mDataTypeValue);
                    if (mDataType == null) {
                        mDataType = new DataType(mDataTypeValue);
                        mDataTypeMap.put(mDataTypeValue, mDataType);
                    }
                    //生成测量参数
                    mMeasureParameter = new Configuration.MeasureParameter(mDataType,
                            mAppendLabel != null ? mDataType.getName() + mAppendLabel : null);
                    mAppendLabel = null;
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
