package com.cjq.lib.weisi.sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CJQ on 2017/8/31.
 */

public class SensorManager {

    private static final Map<Integer, Sensor> SENSOR_MAP = new HashMap<>();
    private static OnSensorAddedListener onSensorAddedListener;

    private SensorManager() {
    }

    public static synchronized Sensor getSensor(int address) {
        Sensor sensor = SENSOR_MAP.get(address);
        if (sensor == null) {
            sensor = new Sensor(address);
            addSensor(address, sensor);
        }
        return sensor;
    }

    public static synchronized Sensor buildSensor(int address, SensorDecorator decorator) {
        Sensor sensor = SENSOR_MAP.get(address);
        if (sensor == null) {
            sensor = new Sensor(address, decorator);
            addSensor(address, sensor);
        } else {
            sensor.setDecorator(decorator);
        }
        return sensor;
    }

    public static synchronized void getAllSensors(List<Sensor> sensorCarrier) {
        if (sensorCarrier == null) {
            return;
        }
        sensorCarrier.addAll(SENSOR_MAP.values());
    }

    private static void addSensor(int address, Sensor sensor) {
        SENSOR_MAP.put(address, sensor);
        if (onSensorAddedListener != null) {
            onSensorAddedListener.onSensorAdded(sensor);
        }
    }

    public static void setOnSensorAddedListener(OnSensorAddedListener listener) {
        onSensorAddedListener = listener;
    }

    public interface OnSensorAddedListener {
        void onSensorAdded(Sensor sensor);
    }
}
