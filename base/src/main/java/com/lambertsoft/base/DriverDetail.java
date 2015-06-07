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

        public static ParseQuery<DriverDetail> getQuery() {
            return ParseQuery.getQuery(DriverDetail.class);
        }
}
