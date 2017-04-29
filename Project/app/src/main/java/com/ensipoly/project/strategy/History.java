package com.ensipoly.project.strategy;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

/**
 * Created by namalgac on 4/29/17.
 */

public class History extends Strategy {

    public History(StrategyParameters params) {
        super(params);
    }

    @Override
    public boolean onBackPressed() {
        return false;
    }

    @Override
    protected int initiallyShownButtons() {
        return 0;
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
