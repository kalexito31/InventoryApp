package com.example.android.gameInventory.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 * API Contract for the Pets app.
 */
public final class GameContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private GameContract() {}

    /**
     * The "Content authority" is a name for the entire content provider, similar to the
     * relationship between a domain name and its website.  A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.android.gameInventory";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.pets/pets/ is a valid path for
     * looking at pet data. content://com.example.android.pets/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_GAMES = "games";

    /**
     * Inner class that defines constant values for the games database table.
     * Each entry in the table represents a single game.
     */
    public static final class GameEntry implements BaseColumns {

        /** The content URI to access the game data in the provider */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_GAMES);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of game.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single game.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_GAMES;

        /** Name of database table for pets */
        public final static String TABLE_NAME = "games";

        /**
         * Unique ID number for the pet (only for use in the database table).
         *
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the game.
         *
         * Type: TEXT
         */
        public final static String COLUMN_GAME_NAME ="name";

        /**
         * Name of Game Publisher.
         *
         * Type: TEXT
         */
        public final static String COLUMN_GAME_PRICE = "price";

        /**
         * Genre of the game.
         *
         *
         * Type: INTEGER
         */
        public final static String COLUMN_GAME_GENRE = "genre";

        /**
         * Quantity of the games.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_GAME_INSTOCK = "inStock";

        /**
         * Image of the game.
         *
         * Type: INTEGER
         */
        public final static String COLUMN_GAME_IMAGE = "image";

        /**
         * Possible values for the genre of the game.
         */
        public static final int GENRE_UNKNOWN = 0;
        public static final int GENRE_ACTION = 1;
        public static final int GENRE_ACTION_ADVENTURE = 2;
        public static final int GENRE_ADVENTURE = 3;
        public static final int GENRE_ROLE_PLAYING = 4;
        public static final int GENRE_SIMULATION = 5;
        public static final int GENRE_STRATEGY = 6;
        public static final int GENRE_SPORTS = 7;
        public static final int GENRE_OTHER = 8;


        /**
         * Returns whether or not the given gender is one of the above possible values for genre
         */
        public static boolean isValidGenre(int genre) {
            if (genre == GENRE_UNKNOWN || genre == GENRE_ACTION || genre == GENRE_ACTION_ADVENTURE
                    || genre == GENRE_ADVENTURE || genre == GENRE_ROLE_PLAYING || genre == GENRE_SIMULATION
                    || genre == GENRE_STRATEGY || genre == GENRE_SPORTS || genre == GENRE_OTHER) {
                return true;
            }
            return false;
        }
    }

}

