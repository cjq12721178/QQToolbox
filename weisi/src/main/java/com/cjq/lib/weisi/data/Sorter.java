package com.cjq.lib.weisi.data;

import android.support.annotation.NonNull;

import com.wsn.lib.wsb.util.ExpandCollections;
import com.wsn.lib.wsb.util.ExpandComparator;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by CJQ on 2018/5/25.
 */

public abstract class Sorter<E> implements Comparator<E> {

    public int add(@NonNull List<E> elements, @NonNull E e) {
        int position = Collections.binarySearch(elements, e, this);
        if (position < 0) {
            position = -position - 1;
            elements.add(position, e);
            return position;
        } else {
            return -1;
        }
    }

    public void sort(@NonNull List<E> elements) {
        Collections.sort(elements, this);
    }

    public int find(@NonNull List<E> elements, E e) {
        return Collections.binarySearch(elements, e, this);
    }
}
