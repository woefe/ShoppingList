/*
 * ShoppingList - A simple shopping list for Android
 *
 * Copyright (C) 2016  Wolfgang Popp
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

package de.wolfgang_popp.shoppinglist.shoppinglist;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import de.wolfgang_popp.shoppinglist.R;
import de.wolfgang_popp.shoppinglist.activity.SettingsFragment;

/**
 * @author Wolfgang Popp.
 */
public class ShoppingListService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = ShoppingListService.class.getSimpleName();

    private static final String DEFAULT_FILENAME = "ShoppingList.lst";

    private ShoppingList shoppingList = null;
    private final IBinder binder = new ShoppingListBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind() called: " + intent.toString());
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            shoppingList.writeIfDirty();
        } catch (IOException e) {
            toastErrorWhileSave();
        }
        Log.v(TAG, "onUnbind() called: " + intent.toString());
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String filename = sharedPreferences.getString(SettingsFragment.KEY_FILE_LOCATION, "");
        File newFile = new File(filename);
        try {
            newFile.createNewFile();
            shoppingList.changeFile(filename);
        } catch (IOException e) {
            toastErrorCreateFile(filename);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String filename = sharedPreferences.getString(SettingsFragment.KEY_FILE_LOCATION, "");
        String defaultFilename = getApplicationContext().getFileStreamPath(DEFAULT_FILENAME).getAbsolutePath();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (filename.equals("")) {
            filename = defaultFilename;
        }

        File listFile = new File(filename);
        try {
            listFile.createNewFile();
            shoppingList = new ShoppingList(filename);
        } catch (IOException e) {
            toastErrorCreateFile(filename);
        }

        if (shoppingList == null) {
            try {
                shoppingList = new ShoppingList(defaultFilename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.v(TAG, "onCreate() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
        try {
            shoppingList.writeIfDirty();
        } catch (IOException e) {
            toastErrorWhileSave();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand() called");
        return START_NOT_STICKY;
    }

    private void toastErrorWhileSave() {
        Toast.makeText(ShoppingListService.this, R.string.error_saving, Toast.LENGTH_SHORT).show();
    }

    private void toastErrorCreateFile(String filename) {
        Toast.makeText(ShoppingListService.this, getResources().getString(R.string.error_create) + filename, Toast.LENGTH_LONG).show();
    }


    public class ShoppingListBinder extends Binder {
        public void addItem(String description, String quantity) {
            shoppingList.add(new ListItem(false, description, quantity));
        }

        public void toggleItemChecked(int index) {
            boolean isChecked = shoppingList.get(index).isChecked();
            shoppingList.setChecked(index, !isChecked);
        }

        public void removeItem(int index) {
            shoppingList.remove(index);
        }

        public void removeAllCheckedItems() {
            shoppingList.removeAllCheckedItems();
        }

        public void edit(int index, String newDescription, String newQuantity) {
            shoppingList.editItem(index, newDescription, newQuantity);
        }

        public ShoppingList getShoppingList() {
            return shoppingList;
        }
    }
}
