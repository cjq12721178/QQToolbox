package com.cjq.tool.qbox.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by CJQ on 2018/2/28.
 */

public class SimpleSQLiteAsyncEventHandler extends Handler {

    private static final String TAG = "SimpleSQLiteAsyncQuery";
    private static final boolean localLOGV = false;

    private static final int EVENT_ARG_QUERY = 1;
    private static final int EVENT_ARG_INSERT = 2;
    private static final int EVENT_ARG_UPDATE = 3;
    private static final int EVENT_ARG_DELETE = 4;
    private static final int EVENT_ARG_RAW_QUERY = 5;
    private static final int EVENT_ARG_REPLACE = 6;
    private static final int EVENT_ARG_RAW_SQL = 7;

    private static Looper sLooper = null;

    private Handler mWorkerThreadHandler;
    private SQLiteResolver mResolver;
    private OnMissionCompleteListener mListener;

    private static class WorkerArgs {
        public Handler handler;
        public Object cookie;
        public Object result;
    }

    private static class QueryArgs extends WorkerArgs {
        public String table;
        public boolean distinct;
        public String[] projection;
        public String selection;
        public String[] selectionArgs;
        public String groupBy;
        public String having;
        public String orderBy;
        public String limit;
    }

    private static class InsertArgs extends ReplaceArgs {
        public int conflictAlgorithm;
    }

    private static class UpdateArgs extends InsertArgs {
        public String selection;
        public String[] selectionArgs;
    }

    private static class DeleteArgs extends WorkerArgs {
        public String table;
        public String selection;
        public String[] selectionArgs;
    }

    private static class RawQueryArgs extends WorkerArgs {
        public String sql;
        public String[] selectionArgs;
    }

    private static class ReplaceArgs extends WorkerArgs {
        public String table;
        public ContentValues values;
    }

    private static class RawSqlArgs extends WorkerArgs {
        public String sql;
        public Object[] bindArgs;
    }

    protected class WorkerHandler extends Handler {
        public WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            final SQLiteResolver resolver = mResolver;
            if (resolver == null) return;

            WorkerArgs args = (WorkerArgs) msg.obj;

            int token = msg.what;
            int event = msg.arg1;

            switch (event) {
                case EVENT_ARG_QUERY: {
                    QueryArgs queryArgs = (QueryArgs) args;
                    Cursor cursor = null;
                    try {
                        cursor = resolver.query(queryArgs.distinct,
                                queryArgs.table, queryArgs.projection,
                                queryArgs.selection, queryArgs.selectionArgs,
                                queryArgs.groupBy, queryArgs.having,
                                queryArgs.orderBy, queryArgs.limit);
                        // Calling getCount() causes the cursor window to be filled,
                        // which will make the first access on the main thread a lot faster.
                        if (cursor != null) {
                            cursor.getCount();
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Exception thrown during handling EVENT_ARG_QUERY", e);
                        if (cursor != null) {
                            cursor.close();
                            cursor = null;
                        }
                    }
                    queryArgs.result = cursor;
                } break;
                case EVENT_ARG_INSERT:
                    InsertArgs insertArgs = (InsertArgs) args;
                    try {
                        insertArgs.result = resolver.insert(insertArgs.table,
                                insertArgs.values, insertArgs.conflictAlgorithm);
                    } catch (Exception e) {
                        Log.w(TAG, "Exception thrown during handling EVENT_ARG_INSERT", e);
                        insertArgs.result = -1;
                    }
                    break;
                case EVENT_ARG_UPDATE:
                    UpdateArgs updateArgs = (UpdateArgs) args;
                    try {
                        updateArgs.result = resolver.update(updateArgs.table,
                                updateArgs.values, updateArgs.selection,
                                updateArgs.selectionArgs, updateArgs.conflictAlgorithm);
                    } catch (Exception e) {
                        Log.w(TAG, "Exception thrown during handling EVENT_ARG_UPDATE", e);
                    }
                    break;
                case EVENT_ARG_DELETE:
                    DeleteArgs deleteArgs = (DeleteArgs) args;
                    try {
                        deleteArgs.result = resolver.delete(deleteArgs.table,
                                deleteArgs.selection, deleteArgs.selectionArgs);
                    } catch (Exception e) {
                        Log.w(TAG, "Exception thrown during handling EVENT_ARG_DELETE", e);
                    }
                    break;
                case EVENT_ARG_RAW_QUERY:
                    RawQueryArgs rawQueryArgs = (RawQueryArgs) args;
                    Cursor cursor = null;
                    try {
                        cursor = resolver.rawQuery(rawQueryArgs.sql,
                                rawQueryArgs.selectionArgs);
                        if (cursor != null) {
                            cursor.getCount();
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Exception thrown during handling EVENT_ARG_RAW_QUERY", e);
                        if (cursor != null) {
                            cursor.close();
                            cursor = null;
                        }
                    }
                    rawQueryArgs.result = cursor;
                    break;
                case EVENT_ARG_REPLACE:
                    ReplaceArgs replaceArgs = (ReplaceArgs) args;
                    try {
                        replaceArgs.result = resolver.replace(replaceArgs.table,
                                replaceArgs.values);
                    } catch (Exception e) {
                        Log.w(TAG, "Exception thrown during handling EVENT_ARG_REPLACE", e);
                        replaceArgs.result = -1;
                    }
                    break;
                case EVENT_ARG_RAW_SQL:
                    RawSqlArgs rawSqlArgs = (RawSqlArgs) args;
                    try {
                        resolver.execSQL(rawSqlArgs.sql, rawSqlArgs.bindArgs);
                        rawSqlArgs.result = true;
                    } catch (Exception e) {
                        Log.w(TAG, "Exception thrown during handling EVENT_ARG_EXEC_SQL", e);
                        rawSqlArgs.result = false;
                    }
                    break;
            }

            // passing the original token value back to the caller
            // on top of the event values in arg1.
            Message reply = args.handler.obtainMessage(token);
            reply.obj = args;
            reply.arg1 = msg.arg1;

            if (localLOGV) {
                Log.d(TAG, "WorkerHandler.handleMsg: msg.arg1=" + msg.arg1
                        + ", reply.what=" + reply.what);
            }

            reply.sendToTarget();
        }
    }

