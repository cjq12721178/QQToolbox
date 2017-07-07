package com.cjq.tool.qbox.ui.manager;

/**
 * Created by CJQ on 2017/7/7.
 */

//fragment实现该接口，用于在数据发生变化的时候
//由SwitchableFragmentManager发出通知，onDataSetChanged()具体实现
public interface OnDataSetChangedListener {
    void onDataSetChanged();
}
