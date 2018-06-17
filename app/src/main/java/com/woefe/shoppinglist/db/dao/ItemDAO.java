package com.woefe.shoppinglist.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.woefe.shoppinglist.db.entity.ItemEntity;

import java.util.List;

@Dao
public interface ItemDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertItems(ItemEntity... items);

    @Update
    void updateItems(ItemEntity... items);

    @Delete
    void deleteItems(ItemEntity... items);

    @Query("SELECT * FROM item")
    LiveData<List<ItemEntity>> getAllItems();

    @Query("SELECT * FROM item WHERE id = :id")
    LiveData<ItemEntity> getItemById(long id);

    @Query("SELECT * FROM item WHERE list_id = :listId")
    LiveData<List<ItemEntity>> getAllItemsOfList(long listId);
}
