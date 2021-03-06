package com.cjq.lib.weisi.iot;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.cjq.lib.weisi.data.Filter;
import com.cjq.lib.weisi.iot.config.Configuration;
import com.cjq.lib.weisi.iot.corrector.ScriptValueCorrector;
import com.cjq.lib.weisi.iot.interpreter.CalendarInterpreter;
import com.cjq.lib.weisi.iot.interpreter.ErrorStateInterpreter;
import com.cjq.lib.weisi.iot.interpreter.FloatInterpreter;
import com.cjq.lib.weisi.iot.interpreter.GroundLeadInterpreter;
import com.cjq.lib.weisi.iot.interpreter.ParaphraseInterpreter;
import com.cjq.lib.weisi.iot.interpreter.StatusInterpreter;
import com.cjq.lib.weisi.iot.interpreter.ValueInterpreter;
import com.wsn.lib.wsb.util.ExpandCollections;
import com.wsn.lib.wsb.util.ExpandComparator;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CJQ on 2017/8/31.
 */

public class SensorManager {

    public static final int USE_MODE_MOBILE = 0;
    public static final int USE_MODE_WEAR = 1;
    static int mode = USE_MODE_MOBILE;

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
        throw new UnsupportedOperationException("SensorManager is not allowed to instantiate");
    }

    //注意：该方法最好在所有SensorManager方法被调用之前使用
    public static boolean init(@NonNull Context context, int useMode) {
        mode = useMode;
        return importEsbConfiguration(context) && importBleConfiguration(context);
    }

    public static void addDynamicSensorValue(int address,
                                             byte dataTypeValue,
                                             int dataTypeValueIndex,
                                             long timestamp,
                                             float batteryVoltage,
                                             double rawValue) {
        addDynamicSensorValue(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue, null);
    }

    public static void addDynamicSensorValue(int address,
                                             byte dataTypeValue,
                                             int dataTypeValueIndex,
                                             long timestamp,
                                             float batteryVoltage,
                                             double rawValue,
                                             Sensor.OnValueAchievedListener listener) {
        Sensor sensor = getSensor(address, dataTypeValue, dataTypeValueIndex);
        if (sensor != null) {
            sensor.addDynamicValue(dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue, listener);
        }
    }

    public static void addHistorySensorValue(int address,
                                             byte dataTypeValue,
                                             int dataTypeValueIndex,
                                             long timestamp,
                                             float batteryVoltage,
                                             double rawValue) {
        addHistorySensorValue(address, dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue, null);
    }

    public static void addHistorySensorValue(int address,
                                             byte dataTypeValue,
                                             int dataTypeValueIndex,
                                             long timestamp,
                                             float batteryVoltage,
                                             double rawValue,
                                             Sensor.OnValueAchievedListener listener) {
        Sensor sensor = getSensor(address, dataTypeValue, dataTypeValueIndex);
        if (sensor != null) {
            sensor.addHistoryValue(dataTypeValue, dataTypeValueIndex, timestamp, batteryVoltage, rawValue, listener);
        }
    }

    public static void addHistorySensorInfoValue(int address,
                                                 long timestamp,
                                                 float batteryVoltage) {
        addHistorySensorInfoValue(address, timestamp, batteryVoltage, null);
    }

    public static void addHistorySensorInfoValue(int address,
                                                 long timestamp,
                                                 float batteryVoltage,
                                                 Sensor.OnValueAchievedListener listener) {
        getPhysicalSensor(address).addHistoryInfoValue(timestamp, batteryVoltage, listener);
    }

    public static void addHistoryMeasurementValue(long measurementId,
                                                  long timestamp,
                                                  double rawValue) {
        addHistoryMeasurementValue(measurementId, timestamp, rawValue, null);
    }

    public static void addHistoryMeasurementValue(long measurementId,
                                                  long timestamp,
                                                  double rawValue,
                                                  Sensor.OnValueAchievedListener listener) {
        LogicalSensor sensor = getLogicalSensor(measurementId);
        if (sensor != null) {
            sensor.addHistoryMeasurementValue(sensor.getPracticalMeasurement(), timestamp, rawValue, listener);
        }
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

    private static @Nullable Sensor.Info getSensorInfo(long id, boolean autoCreate) {
        return getSensorInfo(id, autoCreate, autoCreate ? findSensorType(ID.getAddress(id)) : null);
    }

    private static @Nullable Sensor.Info getSensorInfo(long id, boolean autoCreate, @Nullable PhysicalSensor.Type type) {
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

    public static @Nullable PracticalMeasurement getPracticalMeasurement(@NonNull ID id) {
        return getPracticalMeasurement(id.getId());
    }

    public static @Nullable PracticalMeasurement getPracticalMeasurement(int address, byte dataTypeValue, int dataTypeValueIndex) {
        return getPracticalMeasurement(ID.getId(address, dataTypeValue, dataTypeValueIndex));
    }

    public static @Nullable PracticalMeasurement getPracticalMeasurement(int address, byte dataTypeValue) {
        return getPracticalMeasurement(address, dataTypeValue, 0);
    }

    public static @Nullable PracticalMeasurement getPracticalMeasurement(long id) {
        return getPracticalMeasurement(id, true);
    }

    private static @Nullable PracticalMeasurement getPracticalMeasurement(long id, boolean autoCreate) {
        return getPracticalMeasurement(id, autoCreate, autoCreate ? findSensorType(ID.getAddress(id)) : null);
    }

    private static @Nullable
    PhysicalSensor.Type.PracticalMeasurementParameter findPracticalMeasurementParameter(long id, @Nullable PhysicalSensor.Type type) {
        PhysicalSensor.Type.PracticalMeasurementParameter parameter = null;
        if (type != null) {
            byte dataTypeValue = ID.getDataTypeValue(id);
            int dataTypeValueIndex = ID.getDataTypeValueIndex(id);
            for (int i = 0; i < type.mPracticalMeasurementParameters.length; ++i) {
                parameter = type.mPracticalMeasurementParameters[i];
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

    private static @Nullable PracticalMeasurement getPracticalMeasurement(long id, boolean autoCreate, @Nullable PhysicalSensor.Type type) {
        return getPracticalMeasurement(id, autoCreate, autoCreate ? findPracticalMeasurementParameter(id, type) : null);
    }

    private static @Nullable PracticalMeasurement getPracticalMeasurement(long id, boolean autoCreate, @Nullable PhysicalSensor.Type.PracticalMeasurementParameter parameter) {
        if (!ID.isPracticalMeasurement(id)) {
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

    private static @NonNull PracticalMeasurement createPracticalMeasurement(@NonNull ID id, @Nullable PhysicalSensor.Type.PracticalMeasurementParameter parameter) {
        if (parameter != null) {
            return new PracticalMeasurement(id,
                    parameter.mInvolvedDataType,
                    parameter.mDataTypeAccurateName,
                    parameter.mHideMeasurement);
        } else {
            return new PracticalMeasurement(id,
                    getDataType(id.getAddress(),
                            id.getDataTypeValue(),
                            true),
                    null, false);
        }
    }

    //
    //  获取VirtualMeasurement的相关方法
    //

    public static @Nullable VirtualMeasurement findVirtualMeasurement(int address, int index) {
        return findVirtualMeasurement(ID.getId(address, (byte) 0, index));
    }

    public static @Nullable VirtualMeasurement findVirtualMeasurement(@NonNull ID id) {
        return findVirtualMeasurement(id.getId());
    }

    public static @Nullable VirtualMeasurement findVirtualMeasurement(long id) {
        return getVirtualMeasurement(id, null);
    }

    public static @Nullable VirtualMeasurement getVirtualMeasurement(int address, int index) {
        return getVirtualMeasurement(ID.getId(address, (byte) 0, index));
    }

    public static @Nullable VirtualMeasurement getVirtualMeasurement(@NonNull ID id) {
        return getVirtualMeasurement(id.getId());
    }

    public static @Nullable VirtualMeasurement getVirtualMeasurement(long id) {
        return getVirtualMeasurement(id, findVirtualMeasurementParameter(id));
    }

    private static PhysicalSensor.Type.VirtualMeasurementParameter findVirtualMeasurementParameter(long id) {
        PhysicalSensor.Type type = findSensorType(ID.getAddress(id));
        int index = ID.getDataTypeValueIndex(id);
        int size = type.getVirtualMeasurementParameterSize();
        if (type != null && index > 0 && index <= size) {
            return type.mVirtualMeasurementParameters.get(index - 1);
        } else {
            return null;
        }
    }

    private static VirtualMeasurement getVirtualMeasurement(long id, PhysicalSensor.Type.VirtualMeasurementParameter parameter) {
        if (!ID.isVirtualMeasurement(id)) {
            return null;
        }
        synchronized (MEASUREMENT_MAP) {
            Measurement measurement = MEASUREMENT_MAP.get(id);
            if (measurement == null && parameter != null) {
                VirtualMeasurement virtualMeasurement = createVirtualMeasurement(new ID(id), parameter);
                if (virtualMeasurement != null) {
                    MEASUREMENT_MAP.put(id, virtualMeasurement);
                }
                return virtualMeasurement;
            }
            return (VirtualMeasurement) measurement;
        }
    }

    private static @Nullable VirtualMeasurement createVirtualMeasurement(@NonNull ID id, @NonNull PhysicalSensor.Type.VirtualMeasurementParameter parameter) {
        switch (parameter.mMeasurementType) {
            case "RWA": {
                PracticalMeasurement host = getPracticalMeasurement(id.getAddress(), (byte) 0x70);
                return host != null
                        ? new RatchetWheelMeasurementA(id,
                        parameter.mMeasurementName,
                        parameter.mCurveType,
                        parameter.mValueInterpreter,
                        parameter.mHideMeasurement,
                        host)
                        : null;
            }
            case "RWB": {
                PracticalMeasurement host = getPracticalMeasurement(id.getAddress(), (byte) 0x70);
                return host != null
                        ? new RatchetWheelMeasurementB(id,
                        parameter.mMeasurementName,
                        parameter.mCurveType,
                        parameter.mValueInterpreter,
                        parameter.mHideMeasurement,
                        host)
                        : null;
            }
            default:
                return null;
        }
    }

    //
    //  获取Measurement的相关方法
    //

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
            return autoCreate
                    ? getVirtualMeasurement(id)
                    : findVirtualMeasurement(id);
        }
        return null;
    }

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
            measurements = new ArrayList<>(type.getMeasurementParameterSize());
            //生成实测型测量量
            PhysicalSensor.Type.PracticalMeasurementParameter pmp;
            PracticalMeasurement practicalMeasurement;
            for (int i = 0, dataTypeValueIndex; i < type.mPracticalMeasurementParameters.length; ++i) {
                pmp = type.mPracticalMeasurementParameters[i];
                byte dataTypeValue = pmp.mInvolvedDataType.mValue;
                dataTypeValueIndex = 0;
                do {
                    practicalMeasurement = getPracticalMeasurement(ID.getId(address, dataTypeValue, dataTypeValueIndex++), true, pmp);
                    if (practicalMeasurement != null) {
                        measurements.add(practicalMeasurement);
                    }
                    pmp = pmp.mNext;
                } while (pmp != null);
            }
            //生成虚拟型测量量
            PhysicalSensor.Type.VirtualMeasurementParameter vmp;
            VirtualMeasurement virtualMeasurement;
            if (type.mVirtualMeasurementParameters != null) {
                for (int i = 0;i < type.mVirtualMeasurementParameters.size();++i) {
                    vmp = type.mVirtualMeasurementParameters.get(i);
                    virtualMeasurement = getVirtualMeasurement(ID.getId(address, (byte) 0, i + 1), vmp);
                    if (virtualMeasurement != null) {
                        measurements.add(virtualMeasurement);
                    }
                }
            }
        } else {
            measurements = new ArrayList<>();
        }
        return new PhysicalSensor(info, type, measurements);
    }

    //
    //  获取LogicalSensor的相关方法
    //

    public static @Nullable LogicalSensor findLogicalSensor(int address, byte dataTypeValue) {
        return findLogicalSensor(address, dataTypeValue, 0);
    }

    public static @Nullable LogicalSensor findLogicalSensor(int address, byte dataTypeValue, int dataTypeValueIndex) {
        return findLogicalSensor(ID.getId(address, dataTypeValue, dataTypeValueIndex));
    }

    public static @Nullable LogicalSensor findLogicalSensor(@NonNull ID id) {
        return findLogicalSensor(id.getId());
    }

    public static @Nullable LogicalSensor findLogicalSensor(long id) {
        return getLogicalSensor(id, false);
    }

    public static @Nullable LogicalSensor getLogicalSensor(int address, byte dataTypeValue) {
        return getLogicalSensor(address, dataTypeValue, 0);
    }

    public static @Nullable LogicalSensor getLogicalSensor(int address, byte dataTypeValue, int dataTypeValueIndex) {
        return getLogicalSensor(ID.getId(address, dataTypeValue, dataTypeValueIndex));
    }

    public static @Nullable LogicalSensor getLogicalSensor(@NonNull ID id) {
        return getLogicalSensor(id.getId());
    }

    public static @Nullable LogicalSensor getLogicalSensor(long id) {
        return getLogicalSensor(id, true);
    }

    private static @Nullable LogicalSensor getLogicalSensor(long id, boolean autoCreate) {
        if (!ID.isLogicalSensor(id)) {
            return null;
        }
        synchronized (SENSOR_MAP) {
            Sensor sensor = SENSOR_MAP.get(id);
            if (sensor == null && autoCreate) {
                LogicalSensor logicalSensor = createLogicalSensor(new ID(id));
                if (logicalSensor != null) {
                    SENSOR_MAP.put(id, logicalSensor);
                }
                return logicalSensor;
            }
            return (LogicalSensor) sensor;
        }
    }

    private static @Nullable LogicalSensor createLogicalSensor(@NonNull ID id) {
        int address = id.getAddress();
        PhysicalSensor.Type type = findSensorType(address);
        PracticalMeasurement measurement = getPracticalMeasurement(id.getId(), true, type);
        if (measurement == null) {
            return null;
        }
        Sensor.Info info = getSensorInfo(id.getId(), true, type);
        return new LogicalSensor(info, measurement);
    }

    //
    //  获取Sensor的相关方法
    //

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
        int position = ExpandCollections.INSTANCE.binarySearch(types,
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
        ConfigurationImporter importer = new ConfigurationImporter();
        try {
            return importer.leadIn(context.getAssets().open(fileName))
                    ? importer
                    : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setValueContainerConfigurationProvider(MeasurementConfigurationProvider provider, boolean isResetConfigurations) {
        if (configurationProvider != provider) {
            configurationProvider = provider;
            if (isResetConfigurations) {
                synchronized (MEASUREMENT_MAP) {
                    for (Measurement measurement
                            : MEASUREMENT_MAP.values()) {
                        measurement.resetConfiguration();
                    }
                }
            }
//            if (isResetConfigurations) {
//                for (Sensor sensor
//                        : SENSOR_MAP.values()) {
//                    sensor.resetConfiguration();
//                }
//            }
        }
    }

    static MeasurementConfigurationProvider getMeasurementConfigurationProvider() {
        return configurationProvider;
    }

    public interface MeasurementConfigurationProvider extends Parcelable {
        <C extends Configuration> C getConfiguration(ID id);
        @NonNull List<ID> getConfigurationIds();
        //按ID从小到大排列
        @NonNull List<Configuration> getConfigurationsSortedById();
        @NonNull List<Configuration> getSensorInfoConfigurations();
        @NonNull List<DisplayMeasurement.Configuration> getDisplayMeasurementConfigurations();
    }

    private static class ConfigurationImporter extends com.wsn.lib.wsb.config.ConfigurationImporter {

        private static final String PARAPHRASES = "paraphrases";
        private static final String SENSOR_TYPE = "SensorType";
        private static final String DATA_TYPE_CUSTOM_NAME = "DataTypeCustomName";

        private Map<Byte, PracticalMeasurement.DataType> mDataTypeMap;
        private PracticalMeasurement.DataType mDataType;
        private Map<Double, String> mParaphrases;
        private Double mNumber;
        private String mText;
        private String mOn;
        private String mOff;
        private List<PhysicalSensor.Type> mTypes;
        private PhysicalSensor.Type mType;
        private List<PhysicalSensor.Type.PracticalMeasurementParameter> mPracticalMeasurementParameters;
        private PhysicalSensor.Type.PracticalMeasurementParameter mPracticalMeasurementParameter;
        private int mIndex;
        private String mDataTypeCustomName;
        private int mCustomDataTypeNameType;
        private ScriptValueCorrector.Builder mScriptValueCorrectorBuilder;
        private String mLabel;
        private ErrorStateInterpreter mErrorStateInterpreter;
        private int mErrorPos;
        private int mDecimal;
        private String mUnit;
        private ValueInterpreter mValueInterpreter;
        private boolean mHiddenMeasurement;
        private String mVirtualMeasurementName;
        private String mVirtualMeasurementPattern;
        private int mCurveType;

        public Map<Byte, PracticalMeasurement.DataType> getDataTypeMap() {
            return mDataTypeMap;
        }

        public List<PhysicalSensor.Type> getTypes() {
            return mTypes;
        }

        @Override
        public void startDocument() {
            mDataTypeMap = new HashMap<>();
            mTypes = new ArrayList<>();
            mPracticalMeasurementParameters = new ArrayList<>();
            mScriptValueCorrectorBuilder = new ScriptValueCorrector.Builder();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);
            if (qName.equals(SENSOR_TYPE)) {
                mType = new PhysicalSensor.Type();
            } else if (qName.equals(PARAPHRASES)) {
                mParaphrases = new HashMap<>();
            } else if (qName.equals(DATA_TYPE_CUSTOM_NAME)) {
                mCustomDataTypeNameType = Integer.parseInt(attributes.getValue("type"));
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);
            if (getElementConsumed()) {
                switch (qName) {
                    case DATA_TYPE:
                        mDataType.setInterpreter(mValueInterpreter);
                        mValueInterpreter = null;
                        mDataTypeMap.put(mDataType.mValue, mDataType);
                        break;
                }
            } else {
                setElementConsumed(true);
                switch (qName) {
                    case "curve":
                        mCurveType = Integer.parseInt(getBuilder().toString(), 16);
                        break;
//                    case "value":
//                        mDataType = new PracticalMeasurement.DataType(getDataTypeValue(), mCurveType);
//                        break;
                    case "name":
                        if (mType != null) {
                            mVirtualMeasurementName = getBuilder().toString();
                        } else {
                            //mDataType.setName(getBuilder().toString());
                            mDataType = new PracticalMeasurement.DataType(getDataTypeValue(), mCurveType, getBuilder().toString());
                        }
                        break;
                    case "decimal":
                        mDecimal = Integer.parseInt(getBuilder().toString());
                        break;
                    case "unit":
                        mUnit = getBuilder().toString();
                        break;
                    case "float":
                        mValueInterpreter = new FloatInterpreter(mDecimal, mUnit);
                        mDecimal = 3;
                        mUnit = null;
                        break;
                    case "SensorName":
                        mType.mSensorGeneralName = getBuilder().toString();
                        break;
                    case "start":
                        mType.mStartAddress = Integer.parseInt(getBuilder().toString(), 16);
                        break;
                    case "end":
                        mType.mEndAddress = Integer.parseInt(getBuilder().toString(), 16);
                        break;
                    case "DataTypeValue":
                        setDataTypeValue((byte)Integer.parseInt(getBuilder().toString(), 16));
                        break;
                    case DATA_TYPE_CUSTOM_NAME:
                        mDataTypeCustomName = getBuilder().toString();
                        break;
                    case "hidden":
                        mHiddenMeasurement = Boolean.parseBoolean(getBuilder().toString());
                        break;
                    case "pattern":
                        mVirtualMeasurementPattern = getBuilder().toString();
                        break;
                    case "measurement":
                        if (getDataTypeValue() != 0) {
                            //生成PracticalMeasurementParameter
                            //获取数据类型
                            PracticalMeasurement.DataType dataType = mDataTypeMap.get(getDataTypeValue());
                            if (dataType == null) {
                                dataType = new PracticalMeasurement.DataType(getDataTypeValue());
                                mDataTypeMap.put(getDataTypeValue(), dataType);
                            }
                            setDataTypeValue((byte) 0);
                            //生成测量参数
                            mPracticalMeasurementParameter = new PhysicalSensor.Type.PracticalMeasurementParameter(dataType,
                                    mDataTypeCustomName != null
                                            ? (mCustomDataTypeNameType == 0
                                            ? dataType.getName() + mDataTypeCustomName
                                            : mDataTypeCustomName)
                                            : null,
                                    mHiddenMeasurement);
                            mDataTypeCustomName = null;
                            mHiddenMeasurement = false;
                            //若存在相同数据类型，则为阵列传感器，使用链式附加，否则按数据类型升序排列
                            mIndex = findMeasureParameter(mPracticalMeasurementParameters, mPracticalMeasurementParameter);
                            if (mIndex >= 0) {
                                mPracticalMeasurementParameters.get(mIndex).getLast().mNext = mPracticalMeasurementParameter;
                            } else {
                                mPracticalMeasurementParameters.add(-mIndex-1, mPracticalMeasurementParameter);
                            }
                        } else {
                            //生成VirtualMeasurementParameter
                            PhysicalSensor.Type.VirtualMeasurementParameter parameter = new PhysicalSensor.Type.VirtualMeasurementParameter(mVirtualMeasurementName, mVirtualMeasurementPattern, mValueInterpreter, mCurveType, mHiddenMeasurement);
                            if (mType.mVirtualMeasurementParameters == null) {
                                mType.mVirtualMeasurementParameters = new ArrayList<>();
                            }
                            mType.mVirtualMeasurementParameters.add(parameter);
                        }
                        break;
                    case "measurements":
                        mType.mPracticalMeasurementParameters = new PhysicalSensor.Type.PracticalMeasurementParameter[mPracticalMeasurementParameters.size()];
                        mPracticalMeasurementParameters.toArray(mType.mPracticalMeasurementParameters);
                        mPracticalMeasurementParameters.clear();
                        break;
                    case SENSOR_TYPE:
                        mTypes.add(mType);
                        mType = null;
                        break;
                    case "on":
                        mOn = getBuilder().toString();
                        break;
                    case "off":
                        mOff = getBuilder().toString();
                        break;
                    case "status":
                        mValueInterpreter = new StatusInterpreter(mOn, mOff);
                        break;
                    case "number":
                        mNumber = Double.parseDouble(getBuilder().toString());
                        break;
                    case "text":
                        mText = getBuilder().toString();
                        break;
                    case "paraphrase":
                        mParaphrases.put(mNumber, mText);
                        break;
                    case PARAPHRASES:
                        mValueInterpreter = new ParaphraseInterpreter(mParaphrases);
                        break;
                    case "calendar":
                        mValueInterpreter = CalendarInterpreter.from(getBuilder().toString());
                        break;
                    case "interpreter":
                        switch (getBuilder().toString()) {
                            case "ground":mValueInterpreter = GroundLeadInterpreter.getInstance();
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
                        mLabel = getBuilder().toString();
                        break;
                    case "function":
                        mScriptValueCorrectorBuilder.putScript(mLabel, getBuilder().toString());
                        break;
                    case "ScriptValueCorrectorLabel":
                        mDataType.mCorrector = mScriptValueCorrectorBuilder.getCorrector(getBuilder().toString());
                        break;
                    case "pos":
                        mErrorPos = Integer.parseInt(getBuilder().toString());
                        break;
                    case "state":
                        if (mErrorStateInterpreter == null) {
                            mErrorStateInterpreter = new ErrorStateInterpreter();
                        }
                        mErrorStateInterpreter.setState(mErrorPos, getBuilder().toString());
                        break;
                    case "ErrorState":
                        mValueInterpreter = mErrorStateInterpreter;
                        mErrorStateInterpreter = null;
                        break;
                    default:
                        setElementConsumed(false);
                        break;
                }
            }
        }

        private int findMeasureParameter(
                List<PhysicalSensor.Type.PracticalMeasurementParameter> practicalMeasurementParameters,
                PhysicalSensor.Type.PracticalMeasurementParameter parameterGetter) {
            return Collections.binarySearch(practicalMeasurementParameters,
                    parameterGetter,
                    MEASURE_PARAMETER_COMPARATOR);
        }

        private static final Comparator<PhysicalSensor.Type.PracticalMeasurementParameter> MEASURE_PARAMETER_COMPARATOR = new Comparator<PhysicalSensor.Type.PracticalMeasurementParameter>() {
            @Override
            public int compare(PhysicalSensor.Type.PracticalMeasurementParameter mp1, PhysicalSensor.Type.PracticalMeasurementParameter mp2) {
                return mp1.mInvolvedDataType.getAbsValue() - mp2.mInvolvedDataType.getAbsValue();
            }
        };
    }
}
