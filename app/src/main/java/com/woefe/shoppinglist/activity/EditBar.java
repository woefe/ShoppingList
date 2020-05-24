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

package com.woefe.shoppinglist.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.woefe.shoppinglist.R;
import com.woefe.shoppinglist.shoppinglist.ListItem;
import com.woefe.shoppinglist.shoppinglist.ShoppingList;

public class EditBar implements ShoppingList.ShoppingListListener {
    private static final String KEY_SAVED_DESCRIPTION = "SAVED_DESCRIPTION";
    private static final String KEY_SAVED_QUANTITY = "SAVED_QUANTITY";
    private static final String KEY_SAVED_CATEGORY = "SAVED_CATEGORY";
    private static final String KEY_SAVED_MODE = "SAVED_MODE";
    private static final String KEY_SAVE_IS_VISIBLE = "SAVE_IS_VISIBLE";
    private static final String KEY_SAVE_ITEM = "ITEM";
    private final Context ctx;
    private final RelativeLayout layout;
    private final EditText descriptionText;
    private final EditText quantityText;
    private final Spinner categorySpinner;
    private final TextView duplicateWarnText;
    private Mode mode;
    private EditBarListener listener;
    private final FloatingActionButton fab;
    private ShoppingList shoppingList;
    private ArrayAdapter<String> categoryAdapter;
    private ListItem item;

