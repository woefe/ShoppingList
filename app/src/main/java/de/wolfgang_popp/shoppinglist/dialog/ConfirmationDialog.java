package de.wolfgang_popp.shoppinglist.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * @author Wolfgang Popp.
 */
public class ConfirmationDialog extends DialogFragment {
    private static final String TAG = ConfirmationDialog.class.getSimpleName();
    private static final String KEY_MESSAGE = "MESSAGE";
    private ConfirmationDialogListener listener;
    private String message;


    public interface ConfirmationDialogListener {
        void onPositiveButtonClicked();

        void onNegativeButtonClicked();
    }

    public static void show(Activity activity, String message) {
        ConfirmationDialog dialog = new ConfirmationDialog();
        dialog.message = message;
        dialog.show(activity.getFragmentManager(), TAG);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (ConfirmationDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        if (savedInstanceState != null) {
            message = savedInstanceState.getString(KEY_MESSAGE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Confirm")
                .setMessage(message)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onPositiveButtonClicked();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onNegativeButtonClicked();
                    }
                });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_MESSAGE, message);
    }
}
