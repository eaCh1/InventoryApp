package com.example.each1.inventoryappudacity;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.each1.inventoryappudacity.data.ProductContract;

import org.w3c.dom.Text;


/**
 * Created by each1 on 6/4/17.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Still NEED TO FIGURE OUT HOW TO INCLUDE CAMERA STUFF

    //Identifier for product data loader
    private static final int EXISTING_PRODUCT_LOADER = 0;

    //Content Uri for the existing product (null if it's a new product)
    private Uri mCurrentProductUri;

    // Edit text field to enter product's name
    private EditText mNameEditText;

    // Edit text field to enter the product's price
    private EditText mPriceEditText;

    //Edit text field to enter the product's quantity
    private EditText mQuantityEditText;

    //Edit text field to enter the supplier's email
    private EditText mSupplierEditText;

    // Image view to hold the product's image
    private ImageView mProductImageView;

    //Boolean flag that keeps track of whether the pet has been edited (true) or not (false)
    private boolean mProductHasChanged = false;

    /**
     * OnTouchListener that listens for any touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //use getIntent() and getData() to get the associated URI

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //If the intent DOES NOT contain a product content uri, then we know we are creating a new pet
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.detail_title_add_new_product));

        } else {
            //Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.detail_title_edit_a_product));

            //Initialize a loader to read the pet data form the database
            //and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //Find all relevant views that we will need to read user input
        mNameEditText = (EditText) findViewById(R.id.detail_name);
        mPriceEditText = (EditText) findViewById(R.id.detail_price);
        mQuantityEditText = (EditText) findViewById(R.id.detail_quantity);
        mProductImageView = (ImageView) findViewById(R.id.detail_image);

        //Setup OnTouchListeners on all the input fiels so we can determine if the user has touched
        // or modified them, this will let us know if there are unsaved changes
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mProductImageView.setOnTouchListener(mTouchListener);
    }

    private void saveProduct() {
        //Read from input fields and use trim() to elminiate whitespace
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();

        //String imageString = mProductImageView
        //need to figure out how to SAVE THE PHOTO, maybe don't need this to be under "saved"

        //Check if this is supposed to be a new product
        //and check if all the fiels in the editor are blank
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(quantityString)) {
            //since no fiels were modified, we can return early without creating a new pet
            //No need to create a ContentValues and no need to do any ContentProvider operations;
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);

        //Determine if this is a new or existing product checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            //This is a NEW product, so insert a new product into the provider
            //Return the content URI for the new product
            Uri newUri = getContentResolver().insert(ProductContract.ProductEntry.CONTENT_URI, values);

            //shows a toast message depending on whether ot not the insertion was successful
            if (newUri == null) {
                //If the content URI is null, then there was an error with insertion
                Toast.makeText(this, getString(R.string.detail_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.detail_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            //Otherwise this is an EXISTING product, so update product with content URI: mCurrentProductUri
            //and pass in the new ContentValues.
            // Pass in null for the selection and selection args

            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            //Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                //If no rows were affected, then there was an error with the update
                Toast.makeText(this, getString(R.string.detail_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //Otherwise the update was succesful and we can display a toast.
                Toast.makeText(this, getString(R.string.detail_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ProductContract.ProductEntry._ID,
                ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductContract.ProductEntry.COLUMN_PRODUCT_PICTURE
        };

        return new CursorLoader(this, mCurrentProductUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Bail early if the cursor is null
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }


        if (cursor.moveToFirst()) {
            //Find the names of the columns
            int nameColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int pictureColumnIndex = cursor.getColumnIndex(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE);


            //Extract the values
            String name = cursor.getString(nameColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String picture = cursor.getString(pictureColumnIndex);

            //Update the views on the screen
            mPriceEditText.setText(name);
            mQuantityEditText.setText(quantity);
            mPriceEditText.setText(price);
            mSupplierEditText.setText(supplier);

        }


    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //reset loader if it becomes invalid
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");

    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        //Create an AlertDialog.Builder and set the message, and click listeners
        //for the positive and negative buttons on the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //User clicked the "Keep editing" button, so dimiss the dialog
                // and continue editing the pet
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deletePet();
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

    private void deletePet() {
        if (mCurrentProductUri != null) {
            //Call the Content Resolver to delete the pet at the given content URI
            //Pass in null for the selection and selection args because the mCurrentPetUri
            //content URI already identifies the pet that we want
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            //show a toast message depending on whether or not the delete was succesful
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.detail_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //Otherwise, the delete was successful and we can display a toast
                Toast.makeText(this, getString(R.string.detail_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        //Close the activity
        finish();
    }
}
