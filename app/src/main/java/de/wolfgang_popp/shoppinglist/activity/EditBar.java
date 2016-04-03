package de.wolfgang_popp.shoppinglist.activity;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import de.wolfgang_popp.shoppinglist.R;

public class EditBar {
    private Activity activity;
    private RelativeLayout layout;
    private Button button;
    private EditText descriptionText;
    private EditText quantityText;
    private Mode mode;
    private EditBarListener listener;
    private int position;

    public EditBar(Activity activity) {
        this.activity = activity;
        this.layout = (RelativeLayout) this.activity.findViewById(R.id.layout_add_item);
        this.button = (Button) this.activity.findViewById(R.id.button_add_new_item);
        this.descriptionText = ((EditText) this.activity.findViewById(R.id.new_item_description));
        this.quantityText = ((EditText) this.activity.findViewById(R.id.new_item_quantity));
        this.mode = Mode.ADD;

        layout.setVisibility(View.GONE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String desc = descriptionText.getText().toString();
                String qty = quantityText.getText().toString();

                if (desc.equals("")){
                    Toast.makeText(EditBar.this.activity, R.string.error_description_empty, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mode == Mode.ADD) {
                    listener.onNewItem(desc, qty);
                    descriptionText.requestFocus();
                } else if (mode == Mode.EDIT) {
                    listener.onEditSave(position, desc, qty);
                }

                descriptionText.setText("");
                quantityText.setText("");
            }
        });
    }

    public void showEdit(int position, String description, String quantity) {
        descriptionText.setText(description);
        quantityText.setText(quantity);
        button.setText("✔");
        this.position = position;
        mode = Mode.EDIT;
        show();
    }

    public void showAdd() {
        descriptionText.setText("");
        quantityText.setText("");
        button.setText("+");
        mode = Mode.ADD;
        show();
    }

    private void show(){
        layout.setVisibility(View.VISIBLE);
        descriptionText.requestFocus();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void hide() {
        layout.setVisibility(View.GONE);
    }

    public boolean isVisible(){
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

    public interface EditBarListener {
        void onEditSave(int position, String description, String quantity);

        void onNewItem(String description, String quantity);
    }

    private enum Mode {
        EDIT, ADD
    }


}
