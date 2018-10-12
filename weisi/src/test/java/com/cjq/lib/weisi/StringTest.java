package com.cjq.lib.weisi;

import org.junit.Test;

import static org.junit.Assert.*;

public class StringTest {

    @Test
    public void subStringIsOriginString() {
        String origin = "haha";
        String sub = origin.substring(0, 2);
        assertEquals(false, origin == sub);
        System.out.println("origin = " + origin);
        System.out.println("sub = " + sub);
    }
}
