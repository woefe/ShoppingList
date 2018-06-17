package com.woefe.shoppinglist.model;

public interface Item {
    long getId();

    boolean isChecked();

    long getListId();

    String getDescription();

    String getAmount();
}
