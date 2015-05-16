package com.ghzmdr.eventtracker;

import android.support.v7.widget.SearchView;

/**
 * Created by ghzmdr on 12/01/15.
 */
public class EventSearchListener implements SearchView.OnQueryTextListener {
    @Override
    public boolean onQueryTextSubmit(String query) {
        if (query.length() != 0) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
}
