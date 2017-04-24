package com.cjq.test.qbox.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cjq.test.qbox.R;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by KAT on 2016/11/10.
 */
public abstract class BaseDialog<D extends BaseDialog.Decorator>
        extends DialogFragment
        implements View.OnClickListener {

    protected static final int EXIT_TYPE_NULL = 0;
    protected static final int EXIT_TYPE_OK_CANCEL = 1;
    protected static final int EXIT_TYPE_OK = 2;
    private static final String ARGUMENT_KEY_TITLE_STRING = "in_title_string";
    private static final String ARGUMENT_KEY_TITLE_RESOURCE = "in_title_resource";
    private static final String ARGUMENT_KEY_EXIT_TYPE = "in_exit_type";
    private static final String ARGUMENT_KEY_CUSTOM_DECORATOR = "in_custom_decorator";
    private static final String FLAG_SEPARATION_LINE = "line";

    private static final Map<String, Decorator> overallDecorator = new HashMap<>();

    public static void setOverallDecorator(Decorator decorator) {
        if (decorator != null) {
            //取相应dialog类名为key
            String decoratorName = getDecoratorName(decorator);
            overallDecorator.put(decoratorName, decorator);
        }
    }

    private static String getDecoratorName(Decorator decorator) {
        Class decoratorClass = decorator.getClass();
        Class enclosingClass = decoratorClass.getEnclosingClass();
        while (!BaseDialog.class.isAssignableFrom(enclosingClass)) {
            decoratorClass = decoratorClass.getSuperclass();
            enclosingClass = decoratorClass.getEnclosingClass();
        }
        return enclosingClass.getSimpleName();
    }

    protected D getOverallDecorator() {
        D decorator = (D)overallDecorator.get(getClass().getSimpleName());
        if (decorator == null) {
            decorator = createDecorator();
            if (decorator == null)
                throw new NullPointerException();
            setOverallDecorator(decorator);
        }
        return decorator;
    }

    private D createDecorator() {
        try {
            Class dialogClass = getClass();
            Type superClassType = dialogClass.getGenericSuperclass();
            while (!(superClassType instanceof ParameterizedType)) {
                dialogClass = dialogClass.getSuperclass();
                superClassType = dialogClass.getGenericSuperclass();
            }
            return ((Class<D>)((ParameterizedType)superClassType).
                    getActualTypeArguments()[0]).newInstance();
        } catch (Exception e) {
        }
        return null;
    }

    public void setCustomDecorator(D customDecorator) {
        getArguments().putSerializable(ARGUMENT_KEY_CUSTOM_DECORATOR, customDecorator);
    }

    private D getCustomDecorator() {
        return (D) getArguments().getSerializable(ARGUMENT_KEY_CUSTOM_DECORATOR);
    }

    protected D getDecorator() {
        D decorator = getCustomDecorator();
        return decorator != null ? decorator : getOverallDecorator();
    }

    public static class Decorator implements Serializable {

        @LayoutRes
        public int getContentLayout() {
            return 0;
        }

        //以下三个方法与标题设置有关
        //若要自定义title，需重载Decorator的getTitleLayout()和getTitleId()方法
        @LayoutRes
        public int getTitleLayout() {
            return 0;
        }

        @IdRes
        public int getTitleId() {
            return 0;
        }

        //返回0时，采用默认设置的字体大小，若要自定义字体大小，重载即可
        @DimenRes
        public int getTitleTextSize() {
            return 0;
        }

        //以下11个方法与确认/取消组键设置有关
        @LayoutRes
        public int getOkCancelLayout() {
            return R.layout.group_ok_cancel;
        }

        @LayoutRes
        public int getOkLayout() {
            return R.layout.group_ok;
        }

        @IdRes
        public int getOkId() {
            return R.id.btn_ok;
        }

        @IdRes
        public int getCancelId() {
            return R.id.btn_cancel;
        }

        @StringRes
        public int getOkLabel() {
            return 0;
        }

        @StringRes
        public int getCancelLabel() {
            return 0;
        }

        @DimenRes
        public int getExitButtonTextSize() {
            return 0;
        }

        @DrawableRes
        public int getExitButtonBackground() {
            return 0;
        }

        @ColorRes
        public int getExitButtonTextColor() {
            return 0;
        }

        @IdRes
        final public int getExitButtonId(boolean okOrCancel) {
            return okOrCancel ? getOkId() : getCancelId();
        }

        @StringRes
        final public int getExitButtonLabel(boolean okOrCancel) {
            return okOrCancel ? getOkLabel() : getCancelLabel();
        }

        //整体背景
        @DrawableRes
        public int getBackground() {
            return R.drawable.ic_dialog_background;
        }

        @DimenRes
        public int getBasePadding() {
            return R.dimen.padding_dialog_base;
        }

        @DimenRes
        public int getBaseTopPadding() {
            return 0;
        }

        @DimenRes
        public int getBaseBottomPadding() {
            return 0;
        }

        @DimenRes
        public int getBaseLeftPadding() {
            return 0;
        }

        @DimenRes
        public int getBaseRightPadding() {
            return 0;
        }

        //是否有分隔线
        public boolean hasSeparationLine() {
            return true;
        }

        @DrawableRes
        public int getSeparationLineBackground() {
            return R.color.background_dialog_separation_line;
        }

        @DimenRes
        public int getSeparationLineWidth() {
            return R.dimen.dialog_separation_line_width_fixed;
        }

        @DimenRes
        public int getViewVerticalInterval() {
            return R.dimen.dialog_view_interval_vertical;
        }
    }

    public BaseDialog() {
        setArguments(new Bundle());
        setExitType(EXIT_TYPE_OK_CANCEL);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        D decorator = getDecorator();
        LinearLayout liBase = inflateBaseView(inflater, decorator);
        //设置内容
        onCreateContentView(inflater, liBase, decorator, savedInstanceState);
        //设置标题（可选）
        onCreateTitle(liBase, decorator);
        //设置确定/取消按钮及其事件
        onCreateExitGroup(inflater, liBase, decorator);
        return liBase;
    }

    private LinearLayout inflateBaseView(LayoutInflater inflater,
                                         D decorator) {
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.dialog_base, null);
        int background = getDecorator().getBackground();
        if (background != 0) {
            layout.setBackgroundResource(background);
        }
        Resources resources = getResources();
        int leftPaddingDimenRes = decorator.getBaseLeftPadding();
        int topPaddingDimenRes = decorator.getBaseTopPadding();
        int rightPaddingDimenRes = decorator.getBaseRightPadding();
        int bottomPaddingDimenRes = decorator.getBaseBottomPadding();
        int leftPadding = leftPaddingDimenRes != 0 ?
                resources.getDimensionPixelSize(leftPaddingDimenRes) :
                0;
        int topPadding = topPaddingDimenRes != 0 ?
                resources.getDimensionPixelSize(topPaddingDimenRes) :
                0;
        int rightPadding = rightPaddingDimenRes != 0 ?
                resources.getDimensionPixelSize(rightPaddingDimenRes) :
                0;
        int bottomPadding = bottomPaddingDimenRes != 0 ?
                resources.getDimensionPixelSize(bottomPaddingDimenRes) :
                0;
        if (leftPadding != 0 ||
                topPadding != 0 ||
                rightPadding != 0 ||
                bottomPadding != 0) {
            layout.setPadding(leftPadding, topPadding, rightPadding, bottomPadding);
        } else {
            int paddingDimenRes = decorator.getBasePadding();
            if (paddingDimenRes != 0) {
                int padding = resources.getDimensionPixelOffset(paddingDimenRes);
                layout.setPadding(padding, padding, padding, padding);
            }
        }
        return layout;
    }

    private void onCreateContentView(LayoutInflater inflater,
                                     LinearLayout baseView,
                                     D decorator,
                                     @Nullable Bundle savedInstanceState) {
        int contentLayoutRes = decorator.getContentLayout();
        if (contentLayoutRes != 0) {
            inflater.inflate(contentLayoutRes, baseView);
            //contentView必定是第一个创建的view
            onSetContentView(baseView.getChildAt(baseView.getChildCount() - 1), decorator, savedInstanceState);
        }
    }

    private void onCreateTitle(LinearLayout baseView,
                               D decorator) {
        ViewStub vsTitle = (ViewStub) baseView.findViewById(R.id.vs_title_dialog_base);
        String title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            TextView tvTitle;
            View attachView;
            int titleLayoutRes = decorator.getTitleLayout();
            int titleId = decorator.getTitleId();
            if (titleLayoutRes != 0 && titleId != 0) {
                vsTitle.setLayoutResource(titleLayoutRes);
                attachView = vsTitle.inflate();
                tvTitle = (TextView) attachView.findViewById(titleId);
            } else {
                tvTitle = (TextView) vsTitle.inflate();
                attachView = tvTitle;
            }
            int titleSizeRes = decorator.getTitleTextSize();
            if (titleSizeRes != 0) {
                tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                        getResources().getDimensionPixelSize(titleSizeRes));
            }
            tvTitle.setText(title);
            inflateSeparationLine(baseView, attachView, decorator, false);
        }
    }

    private void inflateSeparationLine(LinearLayout baseView,
                                       View attachView,
                                       D decorator,
                                       boolean isAbove) {
        //获取tvLine在baseView中应该添加的位置
        int position = baseView.indexOfChild(attachView) + (isAbove ? 0 : 1);

        //判断是否需要渲染分隔线
        boolean isSeparationLine = false;
        if (decorator.hasSeparationLine() && position >= 0) {
            View aboveView = baseView.getChildAt(position - 1);
            //若上一个view即为分隔线，则无需再次添加
            if (aboveView == null || !FLAG_SEPARATION_LINE.equals(aboveView.getTag())) {
                isSeparationLine = true;
            } else {
                return;
            }
        }

        //创建分隔线，同时设置baseView中各子view间隔
        TextView tvLine = new TextView(getActivity());
        tvLine.setTag(FLAG_SEPARATION_LINE);
        Resources resources = getResources();
        int interval = resources.getDimensionPixelSize(decorator.getViewVerticalInterval());
        int separationWidth = resources.getDimensionPixelSize(decorator.getSeparationLineWidth());
        LinearLayout.LayoutParams params;
        if (isSeparationLine && separationWidth > 0) {
            tvLine.setBackgroundResource(decorator.getSeparationLineBackground());
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    separationWidth);
            params.setMargins(0, interval / 2, 0, interval / 2);
        } else {
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    interval);
        }
        baseView.addView(tvLine, position, params);
    }

    private void onCreateExitGroup(LayoutInflater inflater,
                                   LinearLayout baseView,
                                   D decorator) {
        int exitType = getExitType();
        View grpExit;
        if (exitType == EXIT_TYPE_OK_CANCEL) {
            grpExit = inflater.inflate(decorator.getOkCancelLayout(), baseView, false);
        } else if (exitType == EXIT_TYPE_OK) {
            grpExit = inflater.inflate(decorator.getOkLayout(), baseView, false);
        } else {
            grpExit = null;
        }
        if (grpExit != null) {
            baseView.addView(grpExit);
            setExitButton(grpExit, decorator, true);
            if (exitType == EXIT_TYPE_OK_CANCEL) {
                setExitButton(grpExit, decorator, false);
            }
            inflateSeparationLine(baseView, grpExit, decorator, true);
        }
    }

    private void setExitButton(View group,
                               D decorator,
                               boolean okOrCancel) {
        Button btn = (Button)group.findViewById(decorator.getExitButtonId(okOrCancel));
        int buttonLabelRes = decorator.getExitButtonLabel(okOrCancel);
        if (buttonLabelRes != 0) {
            btn.setText(buttonLabelRes);
        }
        btn.setOnClickListener(this);
        int textSizeRes = decorator.getExitButtonTextSize();
        if (textSizeRes != 0) {
            btn.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                    getResources().getDimensionPixelSize(textSizeRes));
        }
        int textColorRes = decorator.getExitButtonTextColor();
        if (textColorRes != 0) {
            btn.setTextColor(ContextCompat.getColor(getActivity(), textColorRes));
        }
        int backgroundRes = decorator.getExitButtonBackground();
        if (backgroundRes != 0) {
            btn.setBackgroundResource(backgroundRes);
        }
    }

    protected int getExitType() {
        return getArguments().getInt(ARGUMENT_KEY_EXIT_TYPE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        D decorator = getDecorator();
        if (id == decorator.getOkId()) {
            if (onConfirm()) {
                dismiss();
            }
        } else if (id == decorator.getCancelId()) {
            if (onCancel()) {
                dismiss();
            }
        }
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        throw new UnsupportedOperationException("use show(FragmentManager manager, String tag, String title) for instead");
    }

    @Override
    public int show(FragmentTransaction transaction, String tag) {
        throw new UnsupportedOperationException("use show(FragmentTransaction transaction, String tag, String title) for instead");
    }

    public void show(FragmentManager manager, String tag, String title) {
        setTitle(title);
        super.show(manager, tag);
    }

    public int show(FragmentTransaction transaction, String tag, String title) {
        setTitle(title);
        return super.show(transaction, tag);
    }

    public void show(FragmentManager manager, String tag, @StringRes int titleRes) {
        setTitle(titleRes);
        super.show(manager, tag);
    }

    public int show(FragmentTransaction transaction, String tag, @StringRes int titleRes) {
        setTitle(titleRes);
        return super.show(transaction, tag);
    }

    public void setTitle(String title) {
        getArguments().putString(ARGUMENT_KEY_TITLE_STRING, title);
    }

    public void setTitle(@StringRes int titleRes) {
        getArguments().putInt(ARGUMENT_KEY_TITLE_RESOURCE, titleRes);
    }

    private String getTitle() {
        int titleRes = getArguments().getInt(ARGUMENT_KEY_TITLE_RESOURCE);
        return titleRes != 0 ?
                getString(titleRes) :
                getArguments().getString(ARGUMENT_KEY_TITLE_STRING);
    }

    public void setExitType(int type) {
        getArguments().putInt(ARGUMENT_KEY_EXIT_TYPE, type);
    }

    //绑定布局中的view
    protected abstract void onSetContentView(View content, D decorator, @Nullable Bundle savedInstanceState);

    protected boolean onConfirm() {
        OnDialogConfirmListener listener = getListener(OnDialogConfirmListener.class);
        return listener != null ? listener.onConfirm(this) : true;
    }

    protected boolean onCancel() {
        OnDialogCancelListener listener = getListener(OnDialogCancelListener.class);
        if (listener != null) {
            listener.onCancel(this);
        }
        return true;
    }

    protected <L> L getListener(Class<L> c) {
        if (c == null)
            return null;
        Fragment fragment = getParentFragment();
        if (c.isInstance(fragment)) {
            return (L)fragment;
        }
        Activity activity = getActivity();
        if (c.isInstance(activity)) {
            return (L)activity;
        }
        return null;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        onCancel();
    }

    public interface OnDialogConfirmListener {
        boolean onConfirm(BaseDialog dialog);
    }

    public interface OnDialogCancelListener {
        void onCancel(BaseDialog dialog);
    }
}
