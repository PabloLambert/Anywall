package com.parse.anywall;

import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.lambertsoft.base.Places;
import com.lambertsoft.base.School;
import com.parse.DeleteCallback;
import com.parse.GetDataCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */


public class StudentActivity extends FragmentActivity {

  final static public String TAG = StudentActivity.class.getSimpleName();

  final static public int REQUEST_IMAGE_CAPTURE = 0;
  final static public int REQUEST_SCHOOL_OBJECT = 1;
  final static public int REQUEST_PLACES_OBJECT = 2;

  final static public Calendar c = Calendar.getInstance();
  final static public int DEFAULT_HOUR_DAY = c.get(Calendar.HOUR_OF_DAY);
  final static public int DEFAULT_MINUTES = c.get(Calendar.MINUTE);

  final static public String STUDENT_ACTION = "STUDENT_ACTION";
  final static public String STUDENT_OBJECT_ID = "STUDENT_OBJECT";
  final static public int STUDENT_ACTION_CREATE = 1;
  final static public int STUDENT_ACTION_MODIFY = 2;

  ImageView imgStudent;
  EditText textStudentName;
  TextView textSchoolName;
  TextView textFrom_InitDate, textFrom_EndDate;
  ImageView imgSchool, imgPlaces;
  Button btnStudentAction, btnStudentDelete;

  Bitmap bitmap;

  int action;
  Student student;
  School actualSchool;
  Places actualPlaces;

