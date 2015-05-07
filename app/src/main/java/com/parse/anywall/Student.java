package com.parse.anywall;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;


/**
 * Data model for a Student.
 */
@ParseClassName("Student")
public class Student extends ParseObject {

    public String getName() {
        return getString("name");
    }

    public void setName(String value) {
        put("name", value);
    }

    public String getSchool() {
        return getString("school");
    }

    public void setSchool(String value) {
        put("school", value);
    }


    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public static ParseQuery<Student> getQuery() {
        return ParseQuery.getQuery(Student.class);
    }
}