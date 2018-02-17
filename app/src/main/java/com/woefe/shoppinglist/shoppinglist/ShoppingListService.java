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

package com.woefe.shoppinglist.shoppinglist;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.util.Arrays;

import com.woefe.shoppinglist.activity.SettingsFragment;

/**
 * @author Wolfgang Popp.
 */
public class ShoppingListService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = ShoppingListService.class.getSimpleName();

    private static final String DEFAULT_DIRECTORY = "ShoppingLists";

    private ShoppingListsManager manager = null;
    private final IBinder binder = new ShoppingListBinder();
    private SharedPreferences sharedPreferences;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind() called: " + intent.toString());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        manager = new ShoppingListsManager();
        manager.onStart(getDirectory());

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind() called: " + intent.toString());
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        manager.onStop();
        return false;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        manager.onStop();
        manager.onStart(getDirectory());
    }

    private String getDirectory() {
        String directory = sharedPreferences.getString(SettingsFragment.KEY_DIRECTORY_LOCATION, "");
        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        File file = new File(directory);

        if (directory.equals("")
                || permission != PackageManager.PERMISSION_GRANTED
                || !file.isDirectory()
                || !file.canWrite()) {

            return getApplicationContext().getFileStreamPath(DEFAULT_DIRECTORY).getAbsolutePath();
        }

        new File(directory).mkdirs();
        return directory;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand() called");
        return START_NOT_STICKY;
    }

    public class ShoppingListBinder extends Binder {

        public void addList(String listName) throws ShoppingListException {
            manager.addList(listName);
        }

        public void removeList(String listName) {
            manager.removeList(listName);
        }

        public ShoppingList getList(String listName) {
            return manager.getList(listName);
        }

        public boolean hasList(String listName) {
            return manager.hasList(listName);
        }

        public String[] getListNames() {
            String[] names = manager.getListNames().toArray(new String[0]);
            Arrays.sort(names);
            return names;
        }

        public int size() {
            return manager.size();
        }

        public void onPermissionsGranted() {
            manager.onStop();
            manager.onStart(getDirectory());
        }

        public void addListChangeListener(ListsChangeListener listener) {
            manager.setListChangeListener(listener);
        }

        public void removeListChangeListener(ListsChangeListener listener) {
            manager.removeListChangeListenerListener(listener);
        }
    }
}
