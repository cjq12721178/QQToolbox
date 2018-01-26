package com.cjq.tool.qbox.ui.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

/**
 * Created by KAT on 2016/7/18.
 */
public class ConfirmDialog extends BaseDialog<ConfirmDialog.Decorator> {

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int getDefaultContentLayoutRes() {
            return 0;
        }
    }

    @Override
    protected void onSetContentView(View contentView, Decorator decorator, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void setExitType(int type) {
        if (type == EXIT_TYPE_NULL) {
            return;
        }
        super.setExitType(type);
    }

    public void setDrawCancelButton(boolean drawCancelButton) {
        setExitType(drawCancelButton ? EXIT_TYPE_OK_CANCEL : EXIT_TYPE_OK);
    }

//    public int show(FragmentTransaction transaction, String tag, String title, boolean hasCancelButton) {
//        if (!hasCancelButton) {
//            setExitType(EXIT_TYPE_OK);
//        }
//        return super.show(transaction, tag, title);
//    }
//
//    public void show(FragmentManager manager, String tag, String title, boolean hasCancelButton) {
//        if (!hasCancelButton) {
//            setExitType(EXIT_TYPE_OK);
//        }
//        super.show(manager, tag, title);
//    }
//
//    public int show(FragmentTransaction transaction, String tag, @StringRes int titleRes, boolean hasCancelButton) {
//        if (!hasCancelButton) {
//            setExitType(EXIT_TYPE_OK);
//        }
//        return super.show(transaction, tag, titleRes);
//    }
//
//    public void show(FragmentManager manager, String tag, @StringRes int titleRes, boolean hasCancelButton) {
//        if (!hasCancelButton) {
//            setExitType(EXIT_TYPE_OK);
//        }
//        super.show(manager, tag, titleRes);
//    }

//    @Override
//    public int show(FragmentTransaction transaction, String tag, String title) {
//        return show(transaction, tag, title, true);
//    }
//
//    @Override
//    public void show(FragmentManager manager, String tag, String title) {
//        show(manager, tag, title, true);
//    }
//
//    @Override
//    public int show(FragmentTransaction transaction, String tag, @StringRes int titleRes) {
//        return show(transaction, tag, titleRes, true);
//    }
//
//    @Override
//    public void show(FragmentManager manager, String tag, @StringRes int titleRes) {
//        show(manager, tag, titleRes, true);
//    }
}
