package com.cjq.tool.qbox.ui.adapter;

import android.database.Cursor;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;

import static com.cjq.tool.qbox.ui.adapter.RecyclerViewCursorAdapter.ItemMotion.*;

/**
 * Created by CJQ on 2018/2/26.
 */

public class RecyclerViewCursorAdapter extends RecyclerViewBaseAdapter<Cursor> {

    private final LinkedList<ItemMotion> mItemMotions = new LinkedList<>();
    private boolean mDataValid;
    private Cursor mCursor;
    private int mRowIDColumn;
    private final String mRowIDColumnName;

    public RecyclerViewCursorAdapter() {
        this(null);
    }

    public RecyclerViewCursorAdapter(Cursor cursor) {
        this(cursor, "id");
    }

    public RecyclerViewCursorAdapter(Cursor cursor, String idColumnName) {
        boolean cursorPresent = cursor != null;
        mCursor = cursor;
        mDataValid = cursorPresent;
        mRowIDColumnName = idColumnName;
        mRowIDColumn = cursorPresent ? cursor.getColumnIndexOrThrow(mRowIDColumnName) : -1;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public Cursor getItemByPosition(int position) {
        if (mDataValid && mCursor != null) {
            mCursor.moveToPosition(position);
            return mCursor;
        } else {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        if (mDataValid && mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(int position) {
        if (mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getLong(mRowIDColumn);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public int getItemIdInt(int position) {
        if (mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getInt(mRowIDColumn);
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public String getItemIdString(int position) {
        if (mDataValid && mCursor != null) {
            if (mCursor.moveToPosition(position)) {
                return mCursor.getString(mRowIDColumn);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mRowIDColumn = newCursor.getColumnIndexOrThrow(mRowIDColumnName);
            mDataValid = true;
            if (mItemMotions.isEmpty()) {
                if (oldCursor == null) {
                    notifyItemRangeInserted(0, mCursor.getCount());
                } else {
                    notifyDataSetChanged(oldCursor.getCount());
                }
            } else {
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
                        case MOTION_RESET:
                            notifyDataSetChanged();
                            break;
                    }
                } while (!mItemMotions.isEmpty());
            }
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
            // notify the observers about the lack of a data set
            notifyDataSetChanged();
        }
        return oldCursor;
    }

    public void scheduleDataSetReset() {
        mItemMotions.addLast(new ItemMotion());
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
        ItemMotion itemMotion = new ItemMotion(positionStart, itemCount, MOTION_CHANGE, payload);
        mItemMotions.addLast(itemMotion);
        return itemMotion.getId();
    }

    public int scheduleItemInsert(int position) {
        return scheduleItemRangeInsert(position, 1);
    }

    public int scheduleItemRangeInsert(int positionStart, int itemCount) {
        ItemMotion itemMotion = new ItemMotion(positionStart, itemCount, MOTION_INSERT, null);
        mItemMotions.addLast(itemMotion);
        return itemMotion.getId();
    }

    public int scheduleItemRemove(int position) {
        return scheduleItemRangeRemove(position, 1);
    }

    public int scheduleItemRangeRemove(int positionStart, int itemCount) {
        ItemMotion itemMotion = new ItemMotion(positionStart, itemCount, MOTION_REMOVE, null);
        mItemMotions.addLast(itemMotion);
        return itemMotion.getId();
    }

    public int scheduleItemMotion(ItemMotion motion) {
        if (motion == null) {
            return -1;
        }
        mItemMotions.addLast(motion);
        return motion.getId();
    }

    public void cancelItemMotion(int motionId) {
        for (ItemMotion motion : mItemMotions) {
            if (motion.getId() == motionId) {
                mItemMotions.remove(motion);
                break;
            }
        }
    }

    /**
     * Created by CJQ on 2018/2/26.
     */

    public static class ItemMotion {

        private static int autoincrementId = 0;

        @IntDef({MOTION_RESET, MOTION_CHANGE, MOTION_INSERT, MOTION_REMOVE})
        @Retention(RetentionPolicy.SOURCE)
        @interface Motion {
        }

        public static final int MOTION_RESET = 0;
        public static final int MOTION_CHANGE = 1;
        public static final int MOTION_INSERT = 2;
        public static final int MOTION_REMOVE = 3;

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
