package com.parse.anywall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.IOException;
import java.util.List;


public class PlacesActivity extends FragmentActivity {

    EditText textPlaceName, textPlaceDirection;
    Button btnAddPlace;
    ImageButton btnSearchDirection;
    GoogleMap googleMap;
    private ParseGeoPoint geoPoint;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        textPlaceName = (EditText) findViewById(R.id.textPlaceName);
        textPlaceDirection = (EditText) findViewById(R.id.textPlanceDirection);
        btnAddPlace = (Button) findViewById(R.id.btnAddPlace);
        btnSearchDirection = (ImageButton) findViewById(R.id.btnSearchDirection);


        textPlaceName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateButtonState();


            }
        });
        textPlaceDirection.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateButtonState();

            }
        });

        btnSearchDirection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int direction = textPlaceDirection.getText().toString().length();

                if (direction > 0) {
                    new GeocoderTask().execute(textPlaceDirection.getText().toString().trim());
                } else {
                    Toast.makeText(getApplicationContext(), "Ingresar Dirección...", Toast.LENGTH_SHORT).show();

                }
            }
        });

        btnAddPlace.setEnabled(false);
        btnAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });



        SupportMapFragment supportMapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map_places);

        // Getting a reference to the map
        googleMap = supportMapFragment.getMap();
    }

    public void updateButtonState() {
        int placeNameLength = textPlaceName.getText().toString().length();
        int placeDirectionLength = textPlaceDirection.getText().toString().length();

        boolean enabled = placeDirectionLength > 0 && placeNameLength > 0 && geoPoint != null;

        btnAddPlace.setEnabled(enabled);
    }

    public void saveData() {
        String name = textPlaceName.getText().toString().trim();
        String direction = textPlaceDirection.getText().toString().trim();

        // Set up a progress dialog
        final ProgressDialog dialog = new ProgressDialog(PlacesActivity.this);
        dialog.setMessage("Guardando Lugar...");
        dialog.show();

        // Create a post.
        Places p = new Places();

        // Set the location to the current user's location
        p.setName(name);
        p.setDirection(direction);
        p.setLocation(geoPoint);

        ParseUser user = ParseUser.getCurrentUser();
        p.setACL(new ParseACL(user));

        // Save the Student
        p.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                finish();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_places, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 1 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if(addresses==null || addresses.size()==0){
                Toast.makeText(getBaseContext(), "No Location found", Toast.LENGTH_SHORT).show();
            }

            // Clears all the existing markers on the map
            googleMap.clear();

            // Adding Markers on Google Map for each matching address
            for(int i=0;i<addresses.size();i++){

                Address address = (Address) addresses.get(i);

                // Creating an instance of GeoPoint, to display in Google Map
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                geoPoint = new ParseGeoPoint(address.getLatitude(), address.getLongitude());

                String addressText = String.format("%s, %s",
                        address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
                        address.getCountryName());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                markerOptions.title(addressText);

                googleMap.addMarker(markerOptions);

                // Locate the first location
                if(i==0)
                    googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            }
            updateButtonState();
        }
    }

}
