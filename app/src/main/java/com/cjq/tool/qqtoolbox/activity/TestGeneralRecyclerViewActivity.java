package com.cjq.tool.qqtoolbox.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qbox.util.CodeRunTimeCatcher;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.util.DebugTag;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TestGeneralRecyclerViewActivity extends AppCompatActivity {

    private List<Item> mItems = new ArrayList<>();
    private boolean mItemAutoGenerating;
    private GeneralRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_general_recycler_view);

        RecyclerView rvGeneral = (RecyclerView) findViewById(R.id.rv_general);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvGeneral.setLayoutManager(linearLayoutManager);
        mAdapter = new GeneralRecyclerViewAdapter(mItems);
        rvGeneral.setItemAnimator(new DefaultItemAnimator());
        rvGeneral.setAdapter(mAdapter);
        autoRandomGenerateItems();
    }

    @Override
    protected void onDestroy() {
        mItemAutoGenerating = false;
        super.onDestroy();
    }

    private void autoRandomGenerateItems() {
        Thread thread = new Thread(mAutoRandomGenerateItems);
        thread.start();
    }

    private Runnable mAutoRandomGenerateItems = new Runnable() {

        @Override
        public void run() {
            final int MAX_ITEM_COUNT = 50;
            mItemAutoGenerating = true;
            Random random = new Random();
            while (mItemAutoGenerating) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (mItems.size() < MAX_ITEM_COUNT) {
                    final int start = mItems.size();
                    for (int n = random.nextInt(5);n >= 0 && mItems.size() < MAX_ITEM_COUNT;--n) {
                        mItems.add(new Item(random.nextInt(), System.currentTimeMillis(), (byte) random.nextInt(256), random.nextDouble()));
                    }
                    final int count = mItems.size() - start;
                    if (count > 0) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyItemRangeInserted(start, count);
                            }
                        });
                    }
                } else {
                    for (int n = random.nextInt(5);n >= 0;--n) {
                        final int index = random.nextInt(MAX_ITEM_COUNT);
                        Item item = mItems.get(index);
                        item.setTimestamp(System.currentTimeMillis());
                        item.setValue(random.nextDouble());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mAdapter.notifyItemChanged(index, index);
                            }
                        });
                    }
                }
            }
        }
    };

    private static class Item {

        private int mAddress;
        private long mTimestamp;
        private byte mType;
        private double mValue;

        public Item(int address, long timestamp, byte type, double value) {
            mAddress = address;
            mTimestamp = timestamp;
            mType = type;
            mValue = value;
        }

        public int getAddress() {
            return mAddress;
        }

        public void setAddress(int address) {
            mAddress = address;
        }

        public long getTimestamp() {
            return mTimestamp;
        }

        public void setTimestamp(long timestamp) {
            mTimestamp = timestamp;
        }

        public byte getType() {
            return mType;
        }

        public void setType(byte type) {
            mType = type;
        }

        public double getValue() {
            return mValue;
        }

        public void setValue(double value) {
            mValue = value;
        }
    }

    private static class GeneralRecyclerViewAdapter extends RecyclerView.Adapter<GeneralRecyclerViewAdapter.ViewHolder> {

        private final List<Item> mItems;
        private static final Date TIMESTAMP_SETTER = new Date();
        private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HH:mm:ss");
        private static int createTimes;

        private GeneralRecyclerViewAdapter(List<Item> items) {
            mItems = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            CodeRunTimeCatcher.start();
            ViewHolder holder = new ViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.list_item_test_general_recyclerview,
                            parent,
                            false));
            ClosableLog.d(DebugTag.TEST_GENERAL_RECYCLER_VIEW,
                    "on create view, create times = "
                            + (++createTimes) +
                            ", time = "
                            + CodeRunTimeCatcher.end() / 100000);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            CodeRunTimeCatcher.start();
            Item item = mItems.get(position);
            holder.mTvSensorAddress.setText(String.valueOf(item.getAddress()));
            TIMESTAMP_SETTER.setTime(item.getTimestamp());
            holder.mTvTimestamp.setText(DATE_FORMAT.format(TIMESTAMP_SETTER));
            holder.mTvMeasurementType.setText(String.format("%02X", item.getType()));
            holder.mTvMeasurementValue.setText(String.format("%.2f", item.getValue()));
            ClosableLog.d(DebugTag.TEST_GENERAL_RECYCLER_VIEW,
                    "on bind view, previous position = "
                            + holder.mTvSensorAddress.getTag()
                            + ", current position = "
                            + position
                            + ", create times = "
                            + createTimes
                            + ", time = "
                            + CodeRunTimeCatcher.end() / 100000);
            holder.mTvSensorAddress.setTag(position);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position, List<Object> payloads) {
            if (payloads.isEmpty()) {
                onBindViewHolder(holder, position);
            } else {
                onBindViewHolder(holder, position);
            }
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            ClosableLog.d(DebugTag.TEST_GENERAL_RECYCLER_VIEW,
                    "on recycle view position = " + holder.getLayoutPosition());
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mTvSensorAddress;
            private TextView mTvTimestamp;
            private TextView mTvMeasurementType;
            private TextView mTvMeasurementValue;

            public ViewHolder(View itemView) {
                super(itemView);
                mTvSensorAddress = (TextView) itemView.findViewById(R.id.tv_sensor_address);
                mTvTimestamp = (TextView) itemView.findViewById(R.id.tv_time);
                mTvMeasurementType = (TextView) itemView.findViewById(R.id.tv_measurement_type);
                mTvMeasurementValue = (TextView) itemView.findViewById(R.id.tv_measurement_value);
            }
        }
    }
}
