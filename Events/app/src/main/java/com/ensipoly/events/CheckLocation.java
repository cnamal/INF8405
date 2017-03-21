package com.ensipoly.events;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

/**
 * Created by Adrien on 19/03/2017.
 */

public class CheckLocation extends BroadcastReceiver {


    private Utils.ConnectionInfoManager manager;

    public CheckLocation(Utils.ConnectionInfoManager manager){
        this.manager = manager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CheckLocation", "Location change");
        LocationManager lm = (LocationManager) context.getSystemService(Service.LOCATION_SERVICE);
        boolean isEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        manager.onLocationChanged(isEnabled);
    }

}
