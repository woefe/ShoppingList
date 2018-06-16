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
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.woefe.shoppinglist.shoppinglist.db.Item;
import com.woefe.shoppinglist.shoppinglist.db.ShoppingListDatabase;
import com.woefe.shoppinglist.shoppinglist.db.ShoppingListEntity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author Wolfgang Popp.
 */
public class ShoppingListService extends Service {
    private static final String TAG = ShoppingListService.class.getSimpleName();


    private final ShoppingListBinder binder = new ShoppingListBinder();
    private ShoppingListDatabase db;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind() called: " + intent.toString());

        db = Room.databaseBuilder(getApplicationContext(),
                ShoppingListDatabase.class, "shoppinglist_db").allowMainThreadQueries().build();

        if (db.shoppingListDAO().getAllLists().size() == 0) {
            binder.addList("Shopping List");
        }

        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(TAG, "onUnbind() called: " + intent.toString());
        db.close();
        //TODO sync webdav
        return false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand() called");
        return START_NOT_STICKY;
    }

    public class ShoppingListBinder extends Binder {

        public long addList(String name) {
            ShoppingListEntity shoppingListEntity = new ShoppingListEntity();
            shoppingListEntity.name = name;
            shoppingListEntity.modifyTime = System.currentTimeMillis();
            long[] ids = db.shoppingListDAO().insertLists(shoppingListEntity);
            return ids.length > 0 ? ids[0] : -1;
        }

        public boolean hasList(long listId) {
            return db.shoppingListDAO().getListById(listId) != null;
        }

        public boolean hasName(String name) {
            return db.shoppingListDAO().getListsByName(name).size() > 0;
        }

        public boolean removeList(ShoppingListEntity list) {
            return db.shoppingListDAO().deleteLists(list) != 0;
        }

        public boolean removeList(long listId) {
            return removeList(db.shoppingListDAO().getListById(listId));
        }

        public ShoppingList getList(final ShoppingListEntity list) {
            final ShoppingList shoppingList = new ShoppingList(list.name);

            for (Item item : db.itemDAO().getAllItemsOfList(list.id)) {
                shoppingList.add(new ListItem(item.id, item.isChecked, item.description, item.amount));
            }
            return shoppingList;
        }

        public ShoppingList getList(long listId) {
            ShoppingListEntity list = db.shoppingListDAO().getListById(listId);
            return getList(list);
        }

        public List<ShoppingListEntity> getLists() {
            return db.shoppingListDAO().getAllLists();
        }

        public void rename(ShoppingListEntity list, String newName) {
            if (!list.name.equals(newName)) {
                list.name = newName;
                db.shoppingListDAO().updateLists(list);
            }
        }
    }
}
