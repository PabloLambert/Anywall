package com.lambertsoft.base;

import com.parse.ParseClassName;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by InnovaTI on 02-06-15.
 */

@ParseClassName("DriverDetail")
public class DriverDetail  extends ParseObject {

    public void setPhoto(ParseFile value) { put("photo", value);}

    public ParseFile getPhoto() {return getParseFile("photo");}

        public String getChannel() {
            return getString("channel");
        }

        public void setChannel(String value) {
            put("channel", value);
        }

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

    public static ParseQuery<DriverDetail> getQuery() {
            return ParseQuery.getQuery(DriverDetail.class);
        }
}
