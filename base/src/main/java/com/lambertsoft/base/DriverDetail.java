package com.lambertsoft.base;

import com.parse.ParseClassName;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/**
 * Created by InnovaTI on 02-06-15.
 */

@ParseClassName("DriverDetail")
public class DriverDetail  extends ParseObject {

        public String getChannel() {
            return getString("channel");
        }

        public void setChannel(String value) {
            put("channel", value);
        }

        public static ParseQuery<DriverDetail> getQuery() {
            return ParseQuery.getQuery(DriverDetail.class);
        }
}
