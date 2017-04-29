package com.ensipoly.project.strategy;

import android.view.View;

import com.ensipoly.project.MapsActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

public class Default extends Strategy {

    public Default(StrategyParameters params) {
        super(params);
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchStrategy(MapsActivity.CREATE_STRATEGY);
            }
        });
        go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchStrategy(MapsActivity.GO_STRATEGY);
            }
        });
        history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switchStrategy(MapsActivity.HISTORY_STRATEGY);
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    protected int initiallyShownButtons() {
        return MENU|CREATE|GO|HISTORY;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
