package com.parse.anywall;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */


public class StudentActivity extends FragmentActivity {

  final static public String TAG = StudentActivity.class.getSimpleName();
  final static public String STUDENT_ACTION = "STUDENT_ACTION";
  final static public String STUDENT_OBJECT_ID = "STUDENT_OBJECT";
  final static public int STUDENT_ACTION_CREATE = 1;
  final static public int STUDENT_ACTION_MODIFY = 2;
  final static public Calendar c = Calendar.getInstance();
  final static public int DEFAULT_HOUR_DAY = c.get(Calendar.HOUR_OF_DAY);
  final static public int DEFAULT_MINUTES = c.get(Calendar.MINUTE);

  EditText textStudentName;
  Button btnStudentAction, btnStudentDelete;
  ImageButton btnStudentInfo;

  TextView textFromDate, textToDate;
  Spinner spinnerFromTime, spinnerToTime;
  ImageButton btnFromTimePicker, btnToTimePicker;
  int fromHourDay = DEFAULT_HOUR_DAY, fromMinutes = DEFAULT_MINUTES;
  int toHourDay = DEFAULT_HOUR_DAY, toMinutes = DEFAULT_MINUTES;

  int action;
  Student student;
  Travel travel;
  ArrayList<String> placesList;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_student);

    textStudentName = (EditText) findViewById(R.id.textStudentName);
    btnStudentAction = (Button) findViewById(R.id.btnStudentAction);
    btnStudentDelete = (Button) findViewById(R.id.btnStudentDelete);
    btnStudentInfo = (ImageButton) findViewById(R.id.btnStudentInfo);

    spinnerFromTime = (Spinner) findViewById(R.id.spinnerFromPlace);
    textFromDate = (TextView) findViewById(R.id.textFromDate);
    btnFromTimePicker = (ImageButton) findViewById(R.id.btnFromTimePicker);
    spinnerToTime = (Spinner) findViewById(R.id.spinnerToPlace);
    textToDate = (TextView) findViewById(R.id.textToDate);
    btnToTimePicker = (ImageButton) findViewById(R.id.btnToTimePicker);


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

    placesList = new ArrayList<String>();
    for (Iterator<Places> iterator = MainActivity.mapPlaces.values().iterator(); iterator.hasNext(); ){
      Places _p = iterator.next();
      placesList.add(_p.getName());
    }

    ArrayAdapter<String> adapterFrom = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, placesList);
    spinnerFromTime.setAdapter(adapterFrom);

    textFromDate.setText(showTime(DEFAULT_HOUR_DAY, DEFAULT_MINUTES));
    final TimePickerDialog timePickerFrom = new TimePickerDialog(StudentActivity.this, new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker timePicker, int i, int i1) {
        fromHourDay = i;
        fromMinutes = i1;
        textFromDate.setText(showTime(fromHourDay, fromMinutes));
      }
    },0,0,false);

    btnFromTimePicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        timePickerFrom.updateTime(fromHourDay, fromMinutes);
        timePickerFrom.show();
      }
    });

    ArrayAdapter<String> adapterTo = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, placesList);
    spinnerToTime.setAdapter(adapterTo);

    textToDate.setText(showTime(toHourDay, toMinutes));
    final TimePickerDialog timePickerTo = new TimePickerDialog(StudentActivity.this, new TimePickerDialog.OnTimeSetListener() {
      @Override
      public void onTimeSet(TimePicker timePicker, int i, int i1) {
        toHourDay = i;
        toMinutes = i1;
        textToDate.setText(showTime(toHourDay, toMinutes));
      }
    },0,0,false);
    btnToTimePicker.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        timePickerTo.updateTime(toHourDay, toMinutes);
        timePickerTo.show();
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

    btnStudentInfo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        final List<Travel> travels =  student.getTravels();
        Log.d(TAG, "travels size: " + travels.size());
        for ( Travel t: travels) {
          t.fetchIfNeededInBackground(new GetCallback<Travel>() {
            @Override
            public void done(Travel travel, ParseException e) {
              Toast.makeText(getApplicationContext(), "From :" + travel.getFromPlace().getObjectId(), Toast.LENGTH_SHORT).show();

            }
          });
        }
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
      List<Travel> travels = student.getTravels();
      if (travels.size() > 0 ) {
        travel = travels.get(0);
        Places fromPlaces = MainActivity.mapPlaces.get(travel.getFromPlace().getObjectId());
        Places toPlaces = MainActivity.mapPlaces.get(travel.getToPlace().getObjectId());

        for (int i=0; i < placesList.size(); i++) {
          if (placesList.get(i).compareTo(fromPlaces.getName()) == 0 )
            spinnerFromTime.setSelection(i);

          if (placesList.get(i).compareTo(toPlaces.getName()) == 0 )
            spinnerToTime.setSelection(i);
        }

        fromHourDay = travel.getFromHourOfDay();
        fromMinutes = travel.getFromMinutes();
        textFromDate.setText(showTime(fromHourDay, fromMinutes));
        toHourDay = travel.getToHourOfDay();
        toMinutes = travel.getToMinutes();
        textToDate.setText(showTime(toHourDay, toMinutes));

      } else {
        Log.e(TAG, "no travel asociated to student");
      }



    } else {
      Toast.makeText(getApplicationContext(), "Action Error", Toast.LENGTH_SHORT).show();
      return;
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
      List<Travel> travels = new ArrayList<Travel>();
      tmpTravel = new Travel();
      travels.add(tmpTravel);
      s.setTravels(travels);

    } else if (action == STUDENT_ACTION_MODIFY) {
      // Using existing place
      s = student;
      tmpTravel = travel;
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
    tmpTravel.setFromPlace(getPlaceFromMap(spinnerFromTime.getSelectedItem().toString()));
    tmpTravel.setFromHourOfDay(fromHourDay);
    tmpTravel.setFromMinutes(fromMinutes);

    tmpTravel.setToPlace(getPlaceFromMap(spinnerToTime.getSelectedItem().toString()));
    tmpTravel.setToHourOfDay(toHourDay);
    tmpTravel.setToMinutes(toMinutes);

    tmpTravel.setACL(acl);

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

    List<Travel> travels = student.getTravels();
    for (Travel tmpTravel: travels) {
      tmpTravel.deleteInBackground();
    }

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
    StringBuilder sb = new StringBuilder().append(hour).append(" : ").append(min).append(" ").append(format);
    return sb.toString();
  }


}

