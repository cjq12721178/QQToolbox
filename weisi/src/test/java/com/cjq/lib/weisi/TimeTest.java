package com.cjq.lib.weisi;

import org.junit.Test;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class TimeTest {

    @Test
    public void aheadOneMonth() {
        long t = System.currentTimeMillis();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(t);
        System.out.println(c);
        c.add(Calendar.MONTH, -1);
        System.out.println(c);
        assertEquals(t, c.getTimeInMillis() + TimeUnit.DAYS.toMillis(30));
    }

    @Test
    public void lagOneMonth() {
//        long t = System.currentTimeMillis();
//        Calendar c = Calendar.getInstance();
//        c.setTimeInMillis(t);
        Calendar c = Calendar.getInstance();
        long t = c.getTimeInMillis();
        System.out.println(c);
        System.out.println(c.getTime());
        c.add(Calendar.MONTH, 1);
        System.out.println(c);
        System.out.println(c.getTime());
        assertEquals(t, c.getTimeInMillis() - TimeUnit.DAYS.toMillis(30));
    }
}
