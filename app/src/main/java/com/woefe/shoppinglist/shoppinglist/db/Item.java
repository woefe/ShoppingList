package com.woefe.shoppinglist.shoppinglist.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "item",
        foreignKeys = @ForeignKey(
            entity = ShoppingListEntity.class,
            parentColumns = "id",
            childColumns = "list_id",
            onDelete = CASCADE),
        indices = {@Index("list_id")}
)
public class Item {
    @PrimaryKey
    public long id;

    @ColumnInfo(name = "is_checked")
    public boolean isChecked;

    @ColumnInfo(name = "list_id")
    public long listId;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "amount")
    public String amount;
}
