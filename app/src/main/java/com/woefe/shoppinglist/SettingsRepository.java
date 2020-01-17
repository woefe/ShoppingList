/*
 * ShoppingList - A simple shopping list for Android
 *
 * Copyright (C) 2019.  Wolfgang Popp
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

package com.woefe.shoppinglist;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import com.woefe.shoppinglist.activity.SettingsFragment;

public class SettingsRepository {

    private static final String MODE_NIGHT_FOLLOW_SYSTEM = "MODE_NIGHT_FOLLOW_SYSTEM";
    private static final String MODE_NIGHT_NO = "MODE_NIGHT_NO";
    private static final String MODE_NIGHT_YES = "MODE_NIGHT_YES";

    private final Context context;
    private final SharedPreferences prefs;

    public SettingsRepository(Context context) {
        this.context = context.getApplicationContext();
        prefs = PreferenceManager.getDefaultSharedPreferences(this.context);
    }

    public int getTheme() {
        String theme = prefs.getString(SettingsFragment.KEY_THEME, MODE_NIGHT_FOLLOW_SYSTEM);
        return themeStringToInt(theme);
    }

    private int themeStringToInt(String theme) {
        switch (theme) {
            case MODE_NIGHT_NO:
                return AppCompatDelegate.MODE_NIGHT_NO;
            case MODE_NIGHT_YES:
                return AppCompatDelegate.MODE_NIGHT_YES;
            default:
                return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
    }
}
