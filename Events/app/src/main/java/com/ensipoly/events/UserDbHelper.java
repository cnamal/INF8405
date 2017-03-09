package com.ensipoly.events;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class UserDbHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "User.db";
    private static final String TABLE_NAME = "users";
    private static final String ID = "id";
    private static final String COLUMN_NAME_USERNAME = "username";
    private static final String COLUMN_NAME_PHOTOURL = "photoUrl";
    private static final String COLUMN_NAME_LONGITUTE = "longitude";
    private static final String COLUMN_NAME_LATITUDE = "latitude";
    private static final String COLUMN_NAME_LAST = "last";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    ID + " TEXT PRIMARY KEY NOT NULL," +
                    COLUMN_NAME_USERNAME + " TEXT NOT NULL," +
                    COLUMN_NAME_PHOTOURL + " TEXT NOT NULL," +
                    COLUMN_NAME_LATITUDE + " REAL NOT NULL," +
                    COLUMN_NAME_LONGITUTE + " REAL NOT NULL," +
                    COLUMN_NAME_LAST + " TEXT NOT NULL" +
                    ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    private static UserDbHelper mInstance =null;

    public static synchronized UserDbHelper getHelper(Context context){
        if(mInstance==null)
            mInstance = new UserDbHelper(context);
        return mInstance;
    }

    private UserDbHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void addUser(String id,User user){
        SQLiteDatabase db = mInstance.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDbHelper.ID, id);
        values.put(UserDbHelper.COLUMN_NAME_USERNAME, user.username);
        values.put(UserDbHelper.COLUMN_NAME_PHOTOURL, user.photoUrl);
        values.put(UserDbHelper.COLUMN_NAME_LATITUDE, user.latitude);
        values.put(UserDbHelper.COLUMN_NAME_LONGITUTE, user.longitude);
        values.put(UserDbHelper.COLUMN_NAME_LAST, user.lastActive);
        db.insert(TABLE_NAME, null, values);
    }

    public User getUser(String id){
        SQLiteDatabase db = mInstance.getReadableDatabase();

        String selection = ID + " = ?";
        String[] selectionArgs = { id };


        Cursor cursor = db.query(
                TABLE_NAME,                     // The table to query
                null,                               // The columns to return
                selection,                                // The columns for the WHERE clause
                selectionArgs,                            // The values for the WHERE clause
                null,                                     // don't group the rows
                null,                                     // don't filter by row groups
                null                                 // The sort order
        );

        if (cursor != null)
            cursor.moveToFirst();
        else
            return null;
        User user = new User(
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_USERNAME)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_PHOTOURL)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NAME_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_NAME_LONGITUTE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME_LAST))
        );
        cursor.close();
        return user;
    }
}
