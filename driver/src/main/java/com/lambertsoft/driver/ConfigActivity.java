package com.lambertsoft.driver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.lambertsoft.base.DriverDetail;
import com.lambertsoft.base.Places;
import com.lambertsoft.base.School;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;


public class ConfigActivity extends Activity {

    final static public String TAG = ConfigActivity.class.getSimpleName();

    final static public int REQUEST_IMAGE_CAPTURE = 0;
    final static public int REQUEST_SCHOOL_OBJECT = 1;
    final static public int REQUEST_PLACES_OBJECT = 2;


    final static public Calendar c = Calendar.getInstance();
    final static public int DEFAULT_HOUR_DAY = c.get(Calendar.HOUR_OF_DAY);
    final static public int DEFAULT_MINUTES = c.get(Calendar.MINUTE);

    private ImageView imgDriver, imgSchool, imgPlaces;
    private TextView textSchoolName;
    private EditText textDriverAlias;
    ImageButton btnFromTimePicker;
    private TextView textFrom_InitDate, textFrom_EndDate;

    private Button btnDriveSave;

    private Bitmap bitmap;
    public DriverDetail driverDetail;
    public School actualSchool;
    public Places actualPlaces;

    int fromInitHourDay = DEFAULT_HOUR_DAY, fromInitMinutes = DEFAULT_MINUTES;
    int fromEndHourDay = DEFAULT_HOUR_DAY, fromEndMinutes = DEFAULT_MINUTES+30;
    int toHourDay = DEFAULT_HOUR_DAY, toMinutes = DEFAULT_MINUTES;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        imgDriver = (ImageView) findViewById(R.id.imgDriver);
        textDriverAlias = (EditText) findViewById(R.id.textDriverAlias);
        textSchoolName = (TextView) findViewById(R.id.textSchoolName);

        textFrom_InitDate = (TextView) findViewById(R.id.textFrom_InitDate);
        textFrom_EndDate = (TextView) findViewById(R.id.textFrom_EndDate);
        //btnFromTimePicker = (ImageButton) findViewById(R.id.btnFromTimePicker);
        imgSchool = (ImageView) findViewById(R.id.imgSchool);
        imgPlaces = (ImageView) findViewById(R.id.imgPlaces);

        btnDriveSave = (Button) findViewById(R.id.btnDriverSave);

