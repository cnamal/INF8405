package com.ensipoly.project.strategy;

import com.google.android.gms.maps.model.Marker;

public class History extends ShowItinerary {

    public History(StrategyParameters params) {
        super(params);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }
}
