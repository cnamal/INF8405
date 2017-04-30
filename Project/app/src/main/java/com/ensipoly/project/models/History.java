package com.ensipoly.project.models;

import com.ensipoly.project.utils.LatLng;
import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class History {

    private String itinerary;
    private HashMap<String, String> pictures;
    private Date startTime;
    private Date endTime;
    private String user;
    private List<LatLng> locations;

    public History() {

    }

    public History(String itineraryId, HashMap<String, String> pictures, Date startTime, Date endTime,String user,List<LatLng> locations) {
        this.itinerary = itineraryId;
        this.pictures = pictures;
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
        this.locations = locations;
    }

    public History(String itineraryId, Date startTime, Date endTime, String user,List<LatLng> locations){
        this(itineraryId,null,startTime,endTime,user,locations);
    }

    public String getItinerary() {
        return itinerary;
    }

    public void setItinerary(String itinerary) {
        this.itinerary = itinerary;
    }

    public HashMap<String, String> getPictures() {
        return pictures;
    }

    public void setPictures(HashMap<String, String> pictures) {
        this.pictures = pictures;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }


    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<LatLng> getLocations() {
        return locations;
    }

    public void setLocations(List<LatLng> locations) {
        this.locations = locations;
    }

    @Exclude
    public List<com.google.android.gms.maps.model.LatLng> getGMapsLocations(){
        if(pictures == null)
            return null;
        return LatLng.convertList(locations);
    }
}
