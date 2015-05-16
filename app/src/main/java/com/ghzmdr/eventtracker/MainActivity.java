package com.ghzmdr.eventtracker;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.EditText;
import android.widget.ImageButton;
import android.support.v7.widget.SearchView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.plus.PlusClient;
import com.shamanland.fab.FloatingActionButton;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.InputStream;
import java.net.URL;


import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends ActionBarActivity implements  GoogleApiClient.ConnectionCallbacks, GooglePlayServicesClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    protected final static String TAG = "Event Tracker -> MainActivity";
    private static final int REQUEST_RESOLVE_ERR = 9001;

    protected DBConnector dbConn;
    private PlusClient plusCli;
    private ConnectionResult connectionResult;
    private ProgressDialog connectionProgressDialog;
    private UserHolder userHolder;


    final static OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
    final static AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

    private Map map;

    @InjectView(R.id.btn_exit) protected ImageButton exit;
    @InjectView(R.id.btn_info) protected ImageButton eula;
    @InjectView(R.id.toolbar) Toolbar tb;
    @InjectView(R.id.drawer_layout) DrawerLayout drawer;
    @InjectView(R.id.fab_add_event)FloatingActionButton fabAdd;
    @InjectView(R.id.btn_add_event)ImageButton btnAdd;
    @InjectView(R.id.sliding_layout) SlidingUpPanelLayout bottomPanel;
    //@InjectView(R.id.pager) ViewPager pager;
    //@InjectView(R.id.tabs)PagerSlidingTabStrip tabs;
    @InjectView(R.id.action_bar_title) TextView title;
    @InjectView(R.id.username) TextView userName;
    @InjectView(R.id.userphoto_image) ImageView userLogo;
    @InjectView(R.id.userphoto_cover) ImageView userCover;
    @InjectView(R.id.seekBar)SeekBar sb;
    @InjectView(R.id.home_shortcut) View homeShortcut;
    @InjectView(R.id.btn_edit_home) ImageButton editHome;
    @InjectView(R.id.event_list) ListView lv;

    SearchView searchView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);

        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (!(netInfo != null && netInfo.isConnected())) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Quest'applicazione necessita di una connesione internet").setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    });

            builder.create().show();
        } else {
            initPlus();

            userHolder = UserHolder.getReference();
            dbConn = new DBConnector();

            exit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    plusCli.revokeAccessAndDisconnect(new PlusClient.OnAccessRevokedListener() {
                        @Override
                        public void onAccessRevoked(ConnectionResult connectionResult) {
                            Toast.makeText(getApplicationContext(), "Log Out effettuato", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }
            });

            eula.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showEulaDialog();
                }
            });

            editHome.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showInputAddressDialog();
                }
            });
            EventHolder.getReference();

            initGui();
        }
    }

    private void showInputAddressDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Immetti un Indirizzo");
        final EditText userInput = new EditText(this);
        userInput.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        userInput.setHint("Via, Numero, Città");
        userInput.setTextSize(18);
        builder.setView(userInput);
        builder.setCancelable(false)
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String address = userInput.getText().toString();
                        if (address != null && address != "") {
                            Location home = Locator.getLocationFromAddress(getApplicationContext(), address);
                            if (home != null) UserHolder.setHome(getApplicationContext(), home);
                            else
                                Toast.makeText(getApplicationContext(), "Indirizzo non trovato, riprovare", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showEulaDialog() {
        TextView eula = new TextView(this);
        eula.setText("Applicazione realizzata da: Vincent De Feo \n\nWebService realizzato da: Tommaso Bello\n\nDocumentazione prodotta da: Luca, Mara, Attilio ");
        eula.setTextSize(20);
        eula.setPadding(10,5,10,5);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(eula).setTitle("EULA e Crediti").setCancelable(false)
               .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                   @Override
                   public void onClick(DialogInterface dialog, int which) {
                       dialog.cancel();
                   }
               });

        builder.create().show();
    }

    protected void initGui() {

        //TOOLBAR SETUP
        tb.setTitle("");
        setSupportActionBar(tb);

        //LEFT DRAWER
        final ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawer, tb, R.string.app_name, R.string.app_name);
        drawer.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        //BUTTONS
        btnAdd.setVisibility(View.INVISIBLE);
        fabAdd.setOnClickListener(new AddEventClickListener());
        btnAdd.setOnClickListener(new AddEventClickListener());

        //BOTTOM PANEL
        bottomPanel.setDragView(findViewById(R.id.bottom_drawer_topbar));
        bottomPanel.setPanelSlideListener(new CustomPanelSlideListener());

        //SEEKBAR
        sb.setMax(7);
        sb.setProgress(5);
        map = new Map(((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap(), MainActivity.this, sb, title, lv);

        homeShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LatLng position = UserHolder.getHome(MainActivity.this);
                if (position == null){
                    Toast.makeText(MainActivity.this, "Imposta un indirizzo prima!", Toast.LENGTH_SHORT).show();
                } else {
                    map.updateMapPosition(position);
                    drawer.closeDrawers();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (plusCli != null)
        if (!plusCli.isConnected()) {
            ConnectivityManager cm =
                    (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && netInfo.isConnected())
            plusCli.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    if (plusCli != null)        plusCli.disconnect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESOLVE_ERR && resultCode == RESULT_OK){
            connectionResult = null;
            if (!plusCli.isConnected()) plusCli.connect();
        }
    }

    private void initPlus() {
        plusCli = new PlusClient.Builder(this, this, this).build();
        connectionProgressDialog = new ProgressDialog(this);
        connectionProgressDialog.setMessage("Log In...");
        connectionProgressDialog.show();
    }

    @Override
    public void onConnected(Bundle bundle) {
        connectionProgressDialog.dismiss();

        String nome = plusCli.getCurrentPerson().getName().getGivenName();
        String cognome = plusCli.getCurrentPerson().getName().getFamilyName();
        String ID = plusCli.getCurrentPerson().getId();

        userHolder.user = new User(nome, cognome, ID);
        new SendUserTask().execute(userHolder.user);

        userName.setText(plusCli.getCurrentPerson().getDisplayName());
        new GetImagesTask().execute(new Pair<String, ImageView>(plusCli.getCurrentPerson().getImage().getUrl(), userLogo));
        new GetImagesTask().execute(new Pair<String, ImageView>(plusCli.getCurrentPerson().getCover().getCoverPhoto().getUrl(), userCover));

        map.centerOnUser();
    }

    private class GetImagesTask extends AsyncTask<Pair<String, ImageView>, Void, Drawable>{
        private ImageView iv;
        @Override
        protected Drawable doInBackground(Pair<String, ImageView>... params) {
            Drawable ret = new ColorDrawable(Color.WHITE);
            try {
                InputStream is = (InputStream) new URL(params[0].first).getContent();
                ret = Drawable.createFromStream(is, "src name");
            } catch (Exception e) {
            }
            iv = params[0].second;
            return ret;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            iv.setBackgroundDrawable(drawable);
        }
    }

    @Override
    public void onDisconnected() {
        Log.d(TAG, "disconnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERR);
            } catch (IntentSender.SendIntentException e) {
                if (!plusCli.isConnected()) plusCli.connect();
            }
        }

        this.connectionResult = connectionResult;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setOnQueryTextListener(new LocationSearchListener());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (map != null)        map.unregisterUpdates();
        if (plusCli != null) plusCli.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null)
        map.registerUpdates();
        if (plusCli != null && !plusCli.isConnected()) plusCli.connect();
        Log.i("MAIN ACTIVITY", "RESUME");
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            //FIX POSITION
            case (R.id.action_fix_position) :
                Log.i(TAG, "WAITING FOR POSITION");
                map.centerOnUser();
                return true;
            //SEARCH
            case (R.id.action_search) :
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (bottomPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED)
            bottomPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        else super.onBackPressed();
    }

    private void hideFAB() {

            //HIDE FAB, UNHIDE BUTTON
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(fabAdd, "scaleX", 1, 0);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(fabAdd, "scaleY", 1, 0);

            AnimatorSet animSet = new AnimatorSet();

            animSet.playTogether(scaleX, scaleY);
            animSet.setInterpolator(accelerateInterpolator);
            animSet.setDuration(100);
            animSet.start();

            fabAdd.setVisibility(View.INVISIBLE);
            btnAdd.setVisibility(View.VISIBLE);

    }

    private void showFAB() {
            //HIDE BUTTON, UNHIDE FAB
            btnAdd.setVisibility(View.INVISIBLE);
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(fabAdd, "scaleX", 0, 1);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(fabAdd, "scaleY", 0, 1);
            AnimatorSet animSetXY = new AnimatorSet();
            animSetXY.playTogether(scaleX, scaleY);
            animSetXY.setInterpolator(overshootInterpolator);
            animSetXY.setDuration(200);
            animSetXY.start();
            fabAdd.setVisibility(View.VISIBLE);
    }

    private class CustomPanelSlideListener implements SlidingUpPanelLayout.PanelSlideListener {
        @Override
        public void onPanelSlide(View view, float v) {
            hideFAB();
        }

        @Override
        public void onPanelCollapsed(View view) {
            showFAB();
        }

        @Override
        public void onPanelExpanded(View view) {}

        @Override
        public void onPanelAnchored(View view) {}

        @Override
        public void onPanelHidden(View view) {}
    }

    //LISTENER USED TO START THE ADD EVENT VIEW
    private class AddEventClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(getApplication(), AddEventActivity.class));
        }
    }

    private class LocationSearchListener implements SearchView.OnQueryTextListener {
        @Override
        public boolean onQueryTextSubmit(String query) {
            if (query.length() != 0) {
                Location l = Locator.getLocationFromAddress(MainActivity.this, query);
                if (l != null)
                    map.updateMapPosition(new LatLng(l.getLatitude(), l.getLongitude()));
                    searchView.clearFocus();
                    onBackPressed();
                    return true;
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    }

    private class SendUserTask extends AsyncTask<User, Void, Void> {
        @Override
        protected Void doInBackground(User... params) {
            dbConn.sendUser(params[0]);
            return null;
        }
    }
}
