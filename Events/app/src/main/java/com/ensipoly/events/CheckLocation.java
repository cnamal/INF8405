package com.ensipoly.events;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Adrien on 19/03/2017.
 */

public class CheckLocation extends BroadcastReceiver {

    private TextView info;
    private static final int STATE = 1;

    public CheckLocation(TextView textView){
        info = textView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CheckLocation", "Location change");
        LocationManager lm = (LocationManager) context.getSystemService(Service.LOCATION_SERVICE);
        boolean isEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isEnabled)
            info.setVisibility(View.GONE);
        else
            showInfo("GPS disabled",R.color.severity_high);
    }

    private void showInfo(String text, @android.support.annotation.DrawableRes int resId) {
        info.setText(text);
        info.setVisibility(View.VISIBLE);
        info.setBackgroundResource(resId);
    }
}
