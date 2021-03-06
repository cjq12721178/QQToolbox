package com.cjq.tool.qqtoolbox.switchable_fragment_manager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.util.DebugTag;

/**
 * Created by CJQ on 2017/7/6.
 */

public class VisualFragment3 extends VisualFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visual3, null);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        ClosableLog.d(DebugTag.SWITCHABLE_FRAGMENT_MANAGER, "visual fragment3 onHiddenChanged " + hidden);
    }
}
