package com.woefe.shoppinglist.shoppinglist.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {Item.class, ShoppingListEntity.class}, version = 1, exportSchema = false)
public abstract class ShoppingListDatabase extends RoomDatabase {
    public abstract ShoppingListDAO shoppingListDAO();

    public abstract ItemDAO itemDAO();
}
