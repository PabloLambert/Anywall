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

    //From
    public int getFromHourOfDay() { return getInt("from_hourofday");}

    public void setFromHourOfDay(int value) { put("from_hourofday", value);}

    public int getFromMinutes() { return getInt("from_minutes");}

    public void setFromMinutes(int value) { put("from_minutes", value);}

    //To
    public int getToHourOfDay() { return getInt("to_hourofday");}

    public void setToHourOfDay(int value) { put("to_hourofday", value);}

    public int getToMinutes() { return getInt("to_minutes");}

    public void setToMinutes(int value) { put("to_minutes", value);}

    public static ParseQuery<DriverDetail> getQuery() {
            return ParseQuery.getQuery(DriverDetail.class);
        }
}
