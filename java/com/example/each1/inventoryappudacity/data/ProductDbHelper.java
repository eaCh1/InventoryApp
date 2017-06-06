package com.example.each1.inventoryappudacity.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.each1.inventoryappudacity.data.ProductContract.ProductEntry;
/**
 * Created by each1 on 6/4/17.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    public static final String LOG_TAG = ProductDbHelper.class.getSimpleName();

    // Name of the database file
    private static final String DATABASE_NAME = "inventory.db";

    // Database version... If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //This is called when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create a String that contains the SQL statement to create the products table
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE" + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + "INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NO NULL DEFAULT 0, "
                + ProductEntry.COLUMN_PRODUCT_SUPPLIER + " TEXT NOT NULL, "
                + ProductEntry.COLUMN_PRODUCT_PICTURE + " TEXT);";

        //Execute the SQL statement
        db.execSQL(SQL_CREATE_PRODUCTS_TABLE);

    }

    //Called when the database needs to be upgraded, nothing to do here for this project
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    // database version is still at 1, so there's nothing to do here
    }
}