        imgPlaces.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfigActivity.this, PlacesActivity.class);
                if (actualPlaces == null) {
                    intent.putExtra(PlacesActivity.PLACES_ACTION, PlacesActivity.PLACES_ACTION_CREATE);
                    startActivityForResult(intent, REQUEST_PLACES_OBJECT);
                } else {
                    intent.putExtra(PlacesActivity.PLACES_ACTION, PlacesActivity.PLACES_ACTION_MODIFY);
                    intent.putExtra(PlacesActivity.PLACES_OBJECT_ID, actualPlaces.getObjectId());
                    startActivityForResult(intent, REQUEST_PLACES_OBJECT);
                }
            }
        });


        imgDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        textSchoolName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfigActivity.this, SchoolListActivity.class);
                if (actualSchool != null ) {
                    intent.putExtra(SchoolListActivity.SCHOOL_OBJECT_ID, actualSchool.getObjectId());
                }
                startActivityForResult(intent, REQUEST_SCHOOL_OBJECT);
            }
        });

        textFrom_InitDate.setText(showTime(DEFAULT_HOUR_DAY, DEFAULT_MINUTES));
        final TimePickerDialog timePickerFromInit = new TimePickerDialog(ConfigActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                fromInitHourDay = i;
                fromInitMinutes = i1;
                textFrom_InitDate.setText(showTime(fromInitHourDay, fromInitMinutes));
            }
        },0,0,false);

        textFrom_InitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerFromInit.updateTime(fromInitHourDay, fromInitMinutes);
                timePickerFromInit.show();
            }
        });

        textFrom_EndDate.setText(showTime(DEFAULT_HOUR_DAY, DEFAULT_MINUTES));
        final TimePickerDialog timePickerFromEnd = new TimePickerDialog(ConfigActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                fromEndHourDay = i;
                fromEndMinutes = i1;
                textFrom_EndDate.setText(showTime(fromEndHourDay, fromEndMinutes));
            }
        },0,0,false);

        textFrom_EndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerFromEnd.updateTime(fromEndHourDay, fromEndMinutes);
                timePickerFromEnd.show();
            }
        });

        imgSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ConfigActivity.this, SchoolActivity.class);
                if (actualSchool != null) {
                    intent.putExtra(SchoolActivity.SCHOOL_ACTION, SchoolActivity.SCHOOL_ACTION_VIEW);
                    intent.putExtra(SchoolActivity.SCHOOL_OBJECT_ID, actualSchool.getObjectId());
                    startActivity(intent);
                }
            }
        });

        btnDriveSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveData();
            }
        });

        final ProgressDialog dialog = new ProgressDialog(ConfigActivity.this);
        dialog.setMessage("Configurando");
        dialog.show();

        try {

            ParseQuery<DriverDetail> queryDriver = ParseQuery.getQuery("DriverDetail");
            queryDriver.whereEqualTo("channel", ParseUser.getCurrentUser().getObjectId());
            List<DriverDetail> driverDetailList = queryDriver.find();
            if (driverDetailList.size() > 0) {
                driverDetail = driverDetailList.get(0);
                ParseFile file = driverDetail.getPhoto();
                if (file != null) {
                    byte[] bytes = file.getData();
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imgDriver.setImageBitmap(bitmap);
                }
                if ( driverDetail.getSchool() != null ) {
                    String schoolObjId = driverDetail.getSchool().getObjectId();
                    if (schoolObjId != null)
                        actualSchool = MainActivity.mapSchool.get(schoolObjId);
                }
                if (driverDetail.getPlaces() != null ) {
                    String placesObjId = driverDetail.getPlaces().getObjectId();
                    if (placesObjId != null )
                        actualPlaces = MainActivity.mapPlaces.get(placesObjId);
                }

            } else {
                driverDetail = new DriverDetail();
                driverDetail.setAlias("Tio");
                driverDetail.setChannel(ParseUser.getCurrentUser().getObjectId());
                Collection<School> collSchool = MainActivity.mapSchool.values();
                actualSchool = collSchool.iterator().next();
                driverDetail.setSchool(actualSchool);
                actualPlaces = null;
                //driverDetail.setPlaces(actualPlaces);
                driverDetail.setFromInitHourOfDay(fromInitHourDay);
                driverDetail.setFromInitMinutes(fromInitMinutes);
                driverDetail.setFromEndHourOfDay(fromEndHourDay);
                driverDetail.setFromEndMinutes(fromEndMinutes);
                driverDetail.setToHourOfDay(toHourDay);
                driverDetail.setToMinutes(toMinutes);
            }


        } catch ( Exception e) {
            Toast.makeText(getApplicationContext(), "Error onResume " +e.toString(), Toast.LENGTH_SHORT).show();
        }

        show();
        dialog.dismiss();

    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    public void show() {

        textDriverAlias.setText(driverDetail.getAlias());
        if (driverDetail.getSchool() != null ) {
            School school = MainActivity.mapSchool.get(driverDetail.getSchool().getObjectId());
            textSchoolName.setText(school.getName());
            textSchoolName.setTag(school.getObjectId());
        }

        fromInitHourDay = driverDetail.getFromInitHourOfDay();
        fromInitMinutes = driverDetail.getFromInitMinutes();
        textFrom_InitDate.setText(showTime(fromInitHourDay, fromInitMinutes));

        fromEndHourDay = driverDetail.getFromEndHourOfDay();
        fromEndMinutes = driverDetail.getFromEndMinutes();
        textFrom_EndDate.setText(showTime(fromEndHourDay, fromEndMinutes));

        toHourDay = driverDetail.getToHourOfDay();
        toMinutes = driverDetail.getToMinutes();

    }


    public String showTime(int hour, int min) {
        String format;

        if (hour == 0) {
            hour += 12;
            format = "AM";
        } else if (hour == 12) {
            format = "PM";
        } else if (hour > 12) {
            hour -= 12;
            format = "PM";
        } else {
            format = "AM";
        }
    /*
    String _h = "";
    if (hour < 10 ) _h.concat("0");
    _h.concat(Integer.toString(hour));
    String _m = "";
    if (min < 10 ) _m.concat("0");
    _m.concat(Integer.toString(min));
    */

        StringBuilder sb = new StringBuilder().append(hour).append(" : ").append(min).append(" ").append(format);
        return sb.toString();
    }


    public void saveData() {


        final ProgressDialog dialog = new ProgressDialog(ConfigActivity.this);
        dialog.setMessage("Guardando configuración");
        dialog.show();

        driverDetail.setAlias(textDriverAlias.getText().toString());
        driverDetail.setChannel(ParseUser.getCurrentUser().getObjectId());
        String sObj = (String) textSchoolName.getTag();
        if (sObj != null ) {
            driverDetail.setSchool(MainActivity.mapSchool.get(sObj));
        } else {
            Toast.makeText(getApplicationContext(), "Error en Schoolname", Toast.LENGTH_SHORT).show();
        }
        if (actualPlaces != null )
            driverDetail.setPlaces(actualPlaces);

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

        driverDetail.setFromInitHourOfDay(fromInitHourDay);
        driverDetail.setFromInitMinutes(fromInitMinutes);
        driverDetail.setFromEndHourOfDay(fromEndHourDay);
        driverDetail.setFromEndMinutes(fromEndMinutes);
        driverDetail.setToHourOfDay(toHourDay);
        driverDetail.setToMinutes(toMinutes);

        ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
        acl.setPublicReadAccess(true);
        driverDetail.setACL(acl);


        driverDetail.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                dialog.dismiss();

                if ( e != null ) {
                    Toast.makeText(getApplicationContext(), "Error: " +e.toString(), Toast.LENGTH_SHORT).show();
                } else {
                    finish();
                }
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            bitmap = (Bitmap) extras.get("data");
            imgDriver.setImageBitmap(bitmap);
        } else if (requestCode == REQUEST_SCHOOL_OBJECT && resultCode == RESULT_OK) {
            String sObjId = data.getStringExtra(SchoolListActivity.SCHOOL_OBJECT_ID);
            actualSchool = MainActivity.mapSchool.get(sObjId);
            textSchoolName.setText(actualSchool.getName());
            textSchoolName.setTag(actualSchool.getObjectId());
        } else if (requestCode == REQUEST_PLACES_OBJECT && resultCode == RESULT_OK) {
            String sObjId = data.getStringExtra(PlacesActivity.PLACES_OBJECT_ID);
            actualPlaces = MainActivity.mapPlaces.get(sObjId);
            Log.d(TAG, "actualPlaces = " + actualPlaces.getName());
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
