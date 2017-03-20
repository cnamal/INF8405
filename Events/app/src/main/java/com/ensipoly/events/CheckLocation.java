package com.ensipoly.events;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

import static android.location.LocationManager.GPS_PROVIDER;

/**
 * Created by Adrien on 19/03/2017.
 */

public class CheckLocation extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CheckLocation","Location change");
        LocationManager lm = (LocationManager) context.getSystemService(Service.LOCATION_SERVICE);
        boolean isEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d("CheckLocation", "GPS enabled? : " + isEnabled);
    }
}
