package com.parse.anywall;

import android.content.Context;
import android.content.SharedPreferences;

import com.parse.Parse;
import com.parse.ParseObject;
import com.pubnub.api.Pubnub;

public class Application extends android.app.Application {
  // Debugging switch
  public static final boolean APPDEBUG = false;

  // Debugging tag for the application
  public static final String APPTAG = "AnyWall";

  // Used to pass location from MainActivity to StudentActivity
  public static final String INTENT_EXTRA_LOCATION = "location";

  // Key for saving the search distance preference
  private static final String KEY_SEARCH_DISTANCE = "searchDistance";

  private static final float DEFAULT_SEARCH_DISTANCE = 250.0f;

  private static SharedPreferences preferences;

  private static ConfigHelper configHelper;

  public static Pubnub pubnub;


  public Application() {
  }

  @Override
  public void onCreate() {
    super.onCreate();

    ParseObject.registerSubclass(AnywallPost.class);
    ParseObject.registerSubclass(Student.class);
    ParseObject.registerSubclass(Places.class);
    ParseObject.registerSubclass(Travel.class);

    Parse.initialize(this, "IU2UBVRnjfs8g65F1ixTOrFMnsUFDmSrptwjh9wq", "C0Ghf2UcrxJgWA9P8IxlRenltHnqiYOggFIzBnLn");

    preferences = getSharedPreferences("com.parse.anywall", Context.MODE_PRIVATE);

    configHelper = new ConfigHelper();
    configHelper.fetchConfigIfNeeded();

    pubnub = new Pubnub("pub-c-940b2922-b731-40c0-b37e-dd665be2d8b7", "sub-c-c83accce-0417-11e5-a37b-02ee2ddab7fe");

  }

  public static float getSearchDistance() {
    return preferences.getFloat(KEY_SEARCH_DISTANCE, DEFAULT_SEARCH_DISTANCE);
  }

  public static ConfigHelper getConfigHelper() {
    return configHelper;
  }

  public static void setSearchDistance(float value) {
    preferences.edit().putFloat(KEY_SEARCH_DISTANCE, value).commit();
  }

}
