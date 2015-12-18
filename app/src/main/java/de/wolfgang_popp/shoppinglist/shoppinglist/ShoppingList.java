package de.wolfgang_popp.shoppinglist.shoppinglist;

import android.os.FileObserver;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

    private List<ListChangedListener> listeners = new LinkedList<>();
    private FileObserver fileObserver;

    private String name;
    private String filename;
    private List<ListItem> items;
    private boolean isFileDirty;

    public ShoppingList(final String filename) {
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
            listener.listChanged(this);
        }
    }

    public void changeFile(String filename) {
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
                        readListFromFile(filename);
                        notifyListChanged();
                        break;
                }
            }
        };
        fileObserver.startWatching();
    }

    private void readListFromFile(String filename) {
        items = new LinkedList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String firstLine = reader.readLine();

            if (firstLine != null) {
                Matcher matcher = Pattern.compile("\\[(.*)\\]").matcher(firstLine);
                if (matcher.matches()) {
                    name = matcher.group(1).trim();
                } else {
                    Log.e(TAG, "Invalid file");
                }
            }

            String line;
            while ((line = reader.readLine()) != null) {
                if (!EMPTY_LINE.matcher(line).matches()) {
                    items.add(createListItem(line));
                }
            }

            isFileDirty = false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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

    public void writeIfDirty() {
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
        } catch (IOException e) {
            e.printStackTrace();
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

}
