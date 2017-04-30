package com.ensipoly.project.utils;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class LatLng  {
    public double latitude;
    public double longitude;

    public LatLng(com.google.android.gms.maps.model.LatLng latLng) {
        this.latitude = latLng.latitude;
        this.longitude = latLng.longitude;
    }

    public LatLng(Location location) {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public LatLng(){

    }

    public com.google.android.gms.maps.model.LatLng convert(){
        return new com.google.android.gms.maps.model.LatLng(latitude,longitude);
    }

    public static List<com.google.android.gms.maps.model.LatLng> convertList(List<LatLng> list){
        List<com.google.android.gms.maps.model.LatLng> res = new ArrayList<>();
        for(LatLng waypoint : list)
            res.add(waypoint.convert());
        return res;
    }
}
