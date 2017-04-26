package com.ensipoly.project;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.ensipoly.project.strategy.CreateItinerary;
import com.ensipoly.project.strategy.Default;
import com.ensipoly.project.strategy.Strategy;
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
        params = new Strategy.StrategyParameters();
        params.infoView = infoView;
        params.menu = menu;
        params.undo = undo;
        params.cancel = cancel;
        params.done = done;
        params.go = go;
        params.create = create;
        params.activity = this;
        stepsCounter = new StepsCounter(this);
    }

    @Override
    public void onResume(){
        super.onResume();
        stepsCounter.registerListener();
    }

    @Override
    public void onPause(){
        super.onPause();
        stepsCounter.unregisterListener();
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
                strategy = new Default(params);
        }
    }

    public void switchStrategy(int strategy){
        switch (strategy){
            case DEFAULT_STRATEGY:
                this.strategy = new Default(params);
                return;
            case CREATE_STRATEGY:
                this.strategy = new CreateItinerary(params);
                return;
        }
    }
}
