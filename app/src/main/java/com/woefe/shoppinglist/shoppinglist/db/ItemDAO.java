package com.woefe.shoppinglist.shoppinglist.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

@Dao
public interface ItemDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertItems(Item... items);

    @Update
    void updateItems(Item... items);

    @Delete
    void deleteItems(Item... items);

    @Query("SELECT * FROM item")
    List<Item> getAllItems();

    @Query("SELECT * FROM item WHERE id = :id")
    Item getItemById(long id);

    @Query("SELECT * FROM item WHERE list_id = :listId")
    List<Item> getAllItemsOfList(long listId);
}
