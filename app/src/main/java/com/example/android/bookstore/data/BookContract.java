package com.example.android.bookstore.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BookContract {
    private BookContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.bookstore";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_BOOKS = "books";

    public static final class BookEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_BOOKS);

        public static final String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;

        public final static String TABLE_NAME = "books";
        public final static String COLUMN_PRODUCT_NAME = "name";
        public final static String COLUMN_PRODUCT_PRICE = "price";
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        public final static String COLUMN_SUPPLIER_NAME = "supplier";
        public final static String COLUMN_SUPPLIER_PHONE = "phone";

        public static boolean validatePhone(String phoneNumber) {
            boolean check;
            Pattern pattern;
            Matcher matcher;
            String phonePattern = "^(\\+\\d{1,2}\\s)?\\(?\\d{3}\\)?[\\s.-]\\d{3}[\\s.-]\\d{4}$";
            pattern = Pattern.compile(phonePattern);

            matcher = pattern.matcher(phoneNumber);
            check = matcher.matches();
            return check;
        }

    }
}
