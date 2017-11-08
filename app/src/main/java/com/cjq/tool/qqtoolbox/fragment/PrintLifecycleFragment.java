package com.cjq.tool.qqtoolbox.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.util.DebugTag;

/**
 * Created by CJQ on 2017/11/8.
 */

public class PrintLifecycleFragment extends Fragment {

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onCreate");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onSaveInstanceState");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onCreateView");
        return inflater.inflate(R.layout.fragment_lifecycle, null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onStart");
    }

    @Override
    public void onPause() {
        super.onPause();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onDestroy");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onViewStateRestored");
    }

    @Override
    public void onResume() {
        super.onResume();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onResume");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onDestroyView");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "fragment onDetach");
    }
}