    public SimpleSQLiteAsyncEventHandler(SQLiteResolver sqLiteResolver,
                                         OnMissionCompleteListener listener) {
        super();
        mResolver = sqLiteResolver;
        setOnMissionCompleteListener(listener);
        synchronized (SimpleSQLiteAsyncEventHandler.class) {
            if (sLooper == null) {
                HandlerThread thread = new HandlerThread("SimpleSQLiteAsyncQueryWorker");
                thread.start();

                sLooper = thread.getLooper();
            }
        }
        mWorkerThreadHandler = createHandler(sLooper);
    }

    public void setOnMissionCompleteListener(OnMissionCompleteListener listener) {
        mListener = listener;
    }

    protected Handler createHandler(Looper looper) {
        return new WorkerHandler(looper);
    }

    public void startQuery(int token, Object cookie, boolean distinct,
                           @NonNull String table,
                           @Nullable String[] projection,
                           @Nullable String selection,
                           @Nullable String[] selectionArgs,
                           @Nullable String groupBy,
                           @Nullable String having,
                           @Nullable String orderBy,
                           @Nullable String limit) {
        // Use the token as what so cancelOperations works properly
        Message msg = mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_QUERY;

        QueryArgs args = new QueryArgs();
        args.handler = this;
        args.cookie = cookie;
        args.table = table;
        args.distinct = distinct;
        args.projection = projection;
        args.selection = selection;
        args.selectionArgs = selectionArgs;
        args.groupBy = groupBy;
        args.having = having;
        args.orderBy = orderBy;
        args.limit = limit;
        msg.obj = args;

        mWorkerThreadHandler.sendMessage(msg);
    }

    /**
     * Attempts to cancel operation that has not already started. Note that
     * there is no guarantee that the operation will be canceled. They still may
     * result in a call to on[Query/Insert/Update/Delete]Complete after this
     * call has completed.
     *
     * @param token The token representing the operation to be canceled.
     *  If multiple operations have the same token they will all be canceled.
     */
    public final void cancelOperation(int token) {
        mWorkerThreadHandler.removeMessages(token);
    }

    public final void startInsert(int token, Object cookie,
                                  @NonNull String table,
                                  @Nullable ContentValues values,
                                  int conflictAlgorithm) {
        // Use the token as what so cancelOperations works properly
        Message msg = mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_INSERT;

        InsertArgs args = new InsertArgs();
        args.handler = this;
        args.cookie = cookie;
        args.table = table;
        args.values = values;
        args.conflictAlgorithm = conflictAlgorithm;
        msg.obj = args;

        mWorkerThreadHandler.sendMessage(msg);
    }

