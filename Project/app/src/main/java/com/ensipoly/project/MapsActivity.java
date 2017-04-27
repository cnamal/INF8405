package com.ensipoly.project;

import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.ensipoly.project.strategy.CreateItinerary;
import com.ensipoly.project.strategy.Default;
import com.ensipoly.project.strategy.GoItinerary;
import com.ensipoly.project.strategy.Strategy;
import com.ensipoly.project.utils.CheckConnection;
import com.ensipoly.project.utils.Utils;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import static com.ensipoly.project.R.id.map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Strategy strategy;
    private Strategy.StrategyParameters params;
    private StepsCounter stepsCounter;

    public static final int DEFAULT_STRATEGY = 0;
    public static final int CREATE_STRATEGY = 1;
    public static final int GO_STRATEGY = 2;

    private BottomSheetBehavior mBottomSheetBehavior1;
    private int mMaxHeight;
    private NestedScrollView mNestedScrollView;
    private FloatingActionButton mFAB;
    private RecyclerView recyclerView;
    private CheckConnection mCheckConnection;
    private TextView mConnectionTextView;

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

        mNestedScrollView = (NestedScrollView) findViewById(R.id.bottom_sheet1);
        mMaxHeight = mNestedScrollView.getLayoutParams().height;
        mBottomSheetBehavior1 = BottomSheetBehavior.from(mNestedScrollView);
        mBottomSheetBehavior1.setHideable(true);
        mBottomSheetBehavior1.setPeekHeight(300);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);

        mFAB = (FloatingActionButton) findViewById(R.id.fab_done);
        mFAB.hide(false);
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
        params.activity = this;
        params.mBottomSheetBehavior1 = mBottomSheetBehavior1;
        params.recyclerView = recyclerView;
        stepsCounter = new StepsCounter(this);

    }

    @Override
    public void onResume(){
        super.onResume();
        stepsCounter.registerListener();
        mConnectionTextView = (TextView) findViewById(R.id.connection_text_view);
        Utils.ConnectionInfoManager manager = new Utils.ConnectionInfoManager(mConnectionTextView);
        mCheckConnection = new CheckConnection(manager);
        IntentFilter networkFilter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mCheckConnection, networkFilter);
    }

    @Override
    public void onPause(){
        super.onPause();
        stepsCounter.unregisterListener();
        unregisterReceiver(mCheckConnection);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        params.map = mMap;
        // Add a marker in Sydney and move the camera
        LatLng montreal = new LatLng(45.5016889, -73.56725599999999);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder().target(montreal).zoom(18).build()));
        strategy = new Default(params);
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
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        strategy.onActivityResult(requestCode,resultCode,data);
    }
}
