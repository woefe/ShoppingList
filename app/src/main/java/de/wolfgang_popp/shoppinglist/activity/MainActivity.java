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
import android.app.FragmentManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.Arrays;

import de.wolfgang_popp.shoppinglist.R;
import de.wolfgang_popp.shoppinglist.dialog.ConfirmationDialog;
import de.wolfgang_popp.shoppinglist.dialog.TextInputDialog;
import de.wolfgang_popp.shoppinglist.shoppinglist.ShoppingListService;

public class MainActivity extends BinderActivity implements ConfirmationDialog.ConfirmationDialogListener, TextInputDialog.Listener {
    public static final String KEY_FRAGMENT = "FRAGMENT";
    public static final String KEY_LIST_NAME = "LIST_NAME";
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private ArrayAdapter<String> drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private Fragment currentFragment;
    private String currentListName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerList = findViewById(R.id.left_drawer);
        drawerAdapter = new ArrayAdapter<>(this, R.layout.drawer_list_item);
        drawerList.setAdapter(drawerAdapter);
        drawerList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectList(position);
            }
        });

        final Toolbar toolbar = findViewById(R.id.toolbar_main);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);

        if (savedInstanceState != null) {
            Fragment fragment = getFragmentManager().getFragment(savedInstanceState, KEY_FRAGMENT);
            String name = savedInstanceState.getString(KEY_LIST_NAME);
            setFragment(fragment, name);
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        drawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getFragmentManager().putFragment(outState, KEY_FRAGMENT, currentFragment);
        outState.putString(KEY_LIST_NAME, currentListName);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onServiceConnected(ShoppingListService.ShoppingListBinder binder) {
        updateDrawer();
        if (currentFragment == null){
            selectList(0);
        }
        if (currentFragment != null && currentFragment instanceof ShoppingListFragment) {
            ((ShoppingListFragment) currentFragment).setShoppingList(binder.getList(currentListName));
        }
    }

    @Override
    protected void onServiceDisconnected(ShoppingListService.ShoppingListBinder binder) {
        drawerAdapter.clear();
    }

    @Override
    public void onBackPressed() {
        if (currentFragment instanceof ShoppingListFragment &&
                !((ShoppingListFragment) currentFragment).onBackPressed()) {

            super.onBackPressed();
        }
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
                String message = getString(R.string.remove_checked_items);
                ConfirmationDialog.show(this, message, R.id.action_delete_checked);
                return true;
            case R.id.action_delete_list:
                message = getString(R.string.confirm_delete_list, getTitle());
                ConfirmationDialog.show(this, message, R.id.action_delete_list);
                return true;
            case R.id.action_new_list:
                message = getString(R.string.add_new_list);
                String hint = getString(R.string.add_list_hint);
                TextInputDialog.show(this, message, hint, R.id.action_new_list);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveButtonClicked(int action) {
        switch (action) {
            case R.id.action_delete_checked:
                if (currentFragment != null && currentFragment instanceof ShoppingListFragment) {
                    ((ShoppingListFragment) currentFragment).removeAllCheckedItems();
                }
                break;
            case R.id.action_delete_list:
                getBinder().removeList(getTitle().toString());
                updateDrawer();
                selectList(0);
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int action) {
    }

    @Override
    public void onInputComplete(String input, int action) {
        if (isServiceConnected()) {
            getBinder().addList(input);
            updateDrawer();
            //TODO get fragmentPos from a more reliable source
            int fragmentPos = Arrays.binarySearch(getBinder().getListNames(), input);
            selectList(fragmentPos);
        }
    }

    private void setFragment(Fragment fragment, String name) {
        this.currentListName = name;
        this.currentFragment = fragment;
        setTitle(name);
        FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    private void selectList(int position) {
        if (position >= getBinder().size()) {
            setFragment(new InvalidFragment(), getString(R.string.app_name));
            return;
        }

        String name = drawerAdapter.getItem(position);
        Fragment fragment = ShoppingListFragment.newInstance(name, getBinder().getList(name));
        setFragment(fragment, name);

        drawerList.setItemChecked(position, true);
        drawerLayout.closeDrawer(drawerList);
    }

    private void updateDrawer() {
        drawerAdapter.clear();
        drawerAdapter.addAll(getBinder().getListNames());
    }

}
