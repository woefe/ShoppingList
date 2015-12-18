package de.wolfgang_popp.shoppinglist.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import de.wolfgang_popp.shoppinglist.R;
import de.wolfgang_popp.shoppinglist.activity.MainActivity;

/**
 * @author Wolfgang Popp.
 */
public class ActionDialog extends DialogFragment {
    private static final String TAG = ActionDialog.class.getSimpleName();
    private static final String KEY_POSITION = "POSITION";
    private int position;
    private ActionDialogListener listener;

    public interface ActionDialogListener {
        void onRemoveItemSelected(int position);
        void onEditItemSelected(int position);
    }

    public static void show(Activity activity, int position) {
        ActionDialog dialog = new ActionDialog();
        dialog.position = position;
        dialog.show(activity.getFragmentManager(), TAG);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (ActionDialogListener) activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            position = savedInstanceState.getInt(KEY_POSITION);
        }

        final MainActivity activity = (MainActivity) getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("Edit or Delete?").setItems(R.array.actions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        listener.onRemoveItemSelected(position);
                        break;
                    case 1:
                        listener.onEditItemSelected(position);
                        break;
                    default:
                        throw new IllegalStateException("OnClick got wrong index");
                }

            }
        });
        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(KEY_POSITION, position);
    }

}
