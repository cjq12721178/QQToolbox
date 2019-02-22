package com.cjq.tool.qbox.ui.dialog;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.CheckBox;
import android.widget.TextView;

import com.cjq.tool.qbox.R;

/**
 * Created by KAT on 2016/7/18.
 */
public class ConfirmDialog extends BaseDialog<ConfirmDialog.Decorator> {

    private static String ARGUMENT_KEY_PROMPT_ENABLE = "in_confirm_prompt";
    private CheckBox mChkPrompt;

    public static class Decorator extends BaseDialog.Decorator {

        @Override
        protected int getDefaultContentLayoutRes() {
            return R.layout.qbox_dialog_content_confirm;
        }

        final public @IdRes int getContentId() {
            return mParameters.getInt("dp_tv_id", getDefaultContentId());
        }

        public @IdRes int getDefaultContentId() {
            return R.id.tv_content;
        }

        final public void setContentId(@IdRes int textId) {
            mParameters.putInt("dp_tv_id", textId);
        }

        final public @DimenRes int getContentTextSizeDimenRes() {
            return mParameters.getInt("dp_tv_size", getDefaultContentTextSizeDimenRes());
        }

        public @DimenRes int getDefaultContentTextSizeDimenRes() {
            return 0;
        }

        final public void setContentTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("dp_tv_size", textSizeRes);
        }

        final public @DrawableRes int getContentBackgroundRes() {
            return mParameters.getInt("dp_con_bg", getDefaultContentBackgroundRes());
        }

        public @DrawableRes int getDefaultContentBackgroundRes() {
            return 0;
        }

