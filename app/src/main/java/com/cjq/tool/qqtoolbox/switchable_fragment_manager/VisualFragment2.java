package com.cjq.tool.qqtoolbox.switchable_fragment_manager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cjq.tool.qbox.ui.manager.OnDataSetChangedListener;
import com.cjq.tool.qqtoolbox.R;

/**
 * Created by CJQ on 2017/7/6.
 */

public class VisualFragment2 extends VisualFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visual2, null);
    }

    @Override
    public void onDataSetChanged() {

    }
}
