package com.cjq.tool.qbox.ui.dialog;

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

import com.cjq.tool.qbox.R;

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
    private static final String ARGUMENT_KEY_CUSTOM_DECORATOR_PARAMETERS = "in_custom_decorator_paras";
    private static final String FLAG_SEPARATION_LINE = "line";

    private static final Map<String, Decorator> overallDecorator = new HashMap<>();
    private D mCustomDecorator;

    //Decorator无法在Dialog类外部通过实例化获得，
    //只能通过getOverallDecorator(Class<DL>)和getCustomDecorator()方法获得
    //用于获取每一类Dialog的总体Decorator，即该Decorator做出的样式改变将影响所有该类及其子类的dialog
    //另外，若采用getOverallDecorator(BaseDialog.class)方式获得的Decorator将影响所有继承自BaseDialog的dialog
    //此外，也可以使用getBaseOverallDecorator()方法
    public static Decorator getBaseOverallDecorator() {
        return getOverallDecorator(BaseDialog.class);
    }

    public static <DL extends BaseDialog, DC extends Decorator> DC getOverallDecorator(Class<DL> dialogClass) {
        String decoratorName = dialogClass.getSimpleName();
        DC decorator = (DC)overallDecorator.get(decoratorName);
        if (decorator == null) {
            decorator = createDecorator(dialogClass);
            overallDecorator.put(decoratorName, decorator);
        }
        return decorator;
    }

    private static <DL extends BaseDialog, DC extends Decorator> DC createDecorator(Class<DL> dialogClass) {
        if (dialogClass == BaseDialog.class)
            return (DC)new BaseDecorator();
        DC result = createDecoratorImp(dialogClass);
        Decorator baseDecorator = overallDecorator.get(BaseDialog.class.getSimpleName());
        result.addParameters(baseDecorator);
        return result;
    }

    private static <DL extends BaseDialog, DC extends Decorator> DC createDecoratorImp(Class<DL> dialogClass) {
        try {
            Class c = dialogClass;
            Type superClassType = c.getGenericSuperclass();
            while (!(superClassType instanceof ParameterizedType)) {
                c = c.getSuperclass();
                superClassType = c.getGenericSuperclass();
            }
            return ((Class<DC>)((ParameterizedType)superClassType).
                    getActualTypeArguments()[0]).newInstance();
        } catch (Exception e) {
            throw new NullPointerException();
        }
    }

    final public D getCustomDecorator() {
        if (mCustomDecorator == null) {
            mCustomDecorator = createDecorator(getClass());
        }
        return mCustomDecorator;
    }

    final protected D getDecorator(@Nullable Bundle savedInstanceState) {
        if (mCustomDecorator != null)
            return mCustomDecorator;
        if (savedInstanceState != null) {
            Bundle parameters = savedInstanceState.getBundle(ARGUMENT_KEY_CUSTOM_DECORATOR_PARAMETERS);
            if (parameters != null) {
                mCustomDecorator = createDecoratorImp(getClass());
                mCustomDecorator.addParameters(parameters);
                return mCustomDecorator;
            }
        }
        return (D)getOverallDecorator(getClass());
    }

    public static abstract class Decorator  {

        protected Bundle mParameters = new Bundle();

        protected Decorator() {
            reset();
        }

        public void reset() {
            mParameters.clear();
            setOkCancelLayout(R.layout.qbox_group_ok_cancel);
            setOkLayout(R.layout.qbox_group_ok);
            setOkId(R.id.btn_ok);
            setCancelId(R.id.btn_cancel);
            setBaseBackground(R.drawable.qbox_ic_dialog_background);
            setBasePadding(R.dimen.qbox_padding_dialog_base);
            setSeparationLineExists(true);
            setSeparationLineBackground(R.color.qbox_background_dialog_separation_line);
            setSeparationLineWidth(R.dimen.qbox_dialog_separation_line_width_fixed);
            setViewVerticalInterval(R.dimen.qbox_dialog_view_interval_vertical);
            setContentLayout(onSetContentLayout());
        }

        @LayoutRes
        protected abstract int onSetContentLayout();

        void addParameters(Decorator baseDecorator) {
            if (baseDecorator != null) {
                addParameters(baseDecorator.mParameters);
            }
        }

        void addParameters(Bundle parameters) {
            mParameters.putAll(parameters);
        }

        @LayoutRes
        final public int getContentLayout() {
            return mParameters.getInt("dp_content_layout");
        }

        protected void setContentLayout(@LayoutRes int layoutRes) {
            if (getClass().getEnclosingClass() != BaseDialog.class) {
                mParameters.putInt("dp_content_layout", layoutRes);
            }
        }

        //以下三个方法与标题设置有关
        //若要自定义title，需重载Decorator的getTitleLayout()和getTitleId()方法
        @LayoutRes
        public int getTitleLayout() {
            return mParameters.getInt("dp_title_layout");
        }

        public void setTitleLayout(@LayoutRes int layoutRes) {
            mParameters.putInt("dp_title_layout", layoutRes);
        }

        @IdRes
        public int getTitleId() {
            return mParameters.getInt("dp_title_id");
        }

        public void setTitleId(@IdRes int titleId) {
            mParameters.putInt("dp_title_id", titleId);
        }

        //返回0时，采用默认设置的字体大小，若要自定义字体大小，重载即可
        @DimenRes
        public int getTitleTextSize() {
            return mParameters.getInt("dp_title_size");
        }

        public void setTitleTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("dp_title_size", textSizeRes);
        }

        //以下22个方法与确认/取消组键设置有关
        @LayoutRes
        public int getOkCancelLayout() {
            return mParameters.getInt("dp_ok_cancel_layout");
        }

        public void setOkCancelLayout(@LayoutRes int layoutRes) {
            mParameters.putInt("dp_ok_cancel_layout", layoutRes);
        }

        @LayoutRes
        public int getOkLayout() {
            return mParameters.getInt("dp_ok_layout");
        }

        public void setOkLayout(@LayoutRes int layoutRes) {
            mParameters.putInt("dp_ok_layout", layoutRes);
        }

        @IdRes
        public int getOkId() {
            return mParameters.getInt("dp_ok_id");
        }

        public void setOkId(@IdRes int okId) {
            mParameters.putInt("dp_ok_id", okId);
        }

        @IdRes
        public int getCancelId() {
            return mParameters.getInt("dp_cancel_id");
        }

        public void setCancelId(@IdRes int cancelId) {
            mParameters.putInt("dp_cancel_id", cancelId);
        }

        @StringRes
        public int getOkLabel() {
            return mParameters.getInt("dp_ok_label");
        }

        public void setOkLabel(@StringRes int okLabelRes) {
            mParameters.putInt("dp_ok_label", okLabelRes);
        }

        @StringRes
        public int getCancelLabel() {
            return mParameters.getInt("dp_cancel_label");
        }

        public void setCancelLabel(@StringRes int cancelLabelRes) {
            mParameters.putInt("dp_cancel_label", cancelLabelRes);
        }

        @DimenRes
        public int getExitButtonTextSize() {
            return mParameters.getInt("dp_exit_button_text_size");
        }

        public void setExitButtonTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("dp_exit_button_text_size", textSizeRes);
        }

        @DrawableRes
        public int getExitButtonBackground() {
            return mParameters.getInt("dp_exit_button_background");
        }

        public void setExitButtonBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("dp_exit_button_background", backgroundRes);
        }

        @ColorRes
        public int getExitButtonTextColor() {
            return mParameters.getInt("dp_exit_button_text_color");
        }

        public void setExitButtonTextColor(@ColorRes int colorRes) {
            mParameters.putInt("dp_exit_button_text_color", colorRes);
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
        public int getBaseBackground() {
            return mParameters.getInt("dp_base_background");
        }

        public void setBaseBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("dp_base_background", backgroundRes);
        }

        @DimenRes
        public int getBasePadding() {
            return mParameters.getInt("dp_base_padding");
        }

        public void setBasePadding(@DimenRes int paddingRes) {
            mParameters.putInt("dp_base_padding", paddingRes);
        }

        public void setBasePadding(@DimenRes int leftPaddingRes,
                                   @DimenRes int topPaddingRes,
                                   @DimenRes int rightPaddingRes,
                                   @DimenRes int bottomPaddingRes) {
            mParameters.putInt("dp_base_left_padding", leftPaddingRes);
            mParameters.putInt("dp_base_top_padding", topPaddingRes);
            mParameters.putInt("dp_base_right_padding", rightPaddingRes);
            mParameters.putInt("dp_base_bottom_padding", bottomPaddingRes);
        }

        @DimenRes
        public int getBaseTopPadding() {
            return mParameters.getInt("dp_base_top_padding");
        }

        @DimenRes
        public int getBaseBottomPadding() {
            return mParameters.getInt("dp_base_bottom_padding");
        }

        @DimenRes
        public int getBaseLeftPadding() {
            return mParameters.getInt("dp_base_left_padding");
        }

        @DimenRes
        public int getBaseRightPadding() {
            return mParameters.getInt("dp_base_right_padding");
        }

        //是否有分隔线
        public boolean hasSeparationLine() {
            return mParameters.getByte("dp_separation_line_exists") == 1;
        }

        public void setSeparationLineExists(boolean exists) {
            mParameters.putByte("dp_separation_line_exists", (byte)(exists ? 1 : 0));
        }

        @DrawableRes
        public int getSeparationLineBackground() {
            return mParameters.getInt("dp_separation_line_background");
        }

        public void setSeparationLineBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("dp_separation_line_background", backgroundRes);
        }

        @DimenRes
        public int getSeparationLineWidth() {
            return mParameters.getInt("dp_separation_line_width");
        }

        public void setSeparationLineWidth(@DimenRes int widthRes) {
            mParameters.putInt("dp_separation_line_width", widthRes);
        }

        @DimenRes
        public int getViewVerticalInterval() {
            return mParameters.getInt("dp_view_vertical_interval");
        }

        public void setViewVerticalInterval(@DimenRes int intervalRes) {
            mParameters.putInt("dp_view_vertical_interval", intervalRes);
        }
    }

    private static class BaseDecorator extends Decorator {

        @Override
        protected int onSetContentLayout() {
            return 0;
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
        D decorator = getDecorator(savedInstanceState);
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
        LinearLayout layout = (LinearLayout)inflater.inflate(R.layout.qbox_dialog_base, null);
        int background = decorator.getBaseBackground();
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
            onSetContentView(baseView, decorator, savedInstanceState);
            int intervalRes = decorator.getViewVerticalInterval();
            if (intervalRes != 0) {
                int interval = getResources().getDimensionPixelSize(intervalRes);
                for (int i = 2, n = baseView.getChildCount();i < n;++i) {
                    View childView = baseView.getChildAt(i);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) childView.getLayoutParams();
                    params.setMargins(params.leftMargin, interval, params.rightMargin, params.bottomMargin);
                    childView.setLayoutParams(params);
                }
            }
//            View contentView = rootView != null ? rootView : baseView;
//            onSetContentView(contentView, decorator, savedInstanceState);
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
    public void onSaveInstanceState(Bundle outState) {
        if (mCustomDecorator != null && outState != null) {
            outState.putBundle(ARGUMENT_KEY_CUSTOM_DECORATOR_PARAMETERS,
                    mCustomDecorator.mParameters);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        D decorator = getDecorator(null);
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
    protected abstract void onSetContentView(View contentView, D decorator, @Nullable Bundle savedInstanceState);

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