        final public void setContentBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("dp_con_bg", backgroundRes);
        }

        final public @DimenRes int getContentPaddingDimenRes() {
            return mParameters.getInt("dp_con_padding", getDefaultContentPaddingDimenRes());
        }

        public @DimenRes int getDefaultContentPaddingDimenRes() {
            return R.dimen.qbox_padding;
        }

        final public void setContentPadding(@DimenRes int padding) {
            mParameters.putInt("dp_con_padding", padding);
        }

        @Override
        public boolean isDefaultDrawSeparationLine() {
            return false;
        }

        final public @LayoutRes int getPromptLayoutRes() {
            return mParameters.getInt("dp_con_tip_layout", getDefaultPromptLayoutRes());
        }

        public @LayoutRes int getDefaultPromptLayoutRes() {
            return 0;
        }

        final public void setPromptLayoutRes(@LayoutRes int layoutRes) {
            mParameters.putInt("dp_con_tip_layout", layoutRes);
        }

        final public @IdRes int getPromptId() {
            return mParameters.getInt("dp_con_tip_id", getDefaultPromptId());
        }

        public @IdRes int getDefaultPromptId() {
            return R.id.chk_prompt;
        }

        final public void setPromptId(@IdRes int id) {
            mParameters.putInt("dp_con_tip_id", id);
        }

        final public @DimenRes int getPromptTextSize() {
            return mParameters.getInt("dp_con_tip_ts", getDefaultPromptTextSize());
        }

        public @DimenRes int getDefaultPromptTextSize() {
            return R.dimen.qbox_size_text_comment;
        }

        final public void setPromptTextSize(@DimenRes int textSize) {
            mParameters.putInt("dp_con_tip_ts", textSize);
        }

        final public String getPromptText(@NonNull Context context) {
            Object value = mParameters.get("dp_con_tip_txt");
            if (value instanceof Integer) {
                return context.getString((Integer) value);
            } else if (value instanceof String) {
                return (String) value;
            }
            int textRes = getDefaultPromptTextRes();
            return textRes != 0
                    ? context.getString(textRes)
                    : getDefaultPromptText();
        }

        public String getDefaultPromptText() {
            return "";
        }

        public @StringRes int getDefaultPromptTextRes() {
            return R.string.qbox_no_tips;
        }

        final public @DrawableRes int getPromptCheckBoxDrawableRes() {
            return mParameters.getInt("dp_con_tip_dr", getDefaultPromptCheckBoxDrawableRes());
        }

        public @DrawableRes int getDefaultPromptCheckBoxDrawableRes() {
            return R.drawable.qbox_selector_checkbox_button;
        }

        final public void setPromptCheckBoxDrawableRes(@DrawableRes int res) {
            mParameters.putInt("dp_con_tip_dr", res);
        }

        final public @DimenRes int getPromptCheckBoxSize() {
            return mParameters.getInt("dp_con_tip_size", getDefaultPromptCheckBoxSize());
        }

        public @DimenRes int getDefaultPromptCheckBoxSize() {
            return R.dimen.qbox_dialog_search_remove_size;
        }

        final public void setPromptCheckBoxSize(@DimenRes int size) {
            mParameters.putInt("dp_con_tip_size", size);
        }

        final public @DimenRes int getPromptPaddingDimenRes() {
            return mParameters.getInt("dp_con_tip_pad", getDefaultContentPaddingDimenRes());
        }

        public @DimenRes int getDefaultPromptPaddingDimenRes() {
            return R.dimen.qbox_padding;
        }

        final public void setPromptPaddingDimenRes(@DimenRes int padding) {
            mParameters.putInt("dp_con_tip_pad", padding);
        }
    }

    @Override
    protected int getContentLayoutRes(@NonNull Decorator decorator) {
        return TextUtils.isEmpty(getContent()) && !isDrawNoPromptTag() ? 0 : super.getContentLayoutRes(decorator);
    }

    @Override
    protected void onSetContentView(View contentView, Decorator decorator, @Nullable Bundle savedInstanceState) {
        Resources resources = getResources();
        TextView tvContent = contentView.findViewById(decorator.getContentId());
        tvContent.setText(getContent());
        setViewBackground(tvContent, decorator.getContentBackgroundRes());
        setViewPadding(tvContent, decorator.getContentPaddingDimenRes(), resources);
        setTextViewSize(tvContent, decorator.getContentTextSizeDimenRes(), resources);
        if (isDrawNoPromptTag()) {
            ViewStub vsPrompt = contentView.findViewById(R.id.vs_prompt);
            int layoutRes = decorator.getPromptLayoutRes();
            if (layoutRes != 0) {
                vsPrompt.setLayoutResource(layoutRes);
                View vPromptCarrier = vsPrompt.inflate();
                mChkPrompt = vPromptCarrier.findViewById(decorator.getPromptId());
            } else {
                View vPromptCarrier = vsPrompt.inflate();
                mChkPrompt = vPromptCarrier.findViewById(decorator.getPromptId());
                setTextViewSize(mChkPrompt, decorator.getPromptTextSize(), resources);
                mChkPrompt.setText(decorator.getPromptText(contentView.getContext()));
                int size = resources.getDimensionPixelSize(decorator.getPromptCheckBoxSize());
                Drawable buttonDrawable = ContextCompat.getDrawable(contentView.getContext(), decorator.getPromptCheckBoxDrawableRes());
                buttonDrawable.setBounds(0, 0, size, size);
                mChkPrompt.setCompoundDrawables(buttonDrawable, null, null, null);
                setViewPadding(mChkPrompt, decorator.getPromptPaddingDimenRes(), resources);
            }
        }
    }

    @Override
    protected boolean onConfirm() {
        if (isDrawNoPromptTag()) {
            OnDialogConfirmListener listener = getListener(OnDialogConfirmListener.class);
            return listener == null || mChkPrompt == null || listener.onConfirm(this, mChkPrompt.isChecked());
        }
        return super.onConfirm();
    }

    @Override
    protected boolean onCancel() {
        if (isDrawNoPromptTag()) {
            OnDialogCancelListener listener = getListener(OnDialogCancelListener.class);
            if (listener != null && mChkPrompt != null) {
                listener.onCancel(this, mChkPrompt.isChecked());
            }
            return true;
        }
        return super.onCancel();
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

    public void setDrawNoPromptTag(boolean enabled) {
        getArguments().putBoolean(ARGUMENT_KEY_PROMPT_ENABLE, enabled);
    }

    public boolean isDrawNoPromptTag() {
        return getArguments().getBoolean(ARGUMENT_KEY_PROMPT_ENABLE);
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

    public interface OnDialogConfirmListener {
        boolean onConfirm(@NonNull ConfirmDialog dialog, boolean noTips);
    }

    public interface OnDialogCancelListener {
        void onCancel(@NonNull ConfirmDialog dialog, boolean noTips);
    }
}
