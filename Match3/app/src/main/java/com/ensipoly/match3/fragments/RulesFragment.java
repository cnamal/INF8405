package com.ensipoly.match3.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ensipoly.match3.R;
import com.ensipoly.match3.activities.MainActivity;


/**
 * Rules fragment
 */
public class RulesFragment extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get the return menu
        ((MainActivity) getActivity()).setReturnButtonVisibility(true);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rules, container, false);
    }

}
