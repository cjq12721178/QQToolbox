package com.cjq.tool.qbox.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.cjq.tool.qbox.R;


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

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.QboxDrawableCustomSize);
        Drawable[] drawables = src.getCompoundDrawables();
        for (int i = 0;i < drawables.length;++i) {
            setDrawableDimension(typedArray, drawables[i], i);
        }
        src.setCompoundDrawables(drawables[0], drawables[1], drawables[2], drawables[3]);
        typedArray.recycle();
    }

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
