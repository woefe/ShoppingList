package de.wolfgang_popp.shoppinglist.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import de.wolfgang_popp.shoppinglist.R;
import de.wolfgang_popp.shoppinglist.dialog.ActionDialog;
import de.wolfgang_popp.shoppinglist.dialog.AddItemDialog;
import de.wolfgang_popp.shoppinglist.dialog.EditDialog;
import de.wolfgang_popp.shoppinglist.shoppinglist.ListChangedListener;
import de.wolfgang_popp.shoppinglist.shoppinglist.ListItem;
import de.wolfgang_popp.shoppinglist.shoppinglist.ShoppingList;
import de.wolfgang_popp.shoppinglist.shoppinglist.ShoppingListService;

public class MainActivity extends AppCompatActivity implements EditDialog.EditDialogListener, ActionDialog.ActionDialogListener, AddItemDialog.AddDialogListener {
    private ShoppingListServiceConnection serviceConnection = new ShoppingListServiceConnection();
    private ShoppingListService.ShoppingListBinder binder;

    private ShoppingListAdapter adapter = new ShoppingListAdapter();

    private ListChangedListener listener = new ListChangedListener() {
        @Override
        public void listChanged(ShoppingList shoppingList) {
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
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ShoppingListService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEditSave(int position, String description, String quantity) {
        binder.edit(position, description, quantity);
    }

    @Override
    public void onRemoveItemSelected(int position) {
        binder.removeItem(position);
    }

    @Override
    public void onEditItemSelected(int position) {
        ListItem item = binder.getShoppingList().get(position);
        EditDialog.show(this, position, item.getDescription(), item.getQuantity());
    }

    @Override
    public void onAddNewItem(String description, String quantity) {
        binder.addItem(description, quantity);
    }

    private class ShoppingListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return binder.getShoppingList().size();
        }

        @Override
        public Object getItem(int position) {
            return binder.getShoppingList().get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view;
            if (convertView != null) {
                view = convertView;
            } else {
                LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.list_item, parent, false);
            }

            TextView description = (TextView) view.findViewById(R.id.text_description);
            TextView quantity = (TextView) view.findViewById(R.id.text_quantity);

            ListItem item = binder.getShoppingList().get(position);
            description.setText(item.getDescription());
            quantity.setText(item.getQuantity());

            if (item.isChecked()) {
                description.setTextColor(getResources().getColor(R.color.textColorChecked));
                quantity.setTextColor(getResources().getColor(R.color.textColorChecked));
            } else {
                description.setTextColor(getResources().getColor(R.color.textColorDefault));
                quantity.setTextColor(getResources().getColor(R.color.textColorDefault));
            }

            return view;
        }
    }

    private class ShoppingListServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            binder = ((ShoppingListService.ShoppingListBinder) iBinder);
            binder.getShoppingList().addListChangeListener(listener);

            ListView listView = (ListView) findViewById(R.id.shoppingListView);
            final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add);

            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AddItemDialog.show(MainActivity.this);
                }
            });

            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                private int oldFirstVisibleItem = 0;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    return;
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    if (firstVisibleItem == 0) {
                        fab.show();
                    }
                    if (oldFirstVisibleItem != firstVisibleItem) {
                        if (oldFirstVisibleItem < firstVisibleItem) {
                            fab.hide();
                        } else {
                            fab.show();
                        }
                        oldFirstVisibleItem = firstVisibleItem;
                    }

                }
            });

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    binder.toggleItemChecked(position);
                }
            });
            listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    ActionDialog.show(MainActivity.this, position);
                    return true;
                }
            });

            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            binder.getShoppingList().removeListChangeListener(listener);
            binder = null;
        }
    }
}
