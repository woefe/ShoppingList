package com.woefe.shoppinglist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.woefe.shoppinglist.ShoppingListApp;
import com.woefe.shoppinglist.db.entity.ShoppingListEntity;

import java.util.List;

public class ShoppingListsViewModel extends AndroidViewModel {

    private final MediatorLiveData<List<ShoppingListEntity>> observableLists;

    public ShoppingListsViewModel(Application application) {
        super(application);
        observableLists = new MediatorLiveData<>();
        observableLists.setValue(null);
        LiveData<List<ShoppingListEntity>> lists = ((ShoppingListApp) application).getRepository()
                .getLists();

        observableLists.addSource(lists, observableLists::setValue);
    }

    public LiveData<List<ShoppingListEntity>> getLists() {
        return observableLists;
    }
}
