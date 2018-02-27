package com.cjq.tool.qqtoolbox.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by CJQ on 2018/2/27.
 */
public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    public MySQLiteOpenHelper(Context context) {
        super(context, "students.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE student (_id INT PRIMARY KEY, name VARCHAR(255) NOT NULL, sex BIT NOT NULL, age TINYINT)");
        db.execSQL("INSERT INTO student VALUES(1, '张三', 0, 11)");
        db.execSQL("INSERT INTO student VALUES(2, '李四', 1, 32)");
        db.execSQL("INSERT INTO student VALUES(3, '王五', 0, 25)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
