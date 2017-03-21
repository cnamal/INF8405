package com.ensipoly.events.models;


import java.util.Map;

public class User {

    public static final String USER_ID_KEY_PREFERENCE = "user_id";

    public String username;
    public String photoUrl;
    public double latitude;
    public double longitude;
    public String lastActive;
    public Map<String,Object> groups;

    public User(){}

    public User(String name,String url,double lat,double lng, String time){
        username = name;
        photoUrl =url;
        latitude = lat;
        longitude = lng;
        lastActive = time;
    }

    @Override
    public String toString(){
        return "User={username=" + username +
                "; latitude=" + latitude +
                "; longitude=" + longitude +
                "; lastActive=" + lastActive + "}";
    }
}
