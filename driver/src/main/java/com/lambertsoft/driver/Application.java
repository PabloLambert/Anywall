package com.lambertsoft.driver;

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.Parse;

/**
 * Created by InnovaTI on 23-05-15.
 */

public class Application extends android.app.Application {

    private static SharedPreferences preferences;

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(this, "IU2UBVRnjfs8g65F1ixTOrFMnsUFDmSrptwjh9wq", "C0Ghf2UcrxJgWA9P8IxlRenltHnqiYOggFIzBnLn");

        preferences = getSharedPreferences("com.parse.anywall", Context.MODE_PRIVATE);

    }

}
