package com.cjq.lib.weisi.protocol;


import com.cjq.lib.weisi.util.NumericConverter;

/**
 * Created by CJQ on 2018/1/10.
 */

public abstract class UsbSensorProtocol<A extends Analyzable> extends ControllableSensorProtocol<A> {

    public static final byte COMMAND_CODE_TIME_SYNCHRONIZATION = 0x62;
    protected static final int SENSOR_DATA_LENGTH = 20;
    protected static final int SENSOR_ADDRESS_LENGTH = 4;

    protected UsbSensorProtocol(A analyzer) {
        super(analyzer);
    }

    @Override
    public byte getTimeSynchronizationCommandCode() {
        return COMMAND_CODE_TIME_SYNCHRONIZATION;
    }

    @Override
    protected void onTimeSynchronizationAnalyzed(byte[] data,
                                                 int realDataZoneStart,
                                                 int realDataZoneLength,
                                                 OnFrameAnalyzedListener listener) {
        if (realDataZoneLength != TimeSynchronizationFrameBuilderImp.TIME_ZONE_LENGTH) {
            return;
        }
        listener.onTimeSynchronizationAnalyzed(analyzeTimestamp(data, realDataZoneStart));
    }

    protected long analyzeTimestamp(byte[] data, int position) {
        return NumericConverter.int8toUInt32ByMSB(data,
                position) * 1000;
    }

    @Override
    public TimeSynchronizationFrameBuilder getTimeSynchronizationFrameBuilder() {
        return new TimeSynchronizationFrameBuilderImp();
    }

    public class TimeSynchronizationFrameBuilderImp extends TimeSynchronizationFrameBuilder {

        private static final int TIME_ZONE_LENGTH = 4;

        protected TimeSynchronizationFrameBuilderImp() {
            super(COMMAND_CODE_TIME_SYNCHRONIZATION);
        }

        @Override
        protected int getDataZoneLength() {
            return TIME_ZONE_LENGTH;
        }

        @Override
        protected void fillDataZone(byte[] frame, int offset) {
            int time = (int) (System.currentTimeMillis() / 1000);
            frame[offset] = (byte) (time >> 24);
            frame[++offset] = (byte) ((time >> 16) & 0xff);
            frame[++offset] = (byte) ((time >> 8) & 0xff);
            frame[++offset] = (byte) (time & 0xff);
        }
    }

    @Override
    public Crc getCrc() {
        return Crc.getCcitt();
    }

    @Override
    public boolean isCrcMsb() {
        return true;
    }
}
