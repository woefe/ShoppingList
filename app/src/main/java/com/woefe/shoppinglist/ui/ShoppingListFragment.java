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

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.woefe.shoppinglist.R;
import com.woefe.shoppinglist.db.entity.ItemEntity;
import com.woefe.shoppinglist.viewmodel.ItemsViewModel;

import java.util.List;

public class ShoppingListFragment extends Fragment implements EditBar.EditBarListener {

    public static final String KEY_LIST_ID = "LIST_ID";
    private EditBar editBar;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerListAdapter adapter;
    private View rootView;
    private ItemsViewModel model;

    public static ShoppingListFragment forList(long listId) {
        ShoppingListFragment fragment = new ShoppingListFragment();
        Bundle args = new Bundle();
        args.putLong(KEY_LIST_ID, listId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_shoppinglist, container, false);

        recyclerView = rootView.findViewById(R.id.shoppingListView);
        registerForContextMenu(recyclerView);

        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        editBar = new EditBar(rootView, getActivity());
        editBar.addEditBarListener(this);
        editBar.enableAutoHideFAB(recyclerView);

        if (savedInstanceState != null) {
            editBar.restoreState(savedInstanceState);
        }

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter = new RecyclerListAdapter(getActivity());
        adapter.registerRecyclerView(recyclerView);
        adapter.setOnItemLongClickListener(position -> {
            List<ItemEntity> value = model.getItems().getValue();
            if (value != null) {
                ItemEntity listItem = value.get(position);
                editBar.showEdit(position, listItem.getDescription(), listItem.getAmount());
                return true;
            }
            return false;
        });

        DividerItemDecoration divider = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(divider);

        recyclerView.setAdapter(adapter);

        ItemsViewModel.Factory factory = new ItemsViewModel.Factory(
                getArguments().getLong(KEY_LIST_ID), getActivity().getApplication());

        model = ViewModelProviders.of(this, factory).get(ItemsViewModel.class);
        subscribe(model);
    }

    private void subscribe(ItemsViewModel model) {
        model.getItems().observe(this, itemEntities -> {
            adapter.setItems(itemEntities);
            editBar.setShoppingList(itemEntities);
        });
    }

    @Override
    public void onDestroyView() {
        editBar.removeEditBarListener(this);
        editBar.hide();
        super.onDestroyView();
    }

    public boolean onBackPressed() {
        if (editBar.isVisible()) {
            editBar.hide();
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        editBar.saveState(outState);
    }

    @Override
    public void onEditSave(int position, String description, String quantity) {
        //shoppingList.editItem(position, description, quantity);
        //TODO
        editBar.hide();
        recyclerView.smoothScrollToPosition(position);
    }

    @Override
    public void onNewItem(String description, String quantity) {
        //shoppingList.add(description, quantity);
        //TODO
        recyclerView.smoothScrollToPosition(recyclerView.getAdapter().getItemCount() - 1);
    }

    public void removeAllCheckedItems() {
        // shoppingList.removeAllCheckedItems();
        // TODO
    }
}
