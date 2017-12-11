package com.cjq.tool.qbox.ui.preference;

import android.content.Context;

import android.preference.EditTextPreference;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.AttributeSet;

/**
 * Created by KAT on 2016/8/9.
 */
public class IPPreference extends EditTextPreference {

    public IPPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public IPPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IPPreference(Context context) {
        super(context);
        init();
    }

    @Override
    protected boolean callChangeListener(Object newValue) {
        boolean correct = isIpFormatCorrect(getIpCharArray((String)newValue), true);
        return super.callChangeListener(correct ? newValue : null) && correct;
    }

    private void init() {
        getEditText().setFilters(new InputFilter[] {
                new InputFilter() {
                    @Override
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        int tend = dstart + end - start + dest.length() - dend;
                        if (tend >= charsIp.length)
                            return "";

                        //构造新的IP
                        for (int i = start;i < end;++i) {
                            charsIp[dstart + i] = source.charAt(i);
                        }
                        for (int i = 0, len = dest.length() - dend;i < len;++i) {
                            charsIp[dstart + end - start + i] = dest.charAt(dend + i);
                        }
                        charsIp[tend] = '\0';
                        //判断新IP格式是否正确
                        CharSequence result;
                        if (isIpFormatCorrect(charsIp, false)) {
                            result = null;
                        } else {
                            getEditText().setError(formatErrorMessage);
                            result = start == end ? null : "";
                        }
                        return result;
                    }
                    private char[] charsIp = new char[IP_LEN_LIMIT];
                }
        });
    }

    private boolean isIpFormatCorrect(char[] target, boolean isNeedCompleteCheck) {
        if (target == null)
            return false;

        int dotCount = 0;
        int dotInterval = 0;
        int digit;
        int sum = 0;
        for (char c :
                target) {
            if (c == '\0')
                break;
            if (c == '.') {
                if (dotInterval == 0)
                    return false;
                if (++dotCount > 3)
                    return false;
                dotInterval = 0;
            } else {
                if (++dotInterval > 3)
                    return false;
                digit = c - '0';
                if (digit < 0 || digit > 9)
                    return false;
                if (dotInterval == 1) {
                    sum = 0;
                }
                sum = sum * 10 + digit;
                if (sum > 255)
                    return false;
            }
        }
        if (isNeedCompleteCheck) {
            if (dotCount != 3 || dotInterval == 0)
                return false;
        }
        return true;
    }

    private char[] getIpCharArray(String strIp) {
        if (strIp == null)
            return null;
        char[] charsIp = new char[strIp.length() + 1];
        strIp.getChars(0, strIp.length(), charsIp, 0);
        charsIp[charsIp.length - 1] = '\0';
        return charsIp;
    }

    private static int IP_LEN_LIMIT = 16;
    private static String formatErrorMessage = "不符合IP格式规范的字符将无法输入";
}
