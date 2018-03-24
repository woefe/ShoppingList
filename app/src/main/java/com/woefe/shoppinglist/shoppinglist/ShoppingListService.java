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

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Arrays;
import java.util.Comparator;

/**
 * @author Wolfgang Popp.
 */
public class ShoppingListService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = ShoppingListService.class.getSimpleName();


    private ShoppingListsManager manager = null;
    private final IBinder binder = new ShoppingListBinder();
    private SharedPreferences sharedPreferences;
    private DirectoryStatus directoryStatus;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind() called: " + intent.toString());
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        manager = new ShoppingListsManager();
        directoryStatus = new DirectoryStatus(this);
        manager.onStart(directoryStatus.getDirectory());

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
        directoryStatus = new DirectoryStatus(this);
        manager.onStart(directoryStatus.getDirectory());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand() called");
        return START_NOT_STICKY;
    }

    public class ShoppingListBinder extends Binder {

        public boolean usesFallbackDir() {
            return directoryStatus.isFallback();
        }

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
            Arrays.sort(names, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareToIgnoreCase(o2);
                }
            });
            return names;
        }

        public int indexOf(String listName) {
            if (listName == null) {
                return -1;
            }
            return Arrays.binarySearch(getListNames(), listName);
        }

        public int size() {
            return manager.size();
        }

        public void addListChangeListener(ListsChangeListener listener) {
            manager.setListChangeListener(listener);
        }

        public void removeListChangeListener(ListsChangeListener listener) {
            manager.removeListChangeListenerListener(listener);
        }
    }
}
