package com.ghzmdr.eventtracker;

import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

/**
 * Created by ghzmdr on 13/01/15.
 */
public class EventHolder {
    private static EventHolder ref;
    public static Event event;
    public static HashMap<Marker, Event> markerEventMap;

    public static EventHolder getReference(){
        if (ref == null){
            ref = new EventHolder();
        }

        return ref;
    }
}
