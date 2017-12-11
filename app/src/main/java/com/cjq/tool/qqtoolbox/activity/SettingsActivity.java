package com.cjq.tool.qqtoolbox.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.fragment.SettingsFragment;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(android.R.id.content,
                            new SettingsFragment())
                    .commit();
        }
    }
}
