package com.cjq.tool.qbox.ui.view;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import static android.util.TypedValue.COMPLEX_UNIT_PX;

/**
 * Created by CJQ on 2017/7/13.
 */

public class SizeSelfAdaptionTextView extends AppCompatTextView {

    private static float DEFAULT_MIN_TEXT_SIZE = 10;
    private static float DEFAULT_MAX_TEXT_SIZE = 40;

    // Attributes
    private Paint mTestPaint;
    private float mMinTextSize;
    private float mMaxTextSize;

    public SizeSelfAdaptionTextView(Context context) {
        this(context, null);
    }

    public SizeSelfAdaptionTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public SizeSelfAdaptionTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialise();
    }

    private void initialise() {
        mTestPaint = new Paint();
        mTestPaint.set(getPaint());
        // max size defaults to the intially specified text size unless it is
        // too small
        mMaxTextSize = getTextSize();
        if (mMaxTextSize <= DEFAULT_MIN_TEXT_SIZE) {
            mMaxTextSize = DEFAULT_MAX_TEXT_SIZE;
        }
        mMinTextSize = DEFAULT_MIN_TEXT_SIZE;
    }

    private void resizeText(CharSequence text, int viewWidth) {
        if (text != null && viewWidth > 0) {
            setTextSize(COMPLEX_UNIT_PX,
                    findMostSuitableTextSize(text,
                            viewWidth - getPaddingLeft() - getPaddingRight()));
        }
    }

    private int findMostSuitableTextSize(CharSequence text, int availableWidth) {
        int low = (int) (mMinTextSize + 0.5);
        int high = (int) (mMaxTextSize + 0.5);

        while (low <= high) {
            int mid = (low + high) >>> 1;
            mTestPaint.setTextSize(mid);
            int cmp = (int) (mTestPaint.measureText(text, 0, text.length()) + 0.5)
                    - availableWidth;
            if (cmp < 0)
                low = mid + 1;
            else if (cmp > 0)
                high = mid - 1;
            else
                return mid; // key found
        }
        return low < high ? low : high;  // key not found.
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int before,
                                 int after) {
        super.onTextChanged(text, start, before, after);
        resizeText(text, getWidth());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            resizeText(getText(), w);
        }
    }
}
