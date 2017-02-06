package com.ensipoly.match3.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ensipoly.match3.R;


/**
 * Main menu
 */
public class MenuFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MenuFragment";
    private SharedPreferences sharedPref;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_menu, container, false);
        Button play = (Button) v.findViewById(R.id.play_button);
        play.setOnClickListener(this);
        Button rules = (Button) v.findViewById(R.id.rules_button);
        rules.setOnClickListener(this);
        Button reset = (Button) v.findViewById(R.id.reset_button);
        reset.setOnClickListener(this);
        Button exit = (Button) v.findViewById(R.id.exit_button);
        exit.setOnClickListener(this);
        sharedPref = getActivity().getSharedPreferences(
                getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return v;
    }

    @Override
    public void onClick(View view) {
        Fragment newFragment = null;
        switch (view.getId()) {
            case R.id.play_button:
                newFragment = new GameMenuFragment();
                break;
            case R.id.rules_button:
                newFragment = new RulesFragment();
                break;
            case R.id.reset_button:
                reset();
                return;
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

    /**
     * Reset Button handler.
     */
    private void reset() {
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_warning_black_24dp)
                .setTitle(getString(R.string.reset_alert_title))
                .setMessage(getString(R.string.reset_alert_body))
                .setPositiveButton(getString(R.string.reset), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Set only unlocked level to the first
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putInt(getString(R.string.best_level), 1);
                        editor.apply();

                        //hide Reset button
                        getActivity().findViewById(R.id.reset_button).setVisibility(View.GONE);
                    }

                })
                .setNegativeButton(getString(R.string.cancel), null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        // If at least the second level has been unlocked, show the Reset button
        if (sharedPref.getInt(getString(R.string.best_level), -1) > 1)
            getActivity().findViewById(R.id.reset_button).setVisibility(View.VISIBLE);

    }
}
