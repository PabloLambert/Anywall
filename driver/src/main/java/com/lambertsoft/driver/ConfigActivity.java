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

    private ImageView imgDriver, imgSchool;
    private TextView textDriverName, textChannelName, textSchoolName;
    ImageButton btnFromTimePicker, btnToTimePicker;
    private TextView textFrom_InitDate, textFrom_EndDate, textToDate;

    private Button btnDriveSave;

    private Bitmap bitmap;
    public DriverDetail driverDetail;

    int fromInitHourDay = DEFAULT_HOUR_DAY, fromInitMinutes = DEFAULT_MINUTES;
    int fromEndHourDay = DEFAULT_HOUR_DAY, fromEndMinutes = DEFAULT_MINUTES+30;
    int toHourDay = DEFAULT_HOUR_DAY, toMinutes = DEFAULT_MINUTES;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        imgDriver = (ImageView) findViewById(R.id.imgDriver);
        textDriverName = (TextView) findViewById(R.id.textDriverName);
        textChannelName = (TextView) findViewById(R.id.textChannelName);
        textSchoolName = (TextView) findViewById(R.id.textSchoolName);

        textFrom_InitDate = (TextView) findViewById(R.id.textFrom_InitDate);
        textFrom_EndDate = (TextView) findViewById(R.id.textFrom_EndDate);
        textToDate = (TextView) findViewById(R.id.textToDate);
        //btnFromTimePicker = (ImageButton) findViewById(R.id.btnFromTimePicker);
        btnToTimePicker = (ImageButton) findViewById(R.id.btnToTimePicker);
        imgSchool = (ImageView) findViewById(R.id.imgSchool);

        btnDriveSave = (Button) findViewById(R.id.btnDriverSave);

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
                startActivity(intent);
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
                Intent intent = new Intent(ConfigActivity.this, SchoolListActivity.class);
                startActivity(intent);
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
            ParseQuery<School> query2 = ParseQuery.getQuery("School");
            List<School> sList = query2.find();
            MainActivity.mapSchool.clear();
            for (School s : sList) {
                MainActivity.mapSchool.put(s.getObjectId(), s);
            }

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

    public void show() {

        textDriverName.setText(ParseUser.getCurrentUser().getUsername().toString());
        textChannelName.setText(driverDetail.getChannel());
        if (driverDetail.getSchool() != null ) {
            School school = driverDetail.getSchool();
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
        String sObj = (String) textSchoolName.getTag();
        if (sObj != null ) {
            driverDetail.setSchool(MainActivity.mapSchool.get(sObj));
        }

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