    public final void startUpdate(int token, Object cookie,
                                  @NonNull String table,
                                  @Nullable ContentValues values,
                                  @Nullable String selection,
                                  @Nullable String[] selectionArgs,
                                  int conflictAlgorithm) {
        // Use the token as what so cancelOperations works properly
        Message msg = mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_UPDATE;

        UpdateArgs args = new UpdateArgs();
        args.handler = this;
        args.cookie = cookie;
        args.table = table;
        args.values = values;
        args.selection = selection;
        args.selectionArgs = selectionArgs;
        args.conflictAlgorithm = conflictAlgorithm;
        msg.obj = args;

        mWorkerThreadHandler.sendMessage(msg);
    }

    public final void startDelete(int token, Object cookie,
                                  @NonNull String table,
                                  @Nullable String selection,
                                  @Nullable String[] selectionArgs) {
        // Use the token as what so cancelOperations works properly
        Message msg = mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_DELETE;

        DeleteArgs args = new DeleteArgs();
        args.handler = this;
        args.cookie = cookie;
        args.table = table;
        args.selection = selection;
        args.selectionArgs = selectionArgs;
        msg.obj = args;

        mWorkerThreadHandler.sendMessage(msg);
    }

    public final void startRawQuery(int token, Object cookie,
                                    @NonNull String sql,
                                    @Nullable String[] selectionArgs) {
        // Use the token as what so cancelOperations works properly
        Message msg = mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_RAW_QUERY;

        RawQueryArgs args = new RawQueryArgs();
        args.handler = this;
        args.cookie = cookie;
        args.sql = sql;
        args.selectionArgs = selectionArgs;
        msg.obj = args;

        mWorkerThreadHandler.sendMessage(msg);
    }

    public final void startReplace(int token, Object cookie,
                                   @Nullable String table,
                                   @Nullable ContentValues values) {
        // Use the token as what so cancelOperations works properly
        Message msg = mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_RAW_QUERY;

        ReplaceArgs args = new ReplaceArgs();
        args.handler = this;
        args.cookie = cookie;
        args.table = table;
        args.values = values;
        msg.obj = args;

        mWorkerThreadHandler.sendMessage(msg);
    }

    public final void startExecSQL(int token, Object cookie,
                                   @NonNull String sql,
                                   @Nullable Object[] bindArgs) {
        // Use the token as what so cancelOperations works properly
        Message msg = mWorkerThreadHandler.obtainMessage(token);
        msg.arg1 = EVENT_ARG_RAW_QUERY;

        RawSqlArgs args = new RawSqlArgs();
        args.handler = this;
        args.cookie = cookie;
        args.sql = sql;
        args.bindArgs = bindArgs;
        msg.obj = args;

        mWorkerThreadHandler.sendMessage(msg);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mListener == null) {
            return;
        }

        WorkerArgs args = (WorkerArgs) msg.obj;

        if (localLOGV) {
            Log.d(TAG, "AsyncQueryHandler.handleMessage: msg.what=" + msg.what
                    + ", msg.arg1=" + msg.arg1);
        }

        int token = msg.what;
        int event = msg.arg1;

        // pass token back to caller on each callback.
        switch (event) {
            case EVENT_ARG_QUERY:
            case EVENT_ARG_RAW_QUERY:
                mListener.onQueryComplete(token, args.cookie, (Cursor) args.result);
                break;
            case EVENT_ARG_INSERT:
                mListener.onInsertComplete(token, args.cookie, (long) args.result);
                break;
            case EVENT_ARG_UPDATE:
                mListener.onUpdateComplete(token, args.cookie, (int) args.result);
                break;
            case EVENT_ARG_DELETE:
                mListener.onDeleteComplete(token, args.cookie, (int) args.result);
                break;
            case EVENT_ARG_REPLACE:
                mListener.onReplaceComplete(token, args.cookie, (long) args.result);
                break;
            case EVENT_ARG_RAW_SQL:
                mListener.onExecSqlComplete(token, args.cookie, (boolean) args.result);
                break;
        }
    }

    public interface OnMissionCompleteListener {
        void onQueryComplete(int token, Object cookie, Cursor cursor);
        void onInsertComplete(int token, Object cookie, long rowId);
        void onUpdateComplete(int token, Object cookie, int affectedRowCount);
        void onDeleteComplete(int token, Object cookie, int affectedRowCount);
        void onReplaceComplete(int token, Object cookie, long rowId);
        void onExecSqlComplete(int token, Object cookie, boolean result);
    }
}
