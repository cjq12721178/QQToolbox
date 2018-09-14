package com.cjq.lib.weisi.iot;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cjq.lib.weisi.data.Filter;
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

    private static final Map<Long, Measurement> MEASUREMENT_MAP = new HashMap<>();
    private static final Map<Long, Sensor> SENSOR_MAP = new HashMap<>();
    private static final Map<Byte, PracticalMeasurement.DataType> BLE_DATA_TYPES = new HashMap<>();
    private static final Map<Byte, PracticalMeasurement.DataType> ESB_DATA_TYPES = new HashMap<>();
    private static final List<PhysicalSensor.Type> BLE_SENSOR_TYPES = new ArrayList<>();
    private static final List<PhysicalSensor.Type> ESB_SENSOR_TYPES = new ArrayList<>();
    private static MeasurementConfigurationProvider configurationProvider;

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

    //
    //  获取Sensor.Info的相关方法
    //

    public static @Nullable Sensor.Info findSensorInfo(@NonNull ID id) {
        return findSensorInfo(id.getId());
    }

    public static @Nullable Sensor.Info findSensorInfo(long id) {
        return getSensorInfo(id, false);
    }

    public static @Nullable Sensor.Info findSensorInfo(int address) {
        return findSensorInfo(ID.getId(address));
    }

    public static @NonNull Sensor.Info getSensorInfo(@NonNull ID id) {
        return getSensorInfo(id.getId());
    }

    public static @NonNull Sensor.Info getSensorInfo(long id) {
        return getSensorInfo(id, true);
    }

    public static @NonNull Sensor.Info getSensorInfo(int address) {
        return getSensorInfo(ID.getId(address));
    }

    private static Sensor.Info getSensorInfo(long id, boolean autoCreate) {
        return getSensorInfo(id, autoCreate, autoCreate ? findSensorType(ID.getAddress(id)) : null);
    }

    private static Sensor.Info getSensorInfo(long id, boolean autoCreate, @Nullable PhysicalSensor.Type type) {
        long correctId = ID.ensureSensorInfo(id);
        synchronized (MEASUREMENT_MAP) {
            Measurement measurement = MEASUREMENT_MAP.get(correctId);
            if (measurement == null) {
                if (!autoCreate) {
                    return null;
                }
                Sensor.Info info = createSensorInfo(new ID(correctId), type);
                MEASUREMENT_MAP.put(correctId, info);
                return info;
            }
            return (Sensor.Info) measurement;
        }
    }

    private static @NonNull Sensor.Info createSensorInfo(@NonNull ID id, PhysicalSensor.Type type) {
        return new Sensor.Info(id, type != null ? type.mSensorGeneralName : null);
    }

    //
    //  获取PracticalMeasurement的相关方法
    //

    public static @Nullable PracticalMeasurement findPracticalMeasurement(@NonNull ID id) {
        return findPracticalMeasurement(id.getId());
    }

    public static @Nullable PracticalMeasurement findPracticalMeasurement(long id) {
        return getPracticalMeasurement(id, false);
    }

    public static @Nullable PracticalMeasurement findPracticalMeasurement(int address, byte dataTypeValue, int dataTypeValueIndex) {
        return findPracticalMeasurement(ID.getId(address, dataTypeValue, dataTypeValueIndex));
    }

    public static @Nullable PracticalMeasurement findPracticalMeasurement(int address, byte dataTypeValue) {
        return findPracticalMeasurement(address, dataTypeValue, 0);
    }

    public static @NonNull PracticalMeasurement getPracticalMeasurement(@NonNull ID id) {
        return getPracticalMeasurement(id.getId());
    }

    public static @NonNull PracticalMeasurement getPracticalMeasurement(int address, byte dataTypeValue, int dataTypeValueIndex) {
        return getPracticalMeasurement(ID.getId(address, dataTypeValue, dataTypeValueIndex));
    }

    public static @NonNull PracticalMeasurement getPracticalMeasurement(int address, byte dataTypeValue) {
        return getPracticalMeasurement(address, dataTypeValue, 0);
    }

    public static @NonNull PracticalMeasurement getPracticalMeasurement(long id) {
        return getPracticalMeasurement(id, true);
    }

    private static PracticalMeasurement getPracticalMeasurement(long id, boolean autoCreate) {
        return getPracticalMeasurement(id, autoCreate, autoCreate ? findSensorType(ID.getAddress(id)) : null);
    }

    private static @Nullable PhysicalSensor.Type.MeasureParameter findMeasureParameter(long id, @Nullable PhysicalSensor.Type type) {
        PhysicalSensor.Type.MeasureParameter parameter = null;
        if (type != null) {
            byte dataTypeValue = ID.getDataTypeValue(id);
            int dataTypeValueIndex = ID.getDataTypeValueIndex(id);
            for (int i = 0;i < type.mMeasureParameters.length;++i) {
                parameter = type.mMeasureParameters[i];
                if (parameter.mInvolvedDataType.mValue == dataTypeValue) {
                    for (int j = 0;j < dataTypeValueIndex && parameter != null;++j) {
                        parameter = parameter.mNext;
                    }
                    break;
                }
            }
        }
        return parameter;
    }

    private static PracticalMeasurement getPracticalMeasurement(long id, boolean autoCreate, @Nullable PhysicalSensor.Type type) {
        return getPracticalMeasurement(id, autoCreate, autoCreate ? findMeasureParameter(id, type) : null);
    }

    private static PracticalMeasurement getPracticalMeasurement(long id, boolean autoCreate, @Nullable PhysicalSensor.Type.MeasureParameter parameter) {
        if (!ID.isPracticalMeasurement(id)) {
            if (autoCreate) {
                throw new IllegalArgumentException("id of target practical measurement is abnormal");
            }
            return null;
        }
        synchronized (MEASUREMENT_MAP) {
            Measurement measurement = MEASUREMENT_MAP.get(id);
            if (measurement == null) {
                if (!autoCreate) {
                    return null;
                }
                PracticalMeasurement practicalMeasurement = createPracticalMeasurement(new ID(id), parameter);
                MEASUREMENT_MAP.put(id, practicalMeasurement);
                return practicalMeasurement;
            }
            return (PracticalMeasurement) measurement;
        }
    }

    private static PracticalMeasurement createPracticalMeasurement(@NonNull ID id, @Nullable PhysicalSensor.Type.MeasureParameter parameter) {
        if (parameter != null) {
            return new PracticalMeasurement(id,
                    parameter.mInvolvedDataType,
                    parameter.mDataTypeAccurateName);
        } else {
            return new PracticalMeasurement(id,
                    getDataType(id.getAddress(),
                            id.getDataTypeValue(),
                            true),
                    null);
        }
    }

    public static @Nullable DisplayMeasurement findVirtualMeasurement(@NonNull ID id) {
        return findVirtualMeasurement(id.getId());
    }

    public static @Nullable DisplayMeasurement findVirtualMeasurement(long id) {
        return getVirtualMeasurement(id, null);
    }

    public static @Nullable DisplayMeasurement getVirtualMeasurement(long id) {
        return getVirtualMeasurement(id, configurationProvider);
    }

    public static @Nullable DisplayMeasurement getVirtualMeasurement(long id, VirtualMeasurementBuilder builder) {
        if (!ID.isVirtualMeasurement(id)) {
            return null;
        }
        synchronized (MEASUREMENT_MAP) {
            Measurement measurement = MEASUREMENT_MAP.get(id);
            if (measurement == null && builder != null) {
                DisplayMeasurement displayMeasurement = builder.build(new ID(id));
                MEASUREMENT_MAP.put(id, displayMeasurement);
                return displayMeasurement;
            }
            return (DisplayMeasurement) measurement;
        }
    }

    public static Measurement getMeasurement(int address) {
        return getMeasurement(address, (byte) 0);
    }

    public static Measurement getMeasurement(int address, byte dataTypeValue) {
        return getMeasurement(address, dataTypeValue, 0);
    }

    public static Measurement getMeasurement(int address, byte dataTypeValue, int dataTypeValueIndex) {
        return getMeasurement(ID.getId(address, dataTypeValue, dataTypeValueIndex));
    }

    public static Measurement getMeasurement(@NonNull ID id) {
        return getMeasurement(id.getId());
    }

    public static Measurement getMeasurement(long id) {
        return getMeasurement(id, true);
    }

    private static Measurement getMeasurement(long id, boolean autoCreate) {
        if (ID.isPracticalMeasurement(id)) {
            return getPracticalMeasurement(id, autoCreate);
        }
        if (ID.isSensorInfo(id)) {
            return getSensorInfo(id, autoCreate);
        }
        if (ID.isVirtualMeasurement(id)) {
            return getVirtualMeasurement(id, autoCreate ? configurationProvider : null);
        }
        return null;
    }

