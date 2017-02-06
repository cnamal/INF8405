package com.ensipoly.match3.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ensipoly.match3.R;
import com.ensipoly.match3.fragments.MenuFragment;
import com.github.clans.fab.FloatingActionButton;

/**
 * Main activity that pops when you start the app. It extends AppCompatActivity
 * so that we can use material design (v7 support)
 */
public class MainActivity extends AppCompatActivity {

    private FloatingActionButton returnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            return;
        }

        // If first launch, we need to "unlock" the first level
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if (sharedPref.getInt(getString(R.string.best_level), -1) < 0) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.best_level), 1);
            editor.apply();
        }

        // Default fragment is the main menu
        MenuFragment fragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment).commit();

        returnButton = (FloatingActionButton) findViewById(R.id.return_button);

        // Only visible in specific fragments
        setReturnButtonVisibility(false);

        returnButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        setReturnButtonVisibility(false);
        super.onBackPressed();
    }

    /**
     * Hide or show the return button
     *
     * @param visible if true, the button is visible
     */
    public void setReturnButtonVisibility(boolean visible) {
        if (visible)
            returnButton.show(true);
        else
            returnButton.hide(false);
    }

}
