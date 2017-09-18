package com.cjq.lib.weisi.sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CJQ on 2017/8/31.
 */

public class SensorManager {

    private static final Map<Integer, Sensor> SENSOR_MAP = new HashMap<>();

    private SensorManager() {
    }

    public static Sensor getSensor(int address) {
        return getSensor(address, null, false);
    }

    public static Sensor createSensor(int address, SensorDecorator decorator) {
        return getSensor(address, decorator, true);
    }

    private static synchronized Sensor getSensor(int address, SensorDecorator decorator, boolean autoCreate) {
        Sensor sensor = SENSOR_MAP.get(address);
        if (autoCreate) {
            if (sensor == null) {
                sensor = new Sensor(address, decorator);
                SENSOR_MAP.put(address, sensor);
            } else {
                sensor.setDecorator(decorator);
            }
        }
        return sensor;
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
}
