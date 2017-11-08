package com.cjq.lib.weisi.sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CJQ on 2017/8/31.
 */

public class SensorManager {

    private static final int DEFAULT_DYNAMIC_SENSOR_MAX_VALUE_SIZE = 50;
    private static final Map<Integer, Sensor> SENSOR_MAP = new HashMap<>();
    static OnSensorRawValueCaptureListener onSensorRawValueCaptureListener;

    private SensorManager() {
    }

    public static void setOnSensorRawValueCaptureListener(OnSensorRawValueCaptureListener listener) {
        SensorManager.onSensorRawValueCaptureListener = listener;
    }

    public static Sensor getDynamicSensor(int address, boolean autoCreate) {
        return getDynamicSensor(address, null, autoCreate);
    }

    public static Sensor createDynamicSensor(int address, SensorDecorator decorator) {
        return getDynamicSensor(address, decorator, true);
    }

    private static synchronized Sensor getDynamicSensor(int address, SensorDecorator decorator, boolean autoCreate) {
        Sensor sensor = SENSOR_MAP.get(address);
        if (autoCreate) {
            if (sensor == null) {
                sensor = new Sensor(address, decorator, DEFAULT_DYNAMIC_SENSOR_MAX_VALUE_SIZE);
                SENSOR_MAP.put(address, sensor);
            } else {
                sensor.setDecorator(decorator);
            }
        }
        return sensor;
    }

    public static Sensor createStaticSensor(int address, SensorDecorator decorator) {
        return new Sensor(address, decorator, 0);
    }

    public static synchronized void getAllSensors(List<Sensor> sensorCarrier, Filter filter) {
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

    public interface OnSensorRawValueCaptureListener {
        void onSensorRawValueCapture(int address,
                                     byte dataTypeValue,
                                     int dataTypeValueIndex,
                                     long timestamp,
                                     float batteryVoltage,
                                     double rawValue);
    }
}
