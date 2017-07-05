package com.cjq.tool.qbox.ui.view;

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
        this(context, attrs, android.support.v7.appcompat.R.attr.radioButtonStyle);
    }

    public CustomDrawableSizeRadioButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DrawableCustomSizeBuilder.build(this, context, attrs);
    }
}
