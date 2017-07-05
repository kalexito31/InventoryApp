package com.example.android.gameInventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.gameInventory.data.GameContract.GameEntry;

/**
 * Database helper for Games inventory app. Manages database creation and version management.
 */
public class GameDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = GameDbHelper.class.getSimpleName();

    /** Name of the database file */
    private static final String DATABASE_NAME = "gameInventory.db";

    /**
     * Database version. If you change the database schema, you must increment the database version.
     */
    private static final int DATABASE_VERSION = 1;

    /**
     * Constructs a new instance of {@link GameDbHelper}.
     *
     * @param context of the app
     */
    public GameDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * This is called when the database is created for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the game inventory table
        String SQL_CREATE_GAMES_TABLE =  "CREATE TABLE " + GameEntry.TABLE_NAME + " ("
                + GameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + GameEntry.COLUMN_GAME_NAME + " TEXT NOT NULL, "
                + GameEntry.COLUMN_GAME_PRICE + " INTEGER NOT NULL, "
                + GameEntry.COLUMN_GAME_GENRE + " INTEGER NOT NULL, "
                + GameEntry.COLUMN_GAME_INSTOCK + " INTEGER NOT NULL DEFAULT 1, "
                + GameEntry.COLUMN_GAME_IMAGE+ " BLOB);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_GAMES_TABLE);
    }

    /**
     * This is called when the database needs to be upgraded.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }
}