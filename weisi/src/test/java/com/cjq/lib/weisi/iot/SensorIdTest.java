package com.cjq.lib.weisi.iot;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by CJQ on 2018/3/28.
 */

public class SensorIdTest {

    @Test
    public void idEquals() {
        int address = 0x80FF06;
        Long idValue = ID.getId(address);
        ID id = new ID(address);
        boolean expect = true;
        boolean actual = idValue.equals(id);
        //boolean actual = id.equals(idValue);
        assertEquals(expect, actual);
    }
}
