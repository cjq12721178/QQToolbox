package com.cjq.tool.qqtoolbox

import org.junit.Assert.*
import org.junit.Test

/**
 * Created by CJQ on 2018/1/31.
 */
class TestPalindrome {
    @Test
    fun testEmptyString() {
        test(true, "")
    }

    @Test
    fun testChar() {
        test(true, "a")
    }

    @Test
    fun testPositive1() {
        test(true, "aba")
    }

    @Test
    fun testPositive2() {
        test(true, "abba")
    }

    @Test
    fun testPositive3() {
        test(true, "abbabba")
    }

    @Test
    fun testPositive4() {
        test(true, "abbaabba")
    }

    @Test
    fun testNegative1() {
        test(false, "ab")
    }

    @Test
    fun testNegative2() {
        test(false, "abab")
    }

    @Test
    fun testNegative3() {
        test(false, "abaa")
    }

    fun test(expected: Boolean, data: String) {
        val actual = isPalindrome(data)
        assertEquals(expected, actual)
        //assertEquals(expected, actual, "\ndata = \"$data\"")
    }

    fun isPalindrome(s: String): Boolean {
        //var mid = s.length shr 1;
//        var topHalfEnd: Int
//        var bottomHalfStart: Int
//        if (mid shl 1 == s.length) {
//            topHalfEnd = mid
//            bottomHalfStart = mid
//        } else {
//            topHalfEnd = mid
//            bottomHalfStart = mid + 1
//        }
        //var bound = if (mid shl 1 == s.length) mid else mid + 1
        //return s.subSequence(0, topHalfEnd).(s.subSequence(bottomHalfStart, s.length).reversed())
        return s.endsWith(s.subSequence(0, s.length shr 1).reversed())
    }
}

