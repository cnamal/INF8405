package com.ensipoly.events.models;

import android.os.Bundle;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Event {

    public static final int GOING = 0;
    public static final int MAYBE = 1;
    public static final int NOT_GOING = 2;

    private static final String LATITUDE = "event/latitude";
    private static final String LONGITUDE = "event/longitude";
    private static final String NAME = "event/name";
    private static final String ID = "event/id";
    private static final String INFO = "event/info";
    private static final String PARTICIPATIONS = "event/participations";
    private static final String START = "event/start";
    private static final String END = "event/end";

    private String name;                    // The name of the event
    private String info;                    // Info for the event
    private double latitude;
    private double longitude;
    private Date startingDate, endingDate;  // Two dates start and end (with the time)
    private HashMap<String, Integer> participations;
    private String id;

    public Event() {

    }

    public Event(String name, String info, double latitude, double longitude, Date startingDate, Date endingDate) {
        this.name = name;
        this.info = info;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startingDate = startingDate;
        this.endingDate = endingDate;
    }

    public String getName() {
        return name;
    }

    public String getInfo() {
        return info;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Date getStartingDate() {
        return startingDate;
    }

    public Date getEndingDate() {
        return endingDate;
    }

    public Map<String, Integer> getParticipations() {
        return participations;
    }

    @Exclude
    public boolean hasAnswered(String userID) {
        return participations != null && participations.containsKey(userID);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Exclude
    public void addArguments(Bundle bundle) {
        bundle.putDouble(LATITUDE, latitude);
        bundle.putDouble(LONGITUDE, longitude);
        bundle.putString(NAME, name);
        bundle.putString(ID, id);
        bundle.putString(INFO, info);
        bundle.putSerializable(PARTICIPATIONS, participations);
        bundle.putSerializable(START, startingDate);
        bundle.putSerializable(END, endingDate);
    }

    @Exclude
    public static Event getEventFromBundle(Bundle bundle) {
        Event event = new Event(bundle.getString(NAME), bundle.getString(INFO), bundle.getDouble(LATITUDE), bundle.getDouble(LONGITUDE), (Date) bundle.getSerializable(START), (Date) bundle.getSerializable(END));
        event.id = bundle.getString(ID);
        event.participations = (HashMap) bundle.getSerializable(PARTICIPATIONS);
        return event;
    }
}
