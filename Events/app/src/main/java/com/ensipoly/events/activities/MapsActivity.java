package com.ensipoly.events.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.ensipoly.events.CheckConnection;
import com.ensipoly.events.CheckLocation;
import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.Utils;
import com.ensipoly.events.models.Event;
import com.ensipoly.events.models.Group;
import com.ensipoly.events.models.User;
import com.github.clans.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import static com.ensipoly.events.R.id.location;
import static com.ensipoly.events.R.id.vote;
import static com.ensipoly.events.models.Event.GOING;

public class MapsActivity extends AppCompatActivity implements GoogleMap.OnMapLongClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapClickListener, GoogleMap.OnMarkerClickListener {

    public static final String GROUP_ID = "com.ensipoly.events.activities.MapsActivity.GROUP_ID";
    private GoogleMap mMap;
    private static final int REQUEST_LOCATION_ON_MAP_READY = 0;
    private static final int REQUEST_LOCATION_ON_CONNECTED = 1;
    private static final int REQUEST_LOCATION_START_LOCATION_UPDATES = 2;
    private String mUserId;

    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private final static String LOCATION_KEY = "location-key";
    private final static String LAST_UPDATED_TIME_STRING_KEY = "last-updated-time-string-key";
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mCurrentLocation;
    protected String mLastUpdateTime;

    private String mCurrentBottomSheetID;
    private DatabaseReference mUserDBReference;
    private DatabaseReference mGroupDBReference;
    private DatabaseReference mLocationDBReference;
    private DatabaseReference mEventDBReference;
    private HashMap<String, Marker> map;
    private String mGroupID;
    private BottomSheetBehavior mBottomSheetBehavior1;
    private Marker mCurrentLocationSuggestion;
    private TextView mInfoTextView;
    private boolean canLongClick;
    private FloatingActionButton mFAB;
    private Map<String, Pair<Marker, ValueEventListener>> mLocationSuggestionMap;
    private Votes mVotes;
    private boolean mCanCreateEvent = false;
    private int mMaxHeigt;
    private NestedScrollView mNestedScrollView;
    private BroadcastReceiver mCheckConnection;
    private BroadcastReceiver mCheckLocation;
    private TextView mConnectionTextView;

