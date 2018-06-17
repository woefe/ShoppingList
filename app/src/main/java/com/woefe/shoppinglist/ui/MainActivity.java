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

package com.woefe.shoppinglist.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.util.LongSparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.woefe.shoppinglist.R;
import com.woefe.shoppinglist.ShoppingListApp;
import com.woefe.shoppinglist.db.ShoppingListDatabase;
import com.woefe.shoppinglist.db.entity.ItemEntity;
import com.woefe.shoppinglist.db.entity.ShoppingListEntity;
import com.woefe.shoppinglist.ui.dialog.ConfirmationDialog;
import com.woefe.shoppinglist.ui.dialog.TextInputDialog;
import com.woefe.shoppinglist.viewmodel.ShoppingListsViewModel;
import com.woefe.shoppinglist.webdav.ShoppingListMarshaller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements
        ConfirmationDialog.ConfirmationDialogListener, TextInputDialog.TextInputDialogListener {

    private static final String KEY_LIST_ID = "LIST_ID";
    private DrawerLayout drawerLayout;
    private ListView drawerList;
    private LinearLayout drawerContainer;
    private DrawerAdapter drawerAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private Fragment currentFragment;
    private long currentListId;
    private ShareActionProvider actionProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        drawerContainer = findViewById(R.id.nav_drawer_container);
        drawerList = findViewById(R.id.nav_drawer_content);
        drawerAdapter = new DrawerAdapter(this);
        drawerList.setAdapter(drawerAdapter);
        drawerList.setOnItemClickListener((parent, view, position, id) -> selectList(position));

        final Toolbar toolbar = findViewById(R.id.toolbar_main);
        toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(drawerToggle);

        if (savedInstanceState != null) {
            long id = savedInstanceState.getLong(KEY_LIST_ID);
            ShoppingListFragment fragment = ShoppingListFragment.forList(id);
            setFragment(fragment, id);
        }

        ShoppingListsViewModel model = ViewModelProviders.of(this)
                .get(ShoppingListsViewModel.class);

        subscribe(model);

    }

    private void subscribe(ShoppingListsViewModel model) {
        final Observer<List<ShoppingListEntity>> listsObserver = shoppingListEntities -> {
            if (shoppingListEntities != null) {
                drawerAdapter.clear();
                drawerAdapter.setLists(shoppingListEntities);
                updateDrawer();
            }
        };

        model.getLists().observe(this, listsObserver);
    }

    void updateDrawer() {
        int fragmentPos = drawerAdapter.getPosition(currentListId);
        if (fragmentPos >= 0) {
            drawerList.setItemChecked(fragmentPos, true);
        }
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onPostCreate(savedInstanceState, persistentState);
        drawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(KEY_LIST_ID, currentListId);
        super.onSaveInstanceState(outState);
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
        MenuItem item = menu.findItem(R.id.action_share);
        actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        return true;
    }

    private ShoppingListDatabase getDatabase() {
        return ((ShoppingListApp) getApplication()).getDatabase();
    }

    private void doShare() {
        String text;
        LiveData<List<ItemEntity>> allItemsOfList = getDatabase().itemDAO()
                .getAllItemsOfList(currentListId);

        List<ItemEntity> list = allItemsOfList.getValue();

        if (list == null) {
            Toast.makeText(this, R.string.err_share_list, Toast.LENGTH_LONG).show();
            return;
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ShoppingListMarshaller.marshall(outputStream, getName(currentListId), list);
            text = outputStream.toString();
        } catch (IOException ignored) {
            return;
        }

        Intent intent = new Intent()
                .setAction(Intent.ACTION_SEND)
                .putExtra(Intent.EXTRA_TEXT, text)
                .setType("text/plain");
        if (actionProvider != null) {
            actionProvider.setShareIntent(intent);
        }
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_delete_checked:
                String message = getString(R.string.remove_checked_items);
                ConfirmationDialog.show(this, message, R.id.action_delete_checked);
                return true;
            case R.id.action_delete_list:
                message = getString(R.string.confirm_delete_list, getTitle());
                if (getDatabase().shoppingListDAO().getListById(currentListId).getValue() != null) {
                    ConfirmationDialog.show(this, message, R.id.action_delete_list);
                } else {
                    Toast.makeText(this, R.string.err_cannot_delete_list, Toast.LENGTH_LONG).show();
                }
                return true;
            case R.id.action_new_list:
                NewListDialog.Builder builder = new TextInputDialog.Builder(this, NewListDialog.class);
                builder.setAction(R.id.action_new_list)
                        .setMessage(R.string.add_new_list)
                        .setHint(R.string.add_list_hint)
                        .show();
                return true;
            case R.id.action_view_about:
                Intent intent = new Intent(this, AboutActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_share:
                doShare();
                return true;
            case R.id.action_sort_a_to_z:
                sort(true);
                return true;
            case R.id.action_sort_z_to_a:
                sort(false);
                return true;
            case R.id.action_sort_by_checked_asc:
                sortByChecked(false);
                return true;
            case R.id.action_sort_by_checked_desc:
                sortByChecked(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sort(final boolean ascending) {
        // TODO
//        ShoppingList list = getBinder().getList(currentListId);
//        if (list == null) {
//            return;
//        }
//        list.sort(new Comparator<ListItem>() {
//            @Override
//            public int compare(ListItem o1, ListItem o2) {
//                int i = o1.getDescription().compareToIgnoreCase(o2.getDescription());
//                return i * (ascending ? 1 : -1);
//            }
//        });
    }

    private void sortByChecked(final boolean checkedFirst) {
        //TODO
//        ShoppingList list = getBinder().getList(currentListId);
//        if (list == null) {
//            return;
//        }
//        list.sort(new Comparator<ListItem>() {
//            @Override
//            public int compare(ListItem o1, ListItem o2) {
//                if (o1.isChecked() && !o2.isChecked()) {
//                    return checkedFirst ? 1 : -1;
//                }
//                if (!o1.isChecked() && o2.isChecked()) {
//                    return checkedFirst ? -1 : 1;
//                }
//                return o1.getDescription().compareToIgnoreCase(o2.getDescription());
//            }
//        });

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
                ShoppingListEntity toDelete = new ShoppingListEntity();
                toDelete.setId(currentListId);
                boolean success = getDatabase().shoppingListDAO().deleteLists(toDelete) != 0;
                if (!success) {
                    Toast.makeText(this, R.string.err_cannot_delete_list, Toast.LENGTH_LONG).show();
                } else {
                    selectList(0);
                }
                break;
        }
    }

    @Override
    public void onNegativeButtonClicked(int action) {
    }

    @Override
    public void onInputComplete(String input, int action) {
        ShoppingListEntity newList = new ShoppingListEntity();
        newList.setName(input);
        newList.setModifyTime(System.currentTimeMillis());
        long[] ids = getDatabase().shoppingListDAO().insertLists();
        selectList(drawerAdapter.getPosition(ids[0]));
    }

    private void setFragment(Fragment fragment, long listId) {
        setFragment(fragment, listId, getName(listId));
    }

    private void setFragment(Fragment fragment, long listId, String name) {
        this.currentListId = listId;
        this.currentFragment = fragment;
        setTitle(name);
        updateDrawer();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    private String getName(long listId) {
        return drawerAdapter.getItem(drawerAdapter.getPosition(listId)).getName();
    }

    private void selectList(int position) {
        if (position >= drawerAdapter.getCount()) {
            setFragment(new InvalidFragment(), -1, getString(R.string.app_name));
            return;
        }

        long listId = drawerAdapter.getItem(position).getId();
        Fragment fragment = ShoppingListFragment.forList(listId);
        setFragment(fragment, listId);

        drawerLayout.closeDrawer(drawerContainer);
    }

    public static class NewListDialog extends TextInputDialog {
        @Override
        public boolean onValidateInput(String input) {
            MainActivity activity = (MainActivity) getActivity();

            if (input == null || input.equals("")) {
                Toast.makeText(activity, R.string.error_list_name_empty, Toast.LENGTH_SHORT).show();
                return false;
            }

            /*
            assert activity != null;
            if (!activity.isServiceConnected() || activity.getBinder().hasName(input)) {
                Toast.makeText(activity, R.string.error_list_exists, Toast.LENGTH_SHORT).show();
                return false;
            }
            */

            return true;
        }
    }

    private class DrawerAdapter extends BaseAdapter {
        private List<ShoppingListEntity> lists = new ArrayList<>();
        private LongSparseArray<Integer> idMap = new LongSparseArray<>(); // map<listId, position>
        private Context ctx;

        DrawerAdapter(@NonNull Context context) {
            this.ctx = context;
        }

        public void setLists(List<ShoppingListEntity> lists) {
            for (int i = 0; i < lists.size(); i++) {
                ShoppingListEntity shoppingListEntity = lists.get(i);
                this.lists.add(shoppingListEntity);
                this.idMap.put(shoppingListEntity.getId(), i);
            }
        }

        @Override
        public int getCount() {
            return lists.size();
        }

        @Override
        public ShoppingListEntity getItem(int position) {
            return lists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).getId();
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        public int getPosition(long listId) {
            Integer integer = idMap.get(listId);
            return integer == null ? 0 : integer;
        }

        public void clear() {
            lists.clear();
            idMap.clear();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater =
                    (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view;
            if (convertView == null) {
                view = inflater.inflate(R.layout.drawer_list_item, null, true);
            } else {
                view = convertView;
            }

            TextView text = view.findViewById(R.id.drawer_text);
            text.setText(getItem(position).getName());
            return view;
        }
    }
}
