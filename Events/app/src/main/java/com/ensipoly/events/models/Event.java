package com.ensipoly.events.models;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.Map;

public class Event {

    public static final int GOING=0;
    public static final int MAYBE=1;
    public static final int NOT_GOING=2;

    private String name;                    // The name of the event
    private String info;                    // Info for the event
    private double latitude;
    private double longitude;
    private Date startingDate, endingDate;  // Two dates start and end (with the time)
    private Map<String,Integer> participations;
    private String id;

    public Event(){

    }

    public Event(String name, String info, double latitude,double longitude, Date startingDate, Date endingDate){
        this.name = name;
        this.info = info;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startingDate = startingDate;
        this.endingDate = endingDate;
    }

    public String getName(){
        return name;
    }

    public String getInfo(){
        return info;
    }

    public double getLatitude(){
        return latitude;
    }

    public double getLongitude(){
        return longitude;
    }

    public Date getStartingDate(){
        return startingDate;
    }

    public Date getEndingDate(){
        return endingDate;
    }

    public Map<String,Integer> getParticipations(){
        return participations;
    }

    @Exclude
    public boolean hasAnswered(String userID){
        return participations != null && participations.containsKey(userID);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
