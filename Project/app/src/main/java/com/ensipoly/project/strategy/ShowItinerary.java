package com.ensipoly.project.strategy;


import android.graphics.Color;
import android.support.design.widget.BottomSheetBehavior;
import android.view.View;

import com.ensipoly.project.R;
import com.ensipoly.project.models.Itinerary;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

abstract class ShowItinerary extends Strategy {

    protected View mSelectedView;
    protected List<LatLng> waypoints;
    protected List<Marker> pictures;
    protected List<Circle> circles;
    protected Marker first;
    protected Marker last;
    protected Polyline line;

    protected ShowItinerary(Strategy.StrategyParameters params) {
        super(params);
        mBottomSheetBehavior1.setHideable(false);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    protected void select(View v, Itinerary i) {
        if (mSelectedView != null)
            mSelectedView.setBackgroundColor(Color.WHITE);
        mSelectedView = v;
        mSelectedView.setTag(i);
        mSelectedView.setBackgroundResource(R.color.colorAccent);
        fab.setVisibility(View.VISIBLE);
    }

    protected void showItinerary() {
        cleanupMap();
        Itinerary itinerary = (Itinerary) mSelectedView.getTag();
        waypoints = itinerary.getGMapsWaypoints();
        List<LatLng> picturesLatLng = itinerary.getGMapsPictures();
        first = mMap.addMarker(new MarkerOptions().position(waypoints.get(0)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        last = mMap.addMarker(new MarkerOptions().position(waypoints.get(waypoints.size() - 1)));
        line = mMap.addPolyline(new PolylineOptions().color(Color.RED).jointType(JointType.ROUND));
        line.setPoints(waypoints);
        circles = new ArrayList<>();
        for (int i = 1; i < waypoints.size() - 1; i++)
            circles.add(mMap.addCircle(new CircleOptions().center(waypoints.get(i)).radius(0.5).zIndex(1)));
        if (picturesLatLng != null) {
            pictures = new ArrayList<>();
            for (LatLng picture : picturesLatLng)
                pictures.add(mMap.addMarker(new MarkerOptions().position(picture).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))));
        }
    }

    @Override
    public void cleanup() {
        mBottomSheetBehavior1.setHideable(true);
        mBottomSheetBehavior1.setState(BottomSheetBehavior.STATE_HIDDEN);
        fab.setVisibility(View.GONE);
        cleanupMap();
    }

    @Override
    protected int initiallyShownButtons() {
        return 0;
    }


    @Override
    public void onMapClick(LatLng latLng) {

    }

    protected void cleanupMap() {
        if (first != null)
            first.remove();
        if (last != null)
            last.remove();
        if (line != null)
            line.remove();
        if (pictures != null)
            for (Marker marker : pictures)
                marker.remove();
        if (circles != null)
            for (Circle circle : circles)
                circle.remove();
    }
}
