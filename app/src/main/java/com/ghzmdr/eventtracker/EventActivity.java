package com.ghzmdr.eventtracker;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class EventActivity extends ActionBarActivity {
    @InjectView(R.id.event_activity_toolbar) Toolbar tb;
    @InjectView(R.id.event_description) TextView eventDescr;
    @InjectView(R.id.event_address) TextView eventAddress;
    @InjectView(R.id.event_city) TextView eventCity;
    @InjectView(R.id.event_date) TextView eventDate;
    @InjectView(R.id.event_org) TextView eventOrganizator;

    private Event refEvent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        refEvent = EventHolder.event;
        init();
    }

    private void init() {
        ButterKnife.inject(this);

        setSupportActionBar(tb);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        User org = refEvent.getOrg();

        Location position = refEvent.getPosition();
        Geocoder gc = new Geocoder(this, Locale.getDefault());

        setTitle(refEvent.getName());
        eventDescr.setText(refEvent.getDesc());
        eventOrganizator.setText(org.getNome() + " " + org.getCognome());
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
        eventDate.setText(sdf.format(refEvent.getDate()));
        try {
            Address ad = gc.getFromLocation(position.getLatitude(), position.getLongitude(), 1).get(0);
            eventAddress.setText(ad.getAddressLine(0));
            eventCity.setText(ad.getLocality());
        } catch (IOException e) {
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_event, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share_event) {
            return true;
        } else if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
