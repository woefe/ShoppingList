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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;

import com.woefe.shoppinglist.activity.SettingsFragment;

public class ShoppingListApplication extends Application {

    public static final String MODE_NIGHT_FOLLOW_SYSTEM = "MODE_NIGHT_FOLLOW_SYSTEM";
    public static final String MODE_NIGHT_NO = "MODE_NIGHT_NO";
    public static final String MODE_NIGHT_YES = "MODE_NIGHT_YES";

    @Override
    public void onCreate() {
        super.onCreate();
        setNightMode(this);
    }

    public static void setNightMode(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = prefs.getString(SettingsFragment.KEY_THEME, MODE_NIGHT_FOLLOW_SYSTEM);
        if (theme == null) {
            theme = MODE_NIGHT_FOLLOW_SYSTEM;
        }

        int mode;
        if (theme.equals(MODE_NIGHT_NO)) {
            mode = AppCompatDelegate.MODE_NIGHT_NO;
        } else if (theme.equals(MODE_NIGHT_YES)) {
            mode = AppCompatDelegate.MODE_NIGHT_YES;
        } else {
            mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        AppCompatDelegate.setDefaultNightMode(mode);
    }
}
