package com.woefe.shoppinglist;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.woefe.shoppinglist.db.ShoppingListDatabase;
import com.woefe.shoppinglist.db.entity.ItemEntity;
import com.woefe.shoppinglist.db.entity.ShoppingListEntity;

import java.util.List;

public class DataRepository {

    private static DataRepository instance;
    private final ShoppingListDatabase database;
    private final MediatorLiveData<List<ShoppingListEntity>> observableLists;


    private DataRepository(ShoppingListDatabase database) {
        this.database = database;
        observableLists = new MediatorLiveData<>();

        // wrap lists in LiveData again to automatically get notified as soon as the database is
        // initialized. Methods of DataRepository that require parameters do not need to be wrapped,
        // because they are only called when the database is already available (the parameters
        // passed to these functions were read from the database earlier
        observableLists.addSource(database.shoppingListDAO().getAllLists(),
                lists -> {
                    if (database.getDatabaseInitialized().getValue() != null) {
                        observableLists.postValue(lists);
                    }
                });

    }

    public static DataRepository getInstance(final ShoppingListDatabase db) {
        if (instance == null) {
            synchronized (DataRepository.class) {
                if (instance == null) {
                    instance = new DataRepository(db);
                }
            }
        }
        return instance;
    }

    public LiveData<List<ShoppingListEntity>> getLists() {
        return observableLists;
    }

    public LiveData<ShoppingListEntity> getList(long listId) {
        return database.shoppingListDAO().getListById(listId);
    }

    public LiveData<List<ItemEntity>> getItems(long listId) {
        return database.itemDAO().getAllItemsOfList(listId);
    }
}
