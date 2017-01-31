package com.ensipoly.match3.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ensipoly.match3.R;
import com.ensipoly.match3.fragments.MenuFragment;
import com.github.clans.fab.FloatingActionMenu;

public class MainActivity extends AppCompatActivity {

    private FloatingActionMenu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            return;
        }
        SharedPreferences sharedPref = getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if(sharedPref.getInt(getString(R.string.best_level),-1)<0) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putInt(getString(R.string.best_level), 1);
            editor.apply();
        }
        MenuFragment fragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,fragment).commit();

        menu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        // Only visible in specific fragments
        setMenuVisible(false);

        findViewById(R.id.return_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {
        menu.close(false);
        setMenuVisible(false);
        super.onBackPressed();
    }

    public void setMenuVisible(boolean visible){
        if(visible)
            menu.setVisibility(View.VISIBLE);
        else
            menu.setVisibility(View.INVISIBLE);
    }

}
