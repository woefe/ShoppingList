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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.ListView;

import de.wolfgang_popp.shoppinglist.R;
import de.wolfgang_popp.shoppinglist.dialog.ConfirmationDialog;
import de.wolfgang_popp.shoppinglist.shoppinglist.ListChangedListener;
import de.wolfgang_popp.shoppinglist.shoppinglist.ListItem;
import de.wolfgang_popp.shoppinglist.shoppinglist.ShoppingListService;

public class MainActivity extends AppCompatActivity implements EditBar.EditBarListener, ConfirmationDialog.ConfirmationDialogListener {
    private static final String KEY_SAVED_SCROLL_POSITION = "SAVED_SCROLL_POSITION";
    private static final String KEY_SAVED_TOP_PADDING = "SAVED_TOP_PADDING";
    private final ShoppingListServiceConnection serviceConnection = new ShoppingListServiceConnection();
    private ShoppingListService.ShoppingListBinder binder;
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
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ShoppingListService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editBar = new EditBar(this);
        editBar.addEditBarListener(this);

        if (savedInstanceState != null) {
            savedScrollPosition = savedInstanceState.getInt(KEY_SAVED_SCROLL_POSITION);
            savedTopPadding = savedInstanceState.getInt(KEY_SAVED_TOP_PADDING);
            editBar.restoreState(savedInstanceState);
        }

    }

    private void buildView() {
        listView = (DynamicListView) findViewById(R.id.shoppingListView);
        registerForContextMenu(listView);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        if (Build.VERSION.SDK_INT < 21) {
            listView.setSelection(savedScrollPosition);
        } else {
            listView.setSelectionFromTop(savedScrollPosition, savedTopPadding);
        }

        final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            private final int slop = ViewConfiguration.get(MainActivity.this).getScaledPagingTouchSlop();

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                final float start = e1.getY();
                final float end = e2.getY();

                if (end - start > slop) {
                    editBar.showFAB();
                } else if (end - start < -slop) {
                    editBar.hideFAB();
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }
        };

        final GestureDetector detector = new GestureDetector(MainActivity.this, gestureListener);

        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                binder.toggleItemChecked(position);
            }
        });

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
    protected void onStop() {
        super.onStop();
        unbindService(serviceConnection);
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
                ListItem listItem = binder.getShoppingList().get(info.position);
                editBar.showEdit(info.position, listItem.getDescription(), listItem.getQuantity());
                return true;
            case R.id.context_menu_delete:
                binder.removeItem(info.position);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onEditSave(int position, String description, String quantity) {
        binder.edit(position, description, quantity);
        editBar.hide();
        listView.smoothScrollToPosition(position);
    }

    @Override
    public void onNewItem(String description, String quantity) {
        binder.addItem(description, quantity);
        listView.smoothScrollToPosition(listView.getAdapter().getCount() - 1);
    }

    @Override
    public void onPositiveButtonClicked() {
        binder.removeAllCheckedItems();
    }

    @Override
    public void onNegativeButtonClicked() {
    }

    private class ShoppingListServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            binder = ((ShoppingListService.ShoppingListBinder) iBinder);
            binder.getShoppingList().addListChangeListener(listener);
            adapter.onBinderConnected(binder);
            buildView();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            adapter.onBinderDisconnected(binder);
            binder.getShoppingList().removeListChangeListener(listener);
            binder = null;
        }
    }
}
