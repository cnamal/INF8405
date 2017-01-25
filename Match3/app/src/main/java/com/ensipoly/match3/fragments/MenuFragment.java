package com.ensipoly.match3.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ensipoly.match3.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class MenuFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = "MenuFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu, container, false);
        Button play = (Button) v.findViewById(R.id.play_button);
        play.setOnClickListener(this);
        Button rules = (Button) v.findViewById(R.id.rules_button);
        rules.setOnClickListener(this);
        Button exit = (Button) v.findViewById(R.id.exit_button);
        exit.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        Fragment newFragment = null;
        switch (view.getId()){
            case R.id.play_button:
                newFragment = new GameMenuFragment();
                break;
            case R.id.rules_button:
                newFragment = new RulesFragment();

                break;
            case R.id.exit_button:
                getActivity().finish();
                return;
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack so the user can navigate back
        transaction.replace(R.id.fragment_container, newFragment);
        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
}
