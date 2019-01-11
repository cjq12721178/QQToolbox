package com.cjq.lib.weisi;

import org.junit.Test;

import static org.junit.Assert.*;

public class CalculatorTest {

    @Test
    public void getBinarySearchResult() {
        int result = -4;
        int expect = - result - 1;
        int actual = ~result;
        assertEquals(expect, actual);
    }
}
