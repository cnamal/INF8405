package com.ensipoly.events.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.Utils;
import com.ensipoly.events.activities.MapsActivity;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class LocationAddFragment extends Fragment {

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String GROUP_ID = "groupID";

    public static LocationAddFragment getInstance(LatLng lng,String groupID) {
        LocationAddFragment fragment = new LocationAddFragment();
        Bundle bundle = new Bundle();
        bundle.putDouble(LATITUDE, lng.latitude);
        bundle.putDouble(LONGITUDE, lng.longitude);
        bundle.putString(GROUP_ID, groupID);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final double latitude = getArguments().getDouble(LATITUDE);
        final double longitude = getArguments().getDouble(LONGITUDE);
        final String mGroupID = getArguments().getString(GROUP_ID);

        View v = inflater.inflate(R.layout.fragment_location_add, container, false);
        final FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab_done);
        TextView locationTextView = (TextView) v.findViewById(R.id.location);
        final EditText locationNameEditText = (EditText) v.findViewById(R.id.input_location_name);
        locationTextView.setText(Utils.formatLocation(latitude,longitude));
        locationNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() == 0)
                    fab.hide(true);
                else
                    fab.show(true);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        final DatabaseReference locationDBReference = FirebaseUtils.getLocationDBReference();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setMessage("Do you wish to add this location?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                com.ensipoly.events.models.Location location = new com.ensipoly.events.models.Location(latitude,longitude, locationNameEditText.getText().toString(), null);
                                String locationKey = locationDBReference.push().getKey();
                                DatabaseReference ref = FirebaseUtils.getDatabase().getReference();
                                Map<String, Object> children = new HashMap<>();
                                children.put("/locations/" + locationKey, location);
                                children.put("/groups/" + mGroupID + "/locations/" + locationKey, true);
                                ref.updateChildren(children).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        ((MapsActivity)getActivity()).hideBottomSheet();
                                        hideSoftKeyboard();
                                    }
                                });

                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
                ;
            }
        });

        return v;
    }

    private void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }
}
