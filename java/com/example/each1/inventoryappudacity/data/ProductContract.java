package com.example.each1.inventoryappudacity.data;

import android.graphics.Path;
import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

/**
 * Created by each1 on 6/4/17.
 */

public final class ProductContract {

    //Empty Constructor to prevent
    private ProductContract(){}

    //Content authority is the name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.each1.inventoryappudacity";

    // Use CONTENT_Authority to create the base of all the URIs which apps
    // use to communicate with the content provider
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //possible path (append to base content URI for possible URIs)
    public static final String PATH_PRODUCTS = "products";

    //Inner class that defines constant values for the products database table
    //Each entry in the table represents a single product
    public static final class ProductEntry implements  BaseColumns {

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        public static final String TABLE_NAME = "products";


        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_PRODUCT_NAME = "name";
        public static final String COLUMN_PRODUCT_PRICE = "price";
        public static final String COLUMN_PRODUCT_QUANTITY = "quantity";
        public static final String COLUMN_PRODUCT_SUPPLIER = "supplier";
        public static final String COLUMN_PRODUCT_PICTURE = "picture";

    }


}