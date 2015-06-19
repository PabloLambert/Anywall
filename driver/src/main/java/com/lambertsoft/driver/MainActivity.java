package com.lambertsoft.driver;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.lambertsoft.base.DriverDetail;
import com.lambertsoft.base.Places;
import com.lambertsoft.base.School;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MainActivity extends FragmentActivity implements LocationListener,
        GooglePlayServicesClient.ConnectionCallbacks,
        GooglePlayServicesClient.OnConnectionFailedListener{

    private final static String TAG = MainActivity.class.getSimpleName();
    /*
     * Define a request code to send to Google Play services This code is returned in
     * Activity.onActivityResult
     */
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    public final static int ALARM_ARRIVED = 1;

    /*
     * Constants for location update parameters
     */
    // Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;

    // The update interval
    private static final int UPDATE_INTERVAL_IN_SECONDS = 30;

    // A fast interval ceiling
    private static final int FAST_CEILING_IN_SECONDS = 1;

    // Update interval in milliseconds
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;

    // A fast ceiling of update intervals, used when the app is visible
    private static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;

    /*
     * Constants for handling location results
     */
    // Conversion from feet to meters
    private static final float METERS_PER_FEET = 0.3048f;

    // Conversion from kilometers to meters
    private static final int METERS_PER_KILOMETER = 1000;

    // Initial offset for calculating the map bounds
    private static final double OFFSET_CALCULATION_INIT_DIFF = 1.0;

    // Accuracy for calculating the map bounds
    private static final float OFFSET_CALCULATION_ACCURACY = 0.01f;

    // Maximum results returned from a Parse query
    private static final int MAX_POST_SEARCH_RESULTS = 20;

    // Maximum post search radius for map in kilometers
    private static final int MAX_POST_SEARCH_DISTANCE = 100;

    private static final int STATE_WAITING  = 0;
    private static final int STATE_ONTRAVEL = 1;
    private static final int STATE_FINISHED = 2;

    /*
     * Other class member variables
     */
    // Map fragment
    private SupportMapFragment mapFragment;

    // Represents the circle around a map
    private Circle mapCircle;

    // Fields for the map radius in feet
    private float radius;
    private float lastRadius;

    // Fields for helping process map and location changes
    private int mostRecentMapUpdate;
    private boolean hasSetUpInitialLocation;
    private String selectedPostObjectId;
    private Location lastLocation;
    private Location currentLocation;

    // A request to connect to Location Services
    private LocationRequest locationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient locationClient;

    private Button btnStart, btnStop, btnCount, btnConfig;
    private TextView txtState, txtChannel, txtNextEvent, txtGeoEvent;
    private int count = 0;
    Pubnub pubnub = Application.pubnub;
    JSONObject obj;

    DriverDetail driverDetail;
    Evt evt = new Evt(-1, -1 );

    private int countSchool = 0;
    public static Map<String, School> mapSchool = new HashMap<String, School>();
    public static Map<String, Places> mapPlaces = new HashMap<String, Places>();

    private Map<String, Marker> mapMarkers = new HashMap<String, Marker>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        radius = 250.0f;
        lastRadius = radius;
        setContentView(R.layout.activity_main);

        // Create a new global location parameters object
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        // Set the interval ceiling to one minute
        locationRequest.setFastestInterval(FAST_INTERVAL_CEILING_IN_MILLISECONDS);

        // Create a new location client, using the enclosing class to handle callbacks.
        locationClient = new LocationClient(this, this, this);

        // Set up the map fragment
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        // Enable the current location "blue dot"
        mapFragment.getMap().setMyLocationEnabled(true);
        // Set up the camera change handler


        btnConfig = (Button) findViewById(R.id.btnConfig);
        btnConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                startActivity(intent);
            }
        });

       btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locationClient.connect();

            }
        });

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationClient.isConnected()) {
                    stopPeriodicUpdates();
                }

                // After disconnect() is called, the client is considered "dead".
                locationClient.disconnect();
            }
        });

        txtState = (TextView)findViewById(R.id.txtState);

        txtNextEvent = (TextView) findViewById(R.id.txtNextEvent);

        txtChannel = (TextView) findViewById(R.id.txtChannel);

        txtGeoEvent = (TextView) findViewById(R.id.txtGeoEvents);

        btnCount = (Button) findViewById(R.id.btnCount);

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    while(!isInterrupted()) {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateStatus();
                            }
                        });

                    }

                } catch (Exception e) {

                }
            }

        };

        t.start();
    }

    public void updateStatus() {

        if (driverDetail == null) {
            return;
        }

        Evt newEvt = getTimeForNextEvent(driverDetail);

        if (evt.state == STATE_WAITING && newEvt.state == STATE_ONTRAVEL) {
            Log.d(TAG, "From Waiting to OnTravel");
            locationClient.connect();
        } else if (evt.state == STATE_ONTRAVEL && newEvt.state == STATE_FINISHED) {
            Log.d(TAG, "From OnTravel to Finish");
            // If the client is connected
            if (locationClient.isConnected()) {
                stopPeriodicUpdates();
            }
            locationClient.disconnect();
        }
        evt = newEvt;

        //Update UI
        txtState.setText(evt.getStateName());

        // NextEvent
        long min = evt.t/(60*1000);
        long sec = (evt.t%(60*1000))/1000;
        txtNextEvent.setText( min + " [min] " + sec + " [sec]" );

        // Channel
        txtChannel.setText(driverDetail.getChannel());

        txtGeoEvent.setText("count = " + count);

    }





    public Evt getTimeForNextEvent(DriverDetail driverDetail) {
        Calendar fromInit = Calendar.getInstance();
        fromInit.set(Calendar.HOUR_OF_DAY, driverDetail.getFromInitHourOfDay());
        fromInit.set(Calendar.MINUTE, driverDetail.getFromInitMinutes());
        fromInit.set(Calendar.SECOND, 0);
        long fromInitMinutes = fromInit.getTimeInMillis();

        Calendar fromEnd = Calendar.getInstance();
        fromEnd.set(Calendar.HOUR_OF_DAY, driverDetail.getFromEndHourOfDay());
        fromEnd.set(Calendar.MINUTE, driverDetail.getFromEndMinutes());
        fromEnd.set(Calendar.SECOND, 0);
        long fromEndMinutes = fromEnd.getTimeInMillis();

        long rightNow = Calendar.getInstance().getTimeInMillis();
        Evt evt;

        if (rightNow < fromInitMinutes ) {
            evt = new Evt( fromInitMinutes - rightNow, STATE_WAITING);
            return evt;
        } else if ((rightNow >= fromInitMinutes ) &&  (rightNow < fromEndMinutes) ) {
            evt = new Evt( fromEndMinutes - rightNow, STATE_ONTRAVEL);
            return evt;
        } else if  ( rightNow >= fromEndMinutes ) {
            evt = new Evt( 0, STATE_FINISHED);
            return evt;
        }
        return evt = new Evt(-1,-1);

    }

    private class Evt {
        long t;
        int state;

        public Evt(long _t, int _state) {
            t = _t;
            state = _state;
        }

        public String getStateName() {

            String sName;
            switch (state) {
                case STATE_WAITING:
                    sName = "STATE_WAITING";
                    break;
                case STATE_ONTRAVEL:
                    sName = "STATE_ONTRAVEL";
                    break;
                case STATE_FINISHED:
                    sName = "STATE_FINISHED";
                    break;
                default:
                    sName = "Undefined";
            }
            return sName;
        }

    }

    /*
  * Called when the Activity is no longer visible at all. Stop updates and disconnect.
  */

    @Override
    public void onStop() {

        // If the client is connected
        if (locationClient.isConnected()) {
            //stopPeriodicUpdates();
        }

        // After disconnect() is called, the client is considered "dead".
        //locationClient.disconnect();

        super.onStop();
    }


    /*
     * Called when the Activity is restarted, even before it becomes visible.
     */

    @Override
    public void onStart() {
        super.onStart();

        if ( evt.state == STATE_ONTRAVEL) {
            // Connect to the location services client
            locationClient.connect();
        }
    }


    /*
     * Called when the Activity is resumed. Updates the view.
     */
    @Override
    protected void onResume() {
        super.onResume();

        // Get the latest search distance preference
        radius = 250.0f;
        // Checks the last saved location to show cached data if it's available
        if (lastLocation != null) {
            LatLng myLatLng = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
            // If the search distance preference has been changed, move
            // map to new bounds.
            if (lastRadius != radius) {
                updateZoom(myLatLng);
            }
            // Update the circle map
            updateCircle(myLatLng);
        }
        // Save the current radius
        lastRadius = radius;
        // Query for the latest data to update the views.

        mapFragment.getMap().clear();
        mapMarkers.clear();

        try {

            ParseQuery<DriverDetail> query = ParseQuery.getQuery("DriverDetail");
            List<DriverDetail> list = query.find();

            if (list.size() > 0) {
                    driverDetail = list.get(0);

            } else {
                    driverDetail = new DriverDetail();
                /*
                    ParseUser user = ParseUser.getCurrentUser();
                    ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
                    driverDetail.setChannel(user.getSessionToken());
                    driverDetail.setACL(acl);
                    driverDetail.save();
                    */
            }

            ParseQuery<School> querySchool = ParseQuery.getQuery("School");
            List<School> sList = querySchool.find();
            for (School s : sList) {
                mapSchool.put(s.getObjectId(), s);
            }

        } catch (ParseException e){
            Toast.makeText(getApplicationContext(), "Error en obtener DriverDetail" + e.toString(), Toast.LENGTH_SHORT).show();

        }


        /*
        ParseQuery<DriverDetail> query = ParseQuery.getQuery("DriverDetail");
        query.findInBackground(new FindCallback<DriverDetail>() {
            @Override
            public void done(List<DriverDetail> list, ParseException e) {
                if (e != null) {

                    Toast.makeText(getApplicationContext(), "Error en obtener DriverDetail" + e.toString(), Toast.LENGTH_SHORT).show();

                } else {
                    if (list.size() > 0) {
                        driverDetail = list.get(0);

                    } else {
                        driverDetail = new DriverDetail();
                        ParseUser user = ParseUser.getCurrentUser();
                        ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
                        driverDetail.setChannel(user.getSessionToken());
                        driverDetail.setACL(acl);
                        driverDetail.saveInBackground();
                    }

                }
            }
        });

        ParseQuery<School> querySchool = ParseQuery.getQuery("School");
        querySchool.findInBackground(new FindCallback<School>() {
            @Override
            public void done(List<School> list, ParseException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), "Error en obtener School" + e.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    for (School s : list) {
                        mapSchool.put(s.getObjectId(), s);
                    }

                }
            }
        });
        */
        updatePlaces();
        //updateAlarm();

    }

    public static void updatePlaces() {
        ParseQuery<Places> queryPlaces = ParseQuery.getQuery("Places");


        try {
            List<Places> list = queryPlaces.find();
            for (Places p : list) {
                mapPlaces.put(p.getObjectId(), p);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        /*
        queryPlaces.findInBackground(new FindCallback<Places>() {
            @Override
            public void done(List<Places> list, ParseException e) {
                if (e != null) {
                    //Toast.makeText(this, "Error en obtener Places" + e.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    for (Places p : list) {
                        mapPlaces.put(p.getObjectId(), p);
                    }

                }
            }
        });
        */

    }

    /*
    private void updateAlarm() {

        AlarmManager alarmMgr;
        PendingIntent alarmIntent;

        alarmMgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT|Intent.FLAG_ACTIVITY_CLEAR_TOP);
        alarmIntent = PendingIntent.getActivity(getApplicationContext(), ALARM_ARRIVED , intent, 0);

        alarmMgr.cancel(alarmIntent);

        long t = getTimeForNextEvent(driverDetail);
        if (t > 0 ) {
            alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                    SystemClock.elapsedRealtime() + t, alarmIntent);
        }

    }
    */

    /*
  * Displays a circle on the map representing the search radius
  */
    private void updateCircle(LatLng myLatLng) {
        if (mapCircle == null) {
            mapCircle =
                    mapFragment.getMap().addCircle(
                            new CircleOptions().center(myLatLng).radius(radius * METERS_PER_FEET));
            int baseColor = Color.DKGRAY;
            mapCircle.setStrokeColor(baseColor);
            mapCircle.setStrokeWidth(2);
            mapCircle.setFillColor(Color.argb(50, Color.red(baseColor), Color.green(baseColor),
                    Color.blue(baseColor)));
        }
        mapCircle.setCenter(myLatLng);
        mapCircle.setRadius(radius * METERS_PER_FEET); // Convert radius in feet to meters.
    }

    /*
     * Zooms the map to show the area of interest based on the search radius
     */
    private void updateZoom(LatLng myLatLng) {
        // Get the bounds to zoom to
        LatLngBounds bounds = calculateBoundsWithCenter(myLatLng);
        // Zoom to the given bounds
        mapFragment.getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 5));
    }



    /*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ALARM_ARRIVED && resultCode == RESULT_OK) {

            if (driverDetail != null) {
                long l = getTimeForNextEvent(driverDetail);
            }
            switch (state) {
                case STATE_WAITING:
                    break;

                case STATE_ONTRAVEL:
                    locationClient.connect();
                    break;

                case STATE_FINISHED:
                    if (locationClient.isConnected()) {
                        stopPeriodicUpdates();
                    }

                    // After disconnect() is called, the client is considered "dead".
                    locationClient.disconnect();

                    break;
                default:
            }

            updateAlarm();
            Log.d(TAG, "alarm received!!");

        }
    }
    */

    /*
 * In response to a request to start updates, send a request to Location Services
 */
    private void startPeriodicUpdates() {
        locationClient.requestLocationUpdates(locationRequest, this);
    }

    /*
     * In response to a request to stop updates, send a request to Location Services
     */
    private void stopPeriodicUpdates() {
        locationClient.removeLocationUpdates(this);
    }

    /*
     * Get the current location
     */
    private Location getLocation() {
        // If Google Play Services is available
        if (servicesConnected()) {
            // Get the current location
            return locationClient.getLastLocation();
        } else {
            return null;
        }
    }

    /*
  * Verify that Google Play services is available before making a request.
  *
  * @return true if Google Play services is available, otherwise false
  */
    private boolean servicesConnected() {
        // Check that Google Play services is available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            return true;
            // Google Play services was not available for some reason
        } else {
            // Display an error dialog
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this, 0);
            if (dialog != null) {
                ErrorDialogFragment errorFragment = new ErrorDialogFragment();
                errorFragment.setDialog(dialog);
                errorFragment.show(getSupportFragmentManager(), TAG);
            }
            return false;
        }
    }

    /*
  * Helper method to calculate the bounds for map zooming
  */
    LatLngBounds calculateBoundsWithCenter(LatLng myLatLng) {
        // Create a bounds
        LatLngBounds.Builder builder = LatLngBounds.builder();

        // Calculate east/west points that should to be included
        // in the bounds
        double lngDifference = calculateLatLngOffset(myLatLng, false);
        LatLng east = new LatLng(myLatLng.latitude, myLatLng.longitude + lngDifference);
        builder.include(east);
        LatLng west = new LatLng(myLatLng.latitude, myLatLng.longitude - lngDifference);
        builder.include(west);

        // Calculate north/south points that should to be included
        // in the bounds
        double latDifference = calculateLatLngOffset(myLatLng, true);
        LatLng north = new LatLng(myLatLng.latitude + latDifference, myLatLng.longitude);
        builder.include(north);
        LatLng south = new LatLng(myLatLng.latitude - latDifference, myLatLng.longitude);
        builder.include(south);

        return builder.build();
    }
    /*
      * Helper method to calculate the offset for the bounds used in map zooming
      */
    private double calculateLatLngOffset(LatLng myLatLng, boolean bLatOffset) {
        // The return offset, initialized to the default difference
        double latLngOffset = OFFSET_CALCULATION_INIT_DIFF;
        // Set up the desired offset distance in meters
        float desiredOffsetInMeters = radius * METERS_PER_FEET;
        // Variables for the distance calculation
        float[] distance = new float[1];
        boolean foundMax = false;
        double foundMinDiff = 0;
        // Loop through and get the offset
        do {
            // Calculate the distance between the point of interest
            // and the current offset in the latitude or longitude direction
            if (bLatOffset) {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude
                        + latLngOffset, myLatLng.longitude, distance);
            } else {
                Location.distanceBetween(myLatLng.latitude, myLatLng.longitude, myLatLng.latitude,
                        myLatLng.longitude + latLngOffset, distance);
            }
            // Compare the current difference with the desired one
            float distanceDiff = distance[0] - desiredOffsetInMeters;
            if (distanceDiff < 0) {
                // Need to catch up to the desired distance
                if (!foundMax) {
                    foundMinDiff = latLngOffset;
                    // Increase the calculated offset
                    latLngOffset *= 2;
                } else {
                    double tmp = latLngOffset;
                    // Increase the calculated offset, at a slower pace
                    latLngOffset += (latLngOffset - foundMinDiff) / 2;
                    foundMinDiff = tmp;
                }
            } else {
                // Overshot the desired distance
                // Decrease the calculated offset
                latLngOffset -= (latLngOffset - foundMinDiff) / 2;
                foundMax = true;
            }
        } while (Math.abs(distance[0] - desiredOffsetInMeters) > OFFSET_CALCULATION_ACCURACY);
        return latLngOffset;
    }

    /*
   * Helper method to get the Parse GEO point representation of a location
   */
    private ParseGeoPoint geoPointFromLocation(Location loc) {
        return new ParseGeoPoint(loc.getLatitude(), loc.getLongitude());
    }

    /*
   * Show a dialog returned by Google Play services for the connection error code
   */
    private void showErrorDialog(int errorCode) {
        // Get the error dialog from Google Play services
        Dialog errorDialog =
                GooglePlayServicesUtil.getErrorDialog(errorCode, this,
                        CONNECTION_FAILURE_RESOLUTION_REQUEST);

        // If Google Play services can provide an error dialog
        if (errorDialog != null) {

            // Create a new DialogFragment in which to show the error dialog
            ErrorDialogFragment errorFragment = new ErrorDialogFragment();

            // Set the dialog in the DialogFragment
            errorFragment.setDialog(errorDialog);

            // Show the error dialog in the DialogFragment
            errorFragment.show(getSupportFragmentManager(), TAG);
        }
    }



    // Methods implementation

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("Connected to location services", TAG);
        currentLocation = getLocation();
        startPeriodicUpdates();
    }

    @Override
    public void onDisconnected() {
        Log.d("Disconnected from location services", TAG);

    }

    @Override
    public void onLocationChanged(Location location) {

        if (driverDetail == null ) return;

        currentLocation = location;
        count++;


        /* Publish a simple message to the channel */
        JSONObject message = new JSONObject();
        try {
            message.put("lat", location.getLatitude());
            message.put("lng", location.getLongitude());
            message.put("alt", location.getAltitude());
            message.put("count", count);
        } catch (JSONException e) {
            Log.e(TAG, e.toString());
        }


        pubnub.publish(driverDetail.getChannel(), message, new Callback() {
        });

        if (lastLocation != null
                && geoPointFromLocation(location)
                .distanceInKilometersTo(geoPointFromLocation(lastLocation)) < 0.01) {
            // If the location hasn't changed by more than 10 meters, ignore it.
            return;
        }
        lastLocation = location;
        LatLng myLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (!hasSetUpInitialLocation) {
            // Zoom to the current location.
            updateZoom(myLatLng);
            hasSetUpInitialLocation = true;
        }
        // Update map radius indicator
        updateCircle(myLatLng);
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Google Play services can resolve some errors it detects. If the error has a resolution, try
        // sending an Intent to start a Google Play services activity that can resolve error.
        if (connectionResult.hasResolution()) {
            try {

                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);

            } catch (IntentSender.SendIntentException e) {

                    // Thrown if Google Play services canceled the original PendingIntent
                    Log.d(TAG, "An error occurred when connecting to location services.", e);

            }
        } else {
            // If no resolution is available, display a dialog to the user with the error.
            showErrorDialog(connectionResult.getErrorCode());
        }
    }


    // New Class

    public static class ErrorDialogFragment extends DialogFragment {
        // Global field to contain the error dialog
        private Dialog mDialog;

        /**
         * Default constructor. Sets the dialog field to null
         */
        public ErrorDialogFragment() {
            super();
            mDialog = null;
        }

        /*
         * Set the dialog to display
         *
         * @param dialog An error dialog
         */
        public void setDialog(Dialog dialog) {
            mDialog = dialog;
        }

        /*
         * This method must return a Dialog to the DialogFragment.
         */
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return mDialog;
        }
    }
}
