package com.ensipoly.events;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by namalgac on 3/15/17.
 */

public class Group {

    List<String> users;

    public Group(){
        users = new ArrayList<>();
    }

    public void addUser(String id){
        users.add(id);
    }

    public int getNbUsers() {
        return users.size();
    }



}