//    public static synchronized @Nullable Measurement getMeasurement(long id, boolean autoCreate) {
//        Measurement measurement = MEASUREMENT_MAP.get(id);
//        if (measurement == null && autoCreate) {
//            measurement = createMeasurement(new ID(id));
//            if (measurement != null) {
//                MEASUREMENT_MAP.put(id, measurement);
//            }
//        }
//        return measurement;
//    }

//    private static @Nullable Measurement createMeasurement(@NonNull ID id) {
//        PhysicalSensor.Type type = findSensorType(id.getAddress());
//        if (id.isSensorInfo()) {
//            return new Sensor.Info(id,
//                    type != null ? type.mSensorGeneralName : null);
//        }
//        if (id.isPracticalMeasurement()) {
//            PhysicalSensor.Type.MeasureParameter parameter = null;
//            if (type != null) {
//                byte dataTypeValue = id.getDataTypeValue();
//                for (int i = 0;i < type.mMeasureParameters.length;++i) {
//                    parameter = type.mMeasureParameters[i];
//                    if (parameter.mInvolvedDataType.mValue == dataTypeValue) {
//                        int dataTypeValueIndex = id.getDataTypeValueIndex();
//                        for (int j = 0;j < dataTypeValueIndex && parameter != null;++j) {
//                            parameter = parameter.mNext;
//                        }
//                        break;
//                    }
//                }
//            }
//            if (parameter != null) {
//                return new PracticalMeasurement(id,
//                        parameter.mInvolvedDataType,
//                        parameter.mDataTypeAccurateName);
//            } else {
//                return new PracticalMeasurement(id,
//                        getDataType(id.getAddress(),
//                                id.getDataTypeValue(),
//                                true),
//                        null);
//            }
//        }
//        if (configurationProvider != null) {
//            return configurationProvider.build(id);
//        }
//        return null;
//    }

    //
    //  获取PhysicalSensor的相关方法
    //

    public static @Nullable PhysicalSensor findPhysicalSensor(@NonNull ID id) {
        return findPhysicalSensor(id.getId());
    }

    public static @Nullable PhysicalSensor findPhysicalSensor(long id) {
        return getPhysicalSensor(ID.ensureSensor(id), false);
    }

    public static @Nullable PhysicalSensor findPhysicalSensor(int address) {
        return findPhysicalSensor(ID.getId(address));
    }

    public static @NonNull PhysicalSensor getPhysicalSensor(@NonNull ID id) {
        return getPhysicalSensor(id.getId());
    }

    public static @NonNull PhysicalSensor getPhysicalSensor(long id) {
        return getPhysicalSensor(ID.ensureSensor(id), true);
    }

    public static @NonNull PhysicalSensor getPhysicalSensor(int address) {
        return getPhysicalSensor(ID.getId(address));
    }

    private static PhysicalSensor getPhysicalSensor(long id, boolean autoCreate) {
        synchronized (SENSOR_MAP) {
            if (autoCreate) {
                long correctId = ID.ensurePhysicalSensor(id);
                Sensor sensor = SENSOR_MAP.get(correctId);
                if (sensor == null) {
                    PhysicalSensor physicalSensor = createPhysicalSensor(new ID(correctId));
                    SENSOR_MAP.put(correctId, physicalSensor);
                    return physicalSensor;
                }
                return (PhysicalSensor) sensor;
            } else {
                return ID.isPhysicalSensor(id)
                        ? (PhysicalSensor) SENSOR_MAP.get(id)
                        : null;
            }
        }
    }

    private static @NonNull PhysicalSensor createPhysicalSensor(@NonNull ID id) {
        int address = id.getAddress();
        PhysicalSensor.Type type = findSensorType(address);
        Sensor.Info info = getSensorInfo(id.getId(), true, type);
        List<DisplayMeasurement> measurements;
        if (type != null) {
            PhysicalSensor.Type.MeasureParameter parameter;
            measurements = new ArrayList<>(type.getMeasureParameterSize());
            for (int i = 0;i < type.mMeasureParameters.length;++i) {
                parameter = type.mMeasureParameters[i];
                byte dataTypeValue = parameter.mInvolvedDataType.mValue;
                int dataTypeValueIndex = 0;
                do {
                    measurements.add(getPracticalMeasurement(ID.getId(address, dataTypeValue, dataTypeValueIndex++), true, parameter));
                    parameter = parameter.mNext;
                } while (parameter != null);
            }
        } else {
            measurements = new ArrayList<>();
        }
        return new PhysicalSensor(info, type, measurements);
    }

    private static @Nullable LogicalSensor getLogicalSensor(long id, boolean autoCreate) {
        if (!ID.isLogicalSensor(id)) {
            return null;
        }
        synchronized (SENSOR_MAP) {
            Sensor sensor = SENSOR_MAP.get(id);
            if (sensor == null && autoCreate) {
                LogicalSensor logicalSensor = createLogicalSensor(new ID(id));
                SENSOR_MAP.put(id, logicalSensor);
                return logicalSensor;
            }
            return (LogicalSensor) sensor;
        }
    }

    private static @NonNull LogicalSensor createLogicalSensor(@NonNull ID id) {
        int address = id.getAddress();
        PhysicalSensor.Type type = findSensorType(address);
        Sensor.Info info = getSensorInfo(id.getId(), true, type);
        PracticalMeasurement measurement = getPracticalMeasurement(id.getId(), true, type);
        return new LogicalSensor(info, measurement);
    }

