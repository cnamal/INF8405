package com.ensipoly.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Created by Adrien on 19/03/2017.
 */

public class CheckConnection extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CheckConnection","Network connectivity change");
        if(intent.getExtras()!=null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                Log.i("CheckConnection", "Network " + ni.getTypeName() + " connected");
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                Log.d("CheckConnection", "There's no network connectivity");
            }
        }
    }
}
