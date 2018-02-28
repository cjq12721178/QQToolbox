package com.cjq.tool.qbox.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by CJQ on 2018/2/28.
 */

public class SQLiteResolverDelegate implements SQLiteResolver {

    private SQLiteDatabase mDatabase;

    public SQLiteResolverDelegate(SQLiteDatabase database) {
        setDatabase(database);
    }

    public void setDatabase(SQLiteDatabase database) {
        mDatabase = database;
    }

    @Nullable
    @Override
    public Cursor query(boolean distinct, @NonNull String table,
                        @Nullable String[] projection,
                        @Nullable String selection,
                        @Nullable String[] selectionArgs,
                        @Nullable String groupBy,
                        @Nullable String having,
                        @Nullable String orderBy,
                        @Nullable String limit) {
        return mDatabase != null
                ? mDatabase.query(distinct, table, projection,
                selection, selectionArgs, groupBy, having, orderBy, limit)
                : null;
    }

    @Override
    public long insert(@NonNull String table,
                       @Nullable ContentValues values,
                       int conflictAlgorithm) {
        return mDatabase != null
                ? mDatabase.insertWithOnConflict(table,
                null, values, conflictAlgorithm)
                : -1;
    }

    @Override
    public int update(@NonNull String table,
                      @Nullable ContentValues values,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs,
                      int conflictAlgorithm) {
        return mDatabase != null
                ? mDatabase.updateWithOnConflict(table, values,
                selection, selectionArgs, conflictAlgorithm)
                : 0;
    }

    @Override
    public int delete(@NonNull String table,
                      @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return mDatabase != null
                ? mDatabase.delete(table, selection, selectionArgs)
                : null;
    }

    @Override
    public Cursor rawQuery(@NonNull String sql,
                           @Nullable String[] selectionArgs) {
        return mDatabase != null
                ? mDatabase.rawQuery(sql, selectionArgs)
                : null;
    }

    @Override
    public long replace(@Nullable String table,
                        @Nullable ContentValues values) {
        return mDatabase != null
                ? mDatabase.replace(table, null, values)
                : 0;
    }

    @Override
    public boolean execSQL(@NonNull String sql, @Nullable Object[] bindArgs) {
        if (mDatabase != null) {
            mDatabase.execSQL(sql, bindArgs);
            return true;
        }
        return false;
    }
}