//    private static @NonNull PracticalMeasurement createPracticalMeasurement(@NonNull ID id) {
//        PhysicalSensor.Type type = findSensorType(id.getAddress());
//    }
//    private static synchronized Measurement createLogicalSensor(
//            @NonNull ID id,
//            @Nullable PhysicalSensor.Type.MeasureParameter parameter) {
//        LogicalSensor sensor;
//        if (parameter != null) {
//            sensor = new LogicalSensor(id,
//                    parameter.mInvolvedDataType,
//                    parameter.mDataTypeAccurateName);
//        } else {
//            sensor = new LogicalSensor(id, getDataType(id.getAddress(),
//                    id.getDataTypeValue(), true));
//        }
//        putSensor(sensor);
//        return sensor;
//    }

//    public static synchronized Sensor getSensor(@NonNull ID id, boolean autoCreate) {
//        Sensor sensor = SENSOR_MAP.get(id);
//        if (sensor == null && autoCreate) {
//            sensor = createSensor(id);
//        }
//        return sensor;
//    }

//    private static Sensor createSensor(long id) {
//        return createSensor(new ID(id));
//    }

//    private static Sensor createSensor(@NonNull ID id) {
//        Sensor sensor;
//        if (id.isLogicalSensor()) {
//            sensor = createLogicalSensor(id);
//        } else {
//            sensor = createPhysicalSensor(id);
//        }
//        return sensor;
//    }

