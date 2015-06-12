package com.lambertsoft.base;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by InnovaTI on 15-05-15.
 */

@ParseClassName("Places")
public class Places extends ParseObject {

    public String getName() {
        return getString("name");
    }

    public void setName(String value) {
        put("name", value);
    }

    public String getDirection() {
        return getString("direction");
    }

    public void setDirection(String value) {
        put("direction", value);
    }
    public ParseGeoPoint getLocation() {
        return getParseGeoPoint("location");
    }

    public void setLocation(ParseGeoPoint value) {
        put("location", value);
    }

    public static ParseQuery<Places> getQuery() {
        return ParseQuery.getQuery(Places.class);
    }
}
