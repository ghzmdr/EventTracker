package com.ghzmdr.eventtracker;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by ghzmdr on 15/01/15.
 */
public class UserHolder {
    private static final String PREF_HOME = "HomePref";
    private static final String LOCATION_HOME = "HOME LOCATION";
    private static UserHolder ref = null;
    public static User user = null;

    public static UserHolder getReference() {
        if (ref == null) ref = new UserHolder();
        return ref;
    }

    public static LatLng getHome(Activity parentActivity){
        SharedPreferences userHome = parentActivity.getSharedPreferences(PREF_HOME, 0);
        String result, lat, lng;
        result = userHome.getString(LOCATION_HOME, "");
        Log.i("USER HOLDER", "result: " + result);
        if (result.equals("")) return null;

        int divider =result.indexOf('?');
        lat = result.substring(0, divider);
        lng = result.substring(divider+1, result.length()-1);
        Log.i("USER HOLDER", "LAT=" + lat);
        return new LatLng(Double.parseDouble(lat), Double.parseDouble(lng));
    }

    public static void setHome(Context parentActivity, Location l){
        SharedPreferences userHome = parentActivity.getSharedPreferences(PREF_HOME, 0);
        SharedPreferences.Editor edit = userHome.edit();
        edit.putString(LOCATION_HOME, l.getLatitude() + "?" + l.getLongitude());
        edit.commit();
    }
}
