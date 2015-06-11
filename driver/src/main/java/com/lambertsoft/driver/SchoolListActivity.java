package com.lambertsoft.driver;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.lambertsoft.base.School;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class SchoolListActivity extends Activity {

    final static public String SCHOOL_OBJECT_ID = "School_Objetc_Id";

    ListView schoolListView;
    School actualSchool;
    Button btnSelectedSchool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_list);

        schoolListView = (ListView) findViewById(R.id.listSchoolView);
        btnSelectedSchool = (Button) findViewById(R.id.btnSelectedSchool);

        ArrayList<School> schoolArrayList = new ArrayList<School>();
        for (Iterator<School> iterator = MainActivity.mapSchool.values().iterator(); iterator.hasNext(); ){
            School _s = iterator.next();
            schoolArrayList.add(_s);
        }
        ArrayAdapter<School> arrayAdapter = new ArrayAdapter<School>(this, android.R.layout.simple_list_item_1, schoolArrayList);
        schoolListView.setAdapter(arrayAdapter);
        schoolListView.setSelector(android.R.color.darker_gray);
        schoolListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
               actualSchool = (School) adapterView.getItemAtPosition(i);
            }
        });

        Intent intent = getIntent();
        String sObjId = intent.getStringExtra(SCHOOL_OBJECT_ID);

        if (sObjId != null ) {
            actualSchool = MainActivity.mapSchool.get(sObjId);
        }

        btnSelectedSchool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                if (actualSchool != null )
                     intent.putExtra(SCHOOL_OBJECT_ID, actualSchool.getObjectId());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_school_list, menu);
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
