package com.cjq.test.qbox.ui.dialog;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;

import com.cjq.test.qbox.R;


/**
 * Created by KAT on 2016/11/25.
 */
public class EditDialog extends BaseDialog<EditDialog.Decorator> {

    public interface OnContentReceiver {
        boolean onReceive(EditDialog dialog, String oldValue, String newValue);
    }

    private static final String ARGUMENT_KEY_CONTENT = "in_content";
    private EditText mEtText;

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        public int getContentLayout() {
            return R.layout.dialog_content_edit;
        }

        @IdRes
        public int getEditId() {
            return R.id.il_text;
        }

        @DimenRes
        public int getEditTextSize() {
            return R.dimen.size_text_dialog_view;
        }
    }

    @Override
    protected void onSetContentView(View content, Decorator decorator, @Nullable Bundle savedInstanceState) {
        mEtText = (EditText)content.findViewById(decorator.getEditId());
        mEtText.setText(getArguments().getString(ARGUMENT_KEY_CONTENT));
        if (!decorator.completeCustomForContentView()) {
            Resources resources = getResources();
            mEtText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    resources.getDimensionPixelSize(decorator.getEditTextSize()));
        }
    }

    @Override
    protected boolean onOkClick() {
        OnContentReceiver receiver = getListener(OnContentReceiver.class);
        return receiver != null ?
                receiver.onReceive(this,
                        getArguments().
                        getString(ARGUMENT_KEY_CONTENT),
                        mEtText.getText().toString()) :
                true;
    }

    public void setContent(String content) {
        getArguments().putString(ARGUMENT_KEY_CONTENT, content);
    }

    public void show(FragmentManager manager, String tag, String title, String content) {
        setContent(content);
        super.show(manager, tag, title);
    }

    public int show(FragmentTransaction transaction, String tag, String title, String content) {
        setContent(content);
        return super.show(transaction, tag, title);
    }
}
