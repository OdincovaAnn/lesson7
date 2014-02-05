package com.OdincovaAnn.ifmo.RSS_ReaderFull;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class ChannelDataBaseHelper extends SQLiteOpenHelper implements BaseColumns {

    public static final String DATA_BASE_NAME = "rssTablehhhhh.db";

    public static final String TABLE_NAME = "ChannelTable";
    public static final String CHANNEL = "channel";
    public static final String URL = "url";
    public static final String CHANNEL_TABLE_NAME = "tableName";
    public static final int DATA_BASE_VERSION = 1;

    public static final String TITLE = "title";
    public static final String SUMMARY = "summary";


    public static final String CREATE_MAIN_TABLE = "CREATE TABLE " + TABLE_NAME +
            " (" + ChannelDataBaseHelper._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + CHANNEL + " TEXT, " +
            URL + " TEXT, " + CHANNEL_TABLE_NAME + " INTEGER);";

    public static final String DELETE_MAIN_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    ChannelDataBaseHelper(Context context) {
        super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MAIN_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DELETE_MAIN_TABLE);
        onCreate(db);
    }
}
