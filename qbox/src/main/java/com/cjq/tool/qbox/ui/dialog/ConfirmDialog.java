package com.cjq.tool.qbox.ui.dialog;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.TextView;

import com.cjq.tool.qbox.R;

/**
 * Created by KAT on 2016/7/18.
 */
public class ConfirmDialog extends BaseDialog<ConfirmDialog.Decorator> {

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

        final public void setDefaultContentPadding(@DimenRes int padding) {
            mParameters.putInt("dp_con_padding", padding);
        }

        @Override
        public boolean isDefaultDrawSeparationLine() {
            return false;
        }
    }

    @Override
    protected void onSetContentView(View contentView, Decorator decorator, @Nullable Bundle savedInstanceState) {
        TextView tvContent = contentView.findViewById(decorator.getContentId());
        tvContent.setText(getContent());
        setViewBackground(tvContent, decorator.getContentBackgroundRes());
        setViewPadding(tvContent, decorator.getContentPaddingDimenRes(), getResources());
        setTextViewSize(tvContent, decorator.getContentTextSizeDimenRes(), getResources());
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
