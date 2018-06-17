package com.woefe.shoppinglist.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.support.annotation.NonNull;

import com.woefe.shoppinglist.AppExecutors;
import com.woefe.shoppinglist.db.dao.ItemDAO;
import com.woefe.shoppinglist.db.dao.ShoppingListDAO;
import com.woefe.shoppinglist.db.entity.ItemEntity;
import com.woefe.shoppinglist.db.entity.ShoppingListEntity;

@Database(entities = {ItemEntity.class, ShoppingListEntity.class}, version = 1, exportSchema = false)
public abstract class ShoppingListDatabase extends RoomDatabase {

    private static final String DB_NAME = "shoppinglist_db";
    private static ShoppingListDatabase instance;
    private final MutableLiveData<Boolean> isInitialized = new MutableLiveData<>();


    public abstract ShoppingListDAO shoppingListDAO();

    public abstract ItemDAO itemDAO();

    public static ShoppingListDatabase getInstance(final Context ctx,
                                                   final AppExecutors executors) {
        if (instance == null) {
            synchronized (ShoppingListDatabase.class) {
                instance = buildDatabase(ctx, executors);

                if (ctx.getDatabasePath(DB_NAME).exists()) {
                    instance.setDatabaseInitialized();
                }
            }
        }
        return instance;
    }

    private void setDatabaseInitialized() {
        isInitialized.postValue(true);
    }


    private static ShoppingListDatabase buildDatabase(Context ctx, AppExecutors executors) {
        Callback callback = new Callback() {
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);

                executors.diskIO().execute(() -> {
                    // Insert initial shoppingList
                    ShoppingListDatabase.getInstance(ctx, executors).setDatabaseInitialized();
                });
            }
        };

        return Room.databaseBuilder(ctx, ShoppingListDatabase.class, DB_NAME)
                .addCallback(callback).build();
    }

    public LiveData<Boolean> getDatabaseInitialized() {
        return isInitialized;
    }
}
