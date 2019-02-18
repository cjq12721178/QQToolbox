package com.cjq.tool.qqtoolbox

import com.cjq.lib.weisi.iot.Configuration
import com.cjq.lib.weisi.iot.ID
import com.cjq.lib.weisi.iot.container.Value
import org.junit.Assert.*
import org.junit.Test

/**
 * Created by CJQ on 2018/1/31.
 */
class TestLazy {

    @Test fun testLazy() {
        val lc = LazyClass()
        var l1 = lc.mLazyMember
        var l2 = lc.mLazyMember
        assertEquals(l1, l2)
    }
}

private class LazyClass {
    val mLazyMember: Int by lazy {
        generateLazyMember()
    }

    private fun generateLazyMember() : Int {
        println("3")
        return 3
    }
}