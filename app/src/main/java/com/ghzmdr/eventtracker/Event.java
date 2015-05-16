package com.ghzmdr.eventtracker;

import android.location.Location;

import java.util.Date;

/**
 * Created by ghzmdr on 12/01/15.
 */
public class Event {

    private final String ID;
    private String name, desc;
    private Date date;
    private Location position;
    private User org;

    public Event(String ID, String name, String desc, Date date, Location position, User org) {
        this.ID = ID;
        this.name = name;
        this.desc = desc;
        this.date = date;
        this.position = position;
        this.org = org;
    }


    public String getID() {
        return ID;
    }

    public String getName() {
        return name;
    }

    public String getDesc() {
        return desc;
    }

    public Date getDate() {
        return date;
    }

    public Location getPosition() {
        return position;
    }

    public User getOrg() {
        return org;
    }
}
