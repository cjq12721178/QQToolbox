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

    private SensorManager() {
    }

    public static Sensor getSensor(int address, boolean autoCreate) {
        return getSensor(address, null, autoCreate);
    }

    public static Sensor createSensor(int address, SensorDecorator decorator) {
        return getSensor(address, decorator, true);
    }

    private static synchronized Sensor getSensor(int address, SensorDecorator decorator, boolean autoCreate) {
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

    public static synchronized void getSensors(List<Sensor> sensorCarrier, Filter filter) {
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
}
