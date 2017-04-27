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
        List<com.google.android.gms.maps.model.LatLng> res = new ArrayList<>();
        for(LatLng waypoint : waypoints)
            res.add(waypoint.convert());
        return res;
    }

    @Exclude
    public List<com.google.android.gms.maps.model.LatLng> getGMapsPictures(){
        if(pictures == null)
            return null;
        List<com.google.android.gms.maps.model.LatLng> res = new ArrayList<>();
        for(LatLng picture : pictures)
            res.add(picture.convert());
        return res;
    }
}
