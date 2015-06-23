package com.parse.anywall;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.lambertsoft.base.DriverDetail;
import com.lambertsoft.base.School;

import java.util.ArrayList;
import java.util.Iterator;


public class DriverListActivity extends Activity {


    final static public String DRIVER_OBJECT_ID = "Driver_Object_Id";
    final static public String SCHOOL_OBJECT_ID = "School_Object_Id";

    ListView driverListView;
    DriverDetail actualDriver;
    Button btnSelectedDriver;
    School actualSchool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_list);

        driverListView = (ListView) findViewById(R.id.listDriverView);
        btnSelectedDriver = (Button) findViewById(R.id.btnSelectedDriver);

        Intent intent = getIntent();
        String sObjId = intent.getStringExtra(SCHOOL_OBJECT_ID);

        if (sObjId != null ) {
            actualSchool = MainActivity.mapSchool.get(sObjId);
        }

        ArrayList<DriverDetail> driverArrayList = new ArrayList<DriverDetail>();
        for (Iterator<DriverDetail> iterator = MainActivity.mapDriverDetails.values().iterator(); iterator.hasNext(); ){
            DriverDetail _d = iterator.next();
            if (actualSchool.getObjectId().compareTo(_d.getSchool().getObjectId()) == 0) {
                driverArrayList.add(_d);
            }
        }
        ArrayAdapter<DriverDetail> arrayAdapter = new ArrayAdapter<DriverDetail>(this, android.R.layout.simple_list_item_1, driverArrayList);
        driverListView.setAdapter(arrayAdapter);
        driverListView.setSelector(android.R.color.darker_gray);
        driverListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                actualDriver = (DriverDetail) adapterView.getItemAtPosition(i);
            }
        });

        String dObjId = intent.getStringExtra(DRIVER_OBJECT_ID);

        if (dObjId != null ) {
            actualDriver = MainActivity.mapDriverDetails.get(dObjId);
        }

        btnSelectedDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                if (actualDriver != null)
                    intent.putExtra(DRIVER_OBJECT_ID, actualDriver.getObjectId());
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_driver_list, menu);
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
