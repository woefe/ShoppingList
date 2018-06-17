package com.woefe.shoppinglist.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.woefe.shoppinglist.db.entity.ShoppingListEntity;

import java.util.List;

@Dao
public interface ShoppingListDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long[] insertLists(ShoppingListEntity... lists);

    @Update
    void updateLists(ShoppingListEntity... lists);

    @Delete
    int deleteLists(ShoppingListEntity... lists);

    @Query("SELECT * FROM list")
    LiveData<List<ShoppingListEntity>> getAllLists();

    @Query("SELECT * FROM list WHERE id = :id")
    LiveData<ShoppingListEntity> getListById(long id);


    @Query("SELECT * FROM list WHERE name = :name")
    LiveData<List<ShoppingListEntity>> getListsByName(String name);
}
