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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.woefe.shoppinglist.R;

public class TextInputDialog extends DialogFragment {
    private static final String TAG = DialogFragment.class.getSimpleName();
    private static final String KEY_MESSAGE = "MESSAGE";
    private static final String KEY_INPUT = "INPUT";
    private static final String KEY_HINT = "INPUT";
    private Listener listener;
    private String message;
    private String hint;
    private int action;
    private EditText inputField;


    public interface Listener {
        void onInputComplete(String input, int action);
    }

    public static void show(AppCompatActivity activity, String message, String hint, int action,
                            Class<? extends TextInputDialog> clazz) {

        TextInputDialog dialog;
        try {
            dialog = clazz.newInstance();
        } catch (java.lang.InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Cannot start dialog" + clazz.getSimpleName());
        }
        dialog.message = message;
        dialog.action = action;
        dialog.hint = hint;
        dialog.show(activity.getSupportFragmentManager(), TAG);
    }

    @Override
    public void onAttach(Context ctx) {
        super.onAttach(ctx);
        listener = (Listener) ctx;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        String inputText = "";
        if (savedInstanceState != null) {
            message = savedInstanceState.getString(KEY_MESSAGE);
            hint = savedInstanceState.getString(KEY_HINT);
            inputText = savedInstanceState.getString(KEY_INPUT);
        }

        View dialogRoot = inflater.inflate(R.layout.dialog_text_input, container, false);
        TextView label = dialogRoot.findViewById(R.id.dialog_label);
        Button cancelButton = dialogRoot.findViewById(R.id.button_dialog_cancel);
        Button okButton = dialogRoot.findViewById(R.id.button_dialog_ok);
        label.setText(message);

        inputField = dialogRoot.findViewById(R.id.dialog_text_field);
        inputField.setHint(hint);
        inputField.setText(inputText);
        inputField.requestFocus();

        inputField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                listener.onInputComplete(inputField.getText().toString(), action);
                dismiss();
                return true;
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String input = inputField.getText().toString();
                if (onValidateInput(input)) {
                    listener.onInputComplete(input, action);
                    dismiss();
                }
            }
        });

        return dialogRoot;
    }

    public boolean onValidateInput(String input) {
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_MESSAGE, message);
        outState.putString(KEY_HINT, hint);
        outState.putString(KEY_INPUT, inputField.getText().toString());
    }
}
