package com.example.android.bookstore;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.android.bookstore.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        String currentId = cursor.getString(cursor.getColumnIndexOrThrow(BookEntry._ID));
        final Uri currentUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, Long.parseLong(currentId));

        TextView nameTextView = view.findViewById(R.id.name);
        TextView summaryTextView = view.findViewById(R.id.summary);
        TextView priceTextView = view.findViewById(R.id.price);
        Button selectButton = view.findViewById(R.id.checkout);

        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_PRICE);

        String productName = cursor.getString(nameColumnIndex);
       final String quantity = cursor.getString(quantityColumnIndex);
        String price = cursor.getString(priceColumnIndex);

        String compoundQuantityString = context.getString(R.string.quantity_main_activity, quantity);
        String compoundPriceString = context.getString(R.string.price_main_activity, price);

        nameTextView.setText(productName);
        summaryTextView.setText(compoundQuantityString);
        priceTextView.setText(compoundPriceString);
        RelativeLayout boxView = view.findViewById(R.id.box);
        boxView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, EditActivity.class);

                intent.setData(currentUri);

                context.startActivity(intent);
            }
        });

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newIntQuantity = Integer.parseInt(quantity);
                if (newIntQuantity > 0) {
                    ContentValues values = new ContentValues();

                    newIntQuantity -= 1;

                    values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, newIntQuantity);

                    context.getContentResolver().update(currentUri, values, null, null);

                }else {
                        newIntQuantity = 0;
                    }

            }
        });
    }
}


