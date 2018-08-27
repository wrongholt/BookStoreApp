package com.example.android.bookstore;

import android.Manifest;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstore.data.BookContract.BookEntry;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import butterknife.OnClick;

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

        final int newIntQuantity = Integer.parseInt(quantity);


        nameTextView.setOnClickListener(new View.OnClickListener() {
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
                if (newIntQuantity >= 0) {
                    ContentValues values = new ContentValues();

                    newIntQuantity -= 1;

                    values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, newIntQuantity);

                    newIntQuantity = context.getContentResolver().update(currentUri, values, null, null);

                }else{
                    newIntQuantity = 0;
                }
            }
        });
    }
}


