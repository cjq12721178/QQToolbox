package com.cjq.tool.qqtoolbox.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import android.support.annotation.StringRes;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.text.TextUtils;
import android.util.Log;

import com.cjq.tool.qqtoolbox.R;
import com.cjq.tool.qqtoolbox.util.DebugTag;

/**
 * Created by CJQ on 2017/12/7.
 */

public class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceChangeListener {

    private static final String FILE_NAME = "settings";

    int mDefaultBaseStationPort = 5000;

    public int getDefaultBaseStationPort() {
        return mDefaultBaseStationPort;
    }

    private SharedPreferences getSharedPreferences() {
        return getContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    private int getInt(@StringRes int preferenceKeyRes, int defaultValue) {
        try {
            return Integer.parseInt(getSharedPreferences().getString(getContext().getString(preferenceKeyRes), ""));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public int getBaseStationPort() {
        return getInt(R.string.preference_key_save_value_type, mDefaultBaseStationPort);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setSharedPreferencesName(FILE_NAME);
        addPreferencesFromResource(R.xml.settings);

        Preference preference = findPreference(getString(R.string.preference_key_save_value_type));
        preference.setOnPreferenceChangeListener(this);
        //preference.setPersistent(false);
        onPreferenceChange(preference, getBaseStationPort());
        ListPreference listPreference = (ListPreference) findPreference(getString(R.string.preference_key_serial_port_baud_rate));
        listPreference.setOnPreferenceChangeListener(this);
        onPreferenceChange(listPreference, "115200");
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

//        if (newValue instanceof String) {
//            Log.d(DebugTag.GENERAL_LOG_TAG, "save type is String");
//            return false;
//        } else if (newValue instanceof Integer) {
//            Log.d(DebugTag.GENERAL_LOG_TAG, "save type is Integer");
//        } else {
//            Log.d(DebugTag.GENERAL_LOG_TAG, "save type is other");
//            return false;
//        }
//        if (newValue instanceof String) {
//            try {
//                int port = Integer.parseInt((String) newValue);
//                preference.getSharedPreferences().edit().putInt(preference.getKey(), port).commit();
//                preference.setSummary(newValue.toString());
//            } catch (NumberFormatException e) {
//            }
//            return false;
//        }
        preference.setSummary(newValue.toString());
        if (preference instanceof EditTextPreference) {
            EditTextPreference editTextPreference = (EditTextPreference) preference;
            if (TextUtils.isEmpty(editTextPreference.getText())) {
                editTextPreference.setPersistent(false);
                editTextPreference.setText(newValue.toString());
                editTextPreference.setPersistent(true);
            }
        } else if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;
            if (TextUtils.isEmpty(listPreference.getValue())) {
                listPreference.setPersistent(false);
                listPreference.setValue(newValue.toString());
                listPreference.setPersistent(true);
            }
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
}
