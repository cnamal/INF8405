package com.ensipoly.events.activities;

import android.Manifest;
import android.content.BroadcastReceiver;
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
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.ensipoly.events.CheckConnection;
import com.ensipoly.events.CheckLocation;
import com.ensipoly.events.FirebaseUtils;
import com.ensipoly.events.R;
import com.ensipoly.events.Utils;
import com.ensipoly.events.fragments.CreateEventFragment;
import com.ensipoly.events.fragments.EventDetailsFragment;
import com.ensipoly.events.fragments.LocationAddFragment;
import com.ensipoly.events.fragments.LocationDetailsFragment;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
    private int mMaxHeight;
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
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            Fragment fragment = LocationAddFragment.getInstance(latLng, mGroupID);
            fragmentTransaction.replace(R.id.bottom_sheet1, fragment);
            fragmentTransaction.commit();

            mCurrentLocationSuggestion = mMap.addMarker(new MarkerOptions().position(latLng));
            mNestedScrollView.removeAllViews();
            mNestedScrollView.getLayoutParams().height = 600;
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

        mConnectionTextView = (TextView) findViewById(R.id.connection_text_view);
        mCheckConnection = new CheckConnection(mConnectionTextView);
        mCheckLocation = new CheckLocation(mConnectionTextView);
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter gpsFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
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
        mMaxHeight = mNestedScrollView.getLayoutParams().height;
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
                    mFAB.setImageResource(R.drawable.ic_done_white_24dp);
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
        hideBottomSheet();
    }

    public void hideBottomSheet() {
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
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
                mNestedScrollView.getLayoutParams().height = 500;

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                Fragment fragment = LocationDetailsFragment.getInstance(location, mGroupID, mUserId, mCanCreateEvent);
                fragmentTransaction.replace(R.id.bottom_sheet1, fragment);
                fragmentTransaction.commit();

            } else if (tag instanceof Event) {
                final Event event = (Event) tag;
                showEventDetails(event, true);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mBottomSheetBehavior1.getState() != BottomSheetBehavior.STATE_HIDDEN)
            hideBottomSheet();
        else
            super.onBackPressed();
    }

    public void showCreateEvent(com.ensipoly.events.models.Location location) {
        mNestedScrollView.removeAllViews();
        mNestedScrollView.getLayoutParams().height = mMaxHeight;
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_EXPANDED);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = CreateEventFragment.getInstance(location, mGroupID);
        fragmentTransaction.replace(R.id.bottom_sheet1, fragment);
        fragmentTransaction.commit();
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

        EventDetailsFragment fragment = (EventDetailsFragment) getSupportFragmentManager().findFragmentByTag("EVENT_TAG");
        if (fragment != null) {
            fragment.update(event, mUserId);
        } else {
            mNestedScrollView.removeAllViews();
            mNestedScrollView.getLayoutParams().height = mMaxHeight;
            mCurrentBottomSheetID = event.getId();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragment = EventDetailsFragment.getInstance(event, mUserId);
            fragmentTransaction.replace(R.id.bottom_sheet1, fragment, "EVENT_TAG");
            fragmentTransaction.commit();
        }
    }

    public Votes getVotes() {
        return mVotes;
    }

    public static class Votes {
        HashMap<String, Float> map;

        Votes() {
            map = new HashMap<>();
        }


        public void addVote(String locationId, float vote) {
            map.put(locationId, vote);
        }

        public Iterator<Map.Entry<String, Float>> getIterator() {
            return map.entrySet().iterator();
        }

        public float getVote(String id) {
            if (map.containsKey(id))
                return map.get(id);
            return Float.MIN_VALUE;
        }

        public int getNbVotes() {
            return map.size();
        }
    }

}
