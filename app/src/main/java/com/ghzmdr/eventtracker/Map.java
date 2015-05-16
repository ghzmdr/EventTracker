package com.ghzmdr.eventtracker;

import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by ghzmdr on 14/01/15.
 */
public class Map {

    final static int ZOOM_DEFAULT = 15;
    final static int SEEKBAR_OFFSET = 12;
    private int zoom = ZOOM_DEFAULT;
    private Geocoder gcd;

    private GoogleMap map;
    private Locator locator;
    private final Activity parentActivity;
    private SeekBar sb;
    private TextView title;
    private ListView lv;
    private ArrayList<Event> eventsToShow;

    private DBConnector db;
    private UpdateMarkersTask markersTask;

    public Map(GoogleMap map, final Activity parentActivity, SeekBar sb, TextView title, ListView lv) {
        EventHolder.markerEventMap = new HashMap<>();
        markersTask = new UpdateMarkersTask();
        this.lv = lv;
        this.map = map;
        this.parentActivity = parentActivity;
        this.sb = sb;
        this.title = title;
        db = DBConnector.getReference();
        gcd= new Geocoder(parentActivity.getApplicationContext(), Locale.getDefault());
        sb.setOnSeekBarChangeListener(new MapOnSeekBarChangeListener());

        map.getUiSettings().setAllGesturesEnabled(false);
        map.getUiSettings().setZoomControlsEnabled(false);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(parentActivity, EventActivity.class);
                EventHolder.event = eventsToShow.get(position);

                if (EventHolder.event != null) parentActivity.startActivity(i); else
                    Toast.makeText(parentActivity.getApplicationContext(), "NullEvent" + eventsToShow.get(position), Toast.LENGTH_SHORT).show();
            }
        });

        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                Intent i = new Intent(parentActivity, EventActivity.class);
                EventHolder.event = EventHolder.markerEventMap.get(marker);

                if (EventHolder.event != null)
                    parentActivity.startActivity(i);
                else Log.i("MAP ", "NullEvent: " + EventHolder.markerEventMap.get(marker));

                return false;
            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                updateMarkers();
            }
        });

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                updateMarkers();
            }
        });

        locator = Locator.getLocator(parentActivity);
    }

    public void centerOnUser() {
        try {
            updateMapPosition(locator.getLocation());
        } catch (Locator.NoPositionProvidersException e) {
            locator.showSettingsAlert();
        }
    }

    public void updateMapPosition(LatLng coords) {
        zoom = sb.getProgress() + SEEKBAR_OFFSET;
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(coords, zoom));
        sb.setProgress(zoom - SEEKBAR_OFFSET);

        updateCityTitle(coords);
        //updateMarkers();
    }

    private void updateCityTitle(LatLng coords) {
        String city = "";

        try {
            List<Address> addresses = gcd.getFromLocation(coords.latitude, coords.longitude, 1);
            city = addresses.get(0).getLocality();
        } catch (IOException e) {
            e.printStackTrace();
        }

        title.setText(city);
    }

    private void updateMapZoom(int progress) {
        map.moveCamera(CameraUpdateFactory.zoomTo(progress + SEEKBAR_OFFSET));
        //updateMarkers();
    }

    public void updateMarkers() {

        if (!(markersTask.getStatus() == AsyncTask.Status.RUNNING)) {
            markersTask = new UpdateMarkersTask();
            markersTask.execute(map.getProjection().getVisibleRegion().latLngBounds);
        }
    }

    public void unregisterUpdates() {
        locator.unregisterUpdates();
    }

    public void registerUpdates() {
        locator.registerUpdates();
    }


    private class MapOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            updateMapZoom(progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }


    private class EventAdapter extends ArrayAdapter<Event> {
        public EventAdapter(Activity parentActivity, ArrayList<Event> eventsToShow) {
            super(parentActivity, 0, eventsToShow);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Event e = getItem(position);
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.event_list_item, parent, false);
             TextView tv = (TextView) convertView.findViewById(R.id.event_list_item_title);
            tv.setText(e.getName());
            return convertView;
        }
    }

    private class UpdateMarkersTask extends AsyncTask<LatLngBounds, Void, ArrayList<Event>> {

        @Override
        protected ArrayList<Event> doInBackground(LatLngBounds... params) {

            eventsToShow = new ArrayList<>();

            try {
                eventsToShow = db.getEventsInBounds(params[0]);
            } catch (JSONException | ParseException e) {
                e.printStackTrace();
            }

            return eventsToShow;
        }

        @Override
        protected void onPostExecute(ArrayList<Event> eventsToShow) {
            for (Event ev : eventsToShow) {
                Marker m = map.addMarker(new MarkerOptions()
                        .position(new LatLng(ev.getPosition().getLatitude(), ev.getPosition().getLongitude()))
                        .title(ev.getName()));

                EventHolder.markerEventMap.put(m, ev);
                Log.d("MAP", "Put Event: " + ev.getID());
            }


           lv.setVisibility(View.INVISIBLE);
           if (eventsToShow.size() > 0) {
               lv.setVisibility(View.VISIBLE);
               lv.setAdapter(new EventAdapter(parentActivity, eventsToShow));
           }

        }
    }
}
