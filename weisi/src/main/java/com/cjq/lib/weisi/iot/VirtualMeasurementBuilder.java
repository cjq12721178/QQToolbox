package com.cjq.lib.weisi.iot;

import android.support.annotation.NonNull;

public interface VirtualMeasurementBuilder {

    @NonNull DisplayMeasurement build(@NonNull ID id);
}
