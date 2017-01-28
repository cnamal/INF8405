package com.ensipoly.match3.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.ensipoly.match3.R;
import com.ensipoly.match3.fragments.MenuFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            return;
        }
        MenuFragment fragment = new MenuFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.fragment_container,fragment).commit();
    }


}
