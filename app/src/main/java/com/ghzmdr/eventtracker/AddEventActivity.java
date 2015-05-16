package com.ghzmdr.eventtracker;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class AddEventActivity extends ActionBarActivity {
    public static final int REQUEST_CODE = 1;

    private static final int RESULT_PICK_IMAGE = 9002;
    private static final int RESULT_PICK_DATE= 9002;

    @InjectView(R.id.add_event_activity_toolbar) Toolbar tb;
    @InjectView(R.id.add_event_name) EditText name;
    @InjectView(R.id.add_event_desc) EditText desc;
    @InjectView(R.id.add_event_address) EditText address;
    @InjectView(R.id.add_event_city) EditText city;
    private Location locationAddress;
    @InjectView(R.id.btn_pick_date) Button dateBtn;
//    @InjectView(R.id.btn_pick_img) Button imgBtn;

    @InjectView(R.id.add_event_activity_confirm) Button confirm;
    @InjectView(R.id.add_event_activity_cancel) Button cancel;

    private DBConnector db;
    private Bitmap pickedImage;
    private static Date pickedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);
        ButterKnife.inject(this);
        db = DBConnector.getReference();
        setSupportActionBar(tb);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Aggiungi evento");
        setClickListeners();
    }

    private void setClickListeners() {
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });
        /*imgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage();
            }
        });*/

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()){
                    sendEvent();
                } else {
                    displayError();
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private boolean validateData() {
        ArrayList<EditText> check = new ArrayList<>();

        check.add(name);
        check.add(desc);
        check.add(address);

        for (EditText et : check){
            if (et.getText().toString() == null || et.getText().toString().equals(""))
                return false;
        }
        //add && pickedImage != null
        return pickedDate != null && checkAddress(address.getText().toString() + ", " + city.getText().toString());
    }

    private boolean checkAddress(String s) {
        Location loc;
        loc = Locator.getLocationFromAddress(getApplicationContext(), s);

        if (loc != null) {
            locationAddress = loc;
            return true;
        }

        return false;
    }

    private void sendEvent() {
        String nome = name.getText().toString(), descr = desc.getText().toString();
        String id = generateEventID(nome, pickedDate.toString());
        pickedDate.setYear(pickedDate.getYear() - 1900);
        Log.i("DATE", pickedDate.toString());
        new SendEventTask().execute(new Event(id, nome, descr, pickedDate, locationAddress, UserHolder.user));
    }

    private String generateEventID(String nome, String s) {
        return Math.floor(Math.random()*1000) + nome + Math.floor(Math.random()*100) + s + Math.floor(Math.random()* 1000);
    }

    private void displayError(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(AddEventActivity.this);

        alertDialog.setTitle("Campi non corretti");
        alertDialog.setMessage("Assicurarsi di aver inserito tutte le informazioni");
        alertDialog.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
        alertDialog.show();
    }

    public void pickImage(){
        Intent i = new Intent(
        Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, RESULT_PICK_IMAGE);
    }

    public void pickDate(){
        Calendar today = GregorianCalendar.getInstance();
        new DatePickerDialog(this, new DatePickerListener(), today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH)).show();
    }

    public class DatePickerListener implements DatePickerDialog.OnDateSetListener {


        public void onDateSet(DatePicker view, int year, int month, int day) {
            if (year > 3000) year -= 1900;
            pickedDate = new Date(year, month, day);
        }
    }

    private class SendEventTask extends AsyncTask<Event, Void, Boolean>{
        @Override
        protected Boolean doInBackground(Event... params) {
            return db.sendEvent(params[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                Toast.makeText(AddEventActivity.this, "Evento Inviato", Toast.LENGTH_SHORT).show();
            } else Toast.makeText(AddEventActivity.this, "Evento Non Inviato", Toast.LENGTH_SHORT).show();
            AddEventActivity.this.finish();
        }
    }
}
