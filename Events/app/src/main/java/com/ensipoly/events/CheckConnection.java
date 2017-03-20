package com.ensipoly.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Adrien on 19/03/2017.
 */

public class CheckConnection extends BroadcastReceiver {

    private TextView info;

    public CheckConnection(TextView textView){
        info = textView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CheckConnection","Network connectivity change");
        if(intent.getExtras()!=null) {
            NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
            if (ni != null && ni.getState() == NetworkInfo.State.CONNECTED) {
                info.setVisibility(View.GONE);
            } else if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE)) {
                showInfo("There's no network connectivity",R.color.severity_high);
            }
        }
    }

    private void showInfo(String text, @android.support.annotation.DrawableRes int resId) {
        info.setText(text);
        info.setVisibility(View.VISIBLE);
        info.setBackgroundResource(resId);
    }
}
