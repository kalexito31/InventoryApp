package com.example.android.gameInventory;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.gameInventory.data.GameContract;
import com.example.android.gameInventory.data.GameContract.GameEntry;

import java.io.ByteArrayOutputStream;

/**
 * Displays list of games that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the games data loader */
    private static final int GAME_LOADER = 0;

    /** Adapter for the ListView */
    GameCursorAdapter mCursorAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the game data
        ListView gameListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        gameListView.setEmptyView(emptyView);

        // Setup an Adapter to create a list item for each row of pet data in the Cursor.
        // There is no pet data yet (until the loader finishes) so pass in null for the Cursor.
        mCursorAdapter = new GameCursorAdapter(this, null);
        gameListView.setAdapter(mCursorAdapter);

        // Setup the item click listener
        gameListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Create new intent to go to {@link EditorActivity}
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                // Form the content URI that represents the specific game that was clicked on,
                // by appending the "id" (passed as input to this method) onto the
                // {@link GameEntry#CONTENT_URI}.
                // For example, the URI would be "content://com.example.android.gameInventory/games/2"
                // if the game with ID 2 was clicked on.
                Uri currentGameUri = ContentUris.withAppendedId(GameEntry.CONTENT_URI, id);

                // Set the URI on the data field of the intent
                intent.setData(currentGameUri);

                // Launch the {@link EditorActivity} to display the data for the current game.
                startActivity(intent);
            }
        });

        // Kick off the loader
        getLoaderManager().initLoader(GAME_LOADER, null, this);
    }

    /**
     * Helper method to insert hardcoded game data into the database. For debugging purposes only.
     */
    private void insertGame() {
        // Create a ContentValues object where column names are the keys,
        // and Pokemon Emerald's attributes are the values.
        ContentValues values = new ContentValues();
        values.put(GameContract.GameEntry.COLUMN_GAME_NAME, "Pokemon Emerald");
        values.put(GameContract.GameEntry.COLUMN_GAME_PRICE, 60);
        values.put(GameContract.GameEntry.COLUMN_GAME_GENRE, GameEntry.GENRE_ACTION_ADVENTURE);
        values.put(GameContract.GameEntry.COLUMN_GAME_INSTOCK, 100);

        //Insert Image from Drawable folder
        //Get Uri for the image file in the drawable folder
        //Then Convert it into a bitmap
        //Then convert it into a byte array since SQLite can only store images as BLOBs
        //Put byte array into ContentValues
        Uri imageUri = Uri.parse("android.resource://com.example.android.gameInventory/drawable/pokemon_emerald");
        Bitmap imageBitmap = decodeUri(imageUri, 400);
        byte[] imageByteArray = convertToByteArray(imageBitmap);
        values.put(GameEntry.COLUMN_GAME_IMAGE, imageByteArray);

        // Insert a new row for Pokemon Emerald into the provider using the ContentResolver.
        // Use the {@link GameEntry#CONTENT_URI} to indicate that we want to insert
        // into the games database table.
        // Receive the new content URI that will allow us to access Pokemon Emerald's data in the future.
        Uri newUri = getContentResolver().insert(GameContract.GameEntry.CONTENT_URI, values);
    }

    /**
     * Helper method to delete all games in the database.
     */
    private void deleteAllGames() {
        int rowsDeleted = getContentResolver().delete(GameContract.GameEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity", rowsDeleted + " rows deleted from games database");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertGame();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllGames();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Define a projection that specifies the columns from the table we care about.
        String[] projection = {
                GameEntry._ID,
                GameContract.GameEntry.COLUMN_GAME_NAME,
                GameEntry.COLUMN_GAME_PRICE,
                GameEntry.COLUMN_GAME_GENRE,
                GameEntry.COLUMN_GAME_INSTOCK};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                GameEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // Update {@link GameCursorAdapter} with this new cursor containing updated pet data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }

    //Convert bitmap to bytes
    public static byte[] convertToByteArray(Bitmap b){

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.PNG, 0, bos);
        return bos.toByteArray();
    }

    //Converts "BLOB" into bitmap in order to display the image of the game
    public static Bitmap convertToBitmap(byte[] b){
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    //Convert and resize our image to 400dp for faster uploading our images to the database
    public Bitmap decodeUri(Uri selectedImage, int REQUIRED_SIZE) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            // final int REQUIRED_SIZE =  size;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(selectedImage), null, o2);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
