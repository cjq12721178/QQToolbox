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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
    private static final String ARGUMENT_KEY_DRAW_TITLE = "in_draw_title";
    private static final String ARGUMENT_KEY_EXIT_TYPE = "in_exit_type";
    private static final String ARGUMENT_KEY_CUSTOM_DECORATOR_PARAMETERS = "in_custom_decorator_paras";
    protected static final String ARGUMENT_KEY_CONTENT_STRING = "in_content_str";
    protected static final String ARGUMENT_KEY_CONTENT_RESOURCE = "in_content_res";
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
            //reset();
        }

        public final void reset() {
            mParameters.clear();
        }

        void addParameters(Decorator baseDecorator) {
            if (baseDecorator != null) {
                addParameters(baseDecorator.mParameters);
            }
        }

        void addParameters(Bundle parameters) {
            mParameters.putAll(parameters);
        }

        final public @LayoutRes int getContentLayoutRes() {
            return mParameters.getInt("dp_content_layout", getDefaultContentLayoutRes());
        }

        protected abstract @LayoutRes int getDefaultContentLayoutRes();

        final public void setContentLayoutRes(@LayoutRes int layoutRes) {
            if (getClass().getEnclosingClass() != BaseDialog.class) {
                mParameters.putInt("dp_content_layout", layoutRes);
            }
        }

        //以下三个方法与标题设置有关
        //若要自定义title，需重载Decorator的getTitleLayout()和getTitleId()方法
        final public @LayoutRes int getTitleLayoutRes() {
            return mParameters.getInt("dp_title_layout", getDefaultTitleLayoutRes());
        }

        public @LayoutRes int getDefaultTitleLayoutRes() {
            return 0;
        }

        final public void setTitleLayoutRes(@LayoutRes int layoutRes) {
            mParameters.putInt("dp_title_layout", layoutRes);
        }

        final public @IdRes int getTitleId() {
            return mParameters.getInt("dp_title_id", getDefaultTitleId());
        }

        public @IdRes int getDefaultTitleId() {
            return 0;
        }

        final public void setTitleId(@IdRes int titleId) {
            mParameters.putInt("dp_title_id", titleId);
        }

        //返回0时，采用默认设置的字体大小
        final public @DimenRes int getTitleTextSizeDimenRes() {
            return mParameters.getInt("dp_title_size", getDefaultTitleTextSizeDimenRes());
        }

        public @DimenRes int getDefaultTitleTextSizeDimenRes() {
            return R.dimen.qbox_size_text_dialog_title;
        }

        final public void setTitleTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("dp_title_size", textSizeRes);
        }

        //以下33个方法与确认/取消组键设置有关
        final public @LayoutRes int getOkCancelLayoutRes() {
            return mParameters.getInt("dp_ok_cancel_layout", getDefaultOkCancelLayoutRes());
        }

        public @LayoutRes int getDefaultOkCancelLayoutRes() {
            return R.layout.qbox_group_ok_cancel;
        }

        final public void setOkCancelLayout(@LayoutRes int layoutRes) {
            mParameters.putInt("dp_ok_cancel_layout", layoutRes);
        }

        final public @LayoutRes int getOkLayoutRes() {
            return mParameters.getInt("dp_ok_layout", getDefaultOkLayoutRes());
        }

        public @LayoutRes int getDefaultOkLayoutRes() {
            return R.layout.qbox_group_ok;
        }

        final public void setOkLayout(@LayoutRes int layoutRes) {
            mParameters.putInt("dp_ok_layout", layoutRes);
        }

        final public @IdRes int getOkId() {
            return mParameters.getInt("dp_ok_id", getDefaultOkId());
        }

        public @IdRes int getDefaultOkId() {
            return R.id.btn_ok;
        }

        final public void setOkId(@IdRes int okId) {
            mParameters.putInt("dp_ok_id", okId);
        }

        final public @IdRes int getCancelId() {
            return mParameters.getInt("dp_cancel_id", getDefaultCancelId());
        }

        public @IdRes int getDefaultCancelId() {
            return R.id.btn_cancel;
        }

        final public void setCancelId(@IdRes int cancelId) {
            mParameters.putInt("dp_cancel_id", cancelId);
        }

        final public @StringRes int getOkLabelRes() {
            return mParameters.getInt("dp_ok_label", getDefaultOkLabelRes());
        }

        public @StringRes int getDefaultOkLabelRes() {
            return 0;
        }

        final public void setOkLabel(@StringRes int okLabelRes) {
            mParameters.putInt("dp_ok_label", okLabelRes);
        }

        final public @StringRes int getCancelLabelRes() {
            return mParameters.getInt("dp_cancel_label", getDefaultCancelLabelRes());
        }

        public @StringRes int getDefaultCancelLabelRes() {
            return 0;
        }

        final public void setCancelLabel(@StringRes int cancelLabelRes) {
            mParameters.putInt("dp_cancel_label", cancelLabelRes);
        }

        final public @DimenRes int getExitButtonTextSizeDimenRes() {
            return mParameters.getInt("dp_exit_button_text_size", getDefaultExitButtonTextSizeDimenRes());
        }

        public @DimenRes int getDefaultExitButtonTextSizeDimenRes() {
            return 0;
        }

        final public void setExitButtonTextSize(@DimenRes int textSizeRes) {
            mParameters.putInt("dp_exit_button_text_size", textSizeRes);
        }

        final public @DrawableRes int getExitButtonBackgroundRes() {
            return mParameters.getInt("dp_exit_button_background", getDefaultExitButtonBackgroundRes());
        }

        public @DrawableRes int getDefaultExitButtonBackgroundRes() {
            return 0;
        }

        final public void setExitButtonBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("dp_exit_button_background", backgroundRes);
        }

        final public @ColorRes int getExitButtonTextColorRes() {
            return mParameters.getInt("dp_exit_button_text_color", getDefaultExitButtonTextColorRes());
        }

        public @ColorRes int getDefaultExitButtonTextColorRes() {
            return 0;
        }

        final public void setExitButtonTextColor(@ColorRes int colorRes) {
            mParameters.putInt("dp_exit_button_text_color", colorRes);
        }

        final public @IdRes int getExitButtonId(boolean okOrCancel) {
            return okOrCancel ? getOkId() : getCancelId();
        }

        final public @StringRes int getExitButtonLabelRes(boolean okOrCancel) {
            return okOrCancel ? getOkLabelRes() : getCancelLabelRes();
        }

        //整体背景
        final public @DrawableRes int getBaseBackgroundRes() {
            return mParameters.getInt("dp_base_background", getDefaultBaseBackgroundRes());
        }

        public @DrawableRes int getDefaultBaseBackgroundRes() {
            return R.drawable.qbox_ic_dialog_background;
        }

        final public void setBaseBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("dp_base_background", backgroundRes);
        }

        final public @DimenRes int getBasePaddingDimenRes() {
            return mParameters.getInt("dp_base_padding", getDefaultBasePaddingDimenRes());
        }

        public @DimenRes int getDefaultBasePaddingDimenRes() {
            return R.dimen.qbox_padding_dialog_base;
        }

        final public void setBasePadding(@DimenRes int paddingRes) {
            mParameters.putInt("dp_base_padding", paddingRes);
        }

        final public void setBasePadding(@DimenRes int leftPaddingRes,
                                         @DimenRes int topPaddingRes,
                                         @DimenRes int rightPaddingRes,
                                         @DimenRes int bottomPaddingRes) {
            mParameters.putInt("dp_base_left_padding", leftPaddingRes);
            mParameters.putInt("dp_base_top_padding", topPaddingRes);
            mParameters.putInt("dp_base_right_padding", rightPaddingRes);
            mParameters.putInt("dp_base_bottom_padding", bottomPaddingRes);
        }

        final public @DimenRes int getBaseTopPaddingDimenRes() {
            return mParameters.getInt("dp_base_top_padding", getDefaultBaseTopPaddingDimenRes());
        }

        public @DimenRes int getDefaultBaseTopPaddingDimenRes() {
            return 0;
        }

        final public @DimenRes int getBaseBottomPaddingDimenRes() {
            return mParameters.getInt("dp_base_bottom_padding", getDefaultBaseBottomPaddingDimenRes());
        }

        public @DimenRes int getDefaultBaseBottomPaddingDimenRes() {
            return 0;
        }

        final public @DimenRes int getBaseLeftPaddingDimenRes() {
            return mParameters.getInt("dp_base_left_padding", getDefaultBaseLeftPaddingDimenRes());
        }

        public @DimenRes int getDefaultBaseLeftPaddingDimenRes() {
            return 0;
        }

        final public @DimenRes int getBaseRightPaddingDimenRes() {
            return mParameters.getInt("dp_base_right_padding", getDefaultBaseRightPaddingDimenRes());
        }

        public @DimenRes int getDefaultBaseRightPaddingDimenRes() {
            return 0;
        }

        //是否有分隔线
        final public boolean isDrawSeparationLine() {
            return mParameters.getByte("dp_separation_line_exists", (byte) (isDefaultDrawSeparationLine() ? 1 : 0)) == 1;
        }

        public boolean isDefaultDrawSeparationLine() {
            return true;
        }

        final public void setDrawSeparationLine(boolean exists) {
            mParameters.putByte("dp_separation_line_exists", (byte)(exists ? 1 : 0));
        }

        final public @DrawableRes int getSeparationLineBackgroundRes() {
            return mParameters.getInt("dp_separation_line_background", getDefaultSeparationLineBackgroundRes());
        }

        public @DrawableRes int getDefaultSeparationLineBackgroundRes() {
            return R.color.qbox_background_dialog_separation_line;
        }

        final public void setSeparationLineBackground(@DrawableRes int backgroundRes) {
            mParameters.putInt("dp_separation_line_background", backgroundRes);
        }

        final public @DimenRes int getSeparationLineWidthDimenRes() {
            return mParameters.getInt("dp_separation_line_width", getDefaultSeparationLineWidthDimenRes());
        }

        public @DimenRes int getDefaultSeparationLineWidthDimenRes() {
            return R.dimen.qbox_dialog_separation_line_width_fixed;
        }

        final public void setSeparationLineWidth(@DimenRes int widthRes) {
            mParameters.putInt("dp_separation_line_width", widthRes);
        }

        final public @DimenRes int getViewVerticalIntervalDimenRes() {
            return mParameters.getInt("dp_view_vertical_interval", getDefaultViewVerticalIntervalDimenRes());
        }

        public @DimenRes int getDefaultViewVerticalIntervalDimenRes() {
            return R.dimen.qbox_dialog_view_interval_vertical;
        }

        final public void setViewVerticalInterval(@DimenRes int intervalRes) {
            mParameters.putInt("dp_view_vertical_interval", intervalRes);
        }
    }

    private static class BaseDecorator extends Decorator {

        @Override
        protected int getDefaultContentLayoutRes() {
            return 0;
        }
    }

    public BaseDialog() {
        setArguments(new Bundle());
        //setExitType(EXIT_TYPE_OK_CANCEL);
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
        setViewBackground(layout, decorator.getBaseBackgroundRes());
//        int background = decorator.getBaseBackgroundRes();
//        if (background != 0) {
//            layout.setBackgroundResource(background);
//        }
        Resources resources = getResources();
        int leftPaddingDimenRes = decorator.getBaseLeftPaddingDimenRes();
        int topPaddingDimenRes = decorator.getBaseTopPaddingDimenRes();
        int rightPaddingDimenRes = decorator.getBaseRightPaddingDimenRes();
        int bottomPaddingDimenRes = decorator.getBaseBottomPaddingDimenRes();
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
            setViewPadding(layout, decorator.getBasePaddingDimenRes(), resources);
//            int paddingDimenRes = decorator.getBasePaddingDimenRes();
//            if (paddingDimenRes != 0) {
//                int padding = resources.getDimensionPixelOffset(paddingDimenRes);
//                layout.setPadding(padding, padding, padding, padding);
//            }
        }
        return layout;
    }

    protected void setViewBackground(View view, @DrawableRes int backgroundRes) {
        if (backgroundRes != 0) {
            view.setBackgroundResource(backgroundRes);
        }
    }

    protected void setViewPadding(View view, @DimenRes int paddingDimenRes) {
        setViewPadding(view, paddingDimenRes, getResources());
    }

    protected void setViewPadding(View view, @DimenRes int paddingDimenRes, Resources resources) {
        if (paddingDimenRes != 0) {
            int padding = resources.getDimensionPixelOffset(paddingDimenRes);
            view.setPadding(padding, padding, padding, padding);
        }
    }

    private void onCreateContentView(LayoutInflater inflater,
                                     LinearLayout baseView,
                                     D decorator,
                                     @Nullable Bundle savedInstanceState) {
        int contentLayoutRes = getContentLayoutRes(decorator);
        if (contentLayoutRes != 0) {
            inflater.inflate(contentLayoutRes, baseView);
            onSetContentView(baseView, decorator, savedInstanceState);
            int intervalRes = decorator.getViewVerticalIntervalDimenRes();
            if (intervalRes != 0) {
                int interval = getResources().getDimensionPixelSize(intervalRes);
                for (int i = 2, n = baseView.getChildCount();i < n;++i) {
                    View childView = baseView.getChildAt(i);
                    LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) childView.getLayoutParams();
                    params.setMargins(params.leftMargin, interval, params.rightMargin, params.bottomMargin);
                    childView.setLayoutParams(params);
                }
            }
        }
    }

    protected @LayoutRes int getContentLayoutRes(@NonNull D decorator) {
        return decorator.getContentLayoutRes();
    }

    private void onCreateTitle(LinearLayout baseView,
                               D decorator) {
        if (isDrawTitle()) {
            String title = getTitle();
            if (!TextUtils.isEmpty(title)) {
                ViewStub vsTitle = baseView.findViewById(R.id.vs_title_dialog_base);
                TextView tvTitle;
                View attachView;
                int titleLayoutRes = decorator.getTitleLayoutRes();
                int titleId = decorator.getTitleId();
                if (titleLayoutRes != 0 && titleId != 0) {
                    vsTitle.setLayoutResource(titleLayoutRes);
                    attachView = vsTitle.inflate();
                    tvTitle = (TextView) attachView.findViewById(titleId);
                } else {
                    tvTitle = (TextView) vsTitle.inflate();
                    attachView = tvTitle;
                }
                int titleSizeRes = decorator.getTitleTextSizeDimenRes();
                if (titleSizeRes != 0) {
                    setTextViewSize(tvTitle, titleSizeRes);
//                tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,
//                        getResources().getDimensionPixelSize(titleSizeRes));
                }
                tvTitle.setText(title);
                inflateSeparationLine(baseView, attachView, decorator, false);
            }
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
        if (decorator.isDrawSeparationLine() && position >= 0) {
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
        int interval = resources.getDimensionPixelSize(decorator.getViewVerticalIntervalDimenRes());
        int separationWidth = resources.getDimensionPixelSize(decorator.getSeparationLineWidthDimenRes());
        LinearLayout.LayoutParams params;
        if (isSeparationLine && separationWidth > 0) {
            tvLine.setBackgroundResource(decorator.getSeparationLineBackgroundRes());
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
            grpExit = inflater.inflate(decorator.getOkCancelLayoutRes(), baseView, false);
        } else if (exitType == EXIT_TYPE_OK) {
            grpExit = inflater.inflate(decorator.getOkLayoutRes(), baseView, false);
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
        int buttonLabelRes = decorator.getExitButtonLabelRes(okOrCancel);
        if (buttonLabelRes != 0) {
            btn.setText(buttonLabelRes);
        }
        btn.setOnClickListener(this);
        int textSizeRes = decorator.getExitButtonTextSizeDimenRes();
        if (textSizeRes != 0) {
            setTextViewSize(btn, textSizeRes);
//            btn.setTextSize(TypedValue.COMPLEX_UNIT_PX,
//                    getResources().getDimensionPixelSize(textSizeRes));
        }
        int textColorRes = decorator.getExitButtonTextColorRes();
        if (textColorRes != 0) {
            btn.setTextColor(ContextCompat.getColor(getActivity(), textColorRes));
        }
        int backgroundRes = decorator.getExitButtonBackgroundRes();
        if (backgroundRes != 0) {
            btn.setBackgroundResource(backgroundRes);
        }
    }

    protected void setTextViewSize(TextView view, @DimenRes int textSizeRes) {
        setTextViewSize(view, textSizeRes, getResources());
    }

    protected void setTextViewSize(TextView view, @DimenRes int textSizeRes, Resources resources) {
        if (textSizeRes != 0) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimensionPixelSize(textSizeRes));
        }
    }

    protected void setTextViewHintColor(TextView view, @ColorRes int hintColorRes) {
        setTextViewHintColor(view, hintColorRes, getResources());
    }

    protected void setTextViewHintColor(TextView view, @ColorRes int hintColorRes, Resources resources) {
        if (hintColorRes != 0) {
            view.setHintTextColor(resources.getColor(hintColorRes));
        }
    }

    protected int getExitType() {
        return getArguments().getInt(ARGUMENT_KEY_EXIT_TYPE, getDefaultExitType());
    }

    protected int getDefaultExitType() {
        return EXIT_TYPE_OK_CANCEL;
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

    public void setTitle(String title) {
        getArguments().putString(ARGUMENT_KEY_TITLE_STRING, title);
    }

    public void setTitle(@StringRes int titleRes) {
        getArguments().putInt(ARGUMENT_KEY_TITLE_RESOURCE, titleRes);
    }

    private String getTitle() {
        return getString(ARGUMENT_KEY_TITLE_RESOURCE, ARGUMENT_KEY_TITLE_STRING, getDefaultTitleRes());
    }

    protected String getString(String strResKey, String strKey) {
        return getString(strResKey, strKey, 0);
    }

    protected String getString(String strResKey, String strKey, @StringRes int defaultStrRes) {
        int strRes = getArguments().getInt(strResKey);
        if (strRes != 0) {
            return getString(strRes);
        }
        String str = getArguments().getString(strKey);
        if (!TextUtils.isEmpty(str)) {
            return str;
        }
        if (defaultStrRes != 0) {
            return getString(defaultStrRes);
        }
        return "";
    }

    public void setDrawTitle(boolean drawTitle) {
        getArguments().putBoolean(ARGUMENT_KEY_DRAW_TITLE, drawTitle);
    }

    public boolean isDrawTitle() {
        return getArguments().getBoolean(ARGUMENT_KEY_DRAW_TITLE, isDefaultDrawTitle());
    }

    public boolean isDefaultDrawTitle() {
        return true;
    }

    protected @StringRes int getDefaultTitleRes() {
        return 0;
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
