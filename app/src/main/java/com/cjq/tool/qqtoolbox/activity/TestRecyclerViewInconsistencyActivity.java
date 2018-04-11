package com.cjq.tool.qqtoolbox.activity;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.IntDef;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.cjq.tool.qqtoolbox.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.cjq.tool.qqtoolbox.activity.TestRecyclerViewInconsistencyActivity.NumberAdapter.ItemMotion.*;

public class TestRecyclerViewInconsistencyActivity extends AppCompatActivity implements View.OnClickListener {

    private NumberAdapter mAdapter;
    private List<Integer> mNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_recycler_view_inconsistency);

        findViewById(R.id.btn_add).setOnClickListener(this);
        RecyclerView rvNumbers = findViewById(R.id.rv_numbers);
        //rvNumbers.setLayoutManager(new WrapContentLinearLayoutManager(this));
        rvNumbers.setLayoutManager(new LinearLayoutManager(this));
        mNumbers = new ArrayList<>();
        mAdapter = new NumberAdapter(mNumbers);
        rvNumbers.setAdapter(mAdapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                for (int i = 0;i < 100;++i) {
                    final int n = i;
                    mNumbers.add(n);
                    mAdapter.scheduleItemInsert(n);
                    mHandler.sendEmptyMessage(n);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private boolean mIsDelay = true;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            //mNumbers.add(msg.what);
            //mAdapter.notifyItemInserted(msg.what);
            //mAdapter.scheduleItemInsert(msg.what);
            mAdapter.notifyScheduleDataSetChanged();

            if (mIsDelay) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mIsDelay = false;
            }
        }
    };

    @Override
    public void onClick(View v) {
        int n = mNumbers.size();
        mNumbers.add(n);
        mAdapter.notifyItemInserted(n);
    }

    public static class NumberAdapter extends RecyclerView.Adapter<NumberAdapter.ViewHolder> {

        private List<Integer> mNumbers;
        private final LinkedList<ItemMotion> mItemMotions = new LinkedList<>();

        public NumberAdapter(List<Integer> numbers) {
            mNumbers = numbers;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_number, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mTvNumber.setText(mNumbers.get(position).toString());
        }

        @Override
        public int getItemCount() {
            return mNumbers != null ? mNumbers.size() : 0;
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {

            private TextView mTvNumber;

            public ViewHolder(View itemView) {
                super(itemView);
                mTvNumber = itemView.findViewById(R.id.tv_number);
            }
        }

        public void notifyScheduleDataSetChanged() {
            synchronized (mItemMotions) {
                if (!mItemMotions.isEmpty()) {
                    do {
                        ItemMotion itemMotion = mItemMotions.pollFirst();
                        switch (itemMotion.getMotion()) {
                            case MOTION_CHANGE:
                                if (itemMotion.getPayload() == null) {
                                    notifyItemRangeChanged(itemMotion.getPositionStart(), itemMotion.getItemCount());
                                } else {
                                    notifyItemRangeChanged(itemMotion.getPositionStart(), itemMotion.getItemCount(), itemMotion.getPayload());
                                }
                                break;
                            case MOTION_INSERT:
                                notifyItemRangeInserted(itemMotion.getPositionStart(), itemMotion.getItemCount());
                                break;
                            case MOTION_REMOVE:
                                notifyItemRangeRemoved(itemMotion.getPositionStart(), itemMotion.getItemCount());
                                break;
                            case MOTION_MOVE:
                                notifyItemMoved(itemMotion.getPositionStart(), itemMotion.getItemCount());
                                break;
                            case MOTION_RESET:
                                notifyDataSetChanged();
                                break;
                        }
                    } while (!mItemMotions.isEmpty());
                }
            }
        }

        public void scheduleDataSetReset() {
            synchronized (mItemMotions) {
                mItemMotions.addLast(new ItemMotion());
            }
        }

        public int scheduleItemChange(int position) {
            return scheduleItemRangeChange(position, 1, null);
        }

        public int scheduleItemChange(int position, Object payload) {
            return scheduleItemRangeChange(position, 1, payload);
        }

        public int scheduleItemRangeChange(int positionStart, int itemCount) {
            return scheduleItemRangeChange(positionStart, itemCount, null);
        }

        public int scheduleItemRangeChange(int positionStart, int itemCount, Object payload) {
            synchronized (mItemMotions) {
                ItemMotion itemMotion = new ItemMotion(positionStart, itemCount, MOTION_CHANGE, payload);
                mItemMotions.addLast(itemMotion);
                return itemMotion.getId();
            }
        }

        public int scheduleItemInsert(int position) {
            return scheduleItemRangeInsert(position, 1);
        }

        public int scheduleItemRangeInsert(int positionStart, int itemCount) {
            synchronized (mItemMotions) {
                ItemMotion itemMotion = new ItemMotion(positionStart, itemCount, MOTION_INSERT, null);
                mItemMotions.addLast(itemMotion);
                return itemMotion.getId();
            }
        }

        public int scheduleItemRemove(int position) {
            return scheduleItemRangeRemove(position, 1);
        }

        public int scheduleItemRangeRemove(int positionStart, int itemCount) {
            synchronized (mItemMotions) {
                ItemMotion itemMotion = new ItemMotion(positionStart, itemCount, MOTION_REMOVE, null);
                mItemMotions.addLast(itemMotion);
                return itemMotion.getId();
            }
        }

        public int scheduleItemMove(int fromPosition, int toPosition) {
            synchronized (mItemMotions) {
                ItemMotion itemMotion = new ItemMotion(fromPosition, toPosition, MOTION_MOVE);
                mItemMotions.addLast(itemMotion);
                return itemMotion.getId();
            }
        }

        public int scheduleItemMotion(ItemMotion motion) {
            if (motion == null) {
                return -1;
            }
            synchronized (mItemMotions) {
                mItemMotions.addLast(motion);
                return motion.getId();
            }
        }

        public void cancelItemMotion(int motionId) {
            synchronized (mItemMotions) {
                for (ItemMotion motion : mItemMotions) {
                    if (motion.getId() == motionId) {
                        mItemMotions.remove(motion);
                        break;
                    }
                }
            }
        }

        /**
         * Created by CJQ on 2018/2/26.
         */

        public static class ItemMotion {

            private static int autoincrementId = 0;

            @IntDef({MOTION_RESET, MOTION_CHANGE, MOTION_INSERT, MOTION_REMOVE, MOTION_MOVE})
            @Retention(RetentionPolicy.SOURCE)
            @interface Motion {
            }

            public static final int MOTION_RESET = 0;
            public static final int MOTION_CHANGE = 1;
            public static final int MOTION_INSERT = 2;
            public static final int MOTION_REMOVE = 3;
            public static final int MOTION_MOVE = 4;

            private final int mId;
            private final int mPositionStart;
            private final int mItemCount;
            private final @Motion int mMotion;
            private final Object mPayload;

            public ItemMotion() {
                this(0, 0, MOTION_RESET, null);
            }

            public ItemMotion(int positionStart, int itemCount, @Motion int motion) {
                this(positionStart, itemCount, motion, null);
            }

            public ItemMotion(int positionStart, int itemCount, @Motion int motion, Object payload) {
                mId = autoincrementId++;
                mPositionStart = positionStart;
                mItemCount = itemCount;
                mMotion = motion;
                mPayload = payload;
            }

            public int getId() {
                return mId;
            }

            public int getPositionStart() {
                return mPositionStart;
            }

            public int getItemCount() {
                return mItemCount;
            }

            public @Motion int getMotion() {
                return mMotion;
            }

            public Object getPayload() {
                return mPayload;
            }
        }
    }

    private static class WrapContentLinearLayoutManager extends LinearLayoutManager {
        public WrapContentLinearLayoutManager(Context context) {
            super(context);
        }

        public WrapContentLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
            super(context, orientation, reverseLayout);
        }

        public WrapContentLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
            super(context, attrs, defStyleAttr, defStyleRes);
        }

        //... constructor
        @Override
        public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
            try {
                super.onLayoutChildren(recycler, state);
            } catch (IndexOutOfBoundsException e) {
                Log.e("glt", "meet a IOOBE in RecyclerView");
            }
        }
    }
}
