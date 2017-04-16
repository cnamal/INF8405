package com.ensipoly.project.strategy;

import android.graphics.Color;
import android.view.View;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class CreateItinerary extends Strategy {

    private List<LatLng> waypoints;
    private Stack<Circle> circles;
    private Stack<Marker> pictures;
    private Polyline line;
    private Marker first;
    private Marker last;
    private int mode;
    private static final int WAYPOINTS = 0;
    private static final int PICTURES = 1;

    public CreateItinerary(Strategy.StrateyParameters parameters) {
        super(parameters);
        waypoints = new LinkedList<>();
        line = mMap.addPolyline(new PolylineOptions().color(Color.RED).jointType(JointType.ROUND));
        circles = new Stack<>();
        pictures = new Stack<>();
        first = null;
        last = null;
        mode = WAYPOINTS;
        hide(undo);
        hide(done);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mode == WAYPOINTS)
                    mode = PICTURES;
                else {
                    //TODO
                }
                menu.close(true);
            }
        });
    }

    @Override
    public boolean onBackPressed() {
        if (mode == WAYPOINTS)
            return onBackPressedWaypoints();
        return onBackPressedPictures();
    }

    private boolean onBackPressedWaypoints() {
        if (waypoints.isEmpty())
            return false;
        waypoints.remove(waypoints.size() - 1);
        line.setPoints(waypoints);

        if (last != null)
            last.remove();

        if (waypoints.size() > 1) {
            last = mMap.addMarker(new MarkerOptions().position(waypoints.get(waypoints.size() - 1)));
        } else if (waypoints.isEmpty())
            first.remove();

        if (!circles.isEmpty())
            circles.pop().remove();

        if (waypoints.isEmpty())
            hide(undo);
        if (waypoints.size() < 2)
            hide(done);
        return true;
    }

    private boolean onBackPressedPictures() {
        if (pictures.isEmpty()) {
            mode = WAYPOINTS;
            menu.close(true);
        } else {
            Marker marker = pictures.pop();
            marker.remove();
        }
        return true;
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (mode == WAYPOINTS)
            onMapClickWaypoints(latLng);
        else
            onMapClickPictures(latLng);
    }

    private void onMapClickWaypoints(LatLng latLng) {
        if (waypoints.isEmpty()) {
            first = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        }

        if (last != null) {
            last.remove();
        }

        if (waypoints.size() > 1) {
            Circle circle = mMap.addCircle(new CircleOptions().center(waypoints.get(waypoints.size() - 1)).radius(0.5).zIndex(1));
            circles.push(circle);
        }

        waypoints.add(latLng);
        line.setPoints(waypoints);

        if (waypoints.size() > 1)
            last = mMap.addMarker(new MarkerOptions().position(latLng));

        show(undo);

        if (waypoints.size() > 1)
            show(done);
    }

    private void onMapClickPictures(LatLng latLng) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
        marker.setTag(latLng);
        pictures.add(marker);
        show(undo);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (mode == WAYPOINTS)
            return false;
        LatLng latLng = (LatLng) marker.getTag();
        if (latLng == null)
            return false;
        pictures.remove(marker);
        marker.remove();
        return true;
    }

}
