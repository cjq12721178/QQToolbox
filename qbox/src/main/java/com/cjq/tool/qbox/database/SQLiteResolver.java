package com.cjq.tool.qbox.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by CJQ on 2018/2/28.
 */

public interface SQLiteResolver {

    @Nullable Cursor query(boolean distinct,
                           @NonNull String table,
                           @Nullable String[] projection,
                           @Nullable String selection,
                           @Nullable String[] selectionArgs,
                           @Nullable String groupBy,
                           @Nullable String having,
                           @Nullable String orderBy,
                           @Nullable String limit);
    long insert(@NonNull String table, @Nullable ContentValues values,
                int conflictAlgorithm);
    int update(@NonNull String table, @Nullable ContentValues values,
               @Nullable String selection, @Nullable String[] selectionArgs,
               int conflictAlgorithm);
    int delete(@NonNull String table, @Nullable String selection,
               @Nullable String[] selectionArgs);
    Cursor rawQuery(@NonNull String sql, @Nullable String[] selectionArgs);
    long replace(@Nullable String table, @Nullable ContentValues values);
    boolean execSQL(@NonNull String sql, @Nullable Object[] bindArgs);
}
