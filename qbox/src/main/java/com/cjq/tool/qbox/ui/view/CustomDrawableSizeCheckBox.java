package com.cjq.tool.qbox.ui.view;

import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

/**
 * Created by CJQ on 2017/6/15.
 */

public class CustomDrawableSizeCheckBox extends AppCompatCheckBox {

    public CustomDrawableSizeCheckBox(Context context) {
        this(context, null);
    }

    public CustomDrawableSizeCheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDrawableSizeCheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DrawableCustomSizeBuilder.build(this, context, attrs);
    }
}
