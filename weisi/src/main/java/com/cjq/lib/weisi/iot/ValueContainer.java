package com.cjq.lib.weisi.iot;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by CJQ on 2018/3/16.
 */

public interface ValueContainer<V extends Value> {

    @IntDef({ADD_VALUE_FAILED, NEW_VALUE_ADDED, VALUE_UPDATED, LOOP_VALUE_ADDED})
    @Retention(RetentionPolicy.SOURCE)
    @interface AddResult {
    }

    /**
     * 分为两种情况，一是在传感器中添加数据的时候，未能按照
     * dataTypeValue和dataTypeValueIndex找到相应measurement;
     * 二是在添加动态数据的时候，所要加入的数据早于所有已有数据，
     * 这意味着该次数据添加毫无意义
     */
    int ADD_VALUE_FAILED = 0;

    /**
     * 当添加数据时在数据集中新增一条数据
     */
    int NEW_VALUE_ADDED = 1;

    /**
     * 添加数据时遇到与已有数据集中具有相同timestamp的数据，
     * 则对该条数据的其他信息进行更新
     */
    int VALUE_UPDATED = 2;

    /**
     * 在动态添加数据时，由于采用了循环数组以节省内存空间，
     * 当需要新增数据而数据集规模已达预计最大时，取出最早数据，
     * 并将新数据插入相应位置
     * 一般针对采用循环存储结构的数据容器
     * @see DynamicValueContainer
     */
    int LOOP_VALUE_ADDED = 3;

    /**
     * 添加数据错误时返回该值
     */
    int ADD_FAILED_RETURN_VALUE = Integer.MIN_VALUE;

    /**
     * 根据时间戳将数据按一定顺序添加进数据容器中
     *
     * @param timestamp 待添加{@link Value}的时间戳
     * @return 若返回值大于等于0，则表示数据添加成功，且其在数据容器中的位置为该返回值；
     *          若返回值 == {@link #ADD_FAILED_RETURN_VALUE}，则表示添加错误；
     *          其余情况表示该value所有时间戳已存在于数据容器中，并自动更新该条数据，返回值为-position-1，其中position为重复数据所处位置
     *          注意，该方法返回的是逻辑位置（logicalPosition）,若要获取物理位置（physicalPosition）
     * @see #getPhysicalPositionByLogicalPosition(int)
     */
    int addValue(long timestamp);

    /**
     * 解析{@link #addValue}的返回值
     * @param logicalPosition {@link #addValue}的返回值
     * @return 若logicalPosition大于等于0，返回 {@link #NEW_VALUE_ADDED}，
     *          此处有特殊情况，若容器采用了循环存储的方式，则返回 {@link #LOOP_VALUE_ADDED}
     * @see DynamicValueContainer
     *          若logicalPosition等于{@link #ADD_FAILED_RETURN_VALUE}，返回 {@link #ADD_VALUE_FAILED}
     *          其余情况表示数据更新，返回 {@link #VALUE_UPDATED}
     * @see AddResult
     */
    @AddResult int interpretAddResult(int logicalPosition);

    /**
     * 通过逻辑位置（通常由 {@link #addValue(long)} 得到）获取数据在容器中的物理位置
     * @param logicalPosition 添加数据时得到的逻辑位置
     * @return 若>=0，表示存在，返回数据在容器中的物理位置
     *          若<0，表示不存在，返回-1
     */
    int getPhysicalPositionByLogicalPosition(int logicalPosition);

    /**
     * 获取{@link Value}数量
     * @return 返回{@link Value}数量
     */
    int size();

    /**
     * 是否有{@link Value}
     * @return 你懂的
     */
    boolean empty();

    /**
     * 通过位置获取相应{@link Value}
     * @param physicalPosition 待获取value在数据容器中的物理位置
     * @return value
     */
    V getValue(int physicalPosition);

    /**
     * 获取最早的一条数据
     * @return 最早数据
     */
    V getEarliestValue();

    /**
     * 获取最晚的一条数据
     * @return 最晚数据
     */
    V getLatestValue();

    /**
     * 从数据容器中寻找相同时间戳的某个{@link Value}位置
     *
     * @param timestamp 待寻找value的时间戳
     * @return 若为负数表示没有找到，且其值（-position-1）表示其实际应在数据容器中的物理位置，
     *          若大于等于0表示其在数据容器中的位置
     */
    int findValuePosition(long timestamp);

    /**
     * 从数据容器中寻找相同时间戳的某个{@link Value}
     *
     * @param timestamp 待寻找value的时间戳
     * @return 若为null表示没有找到，否则返回该value
     */
    V findValue(long timestamp);

    /**
     * 从数据容器中的某个位置附近寻找相同时间戳的某个{@link Value}位置
     *
     * @param possiblePosition 可能存在位置，
     *              若>=0，则在possiblePosition附近寻找时间戳等于timestamp的{@link Value}
     *              若<0，则在-possiblePosition-1附近寻找时间戳等于timestamp的{@link Value}
     * @param timestamp 待寻找value的时间戳
     * @return 若为负数表示没有找到，且其值（-position-1）表示其实际应在数据容器中的物理位置，
     *          若大于等于0表示其在数据容器中的位置
     */
    int findValuePosition(int possiblePosition, long timestamp);

    /**
     * 从数据容器中的某个位置附近寻找相同时间戳的某个{@link Value}
     *
     * @param possiblePosition 可能存在位置，可以直接使用 {@link #addValue(long)} 返回的逻辑位置
     *              若>=0，则在possiblePosition附近寻找时间戳等于timestamp的{@link Value}
     *              若<0，则在-possiblePosition-1附近寻找时间戳等于timestamp的{@link Value}
     * @param timestamp 待寻找value的时间戳
     * @return 若为null表示没有找到，否则返回该value
     */
     V findValue(int possiblePosition, long timestamp);

    /**
     * 获取某段时间范围内的子数据容器，当不需要子容器与父容器在新增数据后
     * （即调用了 {@link #addValue} 方法后）保持一致，可以使用
     * {@link #detachSubValueContainer} 方法进行解绑操作
     * 请使用{@link }
     * @param startTime 起始时间（含）
     * @param endTime 结束时间（不含）
     * @return 具有与原数据容器所拥有的所有功能（包括addValue）
     */
     ValueContainer<V> applyForSubValueContainer(long startTime, long endTime);

    /**
     * 父容器与其子容器解除数据一致绑定，之后若父容器新增数据，
     * 则子容器的一些方法将引发未知错误
     * @param subContainer
     */
    void detachSubValueContainer(ValueContainer<V> subContainer);

    /**
     * 注册数据添加监听事件，不用的时候应调用{@link #unregisterOnValueAddListener}
     * @param listener
     */
    void registerOnValueAddListener(@NonNull OnValueAddListener listener);

    /**
     * 注销数据添加监听事件
     */
    void unregisterOnValueAddListener(@NonNull OnValueAddListener listener);

    /**
     * 用于监听数据添加
     */
    interface OnValueAddListener {
        /**
         * 仅当数据正确添加时调用
         * @param logicalPosition {@link #addValue(long)} 返回值
         * @param timestamp 已添加数据时间戳
         */
         void onValueAdd(int logicalPosition, long timestamp);
    }
}
