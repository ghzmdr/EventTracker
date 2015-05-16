package com.ghzmdr.eventtracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class JSONParser {

    public static final String DATE_PATTERN = "yyyy-MM-dd";

    // function get json from url
    // by making HTTP POST or GET mehtod
    public JSONObject makeHttpRequest(String url, String method,
                                      List<NameValuePair> params) {
        InputStream is = null;
        JSONObject jObj = null;
        String json = "";
        // Making HTTP request
        try {

            // check for request method
            if(method == "POST"){
                // request method is POST
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new UrlEncodedFormEntity(params));

                HttpResponse httpResponse = httpClient.execute(httpPost);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            }else if(method == "GET"){
                // request method is GET
                DefaultHttpClient httpClient = new DefaultHttpClient();
                String paramString = URLEncodedUtils.format(params, "utf-8");
                url += "?" + paramString;
                HttpGet httpGet = new HttpGet(url);
                Log.i("JSON PARSER", "URL: "+ url);
                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();
            }

        } catch (UnsupportedEncodingException | ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (is != null) try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "iso-8859-1"), 8);
            StringBuilder sb = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
            json = sb.toString();
        } catch (Exception e) {
            Log.e("Buffer Error", "Error converting result " + e.toString());
        }

        // try parse the string to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }

        // return JSON String
        return jObj;
    }

    protected User getUser(String userID) throws JSONException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("id", userID));


        JSONObject obj = this.makeHttpRequest("get_user_details.php", "GET", params);

        if (obj.getInt("success") == 1) {
            return new User(obj.getString("name"), obj.getString("lastName"), obj.getString("id"));
        } else throw new JSONException("UNSUCCESSFUL REQUEST");
    }

    protected Event getEvent(String eventID) throws JSONException {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("id", eventID));

        JSONObject json = this.makeHttpRequest("get_event_details.php", "GET", params);

        if (json.getInt("success") == 1) {

            SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN);

            Date date = null;
            try {
                date = sdf.parse(json.getString("date"));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            Location loc = new Location("");
            loc.setLatitude(Double.parseDouble(json.getString("lat")));
            loc.setLatitude(Double.parseDouble(json.getString("long")));

            return new Event(json.getString("id"),
                                json.getString("name"),
                                json.getString("description"),
                                date,
                                loc,
                                new User(json.getString("orgName"), json.getString("orgLastName"), json.getString("orgID")));

        } else throw new JSONException("UNSUCCESSFUL RESULT");
    }
}
