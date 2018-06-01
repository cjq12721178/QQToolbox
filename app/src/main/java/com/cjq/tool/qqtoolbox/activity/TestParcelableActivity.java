package com.cjq.tool.qqtoolbox.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cjq.lib.weisi.data.FilterCollection;
import com.cjq.lib.weisi.data.Storage;
import com.cjq.lib.weisi.iot.PhysicalSensor;
import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.bean.Person;

public class TestParcelableActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_parcelable);

        //FilterCollection<PhysicalSensor> filters = getIntent().getParcelableExtra("filters");
        Storage<PhysicalSensor> storage = getIntent().getParcelableExtra("storage");
    }
}
