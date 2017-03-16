package com.ensipoly.events;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FirebaseUtils {

    private static FirebaseDatabase mDatabase;
    private static DatabaseReference mUserDBReference;
    private static DatabaseReference mGroupDBReference;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }

        return mDatabase;
    }

    public static DatabaseReference getUserDBReference(){
        if(mUserDBReference==null){
            mUserDBReference = getDatabase().getReference("users");
            mUserDBReference.keepSynced(true);
        }
        return mUserDBReference;
    }

    public static DatabaseReference getGroupDBReference(){
        if(mGroupDBReference==null){
            mGroupDBReference = getDatabase().getReference("groups");
            mGroupDBReference.keepSynced(true);
        }
        return mGroupDBReference;
    }
}
