package com.ensipoly.events.models;

import android.os.Bundle;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by namalgac on 3/19/17.
 */

public class Location {

    private static final String LATITUDE = "location/latitude";
    private static final String LONGITUDE = "location/longitude";
    private static final String NAME = "location/name";
    private static final String PHOTO_URL = "location/photo";
    private static final String VOTES = "location/votes";
    private static final String ID = "location/id";

    private double latitude;
    private double longitude;
    private String name;
    private String photoURL;
    private HashMap<String,Float> votes;
    private String id;

    public Location(){

    }

    public Location(double latitude,double longitude, String n, String url){
        this.latitude = latitude;
        this.longitude = longitude;
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

    @Exclude
    public void addArguments(Bundle bundle){
        bundle.putDouble(LATITUDE,latitude);
        bundle.putDouble(LONGITUDE,longitude);
        bundle.putString(NAME,name);
        bundle.putString(PHOTO_URL,photoURL);
        bundle.putSerializable(VOTES,votes);
        bundle.putString(ID,id);
    }

    @Exclude
    public static Location getLocationFromBundle(Bundle bundle){
        Location location = new Location(bundle.getDouble(LATITUDE),bundle.getDouble(LONGITUDE),bundle.getString(NAME),bundle.getString(PHOTO_URL));
        location.votes = (HashMap<String, Float>) bundle.getSerializable(VOTES);
        location.id = bundle.getString(ID);
        return location;
    }

    public void setPhotoURL(String url){
        photoURL = url;
    }

    public Map<String,Float> getVotes(){
        return votes;
    }

    @Exclude
    public float getAverage(){
        float sum = 0;
        for(float vote : votes.values())
            sum+=vote;
        return sum/votes.size();
    }
}
