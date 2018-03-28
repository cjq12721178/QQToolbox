package com.cjq.lib.weisi;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by CJQ on 2018/3/23.
 */
@RunWith(Parameterized.class)
public class TestOrder {

    private static int initDataNo = 0;

    @Parameterized.Parameters
    public static Collection<Object[]> initTestData() {
        System.out.println("init data no = " + initDataNo);
        if (initDataNo == 0) {
            ++initDataNo;
            return Arrays.asList(
                    new Object[][] {
                            { 1 },
                            { 2 }
                    }
            );
        }
        return Arrays.asList(
                new Object[][] {
                        { 3 },
                        { 4 },
                        { 5 }
                }
        );
    }

    public TestOrder(int no) {
        System.out.println("constructor, no = " + no);
    }

    private static int parameterGroupNo = 0;

    @BeforeClass
    public static void prepare() {
        System.out.println();
        System.out.println("parameter group no = " + parameterGroupNo);
        ++parameterGroupNo;
        System.out.println("before class");
    }

    @AfterClass
    public static void release() {
        System.out.println("after class");
    }

    @Before
    public void setUp() throws Exception {
        System.out.println("set up");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("tear down");
    }

    @Test
    public void test1() {
        System.out.println("test1");
    }

    @Test
    public void test2() {
        System.out.println("test2");
    }
}