    @Override
    public void onMapLongClick(final LatLng latLng) {
        if (canLongClick) {
            if (mCurrentLocationSuggestion != null) {
                mCurrentLocationSuggestion.remove();
            } else {
                mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
            mCurrentLocationSuggestion = mMap.addMarker(new MarkerOptions().position(latLng));
            mNestedScrollView.removeAllViews();
            mNestedScrollView.getLayoutParams().height = 600;
            View v = getLayoutInflater().inflate(R.layout.location_add, mNestedScrollView);
            TextView locationTextView = (TextView) v.findViewById(location);
            final EditText locatonNameEditText = (EditText) v.findViewById(R.id.input_location_name);
            locationTextView.setText(formatLocation(latLng));
            locatonNameEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    if (charSequence.toString().trim().length() == 0)
                        mFAB.hide(true);
                    else
                        mFAB.show(true);
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            });

            mFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
                    dialog.setMessage("Do you wish to add this location?")
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    com.ensipoly.events.models.Location location = new com.ensipoly.events.models.Location(latLng, locatonNameEditText.getText().toString(), null);
                                    String locationKey = mLocationDBReference.push().getKey();
                                    DatabaseReference ref = FirebaseUtils.getDatabase().getReference();
                                    Map<String, Object> children = new HashMap<>();
                                    children.put("/locations/" + locationKey, location);
                                    children.put("/groups/" + mGroupID + "/locations/" + locationKey, true);
                                    ref.updateChildren(children).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
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
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_ON_MAP_READY);
            return;
        }
        mMap.setMyLocationEnabled(true);
        mGroupDBReference.child(mGroupID).child("members").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String userID = dataSnapshot.getKey();
                if (!userID.equals(mUserId)) {
                    mUserDBReference.child(userID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            User user = dataSnapshot.getValue(User.class);
                            String id = dataSnapshot.getKey();
                            if (!map.containsKey(id)) {
                                Marker marker = mMap.addMarker(new MarkerOptions()
                                        .position(new LatLng(user.latitude, user.longitude))
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                                );
                                marker.setTag(user);
                                map.put(id, marker);
                            } else {
                                Marker marker = map.get(id);
                                marker.setPosition(new LatLng(user.latitude, user.longitude));
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mGroupDBReference.child(mGroupID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Group group = dataSnapshot.getValue(Group.class);
                boolean isOrganizer = group.getOrganizer().equals(mUserId);
                Map<String, Boolean> locations = group.getLocations();
                if (group.getEvent() != null) {
                    if (mLocationSuggestionMap.size() > 0) {
                        clearLocationSuggestions();
                        mInfoTextView.setVisibility(View.GONE);
                        mCanCreateEvent = false;
                    }
                    mEventDBReference.child(group.getEvent()).addValueEventListener(new ValueEventListener() {
                        Marker marker;

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Event event = dataSnapshot.getValue(Event.class);
                            event.setId(dataSnapshot.getKey());
                            if (marker == null)
                                marker = mMap.addMarker(new MarkerOptions().position(new LatLng(event.getLatitude(), event.getLongitude())));
                            marker.setTag(event);
                            showEventDetails(event, false);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                } else if (locations != null && locations.size() == 3) {
                    mInfoTextView.setVisibility(View.GONE);
                    if (group.allMembersVotes()) {
                        if (isOrganizer) {
                            showInfo("Please create an event by clicking on the location of you choice.", R.color.severity_mid);
                            mCanCreateEvent = true;
                        }
                    }
                    if (mLocationSuggestionMap.size() == 3)
                        return;
                    mVotes = new Votes();
                    for (String locationId : locations.keySet()) {
                        Marker marker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).visible(false));
                        ValueEventListener listener = new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Pair<Marker, ValueEventListener> pair = mLocationSuggestionMap.get(dataSnapshot.getKey());
                                com.ensipoly.events.models.Location location = dataSnapshot.getValue(com.ensipoly.events.models.Location.class);
                                location.setId(dataSnapshot.getKey());
                                if (!pair.first.isVisible()) {
                                    pair.first.setVisible(true);
                                    pair.first.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                                }
                                pair.first.setTag(location);
                                if (location.getVotes() == null || !location.getVotes().containsKey(mUserId))
                                    showInfo("Please vote for every location.", R.color.severity_mid);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        };
                        Pair<Marker, ValueEventListener> pair = new Pair<>(marker, listener);
                        mLocationSuggestionMap.put(locationId, pair);
                        mLocationDBReference.child(locationId).addValueEventListener(listener);
                    }
                } else {
                    if (!isOrganizer)
                        return;
                    canLongClick = true;
                    int remaining = locations == null ? 3 : 3 - locations.size();
                    showInfo("Please select " + remaining + " places", R.color.severity_mid);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void clearLocationSuggestions() {
        for (Map.Entry<String, Pair<Marker, ValueEventListener>> e : mLocationSuggestionMap.entrySet()) {
            String locationId = e.getKey();
            Marker marker = e.getValue().first;
            ValueEventListener listener = e.getValue().second;
            marker.setTag(null);
            marker.remove();
            mLocationDBReference.child(locationId).removeEventListener(listener);
        }
        mLocationSuggestionMap.clear();
    }

    public void modifyMapPadding() {
        int height = 0;
        if (mInfoTextView.getVisibility() == View.VISIBLE) {
            mInfoTextView.measure(0, 0);
            height += mInfoTextView.getMeasuredHeight();
        }
        if (mConnectionTextView.getVisibility() == View.VISIBLE) {
            mConnectionTextView.measure(0, 0);
            height += mConnectionTextView.getMeasuredHeight();
        }

        mMap.setPadding(0, height, 0, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        if (grantResults.length == 0)
            return;
        switch (requestCode) {
            case REQUEST_LOCATION_ON_MAP_READY:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    onMapReady(mMap);
                break;
            case REQUEST_LOCATION_ON_CONNECTED:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    onConnected(null);
                break;
            case REQUEST_LOCATION_START_LOCATION_UPDATES:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startLocationUpdates();
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void getCurrentUser() {
        mUserId = Utils.getUserID(this);
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_LOCATION_ON_CONNECTED);
                return;
            }
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
            //TODO Update UI ?
        }

        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();
    }

    private void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_START_LOCATION_UPDATES);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.

        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(LOCATION_KEY, mCurrentLocation);
        savedInstanceState.putString(LAST_UPDATED_TIME_STRING_KEY, mLastUpdateTime);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/latitude", location.getLatitude());
        childUpdates.put("/longitude", location.getLongitude());
        childUpdates.put("/lastActive", DateFormat.getTimeInstance().format(new Date()));
        mUserDBReference.child(mUserId).updateChildren(childUpdates);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        updateValuesFromBundle(savedInstanceState);

        buildGoogleApiClient();
        getCurrentUser();
        map = new HashMap<>();
        mLocationSuggestionMap = new HashMap<>();
        canLongClick = false;

        mConnectionTextView = (TextView)findViewById(R.id.connection_text_view);
        mCheckConnection = new CheckConnection(mConnectionTextView);
        mCheckLocation = new CheckLocation(mConnectionTextView);
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter gpsFilter =  new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        this.registerReceiver(mCheckConnection, networkFilter);
        this.registerReceiver(mCheckLocation, gpsFilter);

        mUserDBReference = FirebaseUtils.getUserDBReference();
        mGroupDBReference = FirebaseUtils.getGroupDBReference();
        mLocationDBReference = FirebaseUtils.getLocationDBReference();
        mEventDBReference = FirebaseUtils.getEventDBReference();
        mGroupID = getIntent().getStringExtra(GROUP_ID);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mNestedScrollView = (NestedScrollView) findViewById(R.id.bottom_sheet1);
        mMaxHeigt = mNestedScrollView.getLayoutParams().height;
        mBottomSheetBehavior1 = BottomSheetBehavior.from(mNestedScrollView);
        mBottomSheetBehavior1.setHideable(true);
        mBottomSheetBehavior1.setPeekHeight(300);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
        mBottomSheetBehavior1.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    if (mCurrentLocationSuggestion != null) {
                        mCurrentLocationSuggestion.remove();
                        mCurrentLocationSuggestion = null;
                    }
                    mFAB.hide(true);
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED && mCanCreateEvent && mFAB.isHidden()) {
                    mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        mInfoTextView = (TextView) findViewById(R.id.info_text_view);
        mInfoTextView.setVisibility(View.GONE);

        mFAB = (FloatingActionButton) findViewById(R.id.fab_done);
        mFAB.hide(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop location updates to save battery, but don't disconnect the GoogleApiClient object.
        if (mGoogleApiClient.isConnected()) {
            stopLocationUpdates();
        }
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // TODO update user instead of mCurrentLocation/mLastUpdateTime if need be

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(LOCATION_KEY)) {
                // Since LOCATION_KEY was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(LOCATION_KEY);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(LAST_UPDATED_TIME_STRING_KEY)) {
                mLastUpdateTime = savedInstanceState.getString(LAST_UPDATED_TIME_STRING_KEY);
            }
            //TODO Update UI ?
        }
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mCurrentLocationSuggestion != null) {
            mCurrentLocationSuggestion.remove();
            mCurrentLocationSuggestion = null;
        }
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
        mFAB.setImageResource(R.drawable.ic_done_white_24dp);
        mFAB.hide(true);
    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Object tag = marker.getTag();
        if (tag != null) {
            if (tag instanceof User) {
                // TODO if we have time
            } else if (tag instanceof com.ensipoly.events.models.Location) {
                final com.ensipoly.events.models.Location location = (com.ensipoly.events.models.Location) tag;
                mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
                mNestedScrollView.removeAllViews();
                mNestedScrollView.getLayoutParams().height = 500;
                View v = getLayoutInflater().inflate(R.layout.location_details, mNestedScrollView);
                TextView name = (TextView) v.findViewById(R.id.location_name);
                final RatingBar ratingBar = (RatingBar) v.findViewById(vote);
                name.setText(location.getName());
                if (location.getVotes() != null && location.getVotes().containsKey(mUserId)) {
                    float vote = location.getVotes().get(mUserId);
                    ratingBar.setRating(vote);
                    if (mCanCreateEvent) {
                        mFAB.setImageResource(R.drawable.ic_event_white_24dp);
                        mFAB.show(true);
                        mFAB.setOnClickListener(new View.OnClickListener() {
                            private EditText startingDateText;
                            private Calendar startingDateCalendar = Calendar.getInstance();
                            private EditText endingDateText;
                            private Calendar endingDateCalendar = Calendar.getInstance();
                            private Calendar currentCalendar = Calendar.getInstance();
                            private EditText nameText;
                            private TextView placeText;
                            private EditText infoText;

                            @Override
                            public void onClick(View view) {
                                mNestedScrollView.removeAllViews();
                                mNestedScrollView.getLayoutParams().height = mMaxHeigt;
                                View v = getLayoutInflater().inflate(R.layout.activity_event, mNestedScrollView);
                                mFAB.hide(true);
                                mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);

                                startingDateText = (EditText) v.findViewById(R.id.input_starting_date);
                                endingDateText = (EditText) v.findViewById(R.id.input_ending_date);
                                nameText = (EditText) v.findViewById(R.id.input_name);
                                nameText.setText(location.getName());
                                placeText = (TextView) v.findViewById(R.id.location);
                                placeText.setText(formatLocation(location));
                                infoText = (EditText) v.findViewById(R.id.input_info);
                                setupDates();

                                // Setup the submit button
                                Button submitButton = (Button) findViewById(R.id.create_event_create_button);
                                submitButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        // Check if the form is OK. If not print error and return
                                        String error = verifyForm();
                                        if (error != null) {
                                            Toast.makeText(MapsActivity.this, error, Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        Event event = new Event(nameText.getText().toString(),
                                                infoText.getText().toString().trim(),
                                                location.getLatitude(),
                                                location.getLongitude(),
                                                startingDateCalendar.getTime(), endingDateCalendar.getTime());
                                        String eventId = mEventDBReference.push().getKey();
                                        DatabaseReference ref = FirebaseUtils.getDatabase().getReference();
                                        Map<String, Object> children = new HashMap<>();
                                        children.put("/groups/" + mGroupID + "/event", eventId);
                                        children.put("/events/" + eventId, event);
                                        ref.updateChildren(children).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
                                            }
                                        });
                                    }
                                });
                            }

                            private void setupDates() {

                                // Setup the listener to call when the date is set. Therefore we need to set the time.
                                // At the end, the label is updated.
                                final TimePickerDialog.OnTimeSetListener startingTime = new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hour, int minute) {
                                        startingDateCalendar.set(Calendar.HOUR_OF_DAY, hour);
                                        startingDateCalendar.set(Calendar.MINUTE, minute);
                                        updateStartingLabel();
                                    }
                                };

                                final TimePickerDialog.OnTimeSetListener endingTime = new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hour, int minute) {
                                        endingDateCalendar.set(Calendar.HOUR_OF_DAY, hour);
                                        endingDateCalendar.set(Calendar.MINUTE, minute);
                                        updateEndingLabel();
                                    }
                                };

                                // Setup the listener to call when we want to set the date.
                                // At the end, set the time.
                                final DatePickerDialog.OnDateSetListener startingDate = new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                          int dayOfMonth) {
                                        // TODO Auto-generated method stub
                                        startingDateCalendar.set(Calendar.YEAR, year);
                                        startingDateCalendar.set(Calendar.MONTH, monthOfYear);
                                        startingDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                                        // Now set the time
                                        new TimePickerDialog(MapsActivity.this, startingTime,
                                                currentCalendar.get(Calendar.HOUR_OF_DAY),
                                                currentCalendar.get(Calendar.MINUTE),
                                                false).show();
                                    }

                                };

                                final DatePickerDialog.OnDateSetListener endingDate = new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                                          int dayOfMonth) {
                                        // TODO Auto-generated method stub
                                        endingDateCalendar.set(Calendar.YEAR, year);
                                        endingDateCalendar.set(Calendar.MONTH, monthOfYear);
                                        endingDateCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);


                                        // Now set the time
                                        new TimePickerDialog(MapsActivity.this, endingTime,
                                                currentCalendar.get(Calendar.HOUR_OF_DAY),
                                                currentCalendar.get(Calendar.MINUTE),
                                                false).show();
                                    }

                                };


                                // Set the listener to open the DatePicker on click
                                // Open it at the current date.
                                startingDateText.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new DatePickerDialog(MapsActivity.this, startingDate,
                                                currentCalendar.get(Calendar.YEAR),
                                                currentCalendar.get(Calendar.MONTH),
                                                currentCalendar.get(Calendar.DAY_OF_MONTH)).show();
                                    }
                                });

                                endingDateText.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        new DatePickerDialog(MapsActivity.this, endingDate,
                                                currentCalendar.get(Calendar.YEAR),
                                                currentCalendar.get(Calendar.MONTH),
                                                currentCalendar.get(Calendar.DAY_OF_MONTH)).show();
                                    }
                                });
                            }

                            // Format the calendar to update the starting date label
                            private void updateStartingLabel() {

                                String myFormat = "MM/dd/yy hh:mm aaa"; //In which you need put here
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                                startingDateText.setText(sdf.format(startingDateCalendar.getTime()));
                            }

                            // Format the calendar to update the ending date label
                            private void updateEndingLabel() {

                                String myFormat = "MM/dd/yy hh:mm aaa"; //In which you need put here
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

                                endingDateText.setText(sdf.format(endingDateCalendar.getTime()));
                            }

                            private String verifyForm() {
                                if (nameText.getText().toString().equals(""))
                                    return getString(R.string.create_event_name_missing);
                                if (placeText.getText().toString().equals(""))
                                    return getString(R.string.create_event_place_missing);
                                if (startingDateText.getText().toString().equals(""))
                                    return getString(R.string.create_event_starting_date_missing);
                                if (endingDateText.getText().toString().equals(""))
                                    return getString(R.string.create_event_ending_date_missing);
                                if (startingDateCalendar.after(endingDateCalendar))
                                    return getString(R.string.create_event_ending_date_before_starting_date_error);
                                return null;
                            }
                        });
                    }
                } else {
                    if (mVotes.getNbVotes() >= 2) {
                        mFAB.setImageResource(R.drawable.ic_done_all_white_24dp);

                    }
                    float vote = mVotes.getVote(location.getId());
                    if (vote >= 0)
                        ratingBar.setRating(vote);
                    mFAB.show(true);
                    mFAB.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            float vote = ratingBar.getRating();
                            mVotes.addVote(location.getId(), vote);
                            onMapClick(null);
                            if (mVotes.getNbVotes() == 3) {
                                // TODO R.string
                                AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
                                dialog.setMessage("Save votes? You won't be able to modify your votes after.")
                                        .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Iterator<Map.Entry<String, Float>> it = mVotes.getIterator();
                                                Map<String, Object> children = new HashMap<>();
                                                while (it.hasNext()) {
                                                    Map.Entry<String, Float> entry = it.next();
                                                    children.put("/locations/" + entry.getKey() + "/votes/" + mUserId, entry.getValue());
                                                }
                                                children.put("/groups/" + mGroupID + "/members/" + mUserId, true);
                                                FirebaseUtils.getDatabase().getReference().updateChildren(children).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(MapsActivity.this,"Location added",Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton("Cancel", null)
                                        .show()
                                ;
                            } else {
                                Toast.makeText(getApplicationContext(), "Vote saved locally", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            } else if (tag instanceof Event) {
                final Event event = (Event) tag;
                showEventDetails(event, true);
            }
        }
        return true;
    }

    private String formatLocation(double latitude, double longitude) {
        return String.format(Locale.getDefault(), "Location: %1$.3f, %2$.3f ", latitude, longitude);
    }

    private String formatLocation(com.ensipoly.events.models.Location location) {
        return formatLocation(location.getLatitude(), location.getLongitude());
    }

    private String formatLocation(LatLng latLng) {
        return formatLocation(latLng.latitude, latLng.longitude);
    }

    private String formatLocation(Event event) {
        return formatLocation(event.getLatitude(), event.getLongitude());
    }

    private void showInfo(String text, @android.support.annotation.DrawableRes int resId) {
        mInfoTextView.setText(text);
        mInfoTextView.setVisibility(View.VISIBLE);
        mInfoTextView.setBackgroundResource(resId);
        modifyMapPadding();
    }

    private boolean shouldNotShowDetails(boolean force, String id) {
        return !force && (mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_HIDDEN || mCurrentBottomSheetID == null || !mCurrentBottomSheetID.equals(id));
    }

    private void showEventDetails(final Event event, boolean force) {
        if (shouldNotShowDetails(force, event.getId()))
            return;
        if (mBottomSheetBehavior1.getState() == BottomSheetBehavior.STATE_HIDDEN)
            mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mNestedScrollView.removeAllViews();
        mNestedScrollView.getLayoutParams().height = mMaxHeigt;
        View v = getLayoutInflater().inflate(R.layout.event_details, mNestedScrollView);
        TextView name = (TextView) v.findViewById(R.id.event_name);
        name.setText(event.getName());
        TextView desc = (TextView) v.findViewById(R.id.desc);
        if (event.getInfo().equals(""))
            desc.setVisibility(View.GONE);
        else
            desc.setText(event.getInfo());
        TextView location = (TextView) v.findViewById(R.id.location);
        location.setText(formatLocation(event));
        TextView begin = (TextView) v.findViewById(R.id.begin);
        begin.setText("Beginning: " + event.getStartingDate().toString());
        TextView end = (TextView) v.findViewById(R.id.end);
        end.setText("Ending: " + event.getEndingDate().toString());
        Button going = (Button) v.findViewById(R.id.going);
        Button maybe = (Button) v.findViewById(R.id.maybe);
        Button notGoing = (Button) v.findViewById(R.id.not_going);
        if (event.hasAnswered(mUserId)) {
            going.setEnabled(false);
            maybe.setEnabled(false);
            notGoing.setEnabled(false);
            switch (event.getParticipations().get(mUserId)) {
                case GOING:
                    going.setEnabled(true);
                    break;
                case Event.MAYBE:
                    maybe.setEnabled(true);
                    break;
                case Event.NOT_GOING:
                    notGoing.setEnabled(true);
                    break;
            }
        } else {
            going.setTag(Event.GOING);
            maybe.setTag(Event.MAYBE);
            notGoing.setTag(Event.NOT_GOING);
            View.OnClickListener listener = new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MapsActivity.this);
                    dialog.setMessage("Submit your participation?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    mEventDBReference.child(event.getId()).child("participations").child(mUserId).setValue(view.getTag());
                                }
                            }).show();

                }
            };
            going.setOnClickListener(listener);
            maybe.setOnClickListener(listener);
            notGoing.setOnClickListener(listener);
        }
        mCurrentBottomSheetID = event.getId();
    }

    private static class Votes {
        Map<String, Float> map;

        Votes() {
            map = new HashMap<>();
        }

        void addVote(String locationId, float vote) {
            map.put(locationId, vote);
        }

        Iterator<Map.Entry<String, Float>> getIterator() {
            return map.entrySet().iterator();
        }

        float getVote(String id) {
            if (map.containsKey(id))
                return map.get(id);
            return Float.MIN_VALUE;
        }

        int getNbVotes() {
            return map.size();
        }
    }
}
