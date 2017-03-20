package com.ensipoly.events;

import android.content.Context;
import android.content.SharedPreferences;

import com.ensipoly.events.models.Event;
import com.ensipoly.events.models.User;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class Utils {

    private static String mUserId;

    public static String getUserID(Context context){
        if(mUserId==null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String tmp = sharedPref.getString(User.USER_ID_KEY_PREFERENCE,"");
            if(tmp.equals(""))
                return null;
            mUserId = tmp;
        }
        return mUserId;
    }

    public static String formatLocation(double latitude, double longitude) {
        return String.format(Locale.getDefault(), "Location: %1$.3f, %2$.3f ", latitude, longitude);
    }

    public static String formatLocation(com.ensipoly.events.models.Location location) {
        return formatLocation(location.getLatitude(), location.getLongitude());
    }

    public static String formatLocation(LatLng latLng) {
        return formatLocation(latLng.latitude, latLng.longitude);
    }

    public static String formatLocation(Event event) {
        return formatLocation(event.getLatitude(), event.getLongitude());
    }

}
