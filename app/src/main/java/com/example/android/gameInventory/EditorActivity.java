package com.example.android.gameInventory;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gameInventory.data.GameContract;
import com.example.android.gameInventory.data.GameContract.GameEntry;

import java.io.ByteArrayOutputStream;

/**
 * Allows user to create a new game or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    /** Identifier for the game data loader */
    private static final int EXISTING_GAME_LOADER = 0;

    /** Identifier for the uploaded image */
    private static final int RESULT_LOAD_IMAGE = 1;

    /** Content URI for the existing pet (null if it's a new game) */
    private Uri mCurrentGameUri;

    /** EditText field to enter the game's name */
    private EditText mNameEditText;

    /** EditText field to enter the games publisher */
    private EditText mPriceEditText;

    /** EditText field to enter games in stock */
    private EditText mInStockEditText;

    /** EditText field to enter the game's genre */
    private Spinner mGenreSpinner;

    /** ImageView that display image of Game*/
    private ImageView mGameImageView;

    /** Uri for the uploaded image */
    private Uri uploadedImage;

    /** Boolean to check if the user uploaded an image or not.*/
    private boolean containsImage = false;

    /** Image bitmap*/
    private Bitmap imageBitmap;

    //Image to upload and the textView that tells user to upload an image
    private ImageView gameImageView;
    TextView addImageText;

    /**
     * Genre of the game. The possible valid values are in the GameContract.java file:
     * {@link GameEntry#GENRE_UNKNOWN}, {@link GameContract.GameEntry#GENRE_ACTION},
     * {@link GameEntry#GENRE_ACTION_ADVENTURE}, etc.
     */
    private int mGenre = GameContract.GameEntry.GENRE_UNKNOWN;

    /** Boolean flag that keeps track of whether the game has been edited (true) or not (false) */
    private boolean mGameHasChanged;

    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mGameHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mGameHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new game or editing an existing one.
        Intent intent = getIntent();
        mCurrentGameUri = intent.getData();

        //Get buttons for adding and subtracting from quantity and for ordering from the supplier
        Button subtractOneUnitButton = (Button) findViewById(R.id.subtract_button);
        Button addOneUnitButton = (Button) findViewById(R.id.add_button);
        Button orderFromSupplierButton = (Button) findViewById(R.id.editor_activity_order_from_supplier_button);

        // If the intent DOES NOT contain a game content URI, then we know that we are
        // creating a new game.
        if (mCurrentGameUri == null) {
            // This is a new game, so change the app bar to say "Add a Game"
            setTitle(getString(R.string.editor_activity_title_new_game));

            // Hide "add", "subtract" and "order from supplier" buttons since we won't be needing them when a
            // new game is added.
            subtractOneUnitButton.setVisibility(View.GONE);
            addOneUnitButton.setVisibility(View.GONE);
            orderFromSupplierButton.setVisibility(View.GONE);

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
        } else {
            // Otherwise this is an existing game, so change app bar to say "Edit Game"
            setTitle(getString(R.string.editor_activity_title_edit_game));

            containsImage = true;

            // Initialize a loader to read the game data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_GAME_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_game_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_game_price);
        mInStockEditText = (EditText) findViewById(R.id.edit_game_instock);
        mGenreSpinner = (Spinner) findViewById(R.id.spinner_genre);
        mGameImageView = (ImageView) findViewById(R.id.game_image_view);

        //Boolean helper to check if a new image has been uploaded
        mGameHasChanged = false;

        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mInStockEditText.setOnTouchListener(mTouchListener);
        mGenreSpinner.setOnTouchListener(mTouchListener);

        setupSpinner();

        subtractOneUnitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Subtract one unit from the quantity
                Integer quantity = Integer.parseInt(mInStockEditText.getText().toString().trim());
                if(quantity>1){
                    quantity--;
                    mInStockEditText.setText(Integer.toString(quantity));
                }
                else{
                    //do nothing
                }
            }
        });

        addOneUnitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Subtract one unit from the quantity
                Integer quantity = Integer.parseInt(mInStockEditText.getText().toString().trim());
                if(quantity<999){
                    quantity++;
                    mInStockEditText.setText(Integer.toString(quantity));
                }
                else{
                    //do nothing
                }

            }
        });

        //User clicks :order more units from supplier"
        orderFromSupplierButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrder();
            }
        });

        //User uploaded an image
        gameImageView = (ImageView) findViewById(R.id.game_image_view);
        addImageText = (TextView) findViewById(R.id.add_image_text);

        gameImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, RESULT_LOAD_IMAGE);
            }
        });
    }

    //Code to handle the uploaded image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null){
            containsImage = true;
            addImageText.setVisibility(View.GONE);

            uploadedImage = data.getData();
            imageBitmap = decodeUri(uploadedImage, 400);
            gameImageView.setImageBitmap(imageBitmap);
        }
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

    // For when the user clicks order from supplier
    public void onOrder() {
        String name = mNameEditText.getText().toString().trim();
        String message = createOrderSummary(name);
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject) + " " + name);
        intent.putExtra(Intent.EXTRA_TEXT, message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    // Creates email message to order more games from supplier
    public String createOrderSummary(String name) {
        String message = "";
        message += getString(R.string.hi) + "\n\n";
        message += getString(R.string.message)+ " " + name +".\n\n";
        message += getString(R.string.thank);
        return message;
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the game.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_gender_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mGenreSpinner.setAdapter(genderSpinnerAdapter);

        // Set the integer mSelected to the constant values
        mGenreSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.genre_action))) {
                        mGenre = GameEntry.GENRE_ACTION;
                    }
                    else if (selection.equals(getString(R.string.genre_action_adventure))) {
                        mGenre = GameEntry.GENRE_ACTION_ADVENTURE;
                    }
                    else if (selection.equals(getString(R.string.genre_adventure))) {
                        mGenre = GameEntry.GENRE_ACTION_ADVENTURE;
                    }
                    else if (selection.equals(getString(R.string.genre_adventure))) {
                        mGenre = GameEntry.GENRE_ADVENTURE;
                    }
                    else if (selection.equals(getString(R.string.genre_role_playing))) {
                        mGenre = GameEntry.GENRE_ROLE_PLAYING;
                    }
                    else if (selection.equals(getString(R.string.genre_simulation))) {
                        mGenre = GameEntry.GENRE_SIMULATION;
                    }
                    else if (selection.equals(getString(R.string.genre_sports))) {
                        mGenre = GameEntry.GENRE_SPORTS;
                    }
                    else if (selection.equals(getString(R.string.genre_other))) {
                        mGenre = GameEntry.GENRE_OTHER;
                    }
                    else {
                        mGenre = GameEntry.GENRE_UNKNOWN;
                    }
                }
            }

            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mGenre = GameContract.GameEntry.GENRE_UNKNOWN;
            }
        });
    }

    /**
     * Get user input from editor and save game into database.
     */
    private void saveGame() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String instockString = mInStockEditText.getText().toString().trim();

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (mCurrentGameUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(instockString) && mGenre == GameContract.GameEntry.GENRE_UNKNOWN) {
            // Since no fields were modified, we can return early without creating a new pet.
            // No need to create ContentValues and no need to do any ContentProvider operations.

            //End activity
            finish();
            return;
        }

        //If not all fields are blank
        //Check if there are any blank fields that are required.
        //Name, Price, and Image are required.
        //Quantity not required. Will be set to 1 by default.
        //Genre of the game is not. Will be set to "Unknown" by default.
        if(TextUtils.isEmpty(nameString)){
            Toast.makeText(this, "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.isEmpty(priceString)){
            Toast.makeText(this, "Price cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(containsImage == false){
            Toast.makeText(this, "Image required.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        ContentValues values = new ContentValues();
        values.put(GameContract.GameEntry.COLUMN_GAME_NAME, nameString);
        values.put(GameEntry.COLUMN_GAME_PRICE, priceString);
        values.put(GameContract.GameEntry.COLUMN_GAME_GENRE, mGenre);

        // If the instock quantity is not provided by the user, don't try to parse the string into an
        // integer value. Use 1 by default.
        int inStock = 1;
        if (!TextUtils.isEmpty(instockString)) {
            inStock = Integer.parseInt(instockString);
        }
        values.put(GameEntry.COLUMN_GAME_INSTOCK, inStock);

        //  Checks if there was an image uploaded. If there was then include in the ContentValues,
        //  else do not include
        if(containsImage == true && imageBitmap != null){
            byte[] imageByteArray = convertToByteArray(imageBitmap);
            values.put(GameEntry.COLUMN_GAME_IMAGE, imageByteArray);
        }

        // Determine if this is a new or existing pet by checking if mCurrentGameUri is null or not
        if (mCurrentGameUri == null) {
            // This is a NEW game, so insert a new game into the provider,
            // returning the content URI for the new pet.
            Uri newUri = getContentResolver().insert(GameEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_game_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_game_successful), Toast.LENGTH_SHORT).show();
            }

            //End activity
            finish();
        }
        else {
            // Otherwise this is an EXISTING game, so update the game with content URI: mCurrentGameUri
            // and pass in the new ContentValues. Pass in null for the selection and selection args
            // because mCurrentGameUri will already identify the correct row in the database that
            // we want to modify.
            int rowsAffected = getContentResolver().update(mCurrentGameUri, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_game_failed), Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_game_successful), Toast.LENGTH_SHORT).show();
            }

            //End activity
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (mCurrentGameUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // Save game to inventory database
                saveGame();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the pet hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mGameHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * This method is called when the back button is pressed.
     */
    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mGameHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all game attributes, define a projection that contains
        // all columns from the games table
        String[] projection = {
                GameEntry._ID,
                GameEntry.COLUMN_GAME_NAME,
                GameEntry.COLUMN_GAME_PRICE,
                GameEntry.COLUMN_GAME_GENRE,
                GameEntry.COLUMN_GAME_INSTOCK,
                GameEntry.COLUMN_GAME_IMAGE};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentGameUri,         // Query the content URI for the current game
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of game attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(GameContract.GameEntry.COLUMN_GAME_NAME);
            int priceColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_PRICE);
            int genreColumnIndex = cursor.getColumnIndex(GameContract.GameEntry.COLUMN_GAME_GENRE);
            int instockColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_INSTOCK);
            int imageColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int genre = cursor.getInt(genreColumnIndex);
            int instock = cursor.getInt(instockColumnIndex);
            byte [] image = cursor.getBlob(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mInStockEditText.setText(Integer.toString(instock));

            //Convert BLOB back to Bitmap in order and set the image into the ImageView
            //If empty then it mean there is not image. No need to do anything
            if(image != null) {
                Bitmap imageBM = convertToBitmap(image);
                mGameImageView.setImageBitmap(imageBM);
                addImageText.setVisibility(View.GONE);
            }

            // Genre is a dropdown spinner, so map the constant value from the database
            // into one of the dropdown options (0 is Unknown, 1 is Action, 2 is Action-Adventure).
            // Then call setSelection() so that option is displayed on screen as the current selection.
            switch (genre) {
                case GameEntry.GENRE_ACTION:
                    mGenreSpinner.setSelection(1);
                    break;
                case GameContract.GameEntry.GENRE_ACTION_ADVENTURE:
                    mGenreSpinner.setSelection(2);
                    break;
                case GameEntry.GENRE_ADVENTURE:
                    mGenreSpinner.setSelection(3);
                    break;
                case GameEntry.GENRE_ROLE_PLAYING:
                    mGenreSpinner.setSelection(4);
                    break;
                case GameEntry.GENRE_SIMULATION:
                    mGenreSpinner.setSelection(5);
                    break;
                case GameEntry.GENRE_STRATEGY:
                    mGenreSpinner.setSelection(6);
                    break;
                case GameEntry.GENRE_SPORTS:
                    mGenreSpinner.setSelection(7);
                    break;
                case GameEntry.GENRE_OTHER:
                    mGenreSpinner.setSelection(8);
                    break;
                default:
                    mGenreSpinner.setSelection(0);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mInStockEditText.setText("");
        mGenreSpinner.setSelection(0); // Select "Unknown" gender
    }

    /**
     * Show a dialog that warns the user there are unsaved changes that will be lost
     * if they continue leaving the editor.
     *
     * @param discardButtonClickListener is the click listener for what to do when
     *                                   the user confirms they want to discard their changes
     */
    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteGame();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the game in the database.
     */
    private void deleteGame() {
        // Only perform the delete if this is an existing pet.
        if (mCurrentGameUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentGameUri
            // content URI already identifies the pet that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentGameUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_game_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_game_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}