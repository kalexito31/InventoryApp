package com.example.android.gameInventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.gameInventory.data.GameContract;
import com.example.android.gameInventory.data.GameContract.GameEntry;

import static com.example.android.gameInventory.R.id.inStock;

/**
 * {@link GameCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of game data as its data source. This adapter knows
 * how to create list items for each row of game data in the {@link Cursor}.
 */
public class GameCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link GameCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public GameCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    /**
     * This method binds the game data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current game can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(final View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        // Find the columns of game attributes that we're interested in
        int idColumnIndex = cursor.getColumnIndex(GameEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_NAME);
        int priceColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_PRICE);
        int genreColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_GENRE);
        int quantityColumnIndex = cursor.getColumnIndex(GameEntry.COLUMN_GAME_INSTOCK);

        // Read the game attributes from the Cursor for the current game
        // This is declared "final" for the OnClickListener's use
        final int gameID = cursor.getInt(idColumnIndex);
        final String gameName = cursor.getString(nameColumnIndex);
        final int gamePrice = cursor.getInt(priceColumnIndex);
        final int gameGenre = cursor.getInt(genreColumnIndex);
        final int gameInStock = cursor.getInt(quantityColumnIndex);

        //Find string that matches genre selection and save it into a string to display it later
        String genre = "";
        switch(gameGenre){
            case GameEntry.GENRE_ACTION:
                genre = context.getString(R.string.genre_action);
                break;
            case GameEntry.GENRE_ACTION_ADVENTURE:
                genre = context.getString(R.string.genre_action_adventure);
                break;
            case GameEntry.GENRE_ADVENTURE:
                genre = context.getString(R.string.genre_adventure);
                break;
            case GameEntry.GENRE_ROLE_PLAYING:
                genre = context.getString(R.string.genre_role_playing);
                break;
            case GameEntry.GENRE_SIMULATION:
                genre = context.getString(R.string.genre_simulation);
                break;
            case GameEntry.GENRE_STRATEGY:
                genre = context.getString(R.string.genre_strategy);
                break;
            case GameEntry.GENRE_SPORTS:
                genre = context.getString(R.string.genre_sports);
                break;
            case GameEntry.GENRE_OTHER:
                genre = context.getString(R.string.genre_other);
                break;
            default:
                //Set genre to unknown by default if none matches
                genre = context.getString(R.string.genre_unknown);
        }

        //String to display price
        //Add dollar sign in front
        String displayPrice = context.getString(R.string.dollar_sign) + gamePrice;

        //String to display in stock quantity
        //Add "In Stock: " in front
        String inStock = context.getString(R.string.message_in_stock)+ gameInStock;

        // Update the TextViews with the attributes for the current game
        viewHolder.nameTextView.setText(gameName);
        viewHolder.priceTextView.setText(displayPrice);
        viewHolder.genreTextView.setText(genre);
        viewHolder.inStockTextView.setText(inStock);

        //Sells a game unit with update method
        viewHolder.sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri currentGameUri = ContentUris.withAppendedId(GameEntry.CONTENT_URI, gameID);

                ContentValues values = new ContentValues();
                values.put(GameContract.GameEntry.COLUMN_GAME_NAME, gameName);
                values.put(GameEntry.COLUMN_GAME_PRICE, gamePrice);
                values.put(GameContract.GameEntry.COLUMN_GAME_GENRE, gameGenre);

                if(gameInStock>1){
                    //decrease the quantity by 1
                    int newInStockQuantity = gameInStock-1;
                    values.put(GameEntry.COLUMN_GAME_INSTOCK, newInStockQuantity);
                    Toast.makeText(view.getContext(), "You sold 1 copy of "+gameName+".", Toast.LENGTH_SHORT).show();
                    view.getContext().getContentResolver().update(currentGameUri, values, null, null);
                }
                else{
                    //Only have one copy left. Delete game from inventory after selling last copy.
                    Toast.makeText(view.getContext(), "You sold your last copy.", Toast.LENGTH_SHORT).show();
                    view.getContext().getContentResolver().delete(currentGameUri, null, null);
                }
            }
        });
    }

    class ViewHolder{
        TextView nameTextView ;
        TextView priceTextView;
        TextView genreTextView;
        TextView inStockTextView;
        Button sellButton;

        ViewHolder(View view){
            nameTextView = (TextView) view.findViewById(R.id.name);
            priceTextView = (TextView) view.findViewById(R.id.price);
            genreTextView = (TextView) view.findViewById(R.id.genre);
            inStockTextView = (TextView) view.findViewById(inStock);
            sellButton = (Button) view.findViewById(R.id.list_item_sell_button);
        }

    }
}