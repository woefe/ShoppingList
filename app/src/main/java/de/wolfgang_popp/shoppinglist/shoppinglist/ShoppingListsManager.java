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

package de.wolfgang_popp.shoppinglist.shoppinglist;

import android.os.FileObserver;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Wolfgang Popp.
 */
public class ShoppingListsManager {
    private static final String TAG = ShoppingListsManager.class.getSimpleName();

    private String directory;
    private final Map<String, ShoppingListMetadata> shoppingListsMetadata = new HashMap<>();
    private final Map<String, ShoppingListMetadata> trashcan = new HashMap<>();

    ShoppingListsManager() {
    }

    void init(String directory) throws IOException, UnmarshallException {
        Log.d(getClass().getSimpleName(), "initializing from dir " + directory);
        shoppingListsMetadata.clear();
        setDirectory(directory);
        maybeAddInitialList();
        loadFromDirectory(directory);
    }

    private void setDirectory(final String directory) throws IOException {
        if (!new File(directory).isDirectory()) {
            throw new IOException(directory + " is not a directory");
        }
        this.directory = directory;
    }

    private void maybeAddInitialList() {
        boolean foundFile = false;

        for (File file : new File(directory).listFiles()) {
            foundFile = foundFile || file.isFile();
        }

        if (!foundFile) {
            addList("Shopping List");
        }
    }

    private void loadFromDirectory(String directory) throws IOException, UnmarshallException {
        File d = new File(directory);
        for (File file : d.listFiles()) {
            if (file.isFile()) {
                final ShoppingList list = ShoppingListUnmarshaller.unmarshal(file.getPath());
                Log.d(getClass().getSimpleName(), "Reading file " + file);
                addShoppingList(list, file.getPath());
            }
        }

    }

    private void addShoppingList(ShoppingList list, String filename) {
        final ShoppingListMetadata metadata = new ShoppingListMetadata(list, filename);
        list.addListener(new ShoppingList.ShoppingListListener() {
            @Override
            public void update() {
                metadata.isDirty = true;
            }
        });
        setupObserver(metadata);
        shoppingListsMetadata.put(metadata.shoppingList.getName(), metadata);
    }

    private void setupObserver(final ShoppingListMetadata metadata) {
        FileObserver fileObserver = new FileObserver(metadata.filename) {
            @Override
            public void onEvent(int event, String path) {
                switch (event) {
                    case FileObserver.CLOSE_WRITE:
                    case FileObserver.CREATE:
                    case FileObserver.ATTRIB:
                        try {
                            ShoppingList list = ShoppingListUnmarshaller.unmarshal(metadata.filename);
                            metadata.shoppingList.clear();
                            metadata.shoppingList.addAll(list);
                            //TODO metadata.shoppingList.setName(list.getName());
                            metadata.isDirty = false;
                        } catch (IOException | UnmarshallException e) {
                            Log.e(TAG, "FileObserver could not read file.", e);
                        }
                        break;
                }
            }
        };
        fileObserver.startWatching();
        metadata.observer = fileObserver;
    }

    void writeAllUnsavedChanges() throws IOException {
        for (ShoppingListMetadata metadata : shoppingListsMetadata.values()) {
            if (metadata.isDirty) {
                OutputStream os = new FileOutputStream(metadata.filename);
                ShoppingListMarshaller.marshall(os, metadata.shoppingList);
                Log.d(getClass().getSimpleName(), "Wrote file " + metadata.filename);
            }
        }

        for (ShoppingListMetadata metadata : trashcan.values()) {
            new File(metadata.filename).delete();
        }
    }

    void addList(String name) {
        String filename = new File(this.directory, name + ".lst").getPath();
        addShoppingList(new ShoppingList(name), filename);
        shoppingListsMetadata.get(name).isDirty = true;
    }

    void removeList(String name) {
        ShoppingListMetadata toRemove = shoppingListsMetadata.remove(name);
        trashcan.put(toRemove.shoppingList.getName(), toRemove);
    }

    ShoppingList getList(String name) {
        return shoppingListsMetadata.get(name).shoppingList;
    }

    Set<String> getListNames() {
        return shoppingListsMetadata.keySet();
    }

    int size() {
        return shoppingListsMetadata.size();
    }

    private class ShoppingListMetadata {
        private final ShoppingList shoppingList;
        private final String filename;
        private boolean isDirty;
        private FileObserver observer;

        private ShoppingListMetadata(ShoppingList shoppingList, String filename) {
            this.shoppingList = shoppingList;
            this.filename = filename;
            this.isDirty = false;
        }
    }
}
