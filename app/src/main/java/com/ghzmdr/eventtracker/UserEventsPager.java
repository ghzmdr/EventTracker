package com.ghzmdr.eventtracker;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Ghizmo on 07/01/2015.
 */
public class UserEventsPager extends FragmentPagerAdapter {

    private final String[] TITLES = {"IN ARRIVO", "SALVATI", "PASSATI"};

    public UserEventsPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return TITLES[position];
    }

    @Override
    public int getCount() {
        return TITLES.length;
    }

    @Override
    public Fragment getItem(int position) {
        return TestFragment.newInstance(position, TITLES[position]);
    }
}
