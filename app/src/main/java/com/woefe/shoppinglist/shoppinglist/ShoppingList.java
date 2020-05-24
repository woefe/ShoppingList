/*
 * ShoppingList - A simple shopping list for Android
 *
 * Copyright (C) 2018.  Wolfgang Popp
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.woefe.shoppinglist.shoppinglist;

import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ShoppingList {

    private String name;
    private List<String> allCategories = new ArrayList<>();
    private final List<ShoppingListListener> listeners = new LinkedList<>();
    private ArrayMap<String, ArrayList<ListItem>> categories = new ArrayMap<>();
    public static final String DEFAULT_CATEGORY = "Allgemein"; // todo translate

    public ShoppingList(String name) {
        this.name = name;
    }

    public void addDefaultCategory() {
        if (categories.isEmpty()) {
            categories.put(DEFAULT_CATEGORY, new ArrayList<ListItem>()); // todo translate
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyListChanged(Event.newOther());
    }

    public boolean add(String description, String quantity, String category) {
        ListItem item = new ListItem(false, description, quantity, category);
        boolean res = getListForItem(item).add(item);

        if (res) {
            notifyListChanged(Event.newItemInserted(0));
        }
        
        return res;
    }

    public void remove(ListItem item) {
        boolean b = getListForItem(item).remove(item);
        if (b) {
            notifyListChanged(Event.newOther());
        }
    }

    void clear() {
        categories.clear();
        notifyListChanged(Event.newOther());
    }

    public int size() {
        int size = 0;

        for (ArrayList<ListItem> categoryList : getCategories().values()) {
            size += categoryList.size();
        }
        return size;
    }

    public void sort(Comparator<? super ListItem> c) {
        for (ArrayList<ListItem> list : categories.values()) {
            list.sort(c);
        }
        notifyListChanged(Event.newOther());
    }

    private void setChecked(ListItem item, boolean isChecked, int absolutePosition) {
        List<ListItem> list = getListForItem(item);
        list.get(list.indexOf(item)).setChecked(isChecked);
        notifyListChanged(Event.newItemChanged(absolutePosition));
    }

    public void toggleChecked(ListItem item, int absolutePosition) {
        setChecked(item, !item.isChecked(), absolutePosition);
    }

    public void moveInCategory(String category,
                               int oldIndex,
                               int newIndex,
                               int fromAbsolutePosition,
                               int toAbsolutePosition) {
        List<ListItem> list = getCategories().get(category);
        list.add(newIndex, list.remove(oldIndex));
        notifyListChanged(Event.newItemMoved(fromAbsolutePosition, toAbsolutePosition));
    }

    public void editItem(ListItem item, String newDescription, String newQuantity, String newCategory) {
        item.setDescription(newDescription);
        item.setQuantity(newQuantity);
        item.setCategory(newCategory);
        notifyListChanged(Event.newItemChanged(-1));
    }


    public void removeAllCheckedItems() {
        for (ArrayList<ListItem> list : categories.values()) {
            for (ListItem item : list) {
                if (item.isChecked()) {
                    list.remove(item);
                }
            }
        }
        notifyListChanged(Event.newOther());
    }

    public void addListener(ShoppingListListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ShoppingListListener listener) {
        listeners.remove(listener);
    }

    public ArrayMap<String, ArrayList<ListItem>> getCategories() {
        return categories;
    }

    public List<String> getAllCategories() {
        return allCategories;
    }

    private void notifyListChanged(ShoppingList.Event event) {
        for (ShoppingListListener listener : listeners) {
            listener.onShoppingListUpdate(this, event);
        }
    }

    public interface ShoppingListListener {
        void onShoppingListUpdate(ShoppingList list, ShoppingList.Event event);
    }

    public static class Event {
        public static final int ITEM_CHANGED = 0b1;
        public static final int ITEM_REMOVED = 0b10;
        public static final int ITEM_MOVED = 0b100;
        public static final int ITEM_INSERTED = 0b1000;
        public static final int OTHER = 0xffffffff;

        private final int state;
        private int index = -1;
        private int oldIndex = -1;
        private int newIndex = -1;

        public Event(int state) {
            this.state = state;
        }

        static Event newOther() {
            return new Event(OTHER);
        }

        static Event newItemMoved(int oldIndex, int newIndex) {
            Event e = new Event(ITEM_MOVED);
            e.oldIndex = oldIndex;
            e.newIndex = newIndex;
            return e;
        }

        static Event newItemChanged(int index) {
            Event e = new Event(ITEM_CHANGED);
            e.index = index;
            return e;
        }

        static Event newItemRemoved(int index) {
            Event e = new Event(ITEM_REMOVED);
            e.index = index;
            e.oldIndex = index;
            return e;
        }

        static Event newItemInserted(int index) {
            Event e = new Event(ITEM_INSERTED);
            e.index = index;
            e.newIndex = index;
            return e;
        }

        public int getState() {
            return state;
        }

        public int getIndex() {
            return index;
        }

        public int getOldIndex() {
            return oldIndex;
        }

        public int getNewIndex() {
            return newIndex;
        }

    }

    private class Itr implements Iterator<ListItem> {

        private Iterator<ListItem> iterator;

        private Itr(Iterator<ListItem> iterator) {
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public ListItem next() {
            return iterator.next();
        }

        @Override
        public void remove() {
            iterator.remove();
            //TODO get index and use Event.newItemRemoved
            notifyListChanged(Event.newOther());
        }
    }

    private List<ListItem> getListForItem(ListItem item) {
        if (!categories.containsKey(item.getCategory())) {
            categories.put(item.getCategory(), new ArrayList<ListItem>());
        }

        return categories.get(item.getCategory());
    }

    public boolean contains(String item) {
        for (ArrayList<ListItem> list : categories.values()) {
            for (ListItem listItem : list) {
                if (item.equalsIgnoreCase(listItem.getDescription())) {
                    return true;
                }
            }
        }
        return false;
    }
}
