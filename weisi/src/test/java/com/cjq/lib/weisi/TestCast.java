package com.cjq.lib.weisi;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TestCast {

    @Test
    public void castNull() {
        assertEquals(null, getNullB());
    }

    private B getNullB() {
        Map<Integer, A> aMap = new HashMap<>();
        return (B) aMap.get(1);
    }

    private static class A {

    }

    private static class B extends A {

    }
}
