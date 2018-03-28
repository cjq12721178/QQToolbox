package com.cjq.lib.weisi;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.cjq.lib.weisi.iot.SensorManager;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Created by CJQ on 2018/2/1.
 */

@RunWith(AndroidJUnit4.class)
public class TestSensorManager {

    @Test
    public void importBleSensorConfigurations() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals(true, SensorManager.importBleConfiguration(appContext));
    }

    @Test
    public void importEsbSensorConfigurations() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals(true, SensorManager.importEsbConfiguration(appContext));
    }
}
