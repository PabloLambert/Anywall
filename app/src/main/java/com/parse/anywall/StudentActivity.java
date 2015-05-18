package com.parse.anywall;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */


public class StudentActivity extends FragmentActivity {

  final static public String TAG = StudentActivity.class.getSimpleName();
  final static public String STUDENT_ACTION = "STUDENT_ACTION";
  final static public String STUDENT_OBJECT_ID = "STUDENT_OBJECT";
  final static public int STUDENT_ACTION_CREATE = 1;
  final static public int STUDENT_ACTION_MODIFY = 2;

  EditText textStudentName;
  Button btnStudentAction, btnStudentDelete;
  ImageButton btnStudentInfo;
  int action;
  Student student;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_student);

    textStudentName = (EditText) findViewById(R.id.textStudentName);
    btnStudentAction = (Button) findViewById(R.id.btnStudentAction);
    btnStudentDelete = (Button) findViewById(R.id.btnStudentDelete);
    btnStudentInfo = (ImageButton) findViewById(R.id.btnStudentInfo);


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
        ArrayList<Travel> travels = (ArrayList<Travel>) student.get("travel");
        Log.d(TAG, "travels size: " + travels.size());
        for ( Travel t: travels) {
          t.fetchIfNeededInBackground(new GetCallback<Travel>() {
            @Override
            public void done(Travel travel, ParseException e) {
              Toast.makeText(getApplicationContext(), travel.getName(), Toast.LENGTH_SHORT).show();

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

    } else {
      Toast.makeText(getApplicationContext(), "Action Error", Toast.LENGTH_SHORT).show();
      return;
    }

  }

  public void updateButtonState() {
    int placeNameLength = textStudentName.getText().toString().length();

    boolean enabled = placeNameLength > 0 ;

    btnStudentAction.setEnabled(enabled);
  }

  public void saveData() {

    String name = textStudentName.getText().toString().trim();
    Student s;

    if ( action == STUDENT_ACTION_CREATE) {
      // Create a post.
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

    s.setName(name);
    ArrayList<Travel> travels = new ArrayList<Travel>();
    Travel t1 = new Travel();
    t1.setName("Desde la casa");
    travels.add(t1);

    Travel t2 = new Travel();
    t2.setName("Desde el colegio");
    travels.add(t2);

    s.put("travel", travels);

    ParseUser user = ParseUser.getCurrentUser();
    s.setACL(new ParseACL(user));

    // Save the Student
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


}

