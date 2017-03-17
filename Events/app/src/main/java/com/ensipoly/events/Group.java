package com.ensipoly.events;

import com.google.firebase.database.Exclude;

import java.util.Map;

public class Group {

    private String organizer;
    private Map<String,Boolean> members;

    public Group(){

    }

    @Exclude
    public int getNbUsers() {
        return members.size();
    }

    public Map<String, Boolean> getMembers(){
        return members;
    }

    public String getOrganizer(){
        return organizer;
    }

    public void setOrganizer(String org){
        organizer = org;
    }

    public void setMembers(Map<String,Boolean> members) {
        this.members = members;
    }
}
