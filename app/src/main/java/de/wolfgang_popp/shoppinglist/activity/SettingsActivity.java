package de.wolfgang_popp.shoppinglist.activity;

import android.app.Activity;
import android.os.Bundle;

/**
 * @author Wolfgang Popp.
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}
