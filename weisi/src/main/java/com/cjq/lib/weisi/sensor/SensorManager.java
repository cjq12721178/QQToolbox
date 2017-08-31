package com.cjq.lib.weisi.sensor;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by CJQ on 2017/8/31.
 */

public class SensorManager {

    private static final Map<Integer, Sensor> SENSOR_MAP = new HashMap<>();

    public static Sensor getSensor(int address) {
        Sensor sensor = SENSOR_MAP.get(address);
        if (sensor == null) {
            sensor = new Sensor(address);
            SENSOR_MAP.put(address, sensor);
        }
        return sensor;
    }

    public static Sensor buildSensor(int address, SensorDecorator decorator) {
        Sensor sensor = SENSOR_MAP.get(address);
        if (sensor == null) {
            sensor = new Sensor(address, decorator);
            SENSOR_MAP.put(address, sensor);
        } else {
            sensor.setDecorator(decorator);
        }
        return sensor;
    }
}
