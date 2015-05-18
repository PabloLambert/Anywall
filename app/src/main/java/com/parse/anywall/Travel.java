package com.parse.anywall;

import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by InnovaTI on 17-05-15.
 */
@ParseClassName("Travel")
public class Travel extends ParseObject {

    public String getName() { return getString("name");}

    public void setName(String value) { put("name", value); }

        public Places getFromPlace() {
            return (Places) getParseObject("from_place");
        }

        public void setFromPlace(Places value) {
            put("from_place", value);
        }

        public static ParseQuery<Travel> getQuery() {
            return ParseQuery.getQuery(Travel.class);
        }
    }

