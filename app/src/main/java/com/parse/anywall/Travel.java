package com.parse.anywall;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.Date;

/**
 * Created by InnovaTI on 17-05-15.
 */
@ParseClassName("Travel")
public class Travel extends ParseObject {

    // From
    public Places getFromPlace() {
            return (Places) getParseObject("from_place");
        }

    public void setFromPlace(Places value) {
            put("from_place", value);
        }

    public int getFromHourOfDay() { return getInt("from_hourofday");}

    public void setFromHourOfDay(int value) { put("from_hourofday", value);}

    public int getFromMinutes() { return getInt("from_minutes");}

    public void setFromMinutes(int value) { put("from_minutes", value);}

    // To
    public Places getToPlace() {
        return (Places) getParseObject("to_place");
    }

    public void setToPlace(Places value) {
        put("to_place", value);
    }

    public int getToHourOfDay() { return getInt("to_hourofday");}

    public void setToHourOfDay(int value) { put("to_hourofday", value);}

    public int getToMinutes() { return getInt("to_minutes");}

    public void setToMinutes(int value) { put("to_minutes", value);}


        public static ParseQuery<Travel> getQuery() {
            return ParseQuery.getQuery(Travel.class);
        }
    }

