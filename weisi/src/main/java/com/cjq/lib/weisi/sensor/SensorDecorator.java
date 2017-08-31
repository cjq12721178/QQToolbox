package com.cjq.lib.weisi.sensor;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by CJQ on 2017/8/9.
 */

public class SensorDecorator implements OnRawAddressComparer {

    private final int mRawAddress;
    private String mName;
    private final MeasurementDecorator[] mMeasurementDecorators;

    public SensorDecorator(int rawAddress) {
        if ((rawAddress & 0xff000000) != 0) {
            throw new IllegalArgumentException("raw address error");
        }
        //根据配置生成测量参数修饰器列表
        Configuration configuration = ConfigurationManager.findConfiguration(rawAddress);
        if (configuration == null) {
            throw new NullPointerException("current sensor address is not in configuration range");
        }
        mRawAddress = rawAddress;
        List<MeasurementDecorator> measurementDecorators = new ArrayList<>();
        for (Configuration.MeasureParameter parameter :
                configuration.getMeasureParameters()) {
            do {
                measurementDecorators.add(new MeasurementDecorator(parameter.mInvolvedDataType.mValue));
            } while ((parameter = parameter.mNext) != null);
        }
        mMeasurementDecorators = new MeasurementDecorator[measurementDecorators.size()];
        measurementDecorators.toArray(mMeasurementDecorators);
    }

    @Override
    public int getRawAddress() {
        return mRawAddress;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public MeasurementDecorator[] getMeasurementDecorators() {
        return mMeasurementDecorators;
    }
}
