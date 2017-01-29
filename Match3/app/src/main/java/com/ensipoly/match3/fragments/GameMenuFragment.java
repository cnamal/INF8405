package com.ensipoly.match3.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.ensipoly.match3.R;
import com.ensipoly.match3.activities.GameActivity;


/**
 * A simple {@link Fragment} subclass.
 */
public class GameMenuFragment extends Fragment implements View.OnClickListener{


    private static final String TAG = "GameMenuFragment";
    public static final String LEVEL = "com.ensipoly.match3.LEVEL";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_game_menu, container, false);
        Button l1 = (Button) view.findViewById(R.id.level1_button);
        l1.setOnClickListener(this);
        Button l2 = (Button) view.findViewById(R.id.level2_button);
        l2.setOnClickListener(this);
        Button l3 = (Button) view.findViewById(R.id.level3_button);
        l3.setOnClickListener(this);
        Button l4 = (Button) view.findViewById(R.id.level4_button);
        l4.setOnClickListener(this);
        Button l5 = (Button) view.findViewById(R.id.level5_button);
        l5.setOnClickListener(this);
        return view;
    }


    @Override
    public void onClick(View view) {
        int level = -1;
        switch (view.getId()){
            case R.id.level1_button:
                level = 1;
                break;
            case R.id.level2_button:
                level = 2;
                break;
            case R.id.level3_button:
                level = 3;
                break;
            case R.id.level4_button:
                level = 4;
                break;
        }
        Intent intent = new Intent(getActivity(),GameActivity.class);
        intent.putExtra(LEVEL,level);
        startActivity(intent);
    }
}
