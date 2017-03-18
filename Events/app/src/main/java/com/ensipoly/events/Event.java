package com.ensipoly.events;

import java.util.Date;

/**
 * Created by Adrien on 17/03/2017.
 */

public class Event {

    private Group group;                    // The group associated with the event
    private String name;                    // The name of the event
    private String info;                    // Info for the event
    private String place;                   // The place for the event
    private Date startingDate, endingDate;  // Two dates start and end (with the time)

    public Event(Group group, String name, String info, String place, Date startingDate, Date endingDate){
        this.group = group;
        this.name = name;
        this.info = info;
        this.place = place;
        this.startingDate = startingDate;
        this.endingDate = endingDate;
    }

    public String getName(){
        return name;
    }

    public Group getGroup(){
        return group;
    }

    public String getInfo(){
        return info;
    }

    public String getPlace(){
        return place;
    }

    public Date getStartingDate(){
        return startingDate;
    }

    public Date getEndingDate(){
        return endingDate;
    }

    @Override
    public String toString(){
        return "Event : {name=" + name + "; info=" + info + "; place=" + place +
                "; startingDate=" + startingDate.toString() +
                "; endingDate=" + endingDate.toString() + "}";
    }

}
