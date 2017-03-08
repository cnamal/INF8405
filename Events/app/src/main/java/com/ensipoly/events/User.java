package com.ensipoly.events;


public class User {

    public static final String USER_ID_KEY_PREFERENCE = "user_id";

    public String username;
    public String photoUrl;

    public User(String name,String url){
        username = name;
        photoUrl =url;
    }
}
