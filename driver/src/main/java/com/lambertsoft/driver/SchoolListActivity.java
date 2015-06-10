package com.lambertsoft.driver;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.lambertsoft.base.School;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class SchoolListActivity extends Activity {

    ArrayList<String> schoolList;
    ArrayAdapter<String> adapterSchool;
    ListView schoolListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_list);

        schoolListView = (ListView) findViewById(R.id.listSchoolView);

        schoolList = new ArrayList<String>();
        adapterSchool = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, schoolList);
        schoolListView.setAdapter(adapterSchool);
    }

    @Override
    protected void onResume() {
        super.onResume();

        schoolList.clear();

        for (Iterator<School> iterator = MainActivity.mapSchool.values().iterator(); iterator.hasNext(); ){
            School _s = iterator.next();
            schoolList.add(_s.getName());
        }
        adapterSchool.notifyDataSetChanged();
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
