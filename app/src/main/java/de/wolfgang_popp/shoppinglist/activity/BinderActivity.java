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

package de.wolfgang_popp.shoppinglist.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import de.wolfgang_popp.shoppinglist.shoppinglist.ShoppingListService;

/**
 * @author Wolfgang Popp
 */
public abstract class BinderActivity extends AppCompatActivity {

    private final ShoppingListServiceConnection serviceConnection = new ShoppingListServiceConnection();
    private ShoppingListService.ShoppingListBinder binder = null;

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, ShoppingListService.class);
        startService(intent);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    protected void onStop() {
        unbindService(serviceConnection);
        super.onStop();
    }

    public boolean isServiceConnected() {
        return binder != null;
    }

    protected ShoppingListService.ShoppingListBinder getBinder() {
        return binder;
    }

    protected abstract void onServiceConnected(ShoppingListService.ShoppingListBinder binder);

    protected abstract void onServiceDisconnected(ShoppingListService.ShoppingListBinder binder);

    private class ShoppingListServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder iBinder) {
            binder = ((ShoppingListService.ShoppingListBinder) iBinder);
            BinderActivity.this.onServiceConnected(binder);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            BinderActivity.this.onServiceDisconnected(binder);
            binder = null;
        }
    }
}
