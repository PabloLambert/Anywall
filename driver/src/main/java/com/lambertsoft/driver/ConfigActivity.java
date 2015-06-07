package com.lambertsoft.driver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.lambertsoft.base.DriverDetail;
import com.lambertsoft.base.School;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class ConfigActivity extends Activity {

    final static public int REQUEST_IMAGE_CAPTURE = 0;

    private ImageView imgDriver;
    private TextView textDriverName, textChannelName;
    private Spinner spinnerSchool;

    private Button btnDriveSave;
    private ImageButton btnAddSchool;

    private Bitmap bitmap;
    private DriverDetail driverDetail;
    ArrayList<String> schoolList;
    ArrayAdapter<String> adapterSchool;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        imgDriver = (ImageView) findViewById(R.id.imgDriver);
        textDriverName = (TextView) findViewById(R.id.textDriverName);
        textChannelName = (TextView) findViewById(R.id.textChannelName);
        spinnerSchool = (Spinner) findViewById(R.id.spinnerSchool);

        btnDriveSave = (Button) findViewById(R.id.btnDriverSave);
        btnAddSchool = (ImageButton) findViewById(R.id.btnAddSchool);


        imgDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        schoolList = new ArrayList<String>();
        adapterSchool = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, schoolList);
        spinnerSchool.setAdapter(adapterSchool);

        btnAddSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(ConfigActivity.this, SchoolActivity.class);
                startActivity(intent);

            }
        });

        btnDriveSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();


        final ProgressDialog dialog = new ProgressDialog(ConfigActivity.this);
        dialog.setMessage("Configurando");
        dialog.show();

        ParseQuery<DriverDetail> query = ParseQuery.getQuery("DriverDetail");
        query.findInBackground(new FindCallback<DriverDetail>() {
            @Override
            public void done(List<DriverDetail> list, ParseException e) {
                if (e != null) {
                    Toast.makeText(getApplicationContext(), "Error en obtener DriverDetail", Toast.LENGTH_SHORT).show();
                } else {
                    if (list.size() > 0) {
                        driverDetail = list.get(0);
                        ParseFile file = driverDetail.getPhoto();
                        if (file != null) {
                            file.getDataInBackground(new GetDataCallback() {
                                @Override
                                public void done(byte[] bytes, ParseException e) {
                                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    imgDriver.setImageBitmap(bitmap);

                                }
                            });
                        }

                    } else {
                        driverDetail = new DriverDetail();
                        ParseUser user = ParseUser.getCurrentUser();
                        ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
                        driverDetail.setChannel(user.getSessionToken());
                        driverDetail.setACL(acl);
                    }

                }
                show();
                dialog.dismiss();
            }
        });

        schoolList.clear();
        ParseQuery<School> query2 = ParseQuery.getQuery("School");
        query2.findInBackground(new FindCallback<School>() {
            @Override
            public void done(List<School> list, ParseException e) {
                for (School s : list) {
                    schoolList.add(s.getName());
                }
                adapterSchool.notifyDataSetChanged();
            }
        });
    }

    public void show() {

        textDriverName.setText(ParseUser.getCurrentUser().getUsername().toString());
        textChannelName.setText(driverDetail.getChannel());
        for (int i=0; i < schoolList.size(); i++) {
            if (driverDetail.getSchool() == null) return;
            if (schoolList.get(i).compareTo(driverDetail.getSchool().getName()) == 0)
                spinnerSchool.setSelection(i);
        }
    }

    public void saveData() {


        final ProgressDialog dialog = new ProgressDialog(ConfigActivity.this);
        dialog.setMessage("Guardando configuración");
        dialog.show();

        driverDetail.setChannel(textChannelName.getText().toString());

        if ( bitmap != null ) {
            // Convert it to byte
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Compress image to lower quality scale 1 - 100
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] image = stream.toByteArray();

            // Create the ParseFile
            ParseFile file = new ParseFile("student_" + ParseUser.getCurrentUser().getUsername().toString() + ".png", image);
            // Upload the image into Parse Cloud
            file.saveInBackground();
            driverDetail.setPhoto(file);
        }

        driverDetail.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();
                finish();
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            imgDriver.setImageBitmap(bitmap);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_config, menu);
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
}
