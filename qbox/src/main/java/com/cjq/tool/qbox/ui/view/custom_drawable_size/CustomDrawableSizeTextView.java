package com.cjq.tool.qbox.ui.view.custom_drawable_size;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

/**
 * Created by CJQ on 2017/6/15.
 */

public class CustomDrawableSizeTextView extends AppCompatTextView {

    public CustomDrawableSizeTextView(Context context) {
        this(context, null);
    }

    public CustomDrawableSizeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomDrawableSizeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        DrawableCustomSizeBuilder.build(this, context, attrs);
    }
}
