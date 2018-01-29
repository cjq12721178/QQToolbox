package com.cjq.tool.qbox.ui.dialog;

import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
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

    private static final String ARGUMENT_KEY_CONTENT_STRING = "in_content_str";
    private static final String ARGUMENT_KEY_CONTENT_RESOURCE = "in_content_res";
    private static final String ARGUMENT_KEY_SUMMARY_STRING = "in_summary_str";
    private static final String ARGUMENT_KEY_SUMMARY_RESOURCE = "in_summary_res";
    private EditText mEtText;

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int getDefaultContentLayoutRes() {
            return R.layout.qbox_dialog_content_edit;
        }

        final public @IdRes int getEditId() {
            return mParameters.getInt("dp_edit_id", getDefaultEditId());
        }

        public @IdRes int getDefaultEditId() {
            return R.id.il_text;
        }

        final public void setEditId(@IdRes int editId) {
            mParameters.putInt("dp_edit_id", editId);
        }

        final public @DimenRes int getEditTextSizeDimenRes() {
            return mParameters.getInt("dp_edit_text_size", getDefaultEditTextSizeDimenRes());
        }

        public @DimenRes int getDefaultEditTextSizeDimenRes() {
            return 0;
        }

        final public void setEditTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("dp_edit_text_size", textSizeRes);
        }
    }

    @Override
    protected void onSetContentView(View contentView,
                                    Decorator decorator,
                                    @Nullable Bundle savedInstanceState) {
        mEtText = contentView.findViewById(decorator.getEditId());
        mEtText.setText(getContent());
        mEtText.setHint(getSummary());
        int textSizeRes = decorator.getEditTextSizeDimenRes();
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
//        int contentRes = getArguments().getInt(ARGUMENT_KEY_CONTENT_RESOURCE);
//        return contentRes != 0 ?
//                getString(contentRes) :
//                getArguments().getString(ARGUMENT_KEY_CONTENT_STRING);
        return getString(ARGUMENT_KEY_CONTENT_RESOURCE, ARGUMENT_KEY_CONTENT_STRING);
    }

    public void setSummary(String summary) {
        getArguments().putString(ARGUMENT_KEY_SUMMARY_STRING, summary);
    }

    public void setSummary(@StringRes int summaryRes) {
        getArguments().putInt(ARGUMENT_KEY_SUMMARY_RESOURCE, summaryRes);
    }

    private String getSummary() {
        return getString(ARGUMENT_KEY_SUMMARY_RESOURCE, ARGUMENT_KEY_SUMMARY_STRING);
    }
}
