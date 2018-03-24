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

package com.woefe.shoppinglist.activity;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.woefe.shoppinglist.R;
import com.woefe.shoppinglist.shoppinglist.ListItem;
import com.woefe.shoppinglist.shoppinglist.ShoppingList;

public class ShoppingListFragment extends Fragment implements EditBar.EditBarListener {
    private static final String KEY_SAVED_SCROLL_POSITION = "SAVED_SCROLL_POSITION";
    private static final String KEY_SAVED_TOP_PADDING = "SAVED_TOP_PADDING";

    private int savedScrollPosition = 0;
    private int savedTopPadding = 0;

    private EditBar editBar;
    private DynamicListView listView;
    private DynamicListViewAdapter adapter;
    private View rootView;
    private ShoppingList shoppingList;

    public static ShoppingListFragment newInstance(ShoppingList shoppingList) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        fragment.setShoppingList(shoppingList);
        return fragment;
    }

    public void setShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
        connectList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);

        listView = rootView.findViewById(R.id.shoppingListView);
        listView.setDragHandler(R.id.dragNDropHandler);
        registerForContextMenu(listView);

        editBar = new EditBar(rootView, getActivity());
        editBar.addEditBarListener(this);
        editBar.enableAutoHideFAB(listView);

        if (savedInstanceState != null) {
            savedScrollPosition = savedInstanceState.getInt(KEY_SAVED_SCROLL_POSITION);
            savedTopPadding = savedInstanceState.getInt(KEY_SAVED_TOP_PADDING);
            editBar.restoreState(savedInstanceState);
        }

        if (Build.VERSION.SDK_INT < 21) {
            listView.setSelection(savedScrollPosition);
        } else {
            listView.setSelectionFromTop(savedScrollPosition, savedTopPadding);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                shoppingList.toggleChecked(position);
            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        editBar.removeEditBarListener(this);
        editBar.hide();
        super.onDestroyView();
    }

    private void connectList() {
        if (shoppingList != null && adapter != null) {
            adapter.connectShoppingList(shoppingList);
        }
        if (shoppingList != null && editBar != null) {
            editBar.connectShoppingList(shoppingList);
        }
    }

    @Override
    public void onStop() {
        adapter.disconnectShoppingList();
        editBar.disconnectShoppingList();
        super.onStop();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new DynamicListViewAdapter(getActivity());
        connectList();
        listView.setAdapter(adapter);
    }

    public boolean onBackPressed() {
        if (editBar.isVisible()) {
            editBar.hide();
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ListView listView = rootView.findViewById(R.id.shoppingListView);
        View v = listView.getChildAt(0);
        int savedScrollPosition = listView.getFirstVisiblePosition();
        int savedTopPadding = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
        outState.putInt(KEY_SAVED_SCROLL_POSITION, savedScrollPosition);
        outState.putInt(KEY_SAVED_TOP_PADDING, savedTopPadding);
        editBar.saveState(outState);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.context_menu_edit:
                ListItem listItem = shoppingList.get(info.position);
                editBar.showEdit(info.position, listItem.getDescription(), listItem.getQuantity());
                return true;
            case R.id.context_menu_delete:
                shoppingList.remove(info.position);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onEditSave(int position, String description, String quantity) {
        shoppingList.editItem(position, description, quantity);
        editBar.hide();
        listView.smoothScrollToPosition(position);
    }

    @Override
    public void onNewItem(String description, String quantity) {
        shoppingList.add(description, quantity);
        listView.smoothScrollToPosition(listView.getAdapter().getCount() - 1);
    }

    public void removeAllCheckedItems() {
        shoppingList.removeAllCheckedItems();
    }
}
