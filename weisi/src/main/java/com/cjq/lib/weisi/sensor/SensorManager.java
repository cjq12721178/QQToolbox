package com.cjq.lib.weisi.sensor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by CJQ on 2017/8/31.
 */

public class SensorManager {

    private static final Map<Integer, Sensor> SENSOR_MAP = new HashMap<>();
    private static OnSensorCreateListener onSensorCreateListener;

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
                if (onSensorCreateListener != null) {
                    onSensorCreateListener.onSensorCreate(sensor);
                }
            } else {
                sensor.setDecorator(decorator);
            }
        }
        return sensor;
    }

    public static synchronized void getAllSensors(List<Sensor> sensorCarrier) {
        if (sensorCarrier == null) {
            return;
        }
        sensorCarrier.addAll(SENSOR_MAP.values());
    }

    public static synchronized void setOnSensorCreateListener(OnSensorCreateListener listener) {
        onSensorCreateListener = listener;
    }

    public interface OnSensorCreateListener {
        void onSensorCreate(Sensor sensor);
    }
}
