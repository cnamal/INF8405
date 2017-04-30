package com.ensipoly.project.models;

import com.ensipoly.project.utils.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.Exclude;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Itinerary {
    private List<LatLng> waypoints;
    private List<LatLng> pictures;
    private String id;

    public Itinerary(){

    }

    public Itinerary(List<com.google.android.gms.maps.model.LatLng> waypoints, List<Marker> pics){
        this.waypoints = new ArrayList<>();
        for(com.google.android.gms.maps.model.LatLng waypoint : waypoints)
            this.waypoints.add(new LatLng(waypoint));
        if(pics.size()>0)
            pictures = new LinkedList<>();
        for(Marker marker: pics){
            com.google.android.gms.maps.model.LatLng latLng = (com.google.android.gms.maps.model.LatLng) marker.getTag();
            pictures.add(new LatLng(latLng));
        }
    }

    public List<LatLng> getWaypoints(){
        return waypoints;
    }

    public List<LatLng> getPictures(){
        return pictures;
    }

    @Exclude
    public List<com.google.android.gms.maps.model.LatLng> getGMapsWaypoints(){
        return LatLng.convertList(waypoints);
    }

    @Exclude
    public List<com.google.android.gms.maps.model.LatLng> getGMapsPictures(){
        if(pictures == null)
            return null;
        return LatLng.convertList(pictures);
    }

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }
}
