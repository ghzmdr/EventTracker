package com.ghzmdr.eventtracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Locale;


/**
 * Created by ghzmdr on 12/01/15.
 */
public class Locator implements android.location.LocationListener {

    private Context context;
    private LocationManager lm;
    private Location loc = null;
    private boolean gpsEnabled = false, networkEnabled = false;

    private static Locator ref;

    private Locator(Context context) {
        this.context = context;
        lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        registerUpdates();
    }

    public void registerUpdates() {
        gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        networkEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (gpsEnabled)
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 20, 1000, this);
        if (networkEnabled)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 20, 1000, this);
    }

    public void unregisterUpdates(){
        lm.removeUpdates(this);
    }

    public LatLng getLocation() throws NoPositionProvidersException {
        Toast.makeText(context, "In attsa della posizione", Toast.LENGTH_SHORT).show();

        String provider = null;
        Location tmpLoc = null;

        if (gpsEnabled) {
            provider = LocationManager.GPS_PROVIDER;
            tmpLoc = lm.getLastKnownLocation(provider);
        }

        if (networkEnabled && tmpLoc == null) {
            provider = LocationManager.NETWORK_PROVIDER;
            tmpLoc = lm.getLastKnownLocation(provider);
        }

        if (provider == null) throw new NoPositionProvidersException("Can't find position provider");

        if (tmpLoc != null) {
            if (loc == null || tmpLoc.getAccuracy() > loc.getAccuracy())
                loc = tmpLoc;
        }

        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    public static Location getLocationFromAddress(Context parentActivity, String adderss){
        Geocoder g = new Geocoder(parentActivity, Locale.getDefault());
        Address a = null;

        int tries = 5;

        for (int i = 0; i < tries; i++) {
            try {
                a = g.getFromLocationName(adderss, i+1).get(0);
                if (a == null) g.getFromLocationName(adderss, i+1).get(i);
            } catch (IOException | IndexOutOfBoundsException ignored) {
            }
        }

        if (a == null) return null;

        Location ret = new Location("");
        ret.setLatitude(a.getLatitude());
        ret.setLongitude(a.getLongitude());
        return ret;
    }

    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        // Setting Dialog Title
        alertDialog.setTitle("Servizi di localizzazione non attivi");

        // Setting Dialog Message
        alertDialog.setMessage("Attivarli nelle impostazioni");

        // Setting Icon to Dialog
        //alertDialog.setIcon(R.drawable.delete);

        // On pressing Settings button
        alertDialog.setPositiveButton("Impostazioni", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Annulla", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



    public class UnavailablePositionException extends Throwable {
        private UnavailablePositionException(String detailMessage) {
            super(detailMessage);
        }
    }

    public class NoPositionProvidersException extends Throwable {
        private NoPositionProvidersException(String detailMessage) {
            super(detailMessage);
        }
    }

    public static Locator getLocator(Context context) {
        if (ref == null) ref = new Locator(context);
        return ref;
    }
}
