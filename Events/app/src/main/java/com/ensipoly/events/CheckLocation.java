package com.ensipoly.events;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;

public class CheckLocation extends BroadcastReceiver {


    private Utils.ConnectionInfoManager manager;

    public CheckLocation(Utils.ConnectionInfoManager manager) {
        this.manager = manager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CheckLocation", "Location change");
        manager.onLocationChanged(isGPSConnected(context));
    }

    public boolean isGPSConnected(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Service.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
