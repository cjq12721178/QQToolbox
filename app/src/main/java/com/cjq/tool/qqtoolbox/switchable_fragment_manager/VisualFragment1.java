package com.cjq.tool.qqtoolbox.switchable_fragment_manager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cjq.tool.qbox.ui.manager.OnDataSetChangedListener;
import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.util.DebugTag;

/**
 * Created by CJQ on 2017/7/6.
 */

public class VisualFragment1 extends VisualFragment {

    private TextView mTvLabel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_visual1, null);
        mTvLabel = (TextView) view.findViewById(R.id.tv_label);
        ClosableLog.d(DebugTag.SWITCHABLE_FRAGMENT_MANAGER, "visual fragment1 onCreateView");
        //onDataSetChanged();
        return view;
    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        ClosableLog.d(DebugTag.SWITCHABLE_FRAGMENT_MANAGER, "visual fragment1 onResume");
//    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        ClosableLog.d(DebugTag.SWITCHABLE_FRAGMENT_MANAGER, "visual fragment1 onHiddenChanged " + hidden);
    }

    @Override
    public void onDataSetChanged() {
        String text;
        if (mStudent == null) {
            text = "name = null, age = null";
            ClosableLog.d(DebugTag.SWITCHABLE_FRAGMENT_MANAGER, text);
            mTvLabel.setText(text);
        } else {
            text = "name = " + mStudent.getName() + ", age = " + mStudent.getAge();
            ClosableLog.d(DebugTag.SWITCHABLE_FRAGMENT_MANAGER, text);
            mTvLabel.setText(text);
        }
    }
}
