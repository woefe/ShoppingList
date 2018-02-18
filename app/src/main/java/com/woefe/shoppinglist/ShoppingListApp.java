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

package com.woefe.shoppinglist;

import android.app.Application;
import android.widget.Toast;

import com.woefe.shoppinglist.shoppinglist.DirectoryStatus;

/**
 * @author Wolfgang Popp
 */

public class ShoppingListApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DirectoryStatus directoryStatus = new DirectoryStatus(getApplicationContext());

        if (directoryStatus.isFallback()) {
            int text = R.string.warn_ignore_directory;
            if (directoryStatus.getReason() == DirectoryStatus.Status.MISSING_PERMISSION) {
                text = R.string.warn_missing_permission;
            } else if (directoryStatus.getReason() == DirectoryStatus.Status.NOT_A_DIRECTORY) {
                text = R.string.warn_not_a_directory;
            } else if (directoryStatus.getReason() == DirectoryStatus.Status.CANNOT_WRITE) {
                text = R.string.warn_cannot_write;
            }
            Toast.makeText(this, text, Toast.LENGTH_LONG).show();
        }
    }
}
