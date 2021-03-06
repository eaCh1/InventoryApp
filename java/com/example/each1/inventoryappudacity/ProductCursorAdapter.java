package com.example.each1.inventoryappudacity;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.each1.inventoryappudacity.data.ProductContract.ProductEntry;

import org.w3c.dom.Text;

/**
 * Created by each1 on 6/4/17.
 */

public class ProductCursorAdapter extends CursorAdapter {
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    // makes a new blank list item view with no data
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    //binds the product data (in the current row in the cursor) to the list-item layout

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        //Find the fields to populate
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        Button saleButton = (Button) view.findViewById(R.id.sale_button);

        //Find the columns of the product attributes
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);

        //Extract the information from those views
        String productName = cursor.getString(nameColumnIndex);
        final String productQuantity = cursor.getString(quantityColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);

        //populate fields
        nameTextView.setText(productName);
        quantityTextView.setText(productQuantity);
        priceTextView.setText(productPrice);

        saleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int quantity = 0;
                quantity = Integer.valueOf(quantityTextView.getText().toString());

                if (quantity > 0) {
                    quantity = quantity - 1;
                }
                quantityTextView.setText("" + quantity);
            }
        });
    }

}
