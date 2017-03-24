package com.ensipoly.events.models;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Group {

    private String organizer;
    private Map<String, Boolean> members;

    private Map<String, Boolean> locations;

    private String event;

    public Group() {

    }

    @Exclude
    public int getNbUsers() {
        return members.size();
    }

    @Exclude
    public boolean allMembersVotes() {
        for (Boolean b : members.values())
            if (!b)
                return false;
        return true;
    }

    public Map<String, Boolean> getMembers() {
        return members;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String org) {
        organizer = org;
    }

    public void setMembers(Map<String, Boolean> members) {
        this.members = members;
    }

    public Map<String, Boolean> getLocations() {
        return locations;
    }

    public String getEvent() {
        return event;
    }

}
