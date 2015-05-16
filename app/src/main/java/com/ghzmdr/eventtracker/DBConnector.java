package com.ghzmdr.eventtracker;

import android.location.Location;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLngBounds;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by ghzmdr on 13/01/15.
 */
public class DBConnector{
    private static DBConnector reference;
    public static DBConnector getReference() { return reference == null ? reference = new DBConnector() : reference;}

    public ArrayList<Event> getEventsInBounds(LatLngBounds bounds) throws JSONException, ParseException {
        JSONParser jParser = new JSONParser();

        ArrayList<Event> events = new ArrayList<>();
        JSONArray jevents = null;

        ArrayList<NameValuePair> params = new ArrayList<>();

        params.add(new BasicNameValuePair("longmin", ""+ bounds.southwest.longitude));
        params.add(new BasicNameValuePair("longmax", ""+ bounds.northeast.longitude));
        params.add(new BasicNameValuePair("latmin", "" + bounds.southwest.latitude));
        params.add(new BasicNameValuePair("latmax",  ""+ bounds.northeast.latitude));

        JSONObject json = jParser.makeHttpRequest("http://webservice.vincentdefeo.me/get_all_events.php", "GET", params);


        if (json.getInt("success") == 1) {

            jevents = json.getJSONArray("events");

            for (int i = 0; i < jevents.length(); i++) {
                JSONObject ev = jevents.getJSONObject(i);
                Location l = new Location("");
                l.setLongitude(Double.parseDouble(ev.getString("longitude")));
                l.setLatitude(Double.parseDouble(ev.getString("latitude")));
                SimpleDateFormat sdf = new SimpleDateFormat(JSONParser.DATE_PATTERN);
                Date evDate = sdf.parse(ev.getString("date"));
                events.add(new Event(ev.getString("id"), ev.getString("name"), ev.getString("description"), evDate, l, new User(ev.getString("orgName"), ev.getString("orgLastName"), ev.getString("orgID"))));
            }

        }

        return events;
    }

    protected boolean sendEvent(Event ev){
        JSONParser parser = new JSONParser();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("id", ev.getID()));
        params.add(new BasicNameValuePair("name", ev.getName()));
        params.add(new BasicNameValuePair("desc", ev.getDesc()));
        SimpleDateFormat sdf = new SimpleDateFormat(JSONParser.DATE_PATTERN);
        params.add(new BasicNameValuePair("date", sdf.format(ev.getDate())));
        params.add(new BasicNameValuePair("long", Double.toString(ev.getPosition().getLongitude())));
        params.add(new BasicNameValuePair("lat",Double.toString(ev.getPosition().getLatitude())));
        params.add(new BasicNameValuePair("org", ev.getOrg().getID()));

        JSONObject json = parser.makeHttpRequest("http://webservice.vincentdefeo.me/create_event.php", "GET", params);
        Log.i("DB CONNECTOR", " JSON: " + json.toString());

        try {
            if(json.getInt("success") == 1)
                return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    protected boolean sendUser(User u){
        JSONParser parser = new JSONParser();
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("id", u.getID()));
        params.add(new BasicNameValuePair("name", u.getNome()));
        params.add(new BasicNameValuePair("lastName", u.getCognome()));

        JSONObject json = 	parser.makeHttpRequest("http://webservice.vincentdefeo.me/create_user.php", "POST", params);

        try {
            if(json.getInt("success") == 1)
                return true;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }
}
