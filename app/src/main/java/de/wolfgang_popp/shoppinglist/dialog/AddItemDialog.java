package de.wolfgang_popp.shoppinglist.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import de.wolfgang_popp.shoppinglist.R;

/**
 * @author Wolfgang Popp.
 */
public class AddItemDialog extends DialogFragment {
    private static final String TAG = EditDialog.class.getSimpleName();
    private static final String KEY_DESCRIPTION = "DESCRIPTION";
    private static final String KEY_QUANTITY = "QUANTITY";

    private AddDialogListener listener;
    private String description;
    private String quantity;

    public interface AddDialogListener {
        void onAddNewItem(String description, String quantity);
    }

    public static void show(Activity activity) {
        AddItemDialog dialog = new AddItemDialog();
        dialog.show(activity.getFragmentManager(), TAG);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (AddDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        if (savedInstanceState != null) {
            description = savedInstanceState.getString(KEY_DESCRIPTION);
            quantity = savedInstanceState.getString(KEY_QUANTITY);
        }

        View view = getActivity().getLayoutInflater().inflate(R.layout.edit_item, null);
        final EditText descriptionInput = (EditText) view.findViewById(R.id.input_new_item);
        final EditText quantityInput = (EditText) view.findViewById(R.id.input_quantity);
        descriptionInput.setText(description);
        quantityInput.setText(quantity);
        descriptionInput.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    AddItemDialog.this.getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onAddNewItem(descriptionInput.getText().toString(), quantityInput.getText().toString());
                    }
                })
                .setNegativeButton("Cancel", null);

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_DESCRIPTION, description);
        outState.putString(KEY_QUANTITY, quantity);
    }
}
