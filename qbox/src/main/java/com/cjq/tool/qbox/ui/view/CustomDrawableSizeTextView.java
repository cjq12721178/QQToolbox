package com.cjq.tool.qbox.ui.view;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by CJQ on 2017/6/15.
 */

public class CustomDrawableSizeTextView extends AppCompatTextView {

    public CustomDrawableSizeTextView(Context context) {
        this(context, null);
    }

    public CustomDrawableSizeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public CustomDrawableSizeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DrawableCustomSizeBuilder.build(this, context, attrs);
    }

//    @Override
//    public boolean performClick() {
//        try {
//            return super.performClick();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return true;
//    }
}
