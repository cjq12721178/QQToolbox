package com.cjq.tool.qqtoolbox.activity;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.cjq.tool.qbox.ui.dialog.EditDialog;
import com.cjq.tool.qbox.ui.dialog.ListDialog;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qbox.ui.loader.SimpleCursorLoader;
import com.cjq.tool.qqtoolbox.database.MySQLiteOpenHelper;

public class TestSimpleCursorLoaderActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        MyCursorAdapter.OnContentClickListener,
        View.OnClickListener,
        EditDialog.OnContentReceiver,
        ListDialog.OnItemSelectedListener {

    private MyCursorAdapter mAdapter;
    private MySQLiteOpenHelper mOpenHelper;
    private MySimpleCursorLoader mCursorLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_cursor_loader);

        findViewById(R.id.btn_add).setOnClickListener(this);

        mOpenHelper = new MySQLiteOpenHelper(this);
        mAdapter = new MyCursorAdapter(this, null, true);
        mAdapter.setOnContentClickListener(this);

        ListView lvStudents = findViewById(R.id.lv_students);
        lvStudents.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onDestroy() {
        mCursorLoader = null;
        mOpenHelper.close();
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return mCursorLoader = new MySimpleCursorLoader(this, mOpenHelper.getReadableDatabase());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.changeCursor(null);
    }

    @Override
    public void onNameClick(int position) {
        EditDialog dialog = new EditDialog();
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        dialog.setTitle("修改姓名");
        dialog.setContent(cursor.getString(cursor.getColumnIndex("name")));
        dialog.getArguments().putInt("position", position);
        dialog.show(getSupportFragmentManager(), "modify_name");
    }

    @Override
    public void onSexClick(int position) {
        ListDialog dialog = new ListDialog();
        //Cursor cursor = mAdapter.getCursor();
        dialog.setTitle("修改性别");
        dialog.setItems(new String[] { "男", "女" });
        dialog.getArguments().putInt("position", position);
        //dialog.setContent(cursor.getString(cursor.getColumnIndex("sex")));
        dialog.show(getSupportFragmentManager(), "modify_sex");
    }

    @Override
    public void onAgeClick(int position) {
        EditDialog dialog = new EditDialog();
        Cursor cursor = mAdapter.getCursor();
        cursor.moveToPosition(position);
        dialog.setTitle("修改年龄");
        dialog.setContent(cursor.getString(cursor.getColumnIndex("age")));
        dialog.getArguments().putInt("position", position);
        dialog.show(getSupportFragmentManager(), "modify_age");
    }

    @Override
    public void onRemoveClick(int position) {
        try {
            mOpenHelper.getWritableDatabase().delete("student", "_id = ?", new String[] { String.valueOf(mAdapter.getItemId(position)) });
            notifyLoader();
        } catch (Exception e) {
            ExceptionLog.display(e);
        }
    }

    private void notifyLoader() {
        if (mCursorLoader != null) {
            mCursorLoader.onContentChanged();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add: {
                EditDialog dialog = new EditDialog();
                dialog.setTitle("增加学生");
                dialog.setSummary("参照格式：id + 空格 + name + 空格 + sex + 空格 + age");
                //dialog.setContent(cursor.getString(cursor.getColumnIndex("name")));
                dialog.show(getSupportFragmentManager(), "add_student");
            } break;
        }
    }

    @Override
    public boolean onReceive(EditDialog dialog, String oldValue, String newValue) {
        switch (String.valueOf(dialog.getTag())) {
            case "modify_name": {
                try {
                    ContentValues values = new ContentValues();
                    values.put("name", newValue);
                    mOpenHelper.getWritableDatabase().update("student", values, "_id = ?", new String[] { String.valueOf(mAdapter.getItemId(dialog.getArguments().getInt("position"))) });
                    notifyLoader();
                } catch (Exception e) {
                    ExceptionLog.display(e);
                }
            } break;
            case "modify_age": {
                try {
                    ContentValues values = new ContentValues();
                    values.put("age", Integer.parseInt(newValue));
                    mOpenHelper.getWritableDatabase().update("student", values, "_id = ?", new String[] { String.valueOf(mAdapter.getItemId(dialog.getArguments().getInt("position"))) });
                    notifyLoader();
                } catch (Exception e) {
                    ExceptionLog.display(e);
                }
            } break;
            case "add_student": {
                try {
                    String[] paras = newValue.split(" ");
                    ContentValues values = new ContentValues();
                    values.put("_id", Integer.parseInt(paras[0]));
                    values.put("name", paras[1]);
                    values.put("sex", paras[2].equals("男") ? 0 : 1);
                    values.put("age", Integer.parseInt(paras[3]));
                    mOpenHelper.getWritableDatabase().insert("student", null, values);
                    notifyLoader();
                } catch (Exception e) {
                    ExceptionLog.display(e);
                }
            } break;
        }
        return true;
    }

    @Override
    public void onItemSelected(@NonNull ListDialog dialog, int position, @NonNull Object[] items) {
        try {
            ContentValues values = new ContentValues();
            values.put("sex", position);
            mOpenHelper.getWritableDatabase().update("student", values, "_id = ?", new String[] { String.valueOf(mAdapter.getItemId(dialog.getArguments().getInt("position"))) });
            notifyLoader();
        } catch (Exception e) {
            ExceptionLog.display(e);
        }
    }
}

