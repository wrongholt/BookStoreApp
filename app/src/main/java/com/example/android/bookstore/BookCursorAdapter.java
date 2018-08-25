package com.example.android.bookstore;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.bookstore.data.BookContract.BookEntry;
public class BookCursorAdapter extends CursorAdapter{
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView =  view.findViewById(R.id.name);
        TextView summaryTextView =  view.findViewById(R.id.summary);

        int productNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
        int supplierColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);

        String productName = cursor.getString(productNameColumnIndex);
        String supplierName = cursor.getString(supplierColumnIndex);

        nameTextView.setText(productName);
        summaryTextView.setText(supplierName);
    }
}
