package com.example.tommy.todolist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class DbHandler extends SQLiteOpenHelper {

    private static final int DB_VERSION = 19;
    private static final String DB_NAME = "notelistDB";
    private static final String TABLE_NOTE = "listdetails";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_TEXT = "text";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LONG = "long";

    public DbHandler(Context context){
        super(context,DB_NAME, null, DB_VERSION);
    }

    //Creazione del db
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NOTE + " ("
                + KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_TITLE + " TEXT, "
                + KEY_TEXT + " TEXT, " + KEY_LAT + " TEXT, " + KEY_LONG + " TEXT" + ");";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);
        // Create tables again
        onCreate(db);
    }

    void insertPosition(String title, String text, String lat, String lon) {
        //Get the Data Repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();
        //Create a new map of values, where column names are the keys
        ContentValues cValues = new ContentValues();
        cValues.put(KEY_TITLE, title);
        cValues.put(KEY_TEXT, text);
        cValues.put(KEY_LAT, lat);
        cValues.put(KEY_LONG, lon);

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(TABLE_NOTE, null, cValues);
        db.close();
    }

    // Get note Ddetails
    public ArrayList<Dati> GetNote(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<Dati> noteList = new ArrayList<>();
        String query = "SELECT title, text, lat, long FROM "+ TABLE_NOTE;
        Cursor cursor = db.rawQuery(query,null);
        Dati d;
        while (cursor.moveToNext()){
            String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
            String text = cursor.getString(cursor.getColumnIndex(KEY_TEXT));
            String lat = cursor.getString(cursor.getColumnIndex(KEY_LAT));
            String lng = cursor.getString(cursor.getColumnIndex(KEY_LONG));
            d = new Dati(title,text,lat,lng);
            noteList.add(d);
        }
        return  noteList;
    }

    //---deletes a particular title---
    public boolean deleteTitle(String name) {
        SQLiteDatabase db = this.getWritableDatabase();

        if(db.delete(TABLE_NOTE, KEY_TITLE + "= '" + name + "'", null) > 0) {
            return true;
        }
        return false;
    }

}
