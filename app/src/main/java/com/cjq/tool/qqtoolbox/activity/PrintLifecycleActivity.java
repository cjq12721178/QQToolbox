package com.cjq.tool.qqtoolbox.activity;

import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;

import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.fragment.PrintLifecycleFragment;
import com.cjq.tool.qqtoolbox.util.DebugTag;

public class PrintLifecycleActivity extends AppCompatActivity {

    //private Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "activity onCreate");
        setContentView(R.layout.activity_print_lifecycle);
//        if (savedInstanceState != null) {
//            mFragment = getSupportFragmentManager()
//                    .findFragmentById(R.id.fragment_lifecycle);
//        } else {
//            mFragment = new PrintLifecycleFragment();
//            getSupportFragmentManager()
//                    .beginTransaction()
//                    .add(mFragment, "lifecycle")
//                    .commit();
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "activity onCreateOptionsMenu");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "activity onSaveInstanceState outPersistentState");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "activity onSaveInstanceState");
    }

    @Override
    protected void onStart() {
        super.onStart();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "activity onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "activity onStop");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "activity onRestoreInstanceState");
    }

    @Override
    protected void onResume() {
        super.onResume();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "activity onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "activity onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        ClosableLog.d(DebugTag.PRINT_LIFECYCLE, "activity onPause");
    }
}
