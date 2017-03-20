package com.ensipoly.events.models;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.util.Map;

/**
 * Created by namalgac on 3/19/17.
 */

public class Location {

    private double latitude;
    private double longitude;
    private String name;
    private String photoURL;
    private Map<String,Float> votes;
    private String id;

    public Location(){

    }

    public Location(LatLng latLng, String n, String url){
        latitude = latLng.latitude;
        longitude = latLng.longitude;
        name = n;
        photoURL = url;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    @Exclude
    public void setId(String id){
        this.id = id;
    }

    @Exclude
    public String getId(){
        return id;
    }

    public Map<String,Float> getVotes(){
        return votes;
    }
}
