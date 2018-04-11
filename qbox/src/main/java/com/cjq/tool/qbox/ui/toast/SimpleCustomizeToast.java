package com.cjq.tool.qbox.ui.toast;

import android.content.Context;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.cjq.tool.qbox.R;

/**
 * Created by CJQ on 2017/7/4.
 */

public class SimpleCustomizeToast {

    private static Decorator defaultDecorator = new DefaultDecorator();
    private static Context applicationContext;

    private SimpleCustomizeToast() {
    }

    private static Context getContext() {
        if (applicationContext == null) {
            throw new NullPointerException("init before use SimpleCustomizeToast");
        }
        return applicationContext;
    }

    public static void init(@NonNull Context context) {
        applicationContext = context.getApplicationContext();
    }

    public static void setDefaultDecorator(Decorator decorator) {
        if (decorator != null) {
            defaultDecorator = decorator;
        }
    }

    public static void show(String information) {
        show(information, defaultDecorator);
    }

    public static void show(@StringRes int informationRes) {
        show(informationRes, defaultDecorator);
    }

    public static void show(String information, Decorator decorator) {
        //防止在工作线程中崩溃
        boolean needLooper = Looper.getMainLooper().getThread() != Thread.currentThread();
        if (needLooper) {
            Looper.prepare();
        }

        //设置toast
        Toast toast = new Toast(getContext());
        if (decorator == null) {
            decorator = defaultDecorator;
        }
        toast.setView(decorator.setInformation(getContext(), information));
        decorator.customize(toast);
        toast.show();

        //在工作线程中处理掉looper
        if (needLooper) {
            Looper.loop();
            Looper.myLooper().quit();
        }
    }

    public static void show(@StringRes int informationRes, Decorator decorator) {
        show(getContext().getString(informationRes), decorator);
    }

    public interface Decorator {
        View setInformation(Context context, String information);
        void customize(Toast toast);
    }

    public static class DefaultDecorator implements Decorator {

        @Override
        public View setInformation(Context context, String information) {
            View view = LayoutInflater.from(context).inflate(R.layout.qbox_toast_appearance, null);
            TextView tvContent = (TextView) view.findViewById(R.id.tv_toast);
            tvContent.setText(information);
            return view;
        }

        @Override
        public void customize(Toast toast) {
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
    }
}
