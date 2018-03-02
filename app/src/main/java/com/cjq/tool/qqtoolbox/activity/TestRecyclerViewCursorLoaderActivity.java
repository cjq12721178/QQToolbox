package com.cjq.tool.qqtoolbox.activity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter;
import com.cjq.tool.qbox.ui.adapter.RecyclerViewCursorAdapter;
import com.cjq.tool.qbox.database.SQLiteResolverDelegate;
import com.cjq.tool.qbox.database.SimpleSQLiteAsyncEventHandler;
import com.cjq.tool.qbox.ui.dialog.EditDialog;
import com.cjq.tool.qbox.ui.dialog.ListDialog;
import com.cjq.tool.qbox.ui.gesture.SimpleRecyclerViewItemTouchListener;
import com.cjq.tool.qbox.util.ExceptionLog;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.database.MySQLiteOpenHelper;

import java.util.List;

public class TestRecyclerViewCursorLoaderActivity
        extends AppCompatActivity
        implements View.OnClickListener,
        /* MyRecyclerViewCursorAdapter.OnContentClickListener, */
        LoaderManager.LoaderCallbacks<Cursor>,
        EditDialog.OnContentReceiver,
        ListDialog.OnItemSelectedListener, SimpleSQLiteAsyncEventHandler.OnMissionCompleteListener {

    private static final int TOKEN_MODIFY_NAME = 1;
    private static final int TOKEN_MODIFY_SEX = 2;
    private static final int TOKEN_MODIFY_AGE = 3;
    private static final int TOKEN_ADD_STUDENT = 4;
    private static final int TOKEN_REMOVE_STUDENT = 5;

    private MyRecyclerViewCursorAdapter mAdapter;
    private MySQLiteOpenHelper mOpenHelper;
    private MySimpleCursorLoader mCursorLoader;
    private SimpleSQLiteAsyncEventHandler mAsyncEventHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_recycler_view_cursor_loader);

        findViewById(R.id.btn_add).setOnClickListener(this);

        mOpenHelper = new MySQLiteOpenHelper(this);
        mAsyncEventHandler = new SimpleSQLiteAsyncEventHandler(new SQLiteResolverDelegate(mOpenHelper.getWritableDatabase()), this);
        mAdapter = new MyRecyclerViewCursorAdapter();
        //mAdapter.setOnContentClickListener(this);

        RecyclerView rvStudents = findViewById(R.id.rv_students);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvStudents.setLayoutManager(linearLayoutManager);
        rvStudents.addOnItemTouchListener(new SimpleRecyclerViewItemTouchListener(rvStudents) {
            @Override
            public void onItemClick(View v, int position) {
                switch (v.getId()) {
                    case R.id.tv_name: {
                        EditDialog dialog = new EditDialog();
                        Cursor cursor = mAdapter.getCursor();
                        cursor.moveToPosition(position);
                        dialog.setTitle("修改姓名");
                        dialog.setContent(cursor.getString(cursor.getColumnIndex("name")));
                        dialog.getArguments().putInt("position", position);
                        dialog.show(getSupportFragmentManager(), "modify_name");
                    } break;
                    case R.id.tv_sex: {
                        ListDialog dialog = new ListDialog();
                        //Cursor cursor = mAdapter.getCursor();
                        dialog.setTitle("修改性别");
                        dialog.setItems(new String[] { "男", "女" });
                        dialog.getArguments().putInt("position", position);
                        //dialog.setContent(cursor.getString(cursor.getColumnIndex("sex")));
                        dialog.show(getSupportFragmentManager(), "modify_sex");
                    } break;
                    case R.id.tv_age: {
                        EditDialog dialog = new EditDialog();
                        Cursor cursor = mAdapter.getCursor();
                        cursor.moveToPosition(position);
                        dialog.setTitle("修改年龄");
                        dialog.setContent(cursor.getString(cursor.getColumnIndex("age")));
                        dialog.getArguments().putInt("position", position);
                        dialog.show(getSupportFragmentManager(), "modify_age");
                    } break;
                    case R.id.btn_remove: {
                        try {
                            //同步
                            //mOpenHelper.getWritableDatabase().delete("student", "_id = ?", new String[] { String.valueOf(mAdapter.getItemId(position)) });
                            //mAdapter.scheduleItemRemove(position);
                            //mCursorLoader.onContentChanged();

                            //异步
                            mAsyncEventHandler.startDelete(TOKEN_REMOVE_STUDENT,
                                    new RecyclerViewCursorAdapter.ItemMotion(position, 1, RecyclerViewCursorAdapter.ItemMotion.MOTION_REMOVE),
                                    "student",
                                    "_id = ?",
                                    new String[] { String.valueOf(mAdapter.getItemId(position)) });
                        } catch (Exception e) {
                            ExceptionLog.display(e);
                        }
                    } break;
                }
            }
        }.addItemChildViewTouchEnabled(R.id.tv_name)
                .addItemChildViewTouchEnabled(R.id.tv_sex)
                .addItemChildViewTouchEnabled(R.id.tv_age)
                .addItemChildViewTouchEnabled(R.id.btn_remove));
        rvStudents.setAdapter(mAdapter);

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    protected void onDestroy() {
        mCursorLoader = null;
        mOpenHelper.close();
        super.onDestroy();
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

//    @Override
//    public void onNameClick(int position) {
//        EditDialog dialog = new EditDialog();
//        Cursor cursor = mAdapter.getCursor();
//        cursor.moveToPosition(position);
//        dialog.setTitle("修改姓名");
//        dialog.setContent(cursor.getString(cursor.getColumnIndex("name")));
//        dialog.getArguments().putInt("position", position);
//        dialog.show(getSupportFragmentManager(), "modify_name");
//    }

//    @Override
//    public void onSexClick(int position) {
//        ListDialog dialog = new ListDialog();
//        //Cursor cursor = mAdapter.getCursor();
//        dialog.setTitle("修改性别");
//        dialog.setItems(new String[] { "男", "女" });
//        dialog.getArguments().putInt("position", position);
//        //dialog.setContent(cursor.getString(cursor.getColumnIndex("sex")));
//        dialog.show(getSupportFragmentManager(), "modify_sex");
//    }

//    @Override
//    public void onAgeClick(int position) {
//        EditDialog dialog = new EditDialog();
//        Cursor cursor = mAdapter.getCursor();
//        cursor.moveToPosition(position);
//        dialog.setTitle("修改年龄");
//        dialog.setContent(cursor.getString(cursor.getColumnIndex("age")));
//        dialog.getArguments().putInt("position", position);
//        dialog.show(getSupportFragmentManager(), "modify_age");
//    }

//    @Override
//    public void onRemoveClick(int position) {
//        try {
//            //同步
//            //mOpenHelper.getWritableDatabase().delete("student", "_id = ?", new String[] { String.valueOf(mAdapter.getItemId(position)) });
//            //mAdapter.scheduleItemRemove(position);
//            //mCursorLoader.onContentChanged();
//
//            //异步
//            mAsyncEventHandler.startDelete(TOKEN_REMOVE_STUDENT,
//                    new RecyclerViewCursorAdapter.ItemMotion(position, 1, RecyclerViewCursorAdapter.ItemMotion.MOTION_REMOVE),
//                    "student",
//                    "_id = ?",
//                    new String[] { String.valueOf(mAdapter.getItemId(position)) });
//        } catch (Exception e) {
//            ExceptionLog.display(e);
//        }
//    }

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
    public boolean onReceive(EditDialog dialog, String oldValue, String newValue) {
        switch (String.valueOf(dialog.getTag())) {
            case "modify_name": {
                try {
                    int position = dialog.getArguments().getInt("position");
                    ContentValues values = new ContentValues();
                    values.put("name", newValue);

                    //同步
                    //mOpenHelper.getWritableDatabase().update("student", values, "_id = ?", new String[] { String.valueOf(mAdapter.getItemId(position)) });
                    //mAdapter.scheduleItemChange(position, MyRecyclerViewCursorAdapter.MODIFY_NAME);
                    //mCursorLoader.onContentChanged();

                    //异步
                    mAsyncEventHandler.startUpdate(TOKEN_MODIFY_NAME,
                            new RecyclerViewCursorAdapter.ItemMotion(position, 1, RecyclerViewCursorAdapter.ItemMotion.MOTION_CHANGE),
                            "student", values,
                            "_id = ?",
                            new String[] { String.valueOf(mAdapter.getItemId(position)) },
                            SQLiteDatabase.CONFLICT_NONE);
                } catch (Exception e) {
                    ExceptionLog.display(e);
                }
            } break;
            case "modify_age": {
                try {
                    int position = dialog.getArguments().getInt("position");
                    ContentValues values = new ContentValues();
                    values.put("age", Integer.parseInt(newValue));

                    //同步
                    //mOpenHelper.getWritableDatabase().update("student", values, "_id = ?", new String[] { String.valueOf(mAdapter.getItemId(position)) });
                    //mAdapter.scheduleItemChange(position, MyRecyclerViewCursorAdapter.MODIFY_AGE);
                    //mCursorLoader.onContentChanged();

                    //异步
                    mAsyncEventHandler.startUpdate(TOKEN_MODIFY_AGE,
                            new RecyclerViewCursorAdapter.ItemMotion(position, 1, RecyclerViewCursorAdapter.ItemMotion.MOTION_CHANGE),
                            "student", values,
                            "_id = ?",
                            new String[] { String.valueOf(mAdapter.getItemId(position)) },
                            SQLiteDatabase.CONFLICT_NONE);
                } catch (Exception e) {
                    ExceptionLog.display(e);
                }
            } break;
            case "add_student": {
                try {
                    String[] paras = newValue.split(" ");
                    ContentValues values = new ContentValues();
                    int id = Integer.parseInt(paras[0]);
                    int position = getInsertingItemPosition(id);
                    values.put("_id", id);
                    values.put("name", paras[1]);
                    values.put("sex", paras[2].equals("男") ? 0 : 1);
                    values.put("age", Integer.parseInt(paras[3]));

                    //同步
                    //mOpenHelper.getWritableDatabase().insert("student", null, values);
                    //mAdapter.scheduleItemInsert(position);
                    //mCursorLoader.onContentChanged();

                    //异步
                    mAsyncEventHandler.startInsert(TOKEN_ADD_STUDENT,
                            new RecyclerViewCursorAdapter.ItemMotion(position, 1, RecyclerViewCursorAdapter.ItemMotion.MOTION_INSERT),
                            "student", values,
                            SQLiteDatabase.CONFLICT_NONE);
                } catch (Exception e) {
                    ExceptionLog.display(e);
                }
            } break;
        }
        return true;
    }

    private int getInsertingItemPosition(int insertItemId) {
        Cursor cursor = mAdapter.getCursor();
        if (cursor == null) {
            throw new NullPointerException();
        }
        //针对排序后
//        cursor.moveToFirst();
//        int idIndex = cursor.getColumnIndex("_id");
//        int position = 0;
//        do {
//            if (insertItemId > cursor.getInt(idIndex)) {
//                return position;
//            } else {
//                ++position;
//            }
//        } while (cursor.moveToNext());
//        return position;
        //针对非排序
        return cursor.getCount();
    }

    @Override
    public void onItemSelected(ListDialog dialog, String item) {
        try {
            int position = dialog.getArguments().getInt("position");
            ContentValues values = new ContentValues();
            values.put("sex", item.equals("男") ? 0 : 1);

            //同步
            //mOpenHelper.getWritableDatabase().update("student", values, "_id = ?", new String[] { String.valueOf(mAdapter.getItemId(dialog.getArguments().getInt("position"))) });
            //mAdapter.scheduleItemChange(position, MyRecyclerViewCursorAdapter.MODIFY_SEX);
            //mCursorLoader.onContentChanged();

            //异步
            mAsyncEventHandler.startUpdate(TOKEN_MODIFY_SEX,
                    new RecyclerViewCursorAdapter.ItemMotion(position, 1, RecyclerViewCursorAdapter.ItemMotion.MOTION_CHANGE),
                    "student", values,
                    "_id = ?",
                    new String[] { String.valueOf(mAdapter.getItemId(position)) },
                    SQLiteDatabase.CONFLICT_NONE);
        } catch (Exception e) {
            ExceptionLog.display(e);
        }
    }

    @Override
    public void onQueryComplete(int token, Object cookie, Cursor cursor) {

    }

    @Override
    public void onInsertComplete(int token, Object cookie, long rowId) {
        if (rowId != -1 && token == TOKEN_ADD_STUDENT) {
            mAdapter.scheduleItemMotion((RecyclerViewCursorAdapter.ItemMotion) cookie);
            mCursorLoader.onContentChanged();
        }
    }

    @Override
    public void onUpdateComplete(int token, Object cookie, int affectedRowCount) {
        if (affectedRowCount > 0) {
            mAdapter.scheduleItemMotion((RecyclerViewCursorAdapter.ItemMotion) cookie);
            mCursorLoader.onContentChanged();
        }
    }

    @Override
    public void onDeleteComplete(int token, Object cookie, int affectedRowCount) {
        if (affectedRowCount > 0) {
            mAdapter.scheduleItemMotion((RecyclerViewCursorAdapter.ItemMotion) cookie);
            mCursorLoader.onContentChanged();
        }
    }

    @Override
    public void onReplaceComplete(int token, Object cookie, long rowId) {

    }

    @Override
    public void onExecSqlComplete(int token, Object cookie, boolean result) {

    }
}

