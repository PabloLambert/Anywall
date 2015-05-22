package com.parse.anywall;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.File;
import java.util.List;


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

    public List<Travel> getTravels() { return getList("travel");}

    public void setTravels(List<Travel> value) { put("travel", value);}

    public void setPhoto(ParseFile value) { put("photo", value);}

    public ParseFile getPhoto() {return getParseFile("photo");}

    public static ParseQuery<Student> getQuery() {
        return ParseQuery.getQuery(Student.class);
    }
}