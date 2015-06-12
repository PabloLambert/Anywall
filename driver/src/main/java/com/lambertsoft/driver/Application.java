package com.lambertsoft.driver;

import android.content.Context;
import android.content.SharedPreferences;

import com.lambertsoft.base.DriverDetail;
import com.lambertsoft.base.Places;
import com.lambertsoft.base.School;
import com.parse.Parse;
import com.parse.ParseObject;
import com.pubnub.api.Pubnub;

/**
 * Created by InnovaTI on 23-05-15.
 */

public class Application extends android.app.Application {

    private static SharedPreferences preferences;
    public static Pubnub pubnub;


    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        ParseObject.registerSubclass(DriverDetail.class);
        ParseObject.registerSubclass(School.class);
        ParseObject.registerSubclass(Places.class);

        Parse.initialize(this, "IU2UBVRnjfs8g65F1ixTOrFMnsUFDmSrptwjh9wq", "C0Ghf2UcrxJgWA9P8IxlRenltHnqiYOggFIzBnLn");

        preferences = getSharedPreferences("com.parse.anywall", Context.MODE_PRIVATE);

        pubnub = new Pubnub("pub-c-940b2922-b731-40c0-b37e-dd665be2d8b7", "sub-c-c83accce-0417-11e5-a37b-02ee2ddab7fe");

    }

}
