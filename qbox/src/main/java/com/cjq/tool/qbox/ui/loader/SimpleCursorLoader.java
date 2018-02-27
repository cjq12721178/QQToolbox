package com.cjq.tool.qbox.ui.loader;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by CJQ on 2018/2/25.
 */

public abstract class SimpleCursorLoader extends AsyncTaskLoader<Cursor> {

    private Cursor mCursor;

    public SimpleCursorLoader(@NonNull Context context) {
        super(context);
    }

    @Nullable
    @Override
    protected Cursor onLoadInBackground() {
        Cursor cursor = null;
        try {
            cursor = loadInBackground();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // Calling getCount() causes the cursor window to be filled,
        // which will make the first access on the main thread a lot faster.
        if (cursor != null) {
            cursor.getCount();
        }
        return cursor;
    }

    /* Runs on a worker thread */
    @Override
    public abstract Cursor loadInBackground();

    /* Runs on the UI thread */
    @Override
    public void deliverResult(Cursor cursor) {
        if (isReset()) {
            // An async query came in while the loader is stopped
            if (cursor != null) {
                cursor.close();
            }
            return;
        }
        Cursor oldCursor = mCursor;
        mCursor = cursor;

        if (isStarted()) {
            super.deliverResult(cursor);
        }

        if (oldCursor != null && oldCursor != cursor && !oldCursor.isClosed()) {
            oldCursor.close();
        }
    }

    /**
     * Starts an asynchronous load of the contacts list data. When the result is ready the callbacks
     * will be called on the UI thread. If a previous load has been completed and is still valid
     * the result may be passed to the callbacks immediately.
     * <p/>
     * Must be called from the UI thread
     */
    @Override
    protected void onStartLoading() {
        if (mCursor != null) {
            deliverResult(mCursor);
        }
        if (takeContentChanged() || mCursor == null) {
            forceLoad();
        }
    }

    /**
     * Must be called from the UI thread
     */
    @Override
    protected void onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad();
    }

    @Override
    public void onCanceled(Cursor cursor) {
        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        // Ensure the loader is stopped
        onStopLoading();

//        if (mCursor != null && !mCursor.isClosed()) {
//            mCursor.close();
//        }
        onCanceled(mCursor);
        mCursor = null;
    }
}
