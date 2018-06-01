package com.cjq.lib.weisi.data;

import android.os.Parcelable;
import android.support.annotation.NonNull;

/**
 * Created by CJQ on 2018/5/25.
 * 注意：若要使用序列化功能，请实现接口Parcelable
 */

public interface Filter<E> {
    /**
     * 对输入元素进行过滤
     * @param e
     * @return  若匹配返回true
     */
    boolean match(@NonNull E e);
}
