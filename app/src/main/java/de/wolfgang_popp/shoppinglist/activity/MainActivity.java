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

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import de.wolfgang_popp.shoppinglist.R;
import de.wolfgang_popp.shoppinglist.dialog.ConfirmationDialog;
import de.wolfgang_popp.shoppinglist.shoppinglist.ListChangedListener;
import de.wolfgang_popp.shoppinglist.shoppinglist.ListItem;
import de.wolfgang_popp.shoppinglist.shoppinglist.ShoppingListService;

public class MainActivity extends BinderActivity implements EditBar.EditBarListener, ConfirmationDialog.ConfirmationDialogListener {
    private static final String KEY_SAVED_SCROLL_POSITION = "SAVED_SCROLL_POSITION";
    private static final String KEY_SAVED_TOP_PADDING = "SAVED_TOP_PADDING";
    private EditBar editBar;
    private DynamicListView listView;
    private int savedScrollPosition = 0;
    private int savedTopPadding = 0;

    private final DynamicListViewAdapter adapter = new DynamicListViewAdapter(this);

    private final ListChangedListener listener = new ListChangedListener() {
        @Override
        public void listChanged() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.notifyDataSetChanged();
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = (DynamicListView) findViewById(R.id.shoppingListView);
        listView.setDragHandler(R.id.dragNDropHandler);
        listView.setAdapter(adapter);
        registerForContextMenu(listView);

        editBar = new EditBar(this);
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
                if (isServiceConnected()) {
                    getBinder().toggleItemChecked(position);
                }
            }
        });
    }

    @Override
    protected void onServiceConnected(ShoppingListService.ShoppingListBinder binder) {
        binder.getShoppingList().addListChangeListener(listener);
        adapter.onBinderConnected(binder);
    }

    @Override
    protected void onServiceDisconnected(ShoppingListService.ShoppingListBinder binder) {
        adapter.onBinderDisconnected(binder);
        binder.getShoppingList().removeListChangeListener(listener);
    }

    @Override
    public void onBackPressed() {
        if (editBar.isVisible()) {
            editBar.hide();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ListView listView = (ListView) findViewById(R.id.shoppingListView);
        View v = listView.getChildAt(0);
        int savedScrollPosition = listView.getFirstVisiblePosition();
        int savedTopPadding = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
        outState.putInt(KEY_SAVED_SCROLL_POSITION, savedScrollPosition);
        outState.putInt(KEY_SAVED_TOP_PADDING, savedTopPadding);
        editBar.saveState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_delete_checked:
                ConfirmationDialog.show(this, getString(R.string.remove_checked_items));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.context_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.context_menu_edit:
                ListItem listItem = getShoppingList().get(info.position);
                editBar.showEdit(info.position, listItem.getDescription(), listItem.getQuantity());
                return true;
            case R.id.context_menu_delete:
                getBinder().removeItem(info.position);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onEditSave(int position, String description, String quantity) {
        getBinder().edit(position, description, quantity);
        editBar.hide();
        listView.smoothScrollToPosition(position);
    }

    @Override
    public void onNewItem(String description, String quantity) {
        getBinder().addItem(description, quantity);
        listView.smoothScrollToPosition(listView.getAdapter().getCount() - 1);
    }

    @Override
    public void onPositiveButtonClicked() {
        getBinder().removeAllCheckedItems();
    }

    @Override
    public void onNegativeButtonClicked() {
    }

}
