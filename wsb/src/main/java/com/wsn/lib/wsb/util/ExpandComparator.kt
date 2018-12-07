package com.wsn.lib.wsb.util

/**
 * Created by CJQ on 2017/12/15.
 */

interface ExpandComparator<A, B> {
    fun compare(a: A, b: B): Int
}
