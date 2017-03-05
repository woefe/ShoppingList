/*
 * ShoppingList - A simple shopping list for Android
 *
 * Copyright (C) 2017  Wolfgang Popp
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

package de.wolfgang_popp.shoppinglist.activity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import de.wolfgang_popp.shoppinglist.R;
import de.wolfgang_popp.shoppinglist.shoppinglist.ListItem;
import de.wolfgang_popp.shoppinglist.shoppinglist.ShoppingListService;

public class DynamicListViewAdapter extends BaseAdapter {

    private ShoppingListService.ShoppingListBinder binder;
    private Context context;
    private Map<Object, Integer> idMap = new HashMap<>();

    public DynamicListViewAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        if (binder != null) {
            return binder.getShoppingList().size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (binder != null) {
            return binder.getShoppingList().get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        if (binder != null) {
            return binder.getShoppingList().getId(position);
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        if (convertView != null) {
            view = convertView;
        } else {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item, parent, false);
        }

        TextView description = (TextView) view.findViewById(R.id.text_description);
        TextView quantity = (TextView) view.findViewById(R.id.text_quantity);

        ListItem item = binder.getShoppingList().get(position);
        description.setText(item.getDescription());
        quantity.setText(item.getQuantity());

        if (item.isChecked()) {
            description.setTextColor(context.getResources().getColor(R.color.textColorChecked));
            quantity.setTextColor(context.getResources().getColor(R.color.textColorChecked));
        } else {
            description.setTextColor(context.getResources().getColor(R.color.textColorDefault));
            quantity.setTextColor(context.getResources().getColor(R.color.textColorDefault));
        }

        //view.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public void onBinderDisconnected(ShoppingListService.ShoppingListBinder binder) {
        this.binder = null;
        notifyDataSetChanged();
    }

    public void onBinderConnected(ShoppingListService.ShoppingListBinder binder) {
        this.binder = binder;
        notifyDataSetChanged();
    }

    public void onItemSwap(int startPosition, int endPosition) {
        if (binder != null) {
            binder.getShoppingList().move(startPosition, endPosition);
        }
    }

    /*
        @Override
        public int getDragHandler() {
            return R.id.drag_n_drop_handler;
        }

        @Override
        public void onItemDrag(DragNDropListView parent, View view, int position, long id) {
            dragStartPosition = position;
            startView = view;
        }

        @Override
        public void onItemDrop(DragNDropListView parent, View view, int startPosition, int endPosition, long id) {
            binder.getShoppingList().move(startPosition, endPosition);
            dragStartPosition = -1;
            startView = null;
        }
        */
}
