/*
 * ShoppingList - A simple shopping list for Android
 *
 * Copyright (C) 2016  Wolfgang Popp
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

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import de.wolfgang_popp.shoppinglist.R;
import de.wolfgang_popp.shoppinglist.shoppinglist.ListItem;
import de.wolfgang_popp.shoppinglist.shoppinglist.ShoppingList;
import de.wolfgang_popp.shoppinglist.shoppinglist.ShoppingListService;

public class ShoppingListFragment extends Fragment implements EditBar.EditBarListener {
    private static final String KEY_SAVED_SCROLL_POSITION = "SAVED_SCROLL_POSITION";
    private static final String KEY_SAVED_TOP_PADDING = "SAVED_TOP_PADDING";
    private static final String ARG_LIST_NAME = "ARG_LIST_NAME";

    private int savedScrollPosition = 0;
    private int savedTopPadding = 0;

    private EditBar editBar;
    private DynamicListView listView;
    private DynamicListViewAdapter adapter;
    private ShoppingListService.ShoppingListBinder binder;
    private View rootView;
    private ShoppingList shoppingList;
    private String listName;

    private void connectService() {
        if (getActivity() != null && this.binder != null && listName != null) {
            Log.d(getClass().getSimpleName(), "successfully connected to service");

            shoppingList = binder.getList(listName);
            adapter = new DynamicListViewAdapter(getActivity());
            adapter.connectShoppingList(shoppingList);
        }
    }

    private void disconnectService() {
        if (adapter != null) {
            adapter.disconnectShoppingList();
            adapter = null;
        }

        if (binder != null) {
            binder = null;
        }

        if (shoppingList != null) {
            shoppingList = null;
        }
    }

    public static ShoppingListFragment newInstance(String name) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_LIST_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listName = getArguments().getString(ARG_LIST_NAME, "");
        connectService();
    }

    @Override
    public void onDestroy() {
        disconnectService();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);

        listView = rootView.findViewById(R.id.shoppingListView);
        listView.setDragHandler(R.id.dragNDropHandler);
        listView.setAdapter(adapter);
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

    protected void onServiceConnected(ShoppingListService.ShoppingListBinder binder) {
        this.binder = binder;
        connectService();
    }

    protected void onServiceDisconnected(ShoppingListService.ShoppingListBinder binder) {
        disconnectService();
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
