package de.wolfgang_popp.shoppinglist.shoppinglist;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import de.wolfgang_popp.shoppinglist.activity.SettingsFragment;

/**
 * @author Wolfgang Popp.
 */
public class ShoppingListService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = ShoppingListService.class.getSimpleName();

    public static final String DEFAULT_FILENAME = "ShoppingList.lst";

    private ShoppingList shoppingList = null;
    private IBinder binder = new ShoppingListBinder();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "onBind() called: " + intent.toString());
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        try {
            shoppingList.writeIfDirty();
        } catch (IOException e) {
            Toast.makeText(ShoppingListService.this, "An error occurred while saving.", Toast.LENGTH_SHORT).show();
        }
        Log.v(TAG, "onUnbind() called: " + intent.toString());
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String filename = sharedPreferences.getString(SettingsFragment.KEY_FILE_LOCATION, "");
        File newFile = new File(filename);
        try {
            newFile.createNewFile();
            shoppingList.changeFile(filename);
        } catch (IOException e) {
            Toast.makeText(ShoppingListService.this, "Cannot create file: " + filename, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String filename = sharedPreferences.getString(SettingsFragment.KEY_FILE_LOCATION, "");
        String defaultFilename = getApplicationContext().getFileStreamPath(DEFAULT_FILENAME).getAbsolutePath();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        if (filename.equals("")) {
            filename = defaultFilename;
        }

        File listFile = new File(filename);
        try {
            listFile.createNewFile();
            shoppingList = new ShoppingList(filename);
        } catch (IOException e) {
            Toast.makeText(ShoppingListService.this, "Cannot create file: " + filename, Toast.LENGTH_LONG).show();
        }

        if (shoppingList == null) {
            try {
                shoppingList = new ShoppingList(defaultFilename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Log.v(TAG, "onCreate() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).unregisterOnSharedPreferenceChangeListener(this);
        try {
            shoppingList.writeIfDirty();
        } catch (IOException e) {
            Toast.makeText(ShoppingListService.this, "An error occurred while saving.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "onStartCommand() called");
        return START_NOT_STICKY;
    }

    public class ShoppingListBinder extends Binder {
        public void addItem(String description, String quantity) {
            shoppingList.add(new ListItem(false, description, quantity));
        }

        public void toggleItemChecked(int index) {
            boolean isChecked = shoppingList.get(index).isChecked();
            shoppingList.setChecked(index, !isChecked);
        }

        public void removeItem(int index) {
            shoppingList.remove(index);
        }

        public void removeAllCheckedItems() {
            shoppingList.removeAllCheckedItems();
        }

        public void edit(int index, String newDescription, String newQuantity) {
            shoppingList.editItem(index, newDescription, newQuantity);
        }

        public ShoppingList getShoppingList() {
            return shoppingList;
        }
    }
}