//    public static synchronized Sensor getSensor(long id, boolean autoCreate) {
//        long correctId = ID.ensureSensor(id);
//        Sensor sensor = SENSOR_MAP.get(correctId);
//        if (sensor == null && autoCreate) {
//            sensor = createSensor(correctId);
//        }
//        return sensor;
//    }

//    static LogicalSensor getLogicalSensor(int address,
//                                          int dataTypeValueIndex,
//                                          @NonNull PhysicalSensor.Type.MeasureParameter parameter) {
//        return getLogicalSensor(address, parameter.mInvolvedDataType.mValue, dataTypeValueIndex, parameter);
//    }
//
//    static LogicalSensor getLogicalSensor(int address,
//                                          byte dataType,
//                                          int dataTypeValueIndex) {
//        return getLogicalSensor(address, dataType, dataTypeValueIndex, null);
//    }

//    private static synchronized LogicalSensor getLogicalSensor(
//            int address, byte dataTypeValue, int dataTypeValueIndex,
//            @Nullable PhysicalSensor.Type.MeasureParameter parameter) {
//        long id = ID.getId(address, dataTypeValue, dataTypeValueIndex);
//        LogicalSensor sensor = (LogicalSensor) SENSOR_MAP.get(id);
//        if (sensor == null) {
//            sensor = createLogicalSensor(new ID(id), parameter);
//        }
//        return sensor;
//    }

