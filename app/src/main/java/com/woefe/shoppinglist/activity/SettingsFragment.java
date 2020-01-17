/*
 * ShoppingList - A simple shopping list for Android
 *
 * Copyright (C) 2018.  Wolfgang Popp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.woefe.shoppinglist.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.view.View;

import com.woefe.shoppinglist.R;
import com.woefe.shoppinglist.SettingsRepository;
import com.woefe.shoppinglist.dialog.DirectoryChooser;

/**
 * @author Wolfgang Popp.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_DIRECTORY_LOCATION = "FILE_LOCATION";
    public static final String KEY_THEME = "THEME";
    private static final int REQUEST_CODE_CHOOSE_DIR = 123;

    private SettingsRepository settingsRepository;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        settingsRepository = new SettingsRepository(context);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        // show the current value in the settings screen
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
            initSummary(getPreferenceScreen().getPreference(i));
        }
        View content = getActivity().findViewById(android.R.id.content);
        content.setBackgroundColor(getResources().getColor(R.color.colorBackground));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Preference fileLocationPref = findPreference("FILE_LOCATION");

        fileLocationPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(getContext(), DirectoryChooser.class);
                startActivityForResult(intent, REQUEST_CODE_CHOOSE_DIR);
                return true;
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceCategory) {
            PreferenceCategory cat = (PreferenceCategory) p;
            for (int i = 0; i < cat.getPreferenceCount(); i++) {
                initSummary(cat.getPreference(i));
            }
        } else {
            updatePreferences(p);
        }
    }

    private void updatePreferences(Preference p) {
        if (KEY_DIRECTORY_LOCATION.equals(p.getKey())) {
            String path = getSharedPreferences().getString(KEY_DIRECTORY_LOCATION, "");
            p.setSummary(path);
        }
        if (p instanceof EditTextPreference) {
            EditTextPreference editTextPref = (EditTextPreference) p;
            p.setSummary(editTextPref.getText());
        }
    }

    private SharedPreferences getSharedPreferences() {
        FragmentActivity activity = getActivity();
        assert activity != null;
        return PreferenceManager.getDefaultSharedPreferences(activity);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_DIRECTORY_LOCATION)) {
            Preference p = findPreference(key);
            updatePreferences(p);
        } else if (key.equals(KEY_THEME)) {
            int theme = settingsRepository.getTheme();
            AppCompatDelegate.setDefaultNightMode(theme);
            Activity activity = getActivity();
            if (activity != null) {
                activity.recreate();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (REQUEST_CODE_CHOOSE_DIR): {
                if (resultCode == Activity.RESULT_OK) {
                    String path = data.getStringExtra(DirectoryChooser.SELECTED_PATH);
                    SharedPreferences.Editor editor = getSharedPreferences().edit();
                    editor.putString(KEY_DIRECTORY_LOCATION, path).apply();
                }
                break;
            }
        }
    }
}
