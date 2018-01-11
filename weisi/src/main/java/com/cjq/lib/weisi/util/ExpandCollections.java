package com.cjq.lib.weisi.util;

import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * Created by CJQ on 2017/12/15.
 */

public class ExpandCollections {

    private static final int BINARY_SEARCH_THRESHOLD = 5000;

    @SuppressWarnings("unchecked")
    public static <V, K> int binarySearch(List<? extends V> list, K key, ExpandComparator<? super V, K> c) {
        return binarySearch(list, 0, list.size() - 1, key, c);
    }

    @SuppressWarnings("unchecked")
    public static <V, K> int binarySearch(List<? extends V> list, int start, int end, K key, ExpandComparator<? super V, K> c) {
        if (list instanceof RandomAccess || start + BINARY_SEARCH_THRESHOLD <= end)
            return ExpandCollections.indexedBinarySearch(list, start, end, key, c);
        else
            return ExpandCollections.iteratorBinarySearch(list, start, end, key, c);
    }

    private static <V, K> int indexedBinarySearch(List<? extends V> l, int start, int end, K key, ExpandComparator<? super V, K> c) {
        int low = start;
        int high = end;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            V midVal = l.get(mid);
            int cmp = c.compare(midVal, key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

    private static <V, K> int iteratorBinarySearch(List<? extends V> l, int start, int end, K key, ExpandComparator<? super V, K> c) {
        int low = start;
        int high = end;
        ListIterator<? extends V> i = l.listIterator();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            V midVal = get(i, mid);
            int cmp = c.compare(midVal, key);

            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return -(low + 1);  // key not found
    }

    private static <V> V get(ListIterator<? extends V> i, int index) {
        V obj;
        int pos = i.nextIndex();
        if (pos <= index) {
            do {
                obj = i.next();
            } while (pos++ < index);
        } else {
            do {
                obj = i.previous();
            } while (--pos > index);
        }
        return obj;
    }
}
