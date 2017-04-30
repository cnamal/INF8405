package com.ensipoly.project.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import com.ensipoly.project.R;

public class Utils {

    public static final String USER_ID_KEY_PREFERENCE = "user_id";

    private static String mUserId;

    public static String getUserID(Context context) {
        if (mUserId == null) {
            SharedPreferences sharedPref = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            String tmp = sharedPref.getString(USER_ID_KEY_PREFERENCE, "");
            if (tmp.equals(""))
                return null;
            mUserId = tmp;
        }
        return mUserId;
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
