package com.cjq.tool.qbox.ui.view.custom_drawable_size;

import android.content.Context;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;

/**
 * Created by CJQ on 2017/6/15.
 */

public class CustomDrawableSizeRadioButton extends AppCompatRadioButton {

    public CustomDrawableSizeRadioButton(Context context) {
        this(context, null);
    }

    public CustomDrawableSizeRadioButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDrawableSizeRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DrawableCustomSizeBuilder.build(this, context, attrs);
    }
}
