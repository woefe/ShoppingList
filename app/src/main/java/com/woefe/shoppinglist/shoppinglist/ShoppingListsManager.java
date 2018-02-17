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

package com.woefe.shoppinglist.shoppinglist;

import android.os.FileObserver;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Wolfgang Popp.
 */
class ShoppingListsManager {
    private static final String TAG = ShoppingListsManager.class.getSimpleName();
    public static final String FILE_ENDING = ".lst";

    private final String directory;
    private final Map<String, ShoppingListMetadata> trashcan = new HashMap<>();
    private final MetadataContainer shoppingListsMetadata = new MetadataContainer();
    private final FileObserver directoryObserver;
    private final List<ListsChangeListener> listeners = new LinkedList<>();

    ShoppingListsManager(final String directory) {
        this.directory = directory;
        this.directoryObserver = new FileObserver(directory) {
            @Override
            public void onEvent(int event, @Nullable String path) {
                if (path == null) {
                    return;
                }
                File file = new File(directory, path);
                switch (event) {
                    case FileObserver.DELETE:
                        shoppingListsMetadata.removeByFile(file.getPath());
                        break;
                    case FileObserver.CREATE:
                        // workaround: When CREATE is triggered, the file might still be empty.
                        SystemClock.sleep(100);
                        loadFromFile(file);
                        break;
                }
            }
        };
    }

    void setListChangeListener(ListsChangeListener listener) {
        this.listeners.add(listener);
    }

    void removeListChangeListenerListener(ListsChangeListener listener) {
        this.listeners.remove(listener);
    }

    void onStart() {
        Log.d(getClass().getSimpleName(), "initializing from dir " + directory);
        maybeAddInitialList();
        loadFromDirectory(directory);
        directoryObserver.startWatching();
    }

    void onStop() {
        listeners.clear();
        directoryObserver.stopWatching();

        for (ShoppingListMetadata metadata : trashcan.values()) {
            metadata.observer.stopWatching();
        }
        for (ShoppingListMetadata metadata : shoppingListsMetadata.values()) {
            metadata.observer.stopWatching();
        }

        try {
            writeAllUnsavedChanges();
        } catch (IOException e) {
            Log.v(getClass().getSimpleName(), "Writing of changes failed", e);
        }

        shoppingListsMetadata.clear();
        trashcan.clear();
    }

    private void maybeAddInitialList() {
        boolean foundFile = false;

        for (File file : new File(directory).listFiles()) {
            foundFile = foundFile || file.isFile();
        }

        if (!foundFile) {
            try {
                addList("Shopping List");
            } catch (ShoppingListException e) {
                Log.e(getClass().getSimpleName(), "Failed to add initial list", e);
            }
        }
    }

    private void loadFromFile(File file) {
        try {
            final ShoppingList list = ShoppingListUnmarshaller.unmarshal(file.getPath());
            addShoppingList(list, file.getPath());
            Log.v(TAG, "successfully loaded file: " + file);
        } catch (IOException | UnmarshallException e) {
            Log.e(getClass().getSimpleName(), "Failed to parse file " + file, e);
        }
    }

    private void loadFromDirectory(String directory) {
        File d = new File(directory);
        for (File file : d.listFiles()) {
            if (file.isFile()) {
                loadFromFile(file);
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
        shoppingListsMetadata.add(metadata);
    }

    private void setupObserver(final ShoppingListMetadata metadata) {
        FileObserver fileObserver = new FileObserver(metadata.filename) {
            @Override
            public void onEvent(int event, String path) {
                switch (event) {
                    case FileObserver.CLOSE_WRITE:
                        try {
                            ShoppingList list = ShoppingListUnmarshaller.unmarshal(metadata.filename);
                            metadata.shoppingList.clear();
                            metadata.shoppingList.addAll(list);
                            metadata.isDirty = false;

                            String oldName = metadata.shoppingList.getName();
                            rename(oldName, list.getName());
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

    private void writeAllUnsavedChanges() throws IOException {
        // first empty trashcan and then write lists. This makes sure that a list that has been
        // removed and was later re-added is not actually deleted.
        for (ShoppingListMetadata metadata : trashcan.values()) {
            new File(metadata.filename).delete();
        }

        for (ShoppingListMetadata metadata : shoppingListsMetadata.values()) {
            if (metadata.isDirty) {
                OutputStream os = new FileOutputStream(metadata.filename);
                ShoppingListMarshaller.marshall(os, metadata.shoppingList);
                Log.d(getClass().getSimpleName(), "Wrote file " + metadata.filename);
            }
        }
    }

    void addList(String name) throws ShoppingListException {
        if (hasList(name)) {
            throw new ShoppingListException("List already exists");
        }

        String filename = new File(this.directory, name + FILE_ENDING).getPath();
        addShoppingList(new ShoppingList(name), filename);
        shoppingListsMetadata.getByName(name).isDirty = true;
    }

    void removeList(String name) {
        ShoppingListMetadata toRemove = shoppingListsMetadata.removeByName(name);
        trashcan.put(toRemove.shoppingList.getName(), toRemove);
    }

    ShoppingList getList(String name) {
        return shoppingListsMetadata.getByName(name).shoppingList;
    }

    Set<String> getListNames() {
        return shoppingListsMetadata.getListNames();
    }

    int size() {
        return shoppingListsMetadata.size();
    }

    boolean hasList(String name) {
        return shoppingListsMetadata.hasName(name);
    }

    void rename(String oldName, String newName) {
        if (!oldName.equals(newName)) {
            ShoppingListMetadata metadata = shoppingListsMetadata.removeByName(oldName);
            metadata.shoppingList.setName(newName);
            shoppingListsMetadata.add(metadata);
        }
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

    private class MetadataContainer {
        private Map<String, ShoppingListMetadata> byName = new HashMap<>();
        private Map<String, String> filenameResolver = new HashMap<>();

        private void add(ShoppingListMetadata metadata) {
            String name = metadata.shoppingList.getName();
            byName.put(name, metadata);
            filenameResolver.put(metadata.filename, name);
            notifyListeners();
        }

        private void clear() {
            filenameResolver.clear();
            byName.clear();
            notifyListeners();
        }

        private ShoppingListMetadata removeByName(String name) {
            ShoppingListMetadata toRemove = byName.remove(name);
            filenameResolver.remove(toRemove.filename);
            notifyListeners();
            return toRemove;
        }

        private ShoppingListMetadata removeByFile(String filename) {
            ShoppingListMetadata toRemove = byName.remove(filenameResolver.remove(filename));
            notifyListeners();
            return toRemove;
        }

        private ShoppingListMetadata getByName(String name) {
            return byName.get(name);
        }

        private ShoppingListMetadata getByFile(String filename) {
            return getByName(filenameResolver.get(filename));
        }

        private boolean hasName(String name) {
            return byName.containsKey(name);
        }

        private Collection<ShoppingListMetadata> values() {
            return byName.values();
        }

        private Set<String> getListNames() {
            return byName.keySet();
        }

        private int size() {
            return byName.size();
        }

        private void notifyListeners() {
            for (ListsChangeListener listener : listeners) {
                listener.onListsChanged();
            }
        }
    }
}
