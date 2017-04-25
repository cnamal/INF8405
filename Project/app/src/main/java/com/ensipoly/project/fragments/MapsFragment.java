package com.ensipoly.project.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ensipoly.project.R;
import com.ensipoly.project.StepsCounter;
import com.ensipoly.project.strategy.CreateItinerary;
import com.ensipoly.project.strategy.Strategy;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import static com.ensipoly.project.R.id.map;

public class MapsFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Strategy strategy;
    private Strategy.StrateyParameters params;
    private StepsCounter stepsCounter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_maps, container, false);
        TextView infoView = (TextView) v.findViewById(R.id.info_text_view);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        MapFragment mapFragment = (MapFragment) getChildFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);
        FloatingActionMenu menu = (FloatingActionMenu) v.findViewById(R.id.fab_menu);
        FloatingActionButton undo = (FloatingActionButton) v.findViewById(R.id.undo);
        FloatingActionButton cancel = (FloatingActionButton) v.findViewById(R.id.cancel);
        FloatingActionButton done = (FloatingActionButton) v.findViewById(R.id.done);
        params = new Strategy.StrateyParameters();
        params.infoView = infoView;
        params.menu = menu;
        params.undo = undo;
        params.cancel = cancel;
        params.done = done;
        stepsCounter = new StepsCounter(getActivity());
        return v;
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
        strategy = new CreateItinerary(params);
    }

}
