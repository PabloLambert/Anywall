package com.parse.anywall;

import com.lambertsoft.base.Places;
import com.lambertsoft.base.School;
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

    public void setPhoto(ParseFile value) { put("photo", value);}

    public ParseFile getPhoto() {return getParseFile("photo");}


    public void setSchool(School value) { put("school", value);}

    public School getSchool() { return (School) getParseObject("school");}

    public void setPlaces(Places value) {put("places", value); }

    public Places getPlaces() { return (Places) getParseObject("places"); }

    //From
    public int getFromInitHourOfDay() { return getInt("fromInit_hourofday");}

    public void setFromInitHourOfDay(int value) { put("fromInit_hourofday", value);}

    public int getFromInitMinutes() { return getInt("fromInit_minutes");}

    public void setFromInitMinutes(int value) { put("fromInit_minutes", value);}


    public int getFromEndHourOfDay() { return getInt("fromEnd_hourofday");}

    public void setFromEndHourOfDay(int value) { put("fromEnd_hourofday", value);}

    public int getFromEndMinutes() { return getInt("fromEnd_minutes");}

    public void setFromEndMinutes(int value) { put("fromEnd_minutes", value);}

    //To
    public int getToHourOfDay() { return getInt("to_hourofday");}

    public void setToHourOfDay(int value) { put("to_hourofday", value);}

    public int getToMinutes() { return getInt("to_minutes");}

    public void setToMinutes(int value) { put("to_minutes", value);}


    public static ParseQuery<Student> getQuery() {
        return ParseQuery.getQuery(Student.class);
    }
}