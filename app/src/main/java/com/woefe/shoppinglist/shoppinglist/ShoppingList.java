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

import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ShoppingList extends ArrayList<ListItem> {

    private String name;
    private static int currentID;
    private final List<ShoppingListListener> listeners = new LinkedList<>();
    private List<String> categories = new ArrayList<>();
    public static final String DEFAULT_CATEGORY = "Allgemein";

    public ShoppingList(String name) {
        super();
        this.name = name;
    }

    public ShoppingList(String name, Collection<ListItem> collection) {
        super(collection);
        this.name = name;
    }

    public void addDefaultCategory() {
        if (categories.isEmpty()) {
            categories.add(DEFAULT_CATEGORY); // todo translate
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyListChanged(Event.newOther());
    }

    public int getId(int index) {
        return ((ListItem.ListItemWithID) get(index)).getId();
    }

    @Override
    public boolean add(ListItem item) {
        boolean res = super.add(new ListItem.ListItemWithID(generateID(), item));
        notifyListChanged(Event.newItemInserted(size() - 1));
        return res;
    }

    public boolean add(String description, String quantity, String category) {
        return add(new ListItem(false, description, quantity, category));
    }

    @Override
    public void add(int index, ListItem element) {
        super.add(index, element);
        notifyListChanged(Event.newItemInserted(index));
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends ListItem> c) {
        boolean b = super.addAll(c);
        notifyListChanged(Event.newOther());
        return b;
    }

    @Override
    public boolean addAll(int index, @NonNull Collection<? extends ListItem> c) {
        boolean b = super.addAll(index, c);
        notifyListChanged(Event.newOther());
        return b;
    }

    @Override
    public ListItem set(int index, ListItem element) {
        ListItem old = super.set(index, element);
        notifyListChanged(Event.newItemChanged(index));
        return old;
    }

    @Override
    public ListItem remove(int index) {
        ListItem res = super.remove(index);
        notifyListChanged(Event.newItemRemoved(index));
        return res;
    }

    @Override
    public boolean remove(Object o) {
        boolean b = super.remove(o);
        if (b) {
            notifyListChanged(Event.newOther());
        }
        return b;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
        notifyListChanged(Event.newOther());
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        boolean b = super.removeAll(c);
        if (b) {
            notifyListChanged(Event.newOther());
        }
        return b;
    }

    @Override
    public boolean removeIf(@NonNull Predicate<? super ListItem> filter) {
        boolean b = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            b = super.removeIf(filter);
        }
        if (b) {
            notifyListChanged(Event.newOther());
        }
        return b;
    }

    @Override
    public void replaceAll(@NonNull UnaryOperator<ListItem> operator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            super.replaceAll(operator);
            notifyListChanged(Event.newOther());
        }
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        boolean b = super.retainAll(c);
        if (b) {
            notifyListChanged(Event.newOther());
        }
        return b;
    }

    @Override
    public void clear() {
        super.clear();
        notifyListChanged(Event.newOther());
    }

    @NonNull
    @Override
    public Iterator<ListItem> iterator() {
        return new Itr(super.iterator());
    }

    @NonNull
    @Override
    public ListIterator<ListItem> listIterator(int index) {
        return new ListItr(super.listIterator(index));
    }

    @NonNull
    @Override
    public ListIterator<ListItem> listIterator() {
        return new ListItr(super.listIterator());
    }

    @Override
    public void sort(Comparator<? super ListItem> c) {
        ListItem[] items = toArray(new ListItem[size()]);
        Arrays.sort(items, c);
        super.clear();
        super.addAll(Arrays.asList(items));
        notifyListChanged(Event.newOther());
    }

    public void setChecked(ListItem item, boolean isChecked, int absolutePosition) {
        get(indexOf(item)).setChecked(isChecked);
        notifyListChanged(Event.newItemChanged(absolutePosition));
    }

    public void toggleChecked(ListItem item, int absolutePosition) {
        setChecked(item, !item.isChecked(), absolutePosition);
    }

    public void move(ListItem from, ListItem to) {
        int oldIndex = indexOf(from);
        int newIndex = indexOf(to);
        
        super.add(newIndex, super.remove(oldIndex));
        notifyListChanged(Event.newItemMoved(oldIndex, newIndex));
    }

    public void editItem(int index, String newDescription, String newQuantity, String newCategory) {
        ListItem listItem = get(index);
        listItem.setDescription(newDescription);
        listItem.setQuantity(newQuantity);
        listItem.setCategory(newCategory);
        notifyListChanged(Event.newItemChanged(index));
    }


    public void removeAllCheckedItems() {
        Iterator<ListItem> it = iterator();

        while (it.hasNext()) {
            ListItem item = it.next();
            if (item.isChecked()) {
                it.remove();
            }
        }
        notifyListChanged(Event.newOther());
    }

    public Set<String> createDescriptionIndex() {
        Set<String> descriptionIndex = new HashSet<>();
        for (ListItem listItem : this) {
            descriptionIndex.add(listItem.getDescription().toLowerCase());
        }
        return descriptionIndex;
    }

    public void addListener(ShoppingListListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ShoppingListListener listener) {
        listeners.remove(listener);
    }

    private synchronized int generateID() {
        return ++currentID;
    }

    public void parseCategories(String line) {
        categories.addAll(Arrays.asList(line.split(":")[1].split(",")));
    }

    public List<String> getCategories() {
        return categories;
    }

    public List<ListItem> getListItemByCategory(String desiredCategory) {
        List<ListItem> list = new ArrayList<>();

        for (int i = 0; i < size(); i++) {
            if (desiredCategory.equals(get(i).getCategory())) {
                list.add(get(i));
            }
        }

        return list;
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

    private class ListItr extends Itr implements ListIterator<ListItem> {

        private ListIterator<ListItem> iterator;

        private ListItr(ListIterator<ListItem> iterator) {
            super(iterator);
            this.iterator = iterator;
        }

        @Override
        public boolean hasPrevious() {
            return iterator.hasPrevious();
        }

        @Override
        public ListItem previous() {
            return iterator.previous();
        }

        @Override
        public int nextIndex() {
            return iterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return iterator.previousIndex();
        }

        @Override
        public void set(ListItem listItem) {
            iterator.set(listItem);
            //TODO get index and use Event.newItemChanged
            notifyListChanged(Event.newOther());
        }

        @Override
        public void add(ListItem listItem) {
            iterator.add(listItem);
            //TODO get index and use Event.newItemInserted
            notifyListChanged(Event.newOther());
        }
    }
}
