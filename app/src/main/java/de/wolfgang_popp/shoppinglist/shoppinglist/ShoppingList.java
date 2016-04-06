package de.wolfgang_popp.shoppinglist.shoppinglist;

import android.os.FileObserver;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Wolfgang Popp.
 */
public class ShoppingList {
    private static final String TAG = ShoppingList.class.getSimpleName();
    private static final Pattern EMPTY_LINE = Pattern.compile("^\\s*$");
    private static final Pattern HEADER = Pattern.compile("\\[(.*)\\]");

    private final List<ListChangedListener> listeners = new LinkedList<>();
    private FileObserver fileObserver;

    private String name;
    private String filename;
    private List<ListItem> items;
    private boolean isFileDirty;

    public ShoppingList(final String filename) throws IOException {
        setFilename(filename);
        readListFromFile(filename);
    }

    public void addListChangeListener(ListChangedListener listener) {
        listeners.add(listener);
    }

    public void removeListChangeListener(ListChangedListener listener) {
        listeners.remove(listener);
    }

    private void notifyListChanged() {
        for (ListChangedListener listener : listeners) {
            listener.listChanged();
        }
    }

    public void changeFile(String filename) throws IOException {
        setFilename(filename);
        readListFromFile(filename);
        notifyListChanged();
    }

    private void setFilename(final String filename) {
        this.filename = filename;
        fileObserver = new FileObserver(filename) {
            @Override
            public void onEvent(int event, String path) {
                switch (event) {
                    case FileObserver.CLOSE_WRITE:
                    case FileObserver.CREATE:
                    case FileObserver.ATTRIB:
                        try {
                            readListFromFile(filename);
                        } catch (IOException e) {
                            Log.e(TAG, "FileObserver could not read file.", e);
                        }
                        notifyListChanged();
                        break;
                }
            }
        };
        fileObserver.startWatching();
    }

    private void readListFromFile(String filename) throws IOException {
        items = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String firstLine = reader.readLine();

            if (firstLine != null) {
                Matcher matcher = HEADER.matcher(firstLine);
                if (matcher.matches()) {
                    name = matcher.group(1).trim();
                } else {
                    name = "ShoppingList";
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (!EMPTY_LINE.matcher(line).matches()) {
                    items.add(createListItem(line));
                }
            }

            isFileDirty = false;
        }
    }

    private ListItem createListItem(String item) {
        boolean isChecked = item.startsWith("//");
        int index;
        String quantity;
        String name;

        if (isChecked) {
            item = item.substring(2);
        }

        index = item.lastIndexOf("#");

        if (index != -1) {
            quantity = item.substring(index + 1).trim();
            name = item.substring(0, index).trim();
        } else {
            quantity = "";
            name = item.trim();
        }

        return new ListItem(isChecked, name.trim(), quantity);
    }

    public void writeIfDirty() throws IOException {
        if (!isFileDirty) {
            return;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename, false))) {
            List<String> lines = new LinkedList<>();
            lines.add("[ " + name + " ]\n");
            lines.add("\n");

            for (ListItem item : items) {
                lines.add((item.isChecked() ? "// " : "") + item.getDescription() + " #" + item.getQuantity() + "\n");
            }

            for (String line : lines) {
                writer.write(line);
            }
            isFileDirty = false;
            Log.v(TAG, "data written");
        }
    }

    public void add(ListItem item) {
        items.add(item);
        isFileDirty = true;
        notifyListChanged();
    }

    public String getName() {
        return name;
    }

    public void setChecked(int index, boolean isChecked) {
        items.get(index).setChecked(isChecked);
        isFileDirty = true;
        notifyListChanged();
    }

    public void remove(int index) {
        items.remove(index);
        isFileDirty = true;
        notifyListChanged();
    }

    public void move(int oldIndex, int newIndex) {
        items.add(newIndex, items.remove(oldIndex));
    }

    public int size() {
        return items.size();
    }

    public ListItem get(int index) {
        return items.get(index);
    }

    public void editItem(int index, String newDescription, String newQuantity) {
        ListItem listItem = items.get(index);
        listItem.setDescription(newDescription);
        listItem.setQuantity(newQuantity);
        isFileDirty = true;
        notifyListChanged();
    }


    public void removeAllCheckedItems() {
        Iterator<ListItem> iterator = items.iterator();

        while (iterator.hasNext()) {
            ListItem item = iterator.next();
            if (item.isChecked()) {
                iterator.remove();
            }
        }
        notifyListChanged();
    }
}
