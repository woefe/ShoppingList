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

package com.woefe.shoppinglist.dialog;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.woefe.shoppinglist.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * @author Wolfgang Popp
 */

public class DirectoryChooser extends AppCompatActivity implements TextInputDialog.TextInputDialogListener {
    public static final String SELECTED_PATH = "SELECTED_PATH";
    private static final String KEY_CURRENT_DIR = "CURRENT_DIR";
    private static final int ACTION_READ_INPUT = 1;
    private static final String PARENT_DIR = "..";
    private static final int REQUEST_CODE_EXT_STORAGE = 2;

    private ArrayAdapter<String> directoryViewAdapter;
    private File currentDirectory;
    private TextView title;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_directory_chooser);

        directoryViewAdapter = new ArrayAdapter<>(this, R.layout.directory_list_item);
        ListView directoryView = findViewById(R.id.directoryListView);
        directoryView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedDir = directoryViewAdapter.getItem(position);
                if (selectedDir != null) {
                    if (selectedDir.equals(PARENT_DIR)) {
                        changeDirectory(currentDirectory.getParentFile());
                    } else {
                        changeDirectory(new File(currentDirectory, selectedDir));
                    }
                }
            }
        });
        directoryView.setAdapter(directoryViewAdapter);


        Button okButton = findViewById(R.id.button_dialog_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept();
            }
        });

        Button cancelButton = findViewById(R.id.button_dialog_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });

        ImageButton storageButton = findViewById(R.id.button_choose_storage);
        storageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseStorageLocation();
            }
        });

        FloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewDir();
            }
        });

        title = findViewById(R.id.title);

        File directory;
        String savedDir;
        File[] storageLocations = listStorageLocations();

        if (savedInstanceState != null
                && (savedDir = savedInstanceState.getString(KEY_CURRENT_DIR)) != null) {

            directory = new File(savedDir);
        } else if (storageLocations.length > 0) {
            directory = storageLocations[0];
        } else {
            directory = new File(""); // Jeez you phone is broken?!?!
        }

        changeDirectory(directory);
    }

    @Override
    protected void onStart() {
        super.onStart();

        int result = ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (result == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_EXT_STORAGE);
        }
    }

    @Override
    public void onInputComplete(String input, int action) {
        if (action == ACTION_READ_INPUT) {
            File newDir = new File(currentDirectory, input);
            boolean success = newDir.mkdir();
            if (success) {
                changeDirectory(newDir);
            } else {
                Toast.makeText(this, getString(R.string.err_create_dir, input), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_CURRENT_DIR, currentDirectory.getAbsolutePath());
        super.onSaveInstanceState(outState);
    }

    private void accept() {
        if (!currentDirectory.canWrite()) {
            Toast.makeText(this, "Cannot write to directory", Toast.LENGTH_LONG).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(SELECTED_PATH, currentDirectory.getAbsolutePath());
        setResult(RESULT_OK, intent);
        finish();
    }

    private void cancel() {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void createNewDir() {
        NewDirectoryDialog.Builder builder =
                new TextInputDialog.Builder(this, NewDirectoryDialog.class);

        builder.setAction(ACTION_READ_INPUT)
                .setMessage(R.string.create_new_dir)
                .setHint(R.string.name_of_new_dir)
                .show();
    }

    private void chooseStorageLocation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final File[] locations = listStorageLocations();
        final String[] locationNames = new String[locations.length];
        for (int i = 0; i < locations.length; i++) {
            locationNames[i] = locations[i].getAbsolutePath();
        }

        builder.setTitle(R.string.select_storage_location)
                .setItems(locationNames, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        changeDirectory(locations[which]);
                    }
                }).create().show();
    }

    private File[] listStorageLocations() {
        final List<File> locations = new ArrayList<>();

        locations.add(Environment.getExternalStorageDirectory());
        locations.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        locations.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        locations.addAll(Arrays.asList(getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS)));

        ListIterator<File> it = locations.listIterator();
        while (it.hasNext()) {
            File directory = it.next();
            if (directory == null
                    || !Environment.MEDIA_MOUNTED.equals(Environment.getStorageState(directory))
                    || !directory.canExecute()
                    || !directory.canRead()) {

                it.remove();
            }
        }

        locations.add(getFilesDir());

        return locations.toArray(new File[locations.size()]);
    }

    private void changeDirectory(File directory) {
        if (directory == null
                || !directory.canRead()
                || !directory.canExecute()) {

            Toast.makeText(this, R.string.warn_no_dir_access, Toast.LENGTH_LONG).show();
            return;
        }

        List<String> directories = new ArrayList<>();
        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                directories.add(file.getName());
            }
        }

        Collections.sort(directories, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });

        title.setText(directory.getAbsolutePath());
        directoryViewAdapter.clear();
        directoryViewAdapter.add(PARENT_DIR);
        directoryViewAdapter.addAll(directories);
        currentDirectory = directory;
    }

    public static class NewDirectoryDialog extends TextInputDialog {
        @Override
        public boolean onValidateInput(String input) {
            boolean isValid = !input.contains("/");
            if (!isValid) {
                Toast.makeText(getContext(), R.string.err_illegal_char, Toast.LENGTH_LONG).show();
            }
            return isValid;
        }
    }
}


