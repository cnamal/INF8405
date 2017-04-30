package com.ensipoly.project.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class FirebaseUtils {

    private static FirebaseDatabase mDatabase;
    private static DatabaseReference mUserDBReference;
    private static DatabaseReference mItineraryDBReference;
    private static DatabaseReference mHistoryDBReference;

    public static FirebaseDatabase getDatabase() {
        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            mDatabase.setPersistenceEnabled(true);
        }

        return mDatabase;
    }

    public static DatabaseReference getUserDBReference() {
        if (mUserDBReference == null) {
            mUserDBReference = getDatabase().getReference("users");
            mUserDBReference.keepSynced(true);
        }
        return mUserDBReference;
    }

    public static DatabaseReference getItinerariesDBReference() {
        if (mItineraryDBReference == null) {
            mItineraryDBReference = getDatabase().getReference("itineraries");
            mItineraryDBReference.keepSynced(true);
        }
        return mItineraryDBReference;
    }

    public static DatabaseReference getHistoryDBReference() {
        if (mHistoryDBReference == null) {
            mHistoryDBReference = getDatabase().getReference("history");
            mHistoryDBReference.keepSynced(true);
        }
        return mHistoryDBReference;
    }
}