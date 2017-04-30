package com.ensipoly.project.models;

import java.util.Date;
import java.util.HashMap;

public class History {

    private String itinerary;
    private HashMap<String, String> pictures;
    private Date startTime;
    private Date endTime;
    private String user;

    public History() {

    }

    public History(String itineraryId, HashMap<String, String> pictures, Date startTime, Date endTime,String user) {
        this.itinerary = itineraryId;
        this.pictures = pictures;
        this.startTime = startTime;
        this.endTime = endTime;
        this.user = user;
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
}
