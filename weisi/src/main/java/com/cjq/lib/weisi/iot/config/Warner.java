package com.cjq.lib.weisi.iot.config;

import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.cjq.lib.weisi.iot.config.Corrector;
import com.cjq.lib.weisi.iot.container.Value;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by CJQ on 2018/3/16.
 */
public interface Warner<V extends Value> extends Parcelable {
    @IntDef({RESULT_NORMAL,
            RESULT_ABOVE_HIGH_LIMIT,
            RESULT_BELOW_LOW_LIMIT,
            RESULT_ABNORMAL})
    @Retention(RetentionPolicy.SOURCE)
    @interface Result {
    }

    int RESULT_NORMAL = 0;
    int RESULT_ABOVE_HIGH_LIMIT = 1;
    int RESULT_BELOW_LOW_LIMIT = 2;
    int RESULT_ABNORMAL = 3;
    @Result int test(@NonNull V value, Corrector corrector);

    public static double getTestingValue(@NonNull Value value, Corrector corrector) {
        double testingValue;
        if (corrector != null) {
            testingValue = corrector.correctValue(value.getRawValue());
        } else {
            testingValue = value.getRawValue();
        }
        return testingValue;
    }
}
