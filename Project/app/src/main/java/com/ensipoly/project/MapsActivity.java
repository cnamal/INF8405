package com.ensipoly.project;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.ensipoly.project.strategy.CreateItinerary;
import com.ensipoly.project.strategy.Default;
import com.ensipoly.project.strategy.GoItinerary;
import com.ensipoly.project.strategy.History;
import com.ensipoly.project.strategy.Strategy;
import com.ensipoly.project.utils.CheckConnection;
import com.ensipoly.project.utils.CheckLocation;
import com.ensipoly.project.utils.FirebaseUtils;
import com.ensipoly.project.utils.Utils;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;

import static com.ensipoly.project.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Strategy strategy;
    private Strategy.StrategyParameters params;
    private StepsCounter stepsCounter;

    public static final int DEFAULT_STRATEGY = 0;
    public static final int CREATE_STRATEGY = 1;
    public static final int GO_STRATEGY = 2;
    public static final int HISTORY_STRATEGY = 3;

    private BottomSheetBehavior mBottomSheetBehavior1;
    private NestedScrollView mNestedScrollView;
    private RecyclerView recyclerView;
    private CheckConnection mCheckConnection;
    private CheckLocation mCheckLocation;
    private TextView mConnectionTextView;

    private static final int REQUEST_LOCATION_ON_MAP_READY = 0;
    private static final String USER_ID_KEY_PREFERENCE = "user_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        TextView infoView = (TextView) findViewById(R.id.info_text_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(map);
        mapFragment.getMapAsync(this);
        FloatingActionMenu menu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        FloatingActionButton undo = (FloatingActionButton) findViewById(R.id.undo);
        FloatingActionButton cancel = (FloatingActionButton) findViewById(R.id.cancel);
        FloatingActionButton done = (FloatingActionButton) findViewById(R.id.done);
        FloatingActionButton go = (FloatingActionButton) findViewById(R.id.go);
        FloatingActionButton create = (FloatingActionButton) findViewById(R.id.create);
        FloatingActionButton history = (FloatingActionButton) findViewById(R.id.history);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_done);

        mNestedScrollView = (NestedScrollView) findViewById(R.id.bottom_sheet1);
        mBottomSheetBehavior1 = BottomSheetBehavior.from(mNestedScrollView);
        mBottomSheetBehavior1.setHideable(true);
        mBottomSheetBehavior1.setPeekHeight(300);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);

        fab.hide(false);
        recyclerView = (RecyclerView) findViewById(R.id.myList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        params = new Strategy.StrategyParameters();
        params.infoView = infoView;
        params.menu = menu;
        params.undo = undo;
        params.cancel = cancel;
        params.done = done;
        params.go = go;
        params.create = create;
        params.history = history;
        params.activity = this;
        params.mBottomSheetBehavior1 = mBottomSheetBehavior1;
        params.recyclerView = recyclerView;
        params.fab = fab;
        stepsCounter = new StepsCounter(this);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String userID = sharedPref.getString(USER_ID_KEY_PREFERENCE, "");
        if(userID.equals("")){
            DatabaseReference userDB = FirebaseUtils.getUserDBReference();
            DatabaseReference user = userDB.push();
            userID = user.getKey();
            user.setValue(true);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString(USER_ID_KEY_PREFERENCE, userID);
            editor.commit();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        stepsCounter.registerListener();
        mConnectionTextView = (TextView) findViewById(R.id.connection_text_view);
        Utils.ConnectionInfoManager manager = new Utils.ConnectionInfoManager(mConnectionTextView);
        mCheckConnection = new CheckConnection(manager);
        mCheckLocation = new CheckLocation(manager);
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        IntentFilter gpsFilter = new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION);
        registerReceiver(mCheckConnection, networkFilter);
        registerReceiver(mCheckLocation, gpsFilter);

        // Initial GPS status is not given by the BroadcastReceiver. We query it ourselves.
        if (!(mCheckLocation.isGPSConnected(getApplicationContext())))
            manager.onLocationChanged(false);
        else
            manager.onLocationChanged(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        stepsCounter.unregisterListener();
        unregisterReceiver(mCheckConnection);
        unregisterReceiver(mCheckLocation);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        params.map = mMap;
        // Add a marker in Sydney and move the camera
        LatLng montreal = new LatLng(45.5016889, -73.56725599999999);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(montreal).zoom(18).build()));
        strategy = new Default(params);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_ON_MAP_READY);
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onBackPressed() {
        if(!strategy.onBackPressed()){
            if(strategy instanceof Default)
                super.onBackPressed();
            else
                switchStrategy(DEFAULT_STRATEGY);
        }
    }

    public void switchStrategy(int strategy){
        this.strategy.cleanup();
        switch (strategy){
            case DEFAULT_STRATEGY:
                this.strategy = new Default(params);
                return;
            case CREATE_STRATEGY:
                this.strategy = new CreateItinerary(params);
                return;
            case GO_STRATEGY:
                this.strategy = new GoItinerary(params);
                return;
            case HISTORY_STRATEGY:
                this.strategy = new History(params);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        strategy.onActivityResult(requestCode,resultCode,data);
    }
}
