package com.cjq.tool.qbox.ui.dialog;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.cjq.tool.qbox.R;

/**
 * Created by CJQ on 2018/2/2.
 */

public abstract class BaseEditDialog<D extends BaseEditDialog.Decorator> extends BaseDialog<D> {

    private static final String ARGUMENT_KEY_SUMMARY_STRING = "in_summary_str";
    private static final String ARGUMENT_KEY_SUMMARY_RESOURCE = "in_summary_res";
    private EditText mEtText;

    @Override
    protected void onSetContentView(View contentView,
                                    D decorator,
                                    @Nullable Bundle savedInstanceState) {
        mEtText = contentView.findViewById(decorator.getEditId());
        mEtText.setText(getContent());
        mEtText.setHint(getSummary());
        Resources resources = getResources();
        setViewBackground(mEtText, decorator.getEditTextBackgroundRes());
        setViewPadding(mEtText, decorator.getEditTextPaddingDimenRes(), resources);
        setTextViewSize(mEtText, decorator.getEditTextSizeDimenRes(), resources);
        setTextViewHintColor(mEtText, decorator.getEditTextHintColorRes(), resources);
    }

    protected EditText getEditText() {
        return mEtText;
    }

    public void setContent(String content) {
        getArguments().putString(ARGUMENT_KEY_CONTENT_STRING, content);
    }

    public void setContent(@StringRes int contentRes) {
        getArguments().putInt(ARGUMENT_KEY_CONTENT_RESOURCE, contentRes);
    }

    private String getContent() {
        return getString(ARGUMENT_KEY_CONTENT_RESOURCE, ARGUMENT_KEY_CONTENT_STRING);
    }

    protected String getOldContent() {
        return getArguments().getString(ARGUMENT_KEY_CONTENT_STRING);
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

    public static abstract class Decorator extends BaseDialog.Decorator {

        final public @IdRes int getEditId() {
            return mParameters.getInt("dp_et_id", getDefaultEditId());
        }

        public @IdRes int getDefaultEditId() {
            return 0;
        }

        final public void setEditId(@IdRes int editId) {
            mParameters.putInt("dp_et_id", editId);
        }

        final public @DimenRes int getEditTextSizeDimenRes() {
            return mParameters.getInt("dp_et_size", getDefaultEditTextSizeDimenRes());
        }

        public @DimenRes int getDefaultEditTextSizeDimenRes() {
            return 0;
        }

        final public void setEditTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("dp_et_size", textSizeRes);
        }

        final public @DrawableRes int getEditTextBackgroundRes() {
            return mParameters.getInt("dp_et_bg", getDefaultEditTextBackgroundRes());
        }

        public @DrawableRes int getDefaultEditTextBackgroundRes() {
            return R.drawable.qbox_selector_edit_text;
        }

        final public void setEditTextBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("dp_et_bg", backgroundRes);
        }

        final public @ColorRes int getEditTextHintColorRes() {
            return mParameters.getInt("dp_et_hint_color", getDefaultEditTextHintColorRes());
        }

        public @ColorRes int getDefaultEditTextHintColorRes() {
            return R.color.qbox_dialog_base_edit_text_hint;
        }

        final public void setEditTextHintColor(@ColorRes int colorRes) {
            mParameters.putInt("dp_et_hint_color", colorRes);
        }

        final public @DimenRes int getEditTextPaddingDimenRes() {
            return mParameters.getInt("dp_et_padding", getDefaultEditTextPaddingDimenRes());
        }

        public @DimenRes int getDefaultEditTextPaddingDimenRes() {
            return R.dimen.qbox_padding;
        }

        final public void setDefaultEditTextPadding(@DimenRes int padding) {
            mParameters.putInt("dp_et_padding", padding);
        }
    }
}
