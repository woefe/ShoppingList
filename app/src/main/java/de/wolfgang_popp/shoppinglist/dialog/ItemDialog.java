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
public class ItemDialog extends DialogFragment {
    private static final String TAG = ItemDialog.class.getSimpleName();
    private static final String KEY_POSITION = "POSITION";
    private static final String KEY_DESCRIPTION = "DESCRIPTION";
    private static final String KEY_QUANTITY = "QUANTITY";

    private ItemDialogListener listener;
    private int position;
    private String description;
    private String quantity;

    public interface ItemDialogListener {
        void onEditSave(int position, String description, String quantity);

        void onNewItem(String description, String quantity);
    }

    public static void showEdit(Activity activity, int position, String description, String quantity) {
        ItemDialog dialog = new ItemDialog();
        dialog.position = position;
        dialog.description = description;
        dialog.quantity = quantity;
        dialog.show(activity.getFragmentManager(), TAG);
    }


    public static void showNew(Activity activity) {
        ItemDialog dialog = new ItemDialog();
        dialog.position = -1;
        dialog.description = "";
        dialog.quantity = "";
        dialog.show(activity.getFragmentManager(), TAG);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (ItemDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(KEY_POSITION);
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
                    ItemDialog.this.getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (position == -1) {
                            listener.onNewItem(descriptionInput.getText().toString(), quantityInput.getText().toString());
                        } else {
                            listener.onEditSave(position, descriptionInput.getText().toString(), quantityInput.getText().toString());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null);
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_POSITION, position);
        outState.putString(KEY_DESCRIPTION, description);
        outState.putString(KEY_QUANTITY, quantity);
    }
}
