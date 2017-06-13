package com.example.each1.inventoryappudacity;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.each1.inventoryappudacity.data.ProductContract.ProductEntry;

public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PRODUCT_LOADER = 0;

    ProductCursorAdapter mCursorAdapter;

    private TextView mQuantityTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog2);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });

        // Find the ListView which will be populated with the pet data
        ListView productListView = (ListView) findViewById(R.id.list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        //setup an adapter to create a list item for each row of pet data in the Cursor
        //there is no pet data yet (until the loader finishes) so pass in null for the Cursor
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        Button saleButton = (Button) findViewById(R.id.sale_button);
        saleButton.setOnClickListener(new AdapterView.OnClickListener() {
            @Override
            public void onClick(View v) {
                trackSale();
            }
        });

        //Setup item click listener
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //Create the new Intent to go to the EditorActivity
                Intent intent = new Intent(CatalogActivity.this, DetailActivity.class);

                //Form the content URI that represents the specific pet that was clicked on,
                //by appending the ID (passed as input to this method) onto the PetEntry.CONTENT_URI
                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                //Set the uri on the data field of the intent
                intent.setData(currentProductUri);

                startActivity(intent);
            }
        });
        //Kick offf the loader
        getSupportLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }

    private void trackSale() {
        //decrease quantity
        int quantity = 0;
        mQuantityTextView = (TextView) findViewById(R.id.quantity);
        quantity =  Integer.valueOf(mQuantityTextView.getText().toString());
        quantity = quantity - 1;
        mQuantityTextView.setText(quantity);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns from the table we care about
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE};

        //THis loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this, //parent activity context
                ProductEntry.CONTENT_URI, //Provider content URI to query
                projection,           //Columns to include in the resulting Cursor
                null,                 // No selection clause
                null,                 //No selection argument
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
