package com.wsn.lib.wsb.util

import java.util.RandomAccess

/**
 * Created by CJQ on 2017/12/15.
 */

object ExpandCollections {

    private const val BINARY_SEARCH_THRESHOLD = 5000

    fun <V, K> binarySearch(list: List<V>, key: K, c: ExpandComparator<in V, K>): Int {
        return binarySearch(list, 0, list.size - 1, key, c)
    }

    fun <V, K> binarySearch(list: List<V>, start: Int, end: Int, key: K, c: ExpandComparator<in V, K>): Int {
        return if (list is RandomAccess || start + BINARY_SEARCH_THRESHOLD <= end)
            ExpandCollections.indexedBinarySearch(list, start, end, key, c)
        else
            ExpandCollections.iteratorBinarySearch(list, start, end, key, c)
    }

    private fun <V, K> indexedBinarySearch(l: List<V>, start: Int, end: Int, key: K, c: ExpandComparator<in V, K>): Int {
        var low = start
        var high = end

        while (low <= high) {
            val mid = (low + high).ushr(1)
            val midVal = l[mid]
            val cmp = c.compare(midVal, key)

            if (cmp < 0)
                low = mid + 1
            else if (cmp > 0)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1)  // key not found
    }

    private fun <V, K> iteratorBinarySearch(l: List<V>, start: Int, end: Int, key: K, c: ExpandComparator<in V, K>): Int {
        var low = start
        var high = end
        val i = l.listIterator()

        while (low <= high) {
            val mid = (low + high).ushr(1)
            val midVal = get(i, mid)
            val cmp = c.compare(midVal, key)

            if (cmp < 0)
                low = mid + 1
            else if (cmp > 0)
                high = mid - 1
            else
                return mid // key found
        }
        return -(low + 1)  // key not found
    }

    private operator fun <V> get(i: ListIterator<V>, index: Int): V {
        var obj: V
        var pos = i.nextIndex()
        if (pos <= index) {
            do {
                obj = i.next()
            } while (pos++ < index)
        } else {
            do {
                obj = i.previous()
            } while (--pos > index)
        }
        return obj
    }
}
