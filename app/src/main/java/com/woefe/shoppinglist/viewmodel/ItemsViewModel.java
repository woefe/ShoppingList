package com.woefe.shoppinglist.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.woefe.shoppinglist.DataRepository;
import com.woefe.shoppinglist.ShoppingListApp;
import com.woefe.shoppinglist.db.entity.ItemEntity;

import java.util.List;

public class ItemsViewModel extends AndroidViewModel {

    private final LiveData<List<ItemEntity>> observableItems;


    public ItemsViewModel(@NonNull Application application, long listId) {
        super(application);
        DataRepository repository = ((ShoppingListApp) application).getRepository();
        observableItems = repository.getItems(listId);
    }

    public LiveData<List<ItemEntity>> getItems() {
        return observableItems;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final long listId;
        private final Application application;

        public Factory(long listId, Application application) {
            this.listId = listId;
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new ItemsViewModel(application, listId);
        }
    }
}