  int fromInitHourDay = DEFAULT_HOUR_DAY, fromInitMinutes = DEFAULT_MINUTES;
  int fromEndHourDay = DEFAULT_HOUR_DAY, fromEndMinutes = DEFAULT_MINUTES+30;
  int toHourDay = DEFAULT_HOUR_DAY, toMinutes = DEFAULT_MINUTES;



  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_student);

    // Get configutation
    imgStudent = (ImageView) findViewById(R.id.imgStudent);
    textStudentName = (EditText) findViewById(R.id.textStudentName);
    textSchoolName = (TextView) findViewById(R.id.textSchoolName);

    textFrom_InitDate = (TextView) findViewById(R.id.textFrom_InitDate);
    textFrom_EndDate = (TextView) findViewById(R.id.textFrom_EndDate);

    imgSchool = (ImageView) findViewById(R.id.imgSchool);
    imgPlaces = (ImageView) findViewById(R.id.imgPlaces);

    btnStudentAction = (Button) findViewById(R.id.btnStudentAction);
    btnStudentDelete = (Button) findViewById(R.id.btnStudentDelete);


    // Add actions
    imgStudent.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
          startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
      }
    });

    textStudentName.addTextChangedListener(new TextWatcher() {
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

    textSchoolName.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(StudentActivity.this, SchoolListActivity.class);
        if (actualSchool != null) {
          intent.putExtra(SchoolListActivity.SCHOOL_OBJECT_ID, actualSchool.getObjectId());
        }
        startActivityForResult(intent, REQUEST_SCHOOL_OBJECT);
      }
    });


    textFrom_InitDate.setText(showTime(DEFAULT_HOUR_DAY, DEFAULT_MINUTES));
    final TimePickerDialog timePickerFromInit = new TimePickerDialog(StudentActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
    final TimePickerDialog timePickerFromEnd = new TimePickerDialog(StudentActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
        Intent intent = new Intent(StudentActivity.this, SchoolActivity.class);
        if (actualSchool != null) {
          intent.putExtra(SchoolActivity.SCHOOL_ACTION, SchoolActivity.SCHOOL_ACTION_VIEW);
          intent.putExtra(SchoolActivity.SCHOOL_OBJECT_ID, actualSchool.getObjectId());
          startActivity(intent);
        }
      }
    });

    imgPlaces.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(StudentActivity.this, PlacesActivity.class);
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



    btnStudentAction.setEnabled(false);
    btnStudentAction.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        saveData();
      }
    });

    btnStudentDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        deleteData();
      }
    });


    Intent intent = getIntent();
    action = intent.getIntExtra(STUDENT_ACTION, STUDENT_ACTION_CREATE);

    if (action == STUDENT_ACTION_CREATE) {
      btnStudentAction.setText("Agregar");
      btnStudentDelete.setEnabled(false);
    } else if (action == STUDENT_ACTION_MODIFY) {
      btnStudentAction.setText("Modificar");
      btnStudentDelete.setEnabled(true);

      student = MainActivity.mapStudent.get(intent.getStringExtra(STUDENT_OBJECT_ID));

      textStudentName.setText(student.getName());
      if (student.getSchool() != null ) {
        School school = MainActivity.mapSchool.get(student.getSchool().getObjectId());
        actualSchool = school;
        textSchoolName.setText(school.getName());
        textSchoolName.setTag(school.getObjectId());
      }

      if (student.getPlaces() != null ) {
        String placesObjId = student.getPlaces().getObjectId();
        if (placesObjId != null )
          actualPlaces = MainActivity.mapPlaces.get(placesObjId);
      }

      fromInitHourDay = student.getFromInitHourOfDay();
      fromInitMinutes = student.getFromInitMinutes();
      textFrom_InitDate.setText(showTime(fromInitHourDay, fromInitMinutes));

      fromEndHourDay = student.getFromEndHourOfDay();
      fromEndMinutes = student.getFromEndMinutes();
      textFrom_EndDate.setText(showTime(fromEndHourDay, fromEndMinutes));

      toHourDay = student.getToHourOfDay();
      toMinutes = student.getToMinutes();

      ParseFile file = student.getPhoto();
        if (file != null ) {

          file.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, ParseException e) {
              bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
              imgStudent.setImageBitmap(bitmap);

            }
          });
        }


    } else {
      Toast.makeText(getApplicationContext(), "Action Error", Toast.LENGTH_SHORT).show();
      return;
    }

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
      Bundle extras = data.getExtras();
      bitmap = (Bitmap) extras.get("data");
      imgStudent.setImageBitmap(bitmap);
    }  else if (requestCode == REQUEST_SCHOOL_OBJECT && resultCode == RESULT_OK) {
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


  public Places getPlaceFromMap(String _name) {

    for (Iterator<Places> iterator = MainActivity.mapPlaces.values().iterator(); iterator.hasNext(); ){
      Places _p = iterator.next();
      if (_p.getName().compareTo(_name) == 0) return _p;
    }
    return null;
  }


  public void updateButtonState() {
    int placeNameLength = textStudentName.getText().toString().length();

    boolean enabled = placeNameLength > 0 ;

    btnStudentAction.setEnabled(enabled);
  }

  public void saveData() {

    String name = textStudentName.getText().toString().trim();
    Student s;
    Travel tmpTravel;

    if ( action == STUDENT_ACTION_CREATE) {
      // Create a Student.
      s = new Student();


    } else if (action == STUDENT_ACTION_MODIFY) {
      // Using existing place
      s = student;
    } else {
      Toast.makeText(getApplicationContext(), "Error in saveData", Toast.LENGTH_SHORT).show();
      return;
    }

    // Set up a progress dialog
    final ProgressDialog dialog = new ProgressDialog(StudentActivity.this);
    dialog.setMessage("Guardando Estudiante...");
    dialog.show();

    ParseUser user = ParseUser.getCurrentUser();
    ParseACL acl = new ParseACL(user);

    s.setName(name);

    if ( bitmap != null ) {
      // Convert it to byte
      ByteArrayOutputStream stream = new ByteArrayOutputStream();
      // Compress image to lower quality scale 1 - 100
      bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
      byte[] image = stream.toByteArray();

      // Create the ParseFile
      ParseFile file = new ParseFile("student_" + s.getName() + ".png", image);
      // Upload the image into Parse Cloud
      file.saveInBackground();
      s.setPhoto(file);
    }

    String sObj = (String) textSchoolName.getTag();
    if (sObj != null ) {
      s.setSchool(MainActivity.mapSchool.get(sObj));
    } else {
      Toast.makeText(getApplicationContext(), "Error en Schoolname", Toast.LENGTH_SHORT).show();
    }

    if (actualPlaces != null )
      s.setPlaces(actualPlaces);

    s.setFromInitHourOfDay(fromInitHourDay);
    s.setFromInitMinutes(fromInitMinutes);
    s.setFromEndHourOfDay(fromEndHourDay);
    s.setFromEndMinutes(fromEndMinutes);
    s.setToHourOfDay(toHourDay);
    s.setToMinutes(toMinutes);

    s.setACL(acl);
    s.saveInBackground(new SaveCallback() {
      @Override
      public void done(ParseException e) {
        dialog.dismiss();
        finish();
      }
    });


  }

  public void deleteData() {
    final ProgressDialog dialog = new ProgressDialog(StudentActivity.this);
    dialog.setMessage("Eliminando Estudiante");
    dialog.show();

    student.deleteInBackground(new DeleteCallback() {
      @Override
      public void done(ParseException e) {
        dialog.dismiss();
        finish();
      }
    });
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


}

