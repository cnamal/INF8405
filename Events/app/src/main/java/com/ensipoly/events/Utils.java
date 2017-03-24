package com.ensipoly.events;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import com.ensipoly.events.models.Event;
import com.ensipoly.events.models.User;
import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

public class Utils {

    private static String mUserId;

    public static String getUserID(Context context) {
        if (mUserId == null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String tmp = sharedPref.getString(User.USER_ID_KEY_PREFERENCE, "");
            if (tmp.equals(""))
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

    public static void showInfo(TextView view, String text, @android.support.annotation.DrawableRes int resId) {
        view.setText(text);
        view.setVisibility(View.VISIBLE);
        view.setBackgroundResource(resId);
    }

    public static class ConnectionInfoManager {
        TextView text;
        int state;

        public ConnectionInfoManager(TextView textView) {
            text = textView;
            state = 0;
        }

        private void updateView() {
            if ((state & 1) == 0)
                showInfo(text, "There's no network connectivity", R.color.severity_high);
            else if ((state & 2) == 0)
                showInfo(text, "GPS disabled", R.color.severity_high);
            else
                text.setVisibility(View.GONE);

        }

        private void changeState(int position, boolean enabled) {
            if (enabled)
                state = state | (1 << position);
            else
                state = state & ~(1 << position);
            updateView();
        }

        public void onInternetConnectionChanged(boolean enabled) {
            changeState(0, enabled);
        }

        public void onLocationChanged(boolean enabled) {
            changeState(1, enabled);
        }
    }

}
