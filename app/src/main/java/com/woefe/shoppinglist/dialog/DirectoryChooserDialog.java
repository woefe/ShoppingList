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

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class DirectoryChooserDialog extends DialogFragment {
    private static final String TAG = DialogFragment.class.getCanonicalName();
    public static final String PARENT_DIR = "..";
    private ListView directoryView;
    private ArrayAdapter<String> directoryViewAdapter;
    private File currentDirectory;
    private DirectoryChooserListener listener;
    private TextView title;

    public interface DirectoryChooserListener {
        void onDirectorySelected(String path);
    }

    public static void show(FragmentActivity activity) {
        DirectoryChooserDialog dialog = new DirectoryChooserDialog();
        dialog.show(activity.getSupportFragmentManager(), TAG);
    }

    public static void show(FragmentActivity activity, Fragment target, int requestCode) {
        DirectoryChooserDialog dialog = new DirectoryChooserDialog();
        dialog.setTargetFragment(target, requestCode);
        dialog.show(activity.getSupportFragmentManager(), TAG);
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);

        if (ctx instanceof DirectoryChooserListener) {
            listener = (DirectoryChooserListener) ctx;
        } else {
            Fragment owner = getTargetFragment();
            if (owner instanceof DirectoryChooserListener) {
                listener = (DirectoryChooserListener) owner;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View dialogRoot = inflater.inflate(R.layout.dialog_directory_chooser, container, false);

        directoryView = dialogRoot.findViewById(R.id.directoryListView);
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

        Button okButton = dialogRoot.findViewById(R.id.button_dialog_ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO check write permissions
                listener.onDirectorySelected(currentDirectory.getAbsolutePath());
                dismiss();
            }
        });

        Button cancelButton = dialogRoot.findViewById(R.id.button_dialog_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        ImageButton storageButton = dialogRoot.findViewById(R.id.button_choose_storage);
        storageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseStorageLocation();
            }
        });

        FloatingActionButton fab = dialogRoot.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewDir();
            }
        });

        title = dialogRoot.findViewById(R.id.title);

        return dialogRoot;
    }

    private void createNewDir() {
        // TODO
    }

    private void chooseStorageLocation() {
        // TODO
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        directoryViewAdapter = new ArrayAdapter<>(getActivity(), R.layout.drawer_list_item);
        directoryView.setAdapter(directoryViewAdapter);

        File[] storageLocations = listStorageLocations();
        File directory;
        if (storageLocations.length < 1) {
            directory = new File(""); // Jeez you phone is broken?!?!
        } else {
            directory = storageLocations[0];
        }

        changeDirectory(directory);
    }

    private File[] listStorageLocations() {
        final List<File> locations = new ArrayList<>();

        locations.add(Environment.getExternalStorageDirectory());
        locations.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));
        locations.add(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));
        Context ctx = getContext();
        if (ctx != null) {
            locations.addAll(Arrays.asList(ctx.getExternalFilesDirs(Environment.DIRECTORY_DOCUMENTS)));
        }

        ListIterator<File> it = locations.listIterator();
        while (it.hasNext()) {
            File directory = it.next();
            if (!Environment.getStorageState(directory).equals(Environment.MEDIA_MOUNTED)
                    || !directory.canExecute()
                    || !directory.canRead()) {

                it.remove();
            }
        }

        if (ctx != null) {
            locations.add(ctx.getFilesDir());
        }

        return locations.toArray(new File[locations.size()]);
    }

    private void changeDirectory(File directory) {
        if (directory == null
                || !directory.canRead()
                || !directory.canExecute()) {

            Toast.makeText(getContext(), R.string.warn_no_dir_access, Toast.LENGTH_LONG).show();
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
}