    public EditBar(View boundView, final Context ctx) {
        this.ctx = ctx;
        this.layout = boundView.findViewById(R.id.layout_add_item);
        final ImageButton button = boundView.findViewById(R.id.button_add_new_item);
        this.descriptionText = boundView.findViewById(R.id.new_item_description);
        this.quantityText = boundView.findViewById(R.id.new_item_quantity);
        this.categorySpinner = boundView.findViewById(R.id.category_description);
        categoryAdapter = new ArrayAdapter<>(ctx, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);
        this.duplicateWarnText = boundView.findViewById(R.id.text_warn);
        this.mode = Mode.ADD;

        layout.setVisibility(View.GONE);

        quantityText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                onConfirm();
                return true;
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConfirm();
            }
        });

        setButtonEnabled(button, false);
        descriptionText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if (str.equals("")) {
                    setButtonEnabled(button, false);
                } else {
                    setButtonEnabled(button, true);
                }
                checkDuplicate(str);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        fab = boundView.findViewById(R.id.fab_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fab.hide();
                showAdd();
            }
        });
    }

    private void checkDuplicate(String str) {
        if (mode != Mode.ADD || !isVisible()) {
            return;
        }
        if (shoppingList.contains(str.toLowerCase())) {
            duplicateWarnText.setText(ctx.getString(R.string.duplicate_warning, str));
            duplicateWarnText.setVisibility(View.VISIBLE);
        } else {
            duplicateWarnText.setVisibility(View.GONE);
        }
    }

    private void setButtonEnabled(ImageButton button, boolean enabled) {
        button.setEnabled(enabled);
        button.setClickable(enabled);
        button.setImageAlpha(enabled ? 255 : 100);
    }

    private void onConfirm() {
        String desc = descriptionText.getText().toString();
        String qty = quantityText.getText().toString();
        String category = categorySpinner.getSelectedItem().toString();

        if (desc.equals("")) {
            Toast.makeText(ctx, R.string.error_description_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mode == Mode.ADD) {
            listener.onNewItem(desc, qty, category);
            descriptionText.requestFocus();
        } else if (mode == Mode.EDIT) {
            listener.onEditSave(item, desc, qty, category);
        }

        descriptionText.setText("");
        quantityText.setText("");
        //categorySpinner.setText("");
    }

    public void showEdit(ListItem item) {
        prepare(Mode.EDIT, item, item.getDescription(), item.getQuantity(), item.getCategory());
        show();
    }

    public void showAdd() {
        prepare(Mode.ADD, new ListItem(), "", "", "");
        show();
    }

    private void prepare(Mode mode, ListItem item, String description, String quantity, String category) {
        this.mode = mode;
        this.item = item;
        quantityText.setText(quantity);
        descriptionText.append(description);
        int position = shoppingList.getCategories().indexOfKey(item.getCategory());
        categorySpinner.setSelection(position);
    }

    public void enableAutoHideFAB(RecyclerView view) {
        final GestureDetector.SimpleOnGestureListener gestureListener = new GestureDetector.SimpleOnGestureListener() {
            private final int slop = ViewConfiguration.get(ctx).getScaledPagingTouchSlop();
            private float start = -1;
            private float triggerPosition = -1;

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (isNewEvent(e1)) {
                    start = e1.getY();
                }
                final float end = e2.getY();

                if (end - start > slop) {
                    showFAB();
                    start = end;
                } else if (end - start < -slop) {
                    hideFAB();
                    start = end;
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            private boolean isNewEvent(MotionEvent e1) {
                boolean isNewEvent = e1 != null && !(e1.getY() == triggerPosition);
                if (isNewEvent) {
                    triggerPosition = e1.getY();
                }
                return isNewEvent;
            }
        };

        final GestureDetector detector = new GestureDetector(ctx, gestureListener);

        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                detector.onTouchEvent(event);
                return false;
            }
        });
    }

    private void showFAB() {
        if (!isVisible()) {
            fab.show();
        }
    }

    private void hideFAB() {
        fab.hide();
    }

    private void show() {
        layout.setVisibility(View.VISIBLE);
        descriptionText.requestFocus();
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(descriptionText, InputMethodManager.SHOW_IMPLICIT);
        }
    }

    public void hide() {
        descriptionText.clearFocus();
        quantityText.clearFocus();
        layout.setVisibility(View.GONE);
        duplicateWarnText.setText("");
        duplicateWarnText.setVisibility(View.GONE);
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
        }
        fab.show();
        fab.requestFocus();
    }

    public boolean isVisible() {
        return layout.getVisibility() != View.GONE;
    }

    public void addEditBarListener(EditBarListener l) {
        listener = l;
    }

    public void removeEditBarListener(EditBarListener l) {
        if (l == listener) {
            listener = null;
        }
    }

    public void saveState(Bundle state) {
        state.putString(KEY_SAVED_DESCRIPTION, descriptionText.getText().toString());
        state.putString(KEY_SAVED_QUANTITY, quantityText.getText().toString());
        state.putInt(KEY_SAVED_CATEGORY, categorySpinner.getSelectedItemPosition());
        state.putBoolean(KEY_SAVE_IS_VISIBLE, isVisible());
        state.putSerializable(KEY_SAVED_MODE, mode);
        state.putParcelable(KEY_SAVE_ITEM, item);
    }

    public void restoreState(Bundle state) {
        String description = state.getString(KEY_SAVED_DESCRIPTION);
        String quantity = state.getString(KEY_SAVED_QUANTITY);
        categorySpinner.setSelection(state.getInt(KEY_SAVED_CATEGORY));
        Mode mode = (Mode) state.getSerializable(KEY_SAVED_MODE);
        ListItem item = state.getParcelable(KEY_SAVE_ITEM);
        
        if (state.getBoolean(KEY_SAVE_IS_VISIBLE)) {
            prepare(mode, item, description, quantity, categorySpinner.getSelectedItem().toString());
            layout.setVisibility(View.VISIBLE);
            fab.hide();
        }
    }

    public void connectShoppingList(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
        this.shoppingList.addListener(this);
        onShoppingListUpdate(shoppingList, null);
    }

    public void disconnectShoppingList() {
        if (shoppingList != null) {
            shoppingList.removeListener(this);
            shoppingList = null;
        }
    }

    @Override
    public void onShoppingListUpdate(ShoppingList list, ShoppingList.Event e) {
        if (mode == Mode.EDIT) {
            hide();
            return;
        }
        categoryAdapter.clear();
        categoryAdapter.addAll(shoppingList.getAllCategories());
        checkDuplicate(descriptionText.getText().toString());
    }

    public interface EditBarListener {
        void onEditSave(ListItem item, String description, String quantity, String category);

        void onNewItem(String description, String quantity, String category);
    }

    private enum Mode {
        EDIT, ADD
    }


}
