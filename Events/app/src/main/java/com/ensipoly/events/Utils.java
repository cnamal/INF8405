package com.ensipoly.events;

import android.content.Context;
import android.content.SharedPreferences;

import com.ensipoly.events.models.User;

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

    public static class Triplet<X,Y,Z> {
        public X first;
        public Y second;
        public Z third;
    }

}
