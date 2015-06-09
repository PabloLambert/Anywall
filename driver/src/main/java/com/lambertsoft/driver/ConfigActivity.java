package com.lambertsoft.driver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.lambertsoft.base.DriverDetail;
import com.lambertsoft.base.School;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConfigActivity extends Activity {

    final static public int REQUEST_IMAGE_CAPTURE = 0;
    final static public Calendar c = Calendar.getInstance();
    final static public int DEFAULT_HOUR_DAY = c.get(Calendar.HOUR_OF_DAY);
    final static public int DEFAULT_MINUTES = c.get(Calendar.MINUTE);

    private ImageView imgDriver;
    private TextView textDriverName, textChannelName;
    private Spinner spinnerSchool;
    ImageButton btnFromTimePicker, btnToTimePicker;
    private TextView textFrom_InitDate, textToDate;

    private Button btnDriveSave;
    private ImageButton btnAddSchool;

    private Bitmap bitmap;
    private DriverDetail driverDetail;
    ArrayList<String> schoolList;
    ArrayAdapter<String> adapterSchool;
    int fromHourDay = DEFAULT_HOUR_DAY, fromMinutes = DEFAULT_MINUTES;
    int toHourDay = DEFAULT_HOUR_DAY, toMinutes = DEFAULT_MINUTES;
    School actualSchool;
    public static Map<String, School> mapSchool = new HashMap<String, School>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        imgDriver = (ImageView) findViewById(R.id.imgDriver);
        textDriverName = (TextView) findViewById(R.id.textDriverName);
        textChannelName = (TextView) findViewById(R.id.textChannelName);
        spinnerSchool = (Spinner) findViewById(R.id.spinnerSchool);
        textFrom_InitDate = (TextView) findViewById(R.id.textFrom_InitDate);
        textToDate = (TextView) findViewById(R.id.textToDate);
        //btnFromTimePicker = (ImageButton) findViewById(R.id.btnFromTimePicker);
        btnToTimePicker = (ImageButton) findViewById(R.id.btnToTimePicker);

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

        textFrom_InitDate.setText(showTime(DEFAULT_HOUR_DAY, DEFAULT_MINUTES));
        final TimePickerDialog timePickerFrom = new TimePickerDialog(ConfigActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                fromHourDay = i;
                fromMinutes = i1;
                textFrom_InitDate.setText(showTime(fromHourDay, fromMinutes));
            }
        },0,0,false);

        textFrom_InitDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerFrom.updateTime(fromHourDay, fromMinutes);
                timePickerFrom.show();
            }
        });

        textToDate.setText(showTime(toHourDay, toMinutes));
        final TimePickerDialog timePickerTo = new TimePickerDialog(ConfigActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                toHourDay = i;
                toMinutes = i1;
                textToDate.setText(showTime(toHourDay, toMinutes));
            }
        },0,0,false);
        textToDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timePickerTo.updateTime(toHourDay, toMinutes);
                timePickerTo.show();
            }
        });

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

        try {
            schoolList.clear();
            ParseQuery<School> query2 = ParseQuery.getQuery("School");
            List<School> sList = query2.find();
            for (School s : sList) {
                schoolList.add(s.getName());
                mapSchool.put(s.getName(), s);
            }
            adapterSchool.notifyDataSetChanged();

            ParseQuery<DriverDetail> query = ParseQuery.getQuery("DriverDetail");
            List<DriverDetail> driverDetailList = query.find();
            if (driverDetailList.size() > 0) {
                driverDetail = driverDetailList.get(0);
                ParseFile file = driverDetail.getPhoto();
                if (file != null) {
                    byte[] bytes = file.getData();
                    bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imgDriver.setImageBitmap(bitmap);
                }
                School s = driverDetail.getSchool();
                s.fetch();

            } else {
                driverDetail = new DriverDetail();
                driverDetail.setChannel(ParseUser.getCurrentUser().getObjectId());
                driverDetail.setFromHourOfDay(fromHourDay);
                driverDetail.setFromMinutes(fromMinutes);
                driverDetail.setToHourOfDay(toHourDay);
                driverDetail.setToMinutes(toMinutes);
            }


        } catch ( Exception e) {
            Toast.makeText(getApplicationContext(), "Error onResume " +e.toString(), Toast.LENGTH_SHORT).show();
        }

        show();
        dialog.dismiss();
    }

    public void show() {

        textDriverName.setText(ParseUser.getCurrentUser().getUsername().toString());
        textChannelName.setText(driverDetail.getChannel());
        for (int i=0; i < schoolList.size(); i++) {
            if (driverDetail.getSchool() != null ) {
                if (schoolList.get(i).compareTo(driverDetail.getSchool().getName()) == 0) {
                    spinnerSchool.setSelection(i);
                    actualSchool = mapSchool.get(schoolList.get(i));
                }
            }
        }

        fromHourDay = driverDetail.getFromHourOfDay();
        fromMinutes = driverDetail.getFromMinutes();
        textFrom_InitDate.setText(showTime(fromHourDay, fromMinutes));
        toHourDay = driverDetail.getToHourOfDay();
        toMinutes = driverDetail.getToMinutes();
        textToDate.setText(showTime(toHourDay, toMinutes));

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

        String schoolName = (String) spinnerSchool.getSelectedItem();
        for (int i=0; i < schoolList.size(); i++) {
            if (schoolList.get(i).compareTo(schoolName) == 0) {
                actualSchool = mapSchool.get(schoolList.get(i));
            }
        }

        driverDetail.setSchool(actualSchool);
        driverDetail.setFromHourOfDay(fromHourDay);
        driverDetail.setFromMinutes(fromMinutes);
        driverDetail.setToHourOfDay(toHourDay);
        driverDetail.setToMinutes(toMinutes);

        ParseACL acl = new ParseACL(ParseUser.getCurrentUser());
        driverDetail.setACL(acl);

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
