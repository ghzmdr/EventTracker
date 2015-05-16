package com.ghzmdr.eventtracker;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ghzmdr on 12/01/15.
 */
public class User {
    private String nome, cognome, ID;

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getID() {
        return ID;
    }

    public User(String nome, String cognome, String ID) {
        this.nome = nome;
        this.cognome = cognome;
        this.ID = ID;
    }
}
