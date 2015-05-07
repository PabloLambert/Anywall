package com.parse.anywall;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class PostActivity extends FragmentActivity {
  // UI references.
  private EditText postEditText;
  private TextView characterCountTextView, textHome, textName, textSchool;
  private Button postButton, btnSearchHome;
  GoogleMap googleMap;

  private int maxCharacterCount = Application.getConfigHelper().getPostMaxCharacterCount();
  private ParseGeoPoint geoPoint;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_post);

    Intent intent = getIntent();
    Location location = intent.getParcelableExtra(Application.INTENT_EXTRA_LOCATION);
    if ( location == null ) {
      geoPoint = null;
    } else {
      geoPoint = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
    }

    postButton = (Button) findViewById(R.id.post_button);
    postButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        post2();
      }
    });

    SupportMapFragment supportMapFragment = (SupportMapFragment)
            getSupportFragmentManager().findFragmentById(R.id.map_fragment_home);

    // Getting a reference to the map
    googleMap = supportMapFragment.getMap();

    textHome = (TextView) findViewById(R.id.textHome);
    textName = (TextView) findViewById(R.id.textName);
    textSchool = (TextView) findViewById(R.id.textSchool);

    textName.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        updatePostButtonState();

      }
    });
    textSchool.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void afterTextChanged(Editable editable) {
        updatePostButtonState();
      }
    });

    btnSearchHome = (Button) findViewById(R.id.btnSearchHome);
    btnSearchHome.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View view) {
        searchHome();
      }
    });

    updatePostButtonState();
  }

  private void post () {
    String text = postEditText.getText().toString().trim();

    // Set up a progress dialog
    final ProgressDialog dialog = new ProgressDialog(PostActivity.this);
    dialog.setMessage(getString(R.string.progress_post));
    dialog.show();

    // Create a post.
    AnywallPost post = new AnywallPost();

    // Set the location to the current user's location
    post.setLocation(geoPoint);
    post.setText(text);
    post.setUser(ParseUser.getCurrentUser());
    ParseACL acl = new ParseACL();

    // Give public read access
    acl.setPublicReadAccess(true);
    post.setACL(acl);

    // Save the post
    post.saveInBackground(new SaveCallback() {
      @Override
      public void done(ParseException e) {
        dialog.dismiss();
        finish();
      }
    });
  }

  private void post2 () {
    String name = textName.getText().toString().trim();
    String school = textSchool.getText().toString().trim();

    if (name == null || name.isEmpty() ) {
      Toast.makeText(getApplicationContext(), "Falta nombre", Toast.LENGTH_SHORT).show();
      return;
    }

    if (school == null || school.isEmpty() ) {
      Toast.makeText(getApplicationContext(), "Falta Colegio", Toast.LENGTH_SHORT).show();
      return;
    }

    if (geoPoint == null ) {
      Toast.makeText(getApplicationContext(), "Falta Dirección", Toast.LENGTH_SHORT).show();
      return;
    }


    // Set up a progress dialog
    final ProgressDialog dialog = new ProgressDialog(PostActivity.this);
    dialog.setMessage(getString(R.string.progress_post));
    dialog.show();

    // Create a post.
    Student student = new Student();

    // Set the location to the current user's location
    student.setLocation(geoPoint);
    student.setName(name);
    student.setSchool(school);

    // Save the Student
    student.saveInBackground(new SaveCallback() {
      @Override
      public void done(ParseException e) {
        dialog.dismiss();
        finish();
      }
    });
  }

  private void searchHome() {
    String direction = textHome.getText().toString().trim();

    Toast.makeText(getApplicationContext(), "Buscando dirección..." + direction, Toast.LENGTH_SHORT).show();

    if(direction!=null && !direction.equals("")){
      new GeocoderTask().execute(direction);
    }

  }

  private String getPostEditTextText () {
    return postEditText.getText().toString().trim();
  }

  private void updatePostButtonState () {
    int lengthName = textName.getText().toString().trim().length();
    int lengthSchool = textSchool.getText().toString().trim().length();

    boolean enabled = lengthName > 0 && lengthSchool > 0 && geoPoint != null;
    postButton.setEnabled(enabled);
  }

  private void updateCharacterCountTextViewText () {
    String characterCountString = String.format("%d/%d", postEditText.length(), maxCharacterCount);
    characterCountTextView.setText(characterCountString);
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
      updatePostButtonState();
    }
  }


}

