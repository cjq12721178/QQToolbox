package com.cjq.tool.qqtoolbox.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.cjq.tool.qbox.ui.adapter.AdapterDelegate;
import com.cjq.tool.qbox.ui.adapter.RecyclerViewBaseAdapter;
import com.cjq.tool.qbox.ui.decoration.SpaceItemDecoration;
import com.cjq.tool.qbox.util.ClosableLog;
import com.cjq.tool.qbox.util.CodeRunTimeCatcher;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.util.DebugTag;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class TestRecyclerViewBaseAdapterActivity
        extends AppCompatActivity
        implements RecyclerViewBaseAdapter.OnItemClickListener,
        View.OnClickListener {

    private List<City> mCities = new ArrayList<>();
    private TestRecyclerViewBaseAdapter mAdapter;
    private static int addCityCount;
    private static int createTimes;
    private EditText mEtMoveFrom;
    private EditText mEtMoveCount;
    private EditText mEtMoveTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_recycler_view_base_adapter);

        addCityCount = 0;
        createTimes = 0;

        mEtMoveFrom = (EditText) findViewById(R.id.et_move_from);
        mEtMoveCount = (EditText) findViewById(R.id.et_move_count);
        mEtMoveTo = (EditText) findViewById(R.id.et_move_to);

        RecyclerView rvCities = (RecyclerView) findViewById(R.id.rv_test_base_adapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        rvCities.setLayoutManager(linearLayoutManager);
        rvCities.addItemDecoration(new SpaceItemDecoration(getResources().getDimensionPixelSize(R.dimen.qbox_list_item_interval_vertical), true));
        generateCities();
        mAdapter = new TestRecyclerViewBaseAdapter(mCities);
        mAdapter.setUpdateSelectedState(true);
        mAdapter.setOnItemClickListener(this);
        rvCities.setAdapter(mAdapter);
    }

    private void generateCities() {
        mCities.add(new City("北京"));
        mCities.add(new City("上海"));
        mCities.add(new City("杭州"));
        mCities.add(new City("南京"));
        mCities.add(new City("苏州"));
        mCities.add(new City("嘉兴"));
        mCities.add(new City("武汉"));
        mCities.add(new City("长沙"));
        mCities.add(new City("厦门"));
        mCities.add(new City("宁波"));
        mCities.add(new City("湖州"));
        mCities.add(new City("金华"));
        mCities.add(new City("福州"));
        mCities.add(new City("大连"));
        mCities.add(new City("哈尔滨"));
        mCities.add(new City("长春"));
        mCities.add(new City("沈阳"));
        mCities.add(new City("天津"));
        mCities.add(new City("重庆"));
        mCities.add(new City("大庆"));
        mCities.add(new City("衢州"));
        mCities.add(new City("西安"));
        mCities.add(new City("贵州"));
        mCities.add(new City("桂林"));
        mCities.add(new City("昆明"));
        mCities.add(new City("呼和浩特"));
        mCities.add(new City("包头"));
        mCities.add(new City("齐齐哈尔"));
        mCities.add(new City("常熟"));
        mCities.add(new City("抚顺"));
        mCities.add(new City("威海卫"));
        mCities.add(new City("雄安"));
        mCities.add(new City("宝鸡"));
        mCities.add(new City("大同"));
        mCities.add(new City("郑州"));
        mCities.add(new City("吉林"));
    }

    @Override
    public void onItemClick(View item, int position) {
        ClosableLog.d(DebugTag.TEST_RECYCLER_VIEW_BASE_ADAPTER,
                "on item click position: " + position
                        + ", city: " + mAdapter.getItemByPosition(position).getName());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_item_before_selected: {
                int selectedIndex = mAdapter.getSelectedIndex();
                if (selectedIndex == -1) {
                    selectedIndex = 0;
                }
                mCities.add(selectedIndex, new City("新加入的城市" + (++addCityCount)));
                mAdapter.notifyItemInserted(selectedIndex);
                ClosableLog.d(DebugTag.TEST_RECYCLER_VIEW_BASE_ADAPTER, "add item before selected, selected index = " + mAdapter.getSelectedIndex());
            } break;
            case R.id.btn_add_item_after_selected: {
                int selectedIndex = mAdapter.getSelectedIndex();
                if (selectedIndex == -1) {
                    selectedIndex = mCities.size();
                } else {
                    selectedIndex = selectedIndex + 1;
                }
                mCities.add(selectedIndex, new City("新加入的城市" + (++addCityCount)));
                mAdapter.notifyItemInserted(selectedIndex);
                ClosableLog.d(DebugTag.TEST_RECYCLER_VIEW_BASE_ADAPTER, "add item after selected, selected index = " + mAdapter.getSelectedIndex());
            } break;
            case R.id.btn_delete_item_before_selected: {
                int selectedIndex = mAdapter.getSelectedIndex();
                if (selectedIndex == -1) {
                    selectedIndex = 0;
                } else {
                    --selectedIndex;
                }
                if (selectedIndex >= 0 && selectedIndex < mCities.size()) {
                    mCities.remove(selectedIndex);
                    mAdapter.notifyItemRemoved(selectedIndex);
                    ClosableLog.d(DebugTag.TEST_RECYCLER_VIEW_BASE_ADAPTER, "delete item before selected, selected index = " + mAdapter.getSelectedIndex());
                }
            } break;
            case R.id.btn_delete_item_selected: {
                int selectedIndex = mAdapter.getSelectedIndex();
                if (selectedIndex == -1) {
                    selectedIndex = 0;
                }
                if (selectedIndex < mCities.size()) {
                    mCities.remove(selectedIndex);
                    mAdapter.notifyItemRemoved(selectedIndex);
                    ClosableLog.d(DebugTag.TEST_RECYCLER_VIEW_BASE_ADAPTER, "delete item selected, selected index = " + mAdapter.getSelectedIndex());
                }
            } break;
            case R.id.btn_delete_item_after_selected: {
                int selectedIndex = mAdapter.getSelectedIndex();
                if (selectedIndex == -1) {
                    selectedIndex = mCities.size() - 1;
                } else {
                    ++selectedIndex;
                }
                if (selectedIndex >= 0 && selectedIndex < mCities.size()) {
                    mCities.remove(selectedIndex);
                    mAdapter.notifyItemRemoved(selectedIndex);
                    ClosableLog.d(DebugTag.TEST_RECYCLER_VIEW_BASE_ADAPTER, "delete item after selected, selected index = " + mAdapter.getSelectedIndex());
                }
            } break;
            case R.id.btn_move: {
                try {
                    int from = Integer.parseInt(mEtMoveFrom.getText().toString());
                    int count = Integer.parseInt(mEtMoveCount.getText().toString());
                    int to = Integer.parseInt(mEtMoveTo.getText().toString());
                    if (from >= 0 && to >= 0 && count > 0 && to + count <= mCities.size()) {
                        for (int i = 0;i < count;++i) {
                            City city = mCities.remove(from + i);
                            mCities.add(to + i, city);
                            mAdapter.notifyItemMoved(from+ i, to + i);
                            ClosableLog.d(DebugTag.TEST_RECYCLER_VIEW_BASE_ADAPTER, "move item from " + (from + i) + " to " + (to + i) + ", selected index = " + mAdapter.getSelectedIndex());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } break;
        }
    }

    private static class City {

        private String mName;

        public City(String name) {
            mName = name;
        }

        public String getName() {
            return mName;
        }

        public void setName(String name) {
            mName = name;
        }
    }

    private static class TestRecyclerViewBaseAdapterDelegate implements AdapterDelegate<City> {

        @Override
        public int getItemViewType() {
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent) {
            CodeRunTimeCatcher.start();
            ViewHolder holder = new ViewHolder(LayoutInflater
                    .from(parent.getContext())
                    .inflate(R.layout.list_item_test_recycler_view_base_adapter,
                            parent,
                            false));
            ClosableLog.d(DebugTag.TEST_RECYCLER_VIEW_BASE_ADAPTER,
                    "on create view, create times = "
                            + (++createTimes) +
                            ", time = "
                            + CodeRunTimeCatcher.end() / 100000);
            return holder;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, City item, int position) {
            CodeRunTimeCatcher.start();
            ViewHolder holder = (ViewHolder) viewHolder;
            holder.mTvCity.setText(item.getName());
            ClosableLog.d(DebugTag.TEST_GENERAL_RECYCLER_VIEW,
                    "on bind view, position = "
                            + position
                            + ", create times = "
                            + createTimes
                            + ", time = "
                            + CodeRunTimeCatcher.end() / 100000);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, City item, int position, List payloads) {
            ClosableLog.d(DebugTag.TEST_GENERAL_RECYCLER_VIEW,
                    "on bind view payloads, position = "
                            + position
                            + ", create times = "
                            + createTimes);
        }

        public static class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView mTvCity;

            public ViewHolder(View itemView) {
                super(itemView);
                mTvCity = (TextView) itemView.findViewById(R.id.tv_city);
            }
        }
    }

    private static class TestRecyclerViewBaseAdapter extends RecyclerViewBaseAdapter<City> {

        private TestRecyclerViewBaseAdapterDelegate mDelegate;
        private List<City> mCities;

        public TestRecyclerViewBaseAdapter(List<City> cities) {
            mCities = cities;
        }

        @Override
        public void onAddAdapterDelegate() {
            mDelegate = new TestRecyclerViewBaseAdapterDelegate();
        }

        @Override
        protected AdapterDelegate<City> getAdapterDelegate(int viewType) {
            return mDelegate;
        }

        @Override
        public City getItemByPosition(int position) {
            return mCities.get(position);
        }

        @Override
        public int getItemCount() {
            return mCities.size();
        }
    }
}
