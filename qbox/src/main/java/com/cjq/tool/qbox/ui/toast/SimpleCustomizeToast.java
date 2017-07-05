package com.cjq.tool.qbox.ui.toast;

import android.content.Context;
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

    private SimpleCustomizeToast() {
    }

    public static void setDefaultDecorator(Decorator decorator) {
        if (decorator != null) {
            defaultDecorator = decorator;
        }
    }

    public static void show(Context context, String information) {
        show(context, information, defaultDecorator);
    }

    public static void show(Context context, @StringRes int informationRes) {
        show(context, informationRes, defaultDecorator);
    }

    public static void show(Context context, String information, Decorator decorator) {
        Toast toast = new Toast(context);
        if (decorator == null) {
            decorator = defaultDecorator;
        }
        toast.setView(decorator.setInformation(context, information));
        decorator.customize(toast);
        toast.show();
    }

    public static void show(Context context, @StringRes int informationRes, Decorator decorator) {
        show(context, context.getString(informationRes), decorator);
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
