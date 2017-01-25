package com.ensipoly.match3.activities;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.ensipoly.match3.fragments.MenuFragment;
import com.ensipoly.match3.R;

public class MainActivity extends FragmentActivity {

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
