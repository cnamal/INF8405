package com.ensipoly.events.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.Utils;
import com.ensipoly.events.activities.MapsActivity;
import com.ensipoly.events.models.Location;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static android.content.Context.INPUT_METHOD_SERVICE;


public class LocationAddFragment extends Fragment {

    private static final String LATITUDE = "latitude";
    private static final String LONGITUDE = "longitude";
    private static final String GROUP_ID = "groupID";

    private static final int PICK_PHOTO_FOR_AVATAR = 0;
    private static final int READ_PERMISSION = 0;
    private Uri uri;

    public static LocationAddFragment getInstance(LatLng lng, String groupID) {
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
        locationTextView.setText(Utils.formatLocation(latitude, longitude));
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

        ImageView image = (ImageView) v.findViewById(R.id.location_photo);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(),
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            READ_PERMISSION
                    );
                    return;
                }
                openGallery();
            }
        });


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                dialog.setMessage("Do you wish to add this location?")
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                final com.ensipoly.events.models.Location location = new com.ensipoly.events.models.Location(latitude, longitude, locationNameEditText.getText().toString(), null);
                                if (uri != null) {
                                    StorageReference ref = FirebaseStorage.getInstance().getReference("location_photos").child(UUID.randomUUID().toString());
                                    ref.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @SuppressWarnings("VisibleForTests")
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            location.setPhotoURL(taskSnapshot.getDownloadUrl().toString());
                                            addLocation(location, mGroupID);
                                        }
                                    });
                                } else
                                    addLocation(location, mGroupID);


                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show()
                ;
            }
        });

        return v;
    }

    private void addLocation(Location location, String groupID) {
        final DatabaseReference locationDBReference = FirebaseUtils.getLocationDBReference();
        String locationKey = locationDBReference.push().getKey();
        DatabaseReference ref = FirebaseUtils.getDatabase().getReference();
        Map<String, Object> children = new HashMap<>();
        children.put("/locations/" + locationKey, location);
        children.put("/groups/" + groupID + "/locations/" + locationKey, true);
        ref.updateChildren(children).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getContext(), "Location added", Toast.LENGTH_SHORT).show();
                ((MapsActivity) getActivity()).hideBottomSheet();
                hideSoftKeyboard();
            }
        });
    }


    private void hideSoftKeyboard() {
        if (getActivity().getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                Toast.makeText(getContext(), "An error occurred, please retry", Toast.LENGTH_SHORT).show();
                return;
            }
            uri = data.getData();
            Toast.makeText(getContext(), "Photo retrieved successfully", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length == 0)
            return;
        if (requestCode == READ_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            openGallery();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }
}
