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

package de.wolfgang_popp.shoppinglist.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import de.wolfgang_popp.shoppinglist.R;

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

    public static void show(Activity activity, String message, String hint, int action) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.message = message;
        dialog.action = action;
        dialog.hint = hint;
        dialog.show(activity.getFragmentManager(), TAG);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (Listener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        String inputText = "";
        if (savedInstanceState != null) {
            message = savedInstanceState.getString(KEY_MESSAGE);
            hint = savedInstanceState.getString(KEY_HINT);
            inputText = savedInstanceState.getString(KEY_INPUT);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogRoot = inflater.inflate(R.layout.dialog_text_input, null);
        TextView label = dialogRoot.findViewById(R.id.dialog_label);
        label.setText(message);

        inputField = dialogRoot.findViewById(R.id.dialog_text_field);
        inputField.setHint(hint);
        inputField.setText(inputText);

        builder.setView(dialogRoot)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onInputComplete(inputField.getText().toString(), action);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_MESSAGE, message);
        outState.putString(KEY_HINT, hint);
        outState.putString(KEY_INPUT, inputField.getText().toString());
    }
}
