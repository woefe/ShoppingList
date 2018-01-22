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

package de.wolfgang_popp.shoppinglist.shoppinglist;

import android.os.Build;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

public class ShoppingList extends ArrayList<ListItem> {

    private String name;
    private static int currentID;
    private final List<ShoppingListListener> listeners = new LinkedList<>();

    public ShoppingList(String name) {
        super();
        this.name = name;
    }

    public ShoppingList(String name, Collection<ListItem> collection) {
        super(collection);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyListChanged();
    }

    public int getId(int index) {
        return ((ListItem.ListItemWithID) get(index)).getId();
    }

    @Override
    public boolean add(ListItem item) {
        boolean res = super.add(new ListItem.ListItemWithID(generateID(), item));
        notifyListChanged();
        return res;
    }

    public boolean add(String description, String quantity) {
        return add(new ListItem(false, description, quantity));
    }

    @Override
    public ListItem remove(int index) {
        ListItem res = super.remove(index);
        notifyListChanged();
        return res;
    }

    @Override
    public boolean remove(Object o) {
        boolean b = super.remove(o);
        if (b) {
            notifyListChanged();
        }
        return b;
    }

    @Override
    protected void removeRange(int fromIndex, int toIndex) {
        super.removeRange(fromIndex, toIndex);
        notifyListChanged();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean b = super.removeAll(c);
        if (b) {
            notifyListChanged();
        }
        return b;
    }

    @Override
    public boolean removeIf(Predicate<? super ListItem> filter) {
        boolean b = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            b = super.removeIf(filter);
        }
        if (b) {
            notifyListChanged();
        }
        return b;
    }

    @Override
    public void replaceAll(UnaryOperator<ListItem> operator) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            super.replaceAll(operator);
            notifyListChanged();
        }
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        boolean b = super.retainAll(c);
        if (b) {
            notifyListChanged();
        }
        return b;
    }

    @Override
    public boolean addAll(Collection<? extends ListItem> c) {
        boolean b = super.addAll(c);
        notifyListChanged();
        return b;
    }

    @Override
    public boolean addAll(int index, Collection<? extends ListItem> c) {
        boolean b = super.addAll(index, c);
        notifyListChanged();
        return b;
    }

    @NonNull
    @Override
    public Iterator<ListItem> iterator() {
        return super.iterator();
    }

    @Override
    public void clear() {
        super.clear();
        notifyListChanged();
    }

    public void setChecked(int index, boolean isChecked) {
        get(index).setChecked(isChecked);
        notifyListChanged();
    }

    public void toggleChecked(int index) {
        setChecked(index, !get(index).isChecked());
    }

    public void move(int oldIndex, int newIndex) {
        super.add(newIndex, super.remove(oldIndex));
        notifyListChanged();
    }

    public void editItem(int index, String newDescription, String newQuantity) {
        ListItem listItem = get(index);
        listItem.setDescription(newDescription);
        listItem.setQuantity(newQuantity);
        notifyListChanged();
    }


    public void removeAllCheckedItems() {
        Iterator<ListItem> it = iterator();

        while (it.hasNext()) {
            ListItem item = it.next();
            if (item.isChecked()) {
                it.remove();
            }
        }
        notifyListChanged();
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

    private void notifyListChanged() {
        for (ShoppingListListener listener : listeners) {
            listener.update();
        }
    }

    public interface ShoppingListListener {
        void update();
    }
}
