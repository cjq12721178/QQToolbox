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
}
