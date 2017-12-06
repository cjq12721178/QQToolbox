package com.cjq.tool.qbox.ui.preference;

import android.content.Context;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;

/**
 * Created by KAT on 2016/8/10.
 */
public class PortPreference extends EditTextPreference {
    public PortPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PortPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PortPreference(Context context) {
        super(context);
        init();
    }

    private void init() {
        getEditText().setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
    }

    @Override
    protected boolean callChangeListener(Object newValue) {
        boolean correct = isPortFormatCorrect((String)newValue);
        return super.callChangeListener(correct ? newValue : null) && correct;
    }

    private boolean isPortFormatCorrect(String strPort) {
        if (strPort == null)
            return false;

        if (strPort.length() > 5)
            return false;

        int intPort = Integer.parseInt(strPort);
        if (intPort <= 0 || intPort >= 65536)
            return false;

        return true;
    }
}
