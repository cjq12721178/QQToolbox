package com.cjq.tool.qbox.ui.view;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.cjq.tool.qbox.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static android.view.View.NO_ID;


/**
 * Created by CJQ on 2017/6/15.
 */

public class DrawableCustomSizeBuilder {

    private DrawableCustomSizeBuilder() {
    }

    public static void build(TextView src, Context context, AttributeSet attrs) {
        if (src == null || context == null || attrs == null) {
            return;
        }

        //设置Drawables可变大小
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.QboxDrawableCustomSize);
        Drawable[] drawables = src.getCompoundDrawables();
        for (int i = 0;i < drawables.length;++i) {
            setDrawableDimension(typedArray, drawables[i], i);
        }
        src.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
//        String handlerName = typedArray.getString(R.styleable.QboxDrawableCustomSize_onClick);
//        if (handlerName != null) {
//            src.setOnClickListener(new DeclaredOnClickListener(src, handlerName));
//        }
        typedArray.recycle();
        //修复XML中onClick事件中止程序运行的bug
//        TypedArray array = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.View);
//        String handlerName = array.getString(R.styleable.View_onClick);
//        if (handlerName.length() > 0) {
//            src.setOnClickListener(new DeclaredOnClickListener(src, handlerName));
//        }
    }

//    private static class DeclaredOnClickListener implements View.OnClickListener {
//        private final View mHostView;
//        private final String mMethodName;
//
//        private Method mResolvedMethod;
//        private Context mResolvedContext;
//
//        public DeclaredOnClickListener(@NonNull View hostView, @NonNull String methodName) {
//            mHostView = hostView;
//            mMethodName = methodName;
//        }
//
//        @Override
//        public void onClick(@NonNull View v) {
//            if (mResolvedMethod == null) {
//                resolveMethod(mHostView.getContext(), mMethodName);
//            }
//
//            try {
//                mResolvedMethod.invoke(mResolvedContext, v);
//            } catch (IllegalAccessException e) {
//                throw new IllegalStateException(
//                        "Could not execute non-public method for android:onClick", e);
//            } catch (InvocationTargetException e) {
//                throw new IllegalStateException(
//                        "Could not execute method for android:onClick", e);
//            }
//        }
//
//        @NonNull
//        private void resolveMethod(@Nullable Context context, @NonNull String name) {
//            while (context != null) {
//                try {
//                    if (!context.isRestricted()) {
//                        final Method method = context.getClass().getMethod(mMethodName, View.class);
//                        if (method != null) {
//                            mResolvedMethod = method;
//                            mResolvedContext = context;
//                            return;
//                        }
//                    }
//                } catch (NoSuchMethodException e) {
//                    // Failed to find method, keep searching up the hierarchy.
//                }
//
//                if (context instanceof ContextWrapper) {
//                    context = ((ContextWrapper) context).getBaseContext();
//                } else {
//                    // Can't search up the hierarchy, null out and fail.
//                    context = null;
//                }
//            }
//
//            final int id = mHostView.getId();
//            final String idText = id == NO_ID ? "" : " with id '"
//                    + mHostView.getContext().getResources().getResourceEntryName(id) + "'";
//            throw new IllegalStateException("Could not find method " + mMethodName
//                    + "(View) in a parent or ancestor Context for android:onClick "
//                    + "attribute defined on view " + mHostView.getClass() + idText);
//        }
//    }

    //index = 0,1,2,3，分别为left,top,right,bottom
    private static void setDrawableDimension(TypedArray typedArray, Drawable drawable, int index) {
        if (drawable == null)
            return;
        int width;
        int height;
        switch (index) {
            case 0:
                width = typedArray.getDimensionPixelSize(R.styleable.QboxDrawableCustomSize_drawableLeft_width, -1);
                height = typedArray.getDimensionPixelSize(R.styleable.QboxDrawableCustomSize_drawableLeft_height, -1);
                break;
            case 1:
                width = typedArray.getDimensionPixelSize(R.styleable.QboxDrawableCustomSize_drawableTop_width, -1);
                height = typedArray.getDimensionPixelSize(R.styleable.QboxDrawableCustomSize_drawableTop_height, -1);
                break;
            case 2:
                width = typedArray.getDimensionPixelSize(R.styleable.QboxDrawableCustomSize_drawableRight_width, -1);
                height = typedArray.getDimensionPixelSize(R.styleable.QboxDrawableCustomSize_drawableRight_height, -1);
                break;
            case 3:
                width = typedArray.getDimensionPixelSize(R.styleable.QboxDrawableCustomSize_drawableBottom_width, -1);
                height = typedArray.getDimensionPixelSize(R.styleable.QboxDrawableCustomSize_drawableBottom_height, -1);
                break;
            default:
                width = -1;
                height = -1;
                break;
        }
        if (width != -1 && height != -1) {
            drawable.setBounds(0, 0, width, height);
        }
    }
}
