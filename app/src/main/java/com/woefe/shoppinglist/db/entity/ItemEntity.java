package com.woefe.shoppinglist.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

import com.woefe.shoppinglist.model.Item;

import static android.arch.persistence.room.ForeignKey.CASCADE;

@Entity(tableName = "item",
        foreignKeys = @ForeignKey(
            entity = ShoppingListEntity.class,
            parentColumns = "id",
            childColumns = "list_id",
            onDelete = CASCADE),
        indices = {@Index("list_id")}
)
public class ItemEntity implements Item{
    @PrimaryKey
    private long id;

    @ColumnInfo(name = "is_checked")
    private boolean isChecked;

    @ColumnInfo(name = "list_id")
    private long listId;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "amount")
    private String amount;

    @Override
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public long getListId() {
        return listId;
    }

    public void setListId(long listId) {
        this.listId = listId;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