class MyRecyclerViewCursorAdapter extends RecyclerViewCursorAdapter {

    public static final int MODIFY_NAME = 1;
    public static final int MODIFY_SEX = 2;
    public static final int MODIFY_AGE = 3;

    //private OnContentClickListener mOnContentClickListener;

    public MyRecyclerViewCursorAdapter() {
        super(null, "_id");
    }

//    public void setOnContentClickListener(OnContentClickListener listener) {
//        mOnContentClickListener = listener;
//    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
        return new ViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.list_item_cursor_loader,
                        parent,
                        false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor, int position) {
        ViewHolder holder = (ViewHolder) viewHolder;
        holder.mTvId.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("_id"))));
        holder.mTvName.setText(cursor.getString(cursor.getColumnIndex("name")));
        holder.mTvSex.setText(cursor.getInt(cursor.getColumnIndex("sex")) == 0 ? "男" : "女");
        holder.mTvAge.setText(cursor.getInt(cursor.getColumnIndex("age")) + "岁");
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor, int position, List payloads) {
        if (payloads.get(0) instanceof Integer) {
            ViewHolder holder = (ViewHolder) viewHolder;
            switch ((int) payloads.get(0)) {
                case MODIFY_NAME:
                    holder.mTvName.setText(cursor.getString(cursor.getColumnIndex("name")));
                    break;
                case MODIFY_SEX:
                    holder.mTvSex.setText(cursor.getInt(cursor.getColumnIndex("sex")) == 0 ? "男" : "女");
                    break;
                case MODIFY_AGE:
                    holder.mTvAge.setText(cursor.getInt(cursor.getColumnIndex("age")) + "岁");
                    break;
            }
        }
    }

