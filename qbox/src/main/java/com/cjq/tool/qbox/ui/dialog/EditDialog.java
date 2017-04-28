package com.cjq.tool.qbox.ui.dialog;

import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;

import com.cjq.tool.qbox.R;


/**
 * Created by KAT on 2016/11/25.
 */
public class EditDialog extends BaseDialog<EditDialog.Decorator> {

    public interface OnContentReceiver {
        boolean onReceive(EditDialog dialog, String oldValue, String newValue);
    }

    private static final String ARGUMENT_KEY_CONTENT_STRING = "in_content_string";
    private static final String ARGUMENT_KEY_CONTENT_RESOURCE = "in_content_resource";
    private EditText mEtText;

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        public void reset() {
            super.reset();
            setContentLayout(R.layout.qbox_dialog_content_edit);
            setEditId(R.id.il_text);
        }

        @IdRes
        public int getEditId() {
            return mParameters.getInt("dp_edit_id");
        }

        public void setEditId(@IdRes int editId) {
            mParameters.putInt("dp_edit_id", editId);
        }

        @DimenRes
        public int getEditTextSize() {
            return mParameters.getInt("dp_edit_text_size");
        }

        public void setEditTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("dp_edit_text_size", textSizeRes);
        }
    }

    @Override
    protected void onSetContentView(View contentView,
                                    Decorator decorator,
                                    @Nullable Bundle savedInstanceState) {
        mEtText = (EditText) contentView.findViewById(decorator.getEditId());
        mEtText.setText(getContent());
        int textSizeRes = decorator.getEditTextSize();
        if (textSizeRes != 0) {
            mEtText.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(textSizeRes));
        }
    }

    @Override
    protected boolean onConfirm() {
        OnContentReceiver receiver = getListener(OnContentReceiver.class);
        return receiver != null ?
                receiver.onReceive(this,
                        getArguments().
                        getString(ARGUMENT_KEY_CONTENT_STRING),
                        mEtText.getText().toString()) :
                true;
    }

    public void setContent(String content) {
        getArguments().putString(ARGUMENT_KEY_CONTENT_STRING, content);
    }

    public void setContent(@StringRes int contentRes) {
        getArguments().putInt(ARGUMENT_KEY_CONTENT_RESOURCE, contentRes);
    }

    private String getContent() {
        int contentRes = getArguments().getInt(ARGUMENT_KEY_CONTENT_RESOURCE);
        return contentRes != 0 ?
                getString(contentRes) :
                getArguments().getString(ARGUMENT_KEY_CONTENT_STRING);
    }

    public void show(FragmentManager manager, String tag, String title, String content) {
        setContent(content);
        super.show(manager, tag, title);
    }

    public int show(FragmentTransaction transaction, String tag, String title, String content) {
        setContent(content);
        return super.show(transaction, tag, title);
    }

    public void show(FragmentManager manager, String tag, @StringRes int titleRes, @StringRes int contentRes) {
        setContent(contentRes);
        super.show(manager, tag, titleRes);
    }

    public int show(FragmentTransaction transaction, String tag, @StringRes int titleRes, @StringRes int contentRes) {
        setContent(contentRes);
        return super.show(transaction, tag, titleRes);
    }
}
