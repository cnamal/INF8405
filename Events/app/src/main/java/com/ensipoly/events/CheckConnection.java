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

    private Utils.ConnectionInfoManager manager;

    public CheckConnection(Utils.ConnectionInfoManager manager) {
        this.manager = manager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CheckConnection", "Network connectivity change");
        if (intent.getExtras() != null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED)
                manager.onInternetConnectionChanged(true);
            else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE))
                manager.onInternetConnectionChanged(false);

        }
    }

}
