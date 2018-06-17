package com.woefe.shoppinglist;

import android.app.Application;

import com.woefe.shoppinglist.db.ShoppingListDatabase;

public class ShoppingListApp extends Application {
    private AppExecutors executors;

    @Override
    public void onCreate() {
        super.onCreate();
        executors = new AppExecutors();
    }

    public ShoppingListDatabase getDatabase() {
        return ShoppingListDatabase.getInstance(this, executors);
    }

    public DataRepository getRepository() {
        return DataRepository.getInstance(getDatabase());
    }
}