//    public static synchronized LogicalSensor getLogicalSensor(
//            long id, boolean autoCreate) {
//        long correctId = ID.ensureSensor(id);
//        LogicalSensor sensor = (LogicalSensor) SENSOR_MAP.get(correctId);
//        if (sensor == null && autoCreate) {
//            sensor = createLogicalSensor(correctId);
//        }
//        return sensor;
//    }
//
//    public static synchronized LogicalSensor getLogicalSensor(
//            int address, byte dataTypeValue, int dataTypeValueIndex,
//            boolean autoCreate) {
//        return getLogicalSensor(ID.getId(address, dataTypeValue, dataTypeValueIndex), autoCreate);
//    }
//
//    public static synchronized LogicalSensor getLogicalSensor(
//            @NonNull ID id, boolean autoCreate) {
//        LogicalSensor sensor = (LogicalSensor) SENSOR_MAP.get(id);
//        if (sensor == null && autoCreate) {
//            sensor = createLogicalSensor(id);
//        }
//        return sensor;
//    }
//
//    private static LogicalSensor createLogicalSensor(long id) {
//        return createLogicalSensor(new ID(id));
//    }

//    private static LogicalSensor createLogicalSensor(@NonNull ID id) {
//        //生成逻辑传感器（即之前的测量量Measurement）
//        PhysicalSensor.Type type = findSensorType(id.getAddress());
//        PhysicalSensor.Type.MeasureParameter parameter = null;
//        if (type != null) {
//            for (int i = 0;i < type.mMeasureParameters.length;++i) {
//                parameter = type.mMeasureParameters[i];
//                if (parameter.mInvolvedDataType.mValue == id.getDataTypeValue()) {
//                    for (int j = 0;j < id.getDataTypeValueIndex() && parameter != null;++j) {
//                        parameter = parameter.mNext;
//                    }
//                    break;
//                }
//            }
//        }
//        return createLogicalSensor(id, parameter);
//    }
//
//    private static synchronized LogicalSensor createLogicalSensor(
//            @NonNull ID id,
//            @Nullable PhysicalSensor.Type.MeasureParameter parameter) {
//        LogicalSensor sensor;
//        if (parameter != null) {
//            sensor = new LogicalSensor(id,
//                    parameter.mInvolvedDataType,
//                    parameter.mDataTypeAccurateName);
//        } else {
//            sensor = new LogicalSensor(id, getDataType(id.getAddress(),
//                    id.getDataTypeValue(), true));
//        }
//        putSensor(sensor);
//        return sensor;
//    }
//
//    public static synchronized PhysicalSensor getPhysicalSensor(int address, boolean autoCreate) {
//        PhysicalSensor sensor = (PhysicalSensor) SENSOR_MAP.get(ID.getId(address));
//        if (sensor == null && autoCreate) {
//            sensor = createPhysicalSensor(address);
//        }
//        return sensor;
//    }
//
//    private static synchronized PhysicalSensor createPhysicalSensor(int address) {
//        return createPhysicalSensor(new ID(address));
//    }
//
//    private static synchronized PhysicalSensor createPhysicalSensor(@NonNull ID id) {
//        PhysicalSensor sensor = new PhysicalSensor(id, type, displayMeasurements);
//        putSensor(sensor);
//        return sensor;
//    }
//
//    private static synchronized void putSensor(@NonNull Sensor sensor) {
//        SENSOR_MAP.put(sensor.getId().getId(), sensor);
//    }

    public static Sensor findSensor(@NonNull ID id) {
        return findSensor(id.getId());
    }

    public static Sensor findSensor(int address) {
        return findSensor(address, (byte) 0);
    }

    public static Sensor findSensor(int address, byte dataTypeValue) {
        return findSensor(address, dataTypeValue, 0);
    }

    public static Sensor findSensor(int address, byte dataTypeValue, int dataTypeValueIndex) {
        return findSensor(ID.getId(address, dataTypeValue, dataTypeValueIndex));
    }

    public static Sensor findSensor(long id) {
        return getSensor(id, false);
    }

    public static Sensor getSensor(@NonNull ID id) {
        return getSensor(id.getId());
    }

    public static Sensor getSensor(int address) {
        return getSensor(address, (byte) 0);
    }

    public static Sensor getSensor(int address, byte dataTypeValue) {
        return getSensor(address, dataTypeValue, 0);
    }

    public static Sensor getSensor(int address, byte dataTypeValue, int dataTypeValueIndex) {
        return getSensor(ID.getId(address, dataTypeValue, dataTypeValueIndex));
    }

    public static Sensor getSensor(long id) {
        return getSensor(id, true);
    }

    private static Sensor getSensor(long id, boolean autoCreate) {
        if (ID.isPhysicalSensor(id)) {
            return getPhysicalSensor(id, autoCreate);
        }
        if (ID.isLogicalSensor(id)) {
            return getLogicalSensor(id, autoCreate);
        }
        if (autoCreate) {
            throw new IllegalArgumentException("id of target sensor may be abnormal");
        }
        return null;
    }

    public static <S extends Sensor> void getSensors(
            @NonNull List<S> sensorCarrier,
            Filter<S> filter,
            @NonNull Class<S> sClass) {
        S s;
        synchronized (SENSOR_MAP) {
            for (Sensor sensor :
                    SENSOR_MAP.values()) {
                if (sClass.isInstance(sensor)) {
                    s = (S) sensor;
                    if (filter == null || filter.match(s)) {
                        sensorCarrier.add(s);
                    }
                }
            }
        }
    }

    public static synchronized <S extends Sensor> int getSensorWithHistoryValuesCount(Class<S> sClass) {
        int count = 0;
        synchronized (SENSOR_MAP) {
            for (Sensor sensor :
                    SENSOR_MAP.values()) {
                if (sClass.isInstance(sensor) && sensor.getInfo().hasHistoryValue()) {
                    ++count;
                }
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

    private static boolean importConfiguration(Map<Byte, PracticalMeasurement.DataType> dataTypes,
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

    public static PracticalMeasurement.DataType getDataType(int address, byte dataTypeValue, boolean autoCreate) {
        Map<Byte, PracticalMeasurement.DataType> dataTypeMap = getDataTypes(address);
        PracticalMeasurement.DataType dataType = dataTypeMap.get(dataTypeValue);
        if (autoCreate && dataType == null) {
            dataType = new PracticalMeasurement.DataType(dataTypeValue);
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

    public static List<PracticalMeasurement.DataType> getBleDataTypes() {
        return new ArrayList<>(BLE_DATA_TYPES.values());
    }

    public static List<PracticalMeasurement.DataType> getEsbDataTypes() {
        return new ArrayList<>(ESB_DATA_TYPES.values());
    }

    private static List<PhysicalSensor.Type> getSensorTypes(int address) {
        return ID.isBleProtocolFamily(address) ? BLE_SENSOR_TYPES : ESB_SENSOR_TYPES;
    }

    private static Map<Byte, PracticalMeasurement.DataType> getDataTypes(int address) {
        return ID.isBleProtocolFamily(address) ? BLE_DATA_TYPES : ESB_DATA_TYPES;
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

    public static synchronized void setValueContainerConfigurationProvider(MeasurementConfigurationProvider provider, boolean isResetConfigurations) {
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

    static MeasurementConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    public interface MeasurementConfigurationProvider extends VirtualMeasurementBuilder {
        <C extends Configuration> C getConfiguration(ID id);
    }

    private static class ConfigurationImporter extends DefaultHandler {

        private static final String DATA_TYPE = "DataType";
        private static final String PARAPHRASES = "paraphrases";
        private static final String SENSOR_TYPE = "SensorType";
        private static final String DATA_TYPE_CUSTOM_NAME = "DataTypeCustomName";

        private Map<Byte, PracticalMeasurement.DataType> mDataTypeMap;
        private PracticalMeasurement.DataType mDataType;
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
        private ErrorStateInterpreter mErrorStateInterpreter;
        private int mErrorPos;

        public Map<Byte, PracticalMeasurement.DataType> getDataTypeMap() {
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
                    mDataType = new PracticalMeasurement.DataType((byte)Integer.parseInt(mBuilder.toString(), 16));
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
                        mDataType = new PracticalMeasurement.DataType(mDataTypeValue);
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
                case "pos":
                    mErrorPos = Integer.parseInt(mBuilder.toString());
                    break;
                case "state":
                    if (mErrorStateInterpreter == null) {
                        mErrorStateInterpreter = new ErrorStateInterpreter();
                    }
                    mErrorStateInterpreter.setState(mErrorPos, mBuilder.toString());
                    break;
                case "ErrorState":
                    mDataType.mInterpreter = mErrorStateInterpreter;
                    mErrorStateInterpreter = null;
                    break;
                default:
                    break;
            }
        }

        private int findMeasureParameter(
                List<PhysicalSensor.Type.MeasureParameter> measureParameters,
                PhysicalSensor.Type.MeasureParameter parameterGetter) {
            return Collections.binarySearch(measureParameters,
                    parameterGetter,
                    MEASURE_PARAMETER_COMPARATOR);
//            int index, size = measureParameters.size();
//            final int threshold = 3;
//            if (size > threshold) {
//                index = Collections.binarySearch(measureParameters,
//                        parameterGetter,
//                        MEASURE_PARAMETER_COMPARATOR);
//            } else {
//                byte dataTypeValue = parameterGetter.mInvolvedDataType.mValue;
//                for (index = 0;index < size;++index) {
//                    if (measureParameters.get(index).mInvolvedDataType.mValue == dataTypeValue) {
//                        break;
//                    }
//                }
//                if (index == size) {
//                    index = -(index + 1);
//                }
//            }
//            return index;
        }

        private static final Comparator<PhysicalSensor.Type.MeasureParameter> MEASURE_PARAMETER_COMPARATOR = new Comparator<PhysicalSensor.Type.MeasureParameter>() {
            @Override
            public int compare(PhysicalSensor.Type.MeasureParameter mp1, PhysicalSensor.Type.MeasureParameter mp2) {
                return mp1.mInvolvedDataType.getAbsValue() - mp2.mInvolvedDataType.getAbsValue();
            }
        };
    }
}