class Student {

    private final int mId;
    private String mName;
    private boolean mSex;
    private int mAge;

    Student(int id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isSex() {
        return mSex;
    }

    public void setSex(boolean sex) {
        mSex = sex;
    }

    public int getAge() {
        return mAge;
    }

    public void setAge(int age) {
        mAge = age;
    }
}

class MySimpleCursorLoader extends SimpleCursorLoader {

    private final SQLiteDatabase mDatabase;

    public MySimpleCursorLoader(@NonNull Context context, SQLiteDatabase database) {
        super(context);
        mDatabase = database;
    }

    @Override
    public Cursor loadInBackground() {
        return mDatabase.rawQuery("SELECT * FROM student", null);
    }
}

class MyCursorAdapter extends CursorAdapter {

    private OnContentClickListener mOnContentClickListener;

    public void setOnContentClickListener(OnContentClickListener listener) {
        mOnContentClickListener = listener;
    }

    public class ViewHolder implements View.OnClickListener {
        private TextView mTvId;
        private TextView mTvName;
        private TextView mTvSex;
        private TextView mTvAge;
        private Button mBtnRemove;
        private int mPosition;

        @Override
        public void onClick(View v) {
            if (mOnContentClickListener != null) {
                switch (v.getId()) {
                    case R.id.tv_name:
                        mOnContentClickListener.onNameClick(mPosition);
                        break;
                    case R.id.tv_sex:
                        mOnContentClickListener.onSexClick(mPosition);
                        break;
                    case R.id.tv_age:
                        mOnContentClickListener.onAgeClick(mPosition);
                        break;
                    case R.id.btn_remove:
                        mOnContentClickListener.onRemoveClick(mPosition);
                        break;
                }
            }
        }
    }

    public interface OnContentClickListener {
        void onNameClick(int position);
        void onSexClick(int position);
        void onAgeClick(int position);
        void onRemoveClick(int position);
    }

    public MyCursorAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item_cursor_loader, parent, false);
        ViewHolder holder = new ViewHolder();
        holder.mTvId = view.findViewById(R.id.tv_id);
        holder.mTvName = view.findViewById(R.id.tv_name);
        holder.mTvName.setOnClickListener(holder);
        holder.mTvSex = view.findViewById(R.id.tv_sex);
        holder.mTvSex.setOnClickListener(holder);
        holder.mTvAge = view.findViewById(R.id.tv_age);
        holder.mTvAge.setOnClickListener(holder);
        holder.mBtnRemove = view.findViewById(R.id.btn_remove);
        holder.mBtnRemove.setOnClickListener(holder);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.mTvId.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))));
        holder.mTvName.setText(cursor.getString(cursor.getColumnIndex("name")));
        holder.mTvSex.setText(cursor.getInt(cursor.getColumnIndex("sex")) == 0 ? "男" : "女");
        holder.mTvAge.setText(cursor.getInt(cursor.getColumnIndex("age")) + "岁");
        holder.mPosition = cursor.getPosition();
    }
}