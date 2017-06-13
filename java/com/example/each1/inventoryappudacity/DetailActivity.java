//camera implementation utilizes code from https://github.com/crlsndrsjmnz/MyFileProviderExample
//bitmap implementation utilizes code from https://stackoverflow.com/questions/3879992/how-to-get-bitmap-from-an-uri

package com.example.each1.inventoryappudacity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.FileProvider;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.each1.inventoryappudacity.data.ProductContract.ProductEntry;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.getExternalStorageDirectory;


/**
 * Created by each1 on 6/4/17.
 */

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //Still NEED TO FIGURE OUT HOW TO INCLUDE CAMERA STUFF

    public final String APP_TAG = "InventoryApp";

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    //Identifier for product data loader
    private static final int EXISTING_PRODUCT_LOADER = 0;

    static final int REQUEST_TAKE_PHOTO = 1;

    private static final int PERMISSIONS_REQUEST = 2;

    private static final String FILE_PROVIDER_AUTHORITY = "com.example.android.fileprovider";

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
    private ImageView mProductImage;

    // member variable for photo
    String mCurrentPhotoPath;

    //Boolean flag that keeps track of whether the product has been edited (true) or not (false)
    private boolean mProductHasChanged = false;

    private Bitmap mBitmap;
    public String mImage;
    private Uri mUri;
    Button mCameraButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        requestPermissions();

        //use getIntent() and getData() to get the associated URI
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //If the intent doesn't contain a product content uri, then creating a new pet
        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.detail_title_add_new_product));

        } else {
            //existing product, so change app to "Edit Product"
            setTitle(getString(R.string.detail_title_edit_a_product));

            //Initialize a loader to read the product data form the database
            //and display the current values in the editor
            getSupportLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //Find views that we will need to read user input
        mNameEditText = (EditText) findViewById(R.id.detail_name);
        mPriceEditText = (EditText) findViewById(R.id.detail_price);
        mQuantityEditText = (EditText) findViewById(R.id.detail_quantity);
        mSupplierEditText = (EditText) findViewById(R.id.detail_supplier);
        mProductImage = (ImageView) findViewById(R.id.detail_image);
        mCameraButton = (Button) findViewById(R.id.detail_take_picture);

        Button orderButton = (Button) findViewById(R.id.detail_order_from_supplier);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentProductUri != null) {
                    sendEmail();
                }
            }
        });

        Button saleButton = (Button) findViewById(R.id.detail_track_sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentProductUri != null) {
                    trackSale();
                }
            }
        });

        Button shipmentButton = (Button) findViewById(R.id.detail_receive_shipment);
        shipmentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentProductUri != null) {
                    receiveShipment();
                }
            }
        });

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });
    }

    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            File file = createImageFile();
            Log.d(LOG_TAG, "File: " + file.getAbsolutePath());
            mUri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            mBitmap = getBitmapFromUri(mUri);
            mProductImage.setImageBitmap(mBitmap);
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void trackSale() {
        //decrease quantity
        int quantity = 0;
        quantity = Integer.valueOf(mQuantityEditText.getText().toString());
        quantity = quantity - 1;
        mQuantityEditText.setText("" + quantity);
    }

    private void receiveShipment() {
        //increase quantity
        int quantity = 0;
        quantity = Integer.valueOf(mQuantityEditText.getText().toString());
        quantity = quantity + 1;
        mQuantityEditText.setText("" + quantity);
    }

    private void sendEmail() {
        //Get name of product for email subject
        String productName = mNameEditText.getText().toString();
        //Get email from supplier edit text
        String sendTo = mSupplierEditText.getText().toString();
        //create text with addressee and name of product for the subject of the email
        String uriText = "mailto:" + sendTo + "?subject=" + Uri.encode("We need more " + productName);
        //create intent
        Intent email = new Intent(Intent.ACTION_SENDTO);
        //pass through user information through intent
        email.setData(Uri.parse(uriText));

        if (email.resolveActivity(getPackageManager()) != null) {
            startActivity(Intent.createChooser(email, "Choose an email client:"));
        }
    }

    private void saveProduct() {
        //Read from input fields and use trim() to eliminate whitespace
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierString = mSupplierEditText.getText().toString().trim();
        String photoString = "";


        // if there is a photo in mUri, set the String photoString as the mUri
        // set a Tag, temp memory, to the ImageView
        if (mUri != null) {
            photoString = mUri.getPath();
            mProductImage.setTag(photoString);
        }

        //Check if this is supposed to be a new product
        //and check if the fields in the editor are blank
        if (mCurrentProductUri == null && photoString.equals("") ||
                TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) ||
                TextUtils.isEmpty(quantityString) || TextUtils.isEmpty(supplierString)) {
            //since no fields were modified, return early
            //No ContentValues, nor ContentProvider operations;
            return;
        }

        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(ProductEntry.COLUMN_PRODUCT_SUPPLIER, supplierString);
        values.put(ProductEntry.COLUMN_PRODUCT_PICTURE, photoString);

        //Determine if this is a new or existing product checking if mCurrentProductUri is null or not
        if (mCurrentProductUri == null) {
            //new product, insert a new product into the provider
            //Return the content URI for the new product
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            //shows a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                //If URI is null, there was an error with insertion
                Toast.makeText(this, getString(R.string.detail_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.detail_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            //EXISTING product, update product with content URI: mCurrentProductUri, pass in the new ContentValues.
            // Pass in null for the selection and selection args

            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            //Show a toast message depending on whether or not the update was successful
            if (rowsAffected == 0) {
                //If no rows were affected, then there was an error with the update
                Toast.makeText(this, getString(R.string.detail_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // update was successful, display a toast.
                Toast.makeText(this, getString(R.string.detail_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    //This method is called after invalidateOptionsMenu(), so that the
    //menu can be updated (some menu items can me hidden or made visible).
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
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
                // Save product to database
                saveProduct();
                //Exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                //Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                //if product hasn't changed, continue with navigating up to the parent activity
                //which is the {@link CatalogActivity}
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }
                //if there are unsaved changes, warn the user.
                //Create a click listener to handle the user confirming that
                //changes should be discarded
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //user clicked "Discard" button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };
                //Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_SUPPLIER,
                ProductEntry.COLUMN_PRODUCT_PICTURE
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
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_SUPPLIER);
            int pictureColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PICTURE);

            //Extract the values
            String name = cursor.getString(nameColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            mImage = cursor.getString(pictureColumnIndex);
            Uri imageUri = Uri.parse("content://" + FILE_PROVIDER_AUTHORITY + mImage);

            //Update the views on the screen
            mNameEditText.setText(name);
            mQuantityEditText.setText(quantity);
            mPriceEditText.setText(price);
            mSupplierEditText.setText(supplier);
            mProductImage.setImageBitmap(getBitmapFromUri(imageUri));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //reset loader if it becomes invalid
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierEditText.setText("");

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
                deleteProduct();
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

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            //Call the Content Resolver to delete product at the given content URI
            //Pass in null for the selection and selection args
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            //show a toast message depending on whether or not the delete was succesful
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.detail_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                //Otherwise, the delete was successful and we can display a toast
                Toast.makeText(this, getString(R.string.detail_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        //Close the activity
        finish();
    }

    public void requestPermissions() {

        //check to see if the permissions are established already
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {


            //created which permissions should be asked for
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {

                return;
            } else {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        PERMISSIONS_REQUEST);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mCameraButton.setEnabled(true);
                } else {
                    mCameraButton.setEnabled(false);
                }
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        }
    }
}