//    public interface OnContentClickListener {
//        void onNameClick(int position);
//        void onSexClick(int position);
//        void onAgeClick(int position);
//        void onRemoveClick(int position);
//    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        //private final OnContentClickListener mOnContentClickListener;
        private TextView mTvId;
        private TextView mTvName;
        private TextView mTvSex;
        private TextView mTvAge;
        private Button mBtnRemove;

        public ViewHolder(View itemView) {
            super(itemView);
            //mOnContentClickListener = listener;
            mTvId = itemView.findViewById(R.id.tv_id);
            mTvName = itemView.findViewById(R.id.tv_name);
            mTvSex = itemView.findViewById(R.id.tv_sex);
            mTvAge = itemView.findViewById(R.id.tv_age);
            mBtnRemove = itemView.findViewById(R.id.btn_remove);
//            if (listener != null) {
//                mTvName.setOnClickListener(this);
//                mTvSex.setOnClickListener(this);
//                mTvAge.setOnClickListener(this);
//                mBtnRemove.setOnClickListener(this);
//            }
        }

//        @Override
//        public void onClick(View v) {
//            if (mOnContentClickListener != null) {
//                int position = RecyclerViewBaseAdapter.getPositionByItemChildView(v);
//                switch (v.getId()) {
//                    case R.id.tv_name:
//                        mOnContentClickListener.onNameClick(position);
//                        break;
//                    case R.id.tv_sex:
//                        mOnContentClickListener.onSexClick(position);
//                        break;
//                    case R.id.tv_age:
//                        mOnContentClickListener.onAgeClick(position);
//                        break;
//                    case R.id.btn_remove:
//                        mOnContentClickListener.onRemoveClick(position);
//                        break;
//                }
//            }
//        }
    }
}
