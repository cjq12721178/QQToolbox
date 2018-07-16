package com.cjq.lib.weisi;

import android.support.annotation.NonNull;

import com.cjq.lib.weisi.data.Filter;
import com.cjq.lib.weisi.iot.LogicalSensor;
import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.cjq.lib.weisi.iot.Sensor;
import com.cjq.lib.weisi.util.SimpleReflection;

import org.junit.Test;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by CJQ on 2018/3/28.
 */

public class ReflectionTest {

    @Test
    public void getInterfaceParameterizedType() {
        assertEquals(Sensor.class, getFilterParameterizedType(new Filter<Sensor>() {
            @Override
            public boolean match(@NonNull Sensor sensor) {
                return false;
            }
        }));
        assertEquals(PhysicalSensor.class, getFilterParameterizedType(new Filter<PhysicalSensor>() {
            @Override
            public boolean match(@NonNull PhysicalSensor sensor) {
                return false;
            }
        }));
        assertEquals(LogicalSensor.class, getFilterParameterizedType(new Filter<LogicalSensor>() {
            @Override
            public boolean match(@NonNull LogicalSensor sensor) {
                return false;
            }
        }));
        assertEquals(PhysicalSensor.class, getFilterParameterizedType(new PhysicalSensorFilter()));
        assertEquals(PhysicalSensor.class, getFilterParameterizedType(new ChildPhysicalSensorFilter()));
    }

    public interface FirstInterface {

    }

    private static class PhysicalSensorFilter implements FirstInterface, Filter<PhysicalSensor> {

        @Override
        public boolean match(@NonNull PhysicalSensor sensor) {
            return false;
        }
    }

    private static class ChildPhysicalSensorFilter extends PhysicalSensorFilter {

    }

    public <S extends Sensor> Class<S> getFilterParameterizedType(Filter<S> filter) {
        return (Class<S>) SimpleReflection.getInterfaceParameterizedType(filter, Filter.class, 0);
    }

    @Test
    public void test_printT() {
        List<Child1> child1s = new ArrayList<>();
        List<Child2> child2s = new ArrayList<>();
        printT(child1s);
        printT(child2s);
    }

    public <E extends Base> void printT(@NonNull List<E> list) {
        Class<E> eClass = (Class<E>) ((ParameterizedType) list.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        System.out.println(eClass.getSimpleName());
    }

    private static class Base {

    }

    private static class Child1 extends Base {

    }

    private static class Child2 extends Base {

    }
}
