package com.example.android.bookstore;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.NumberFormat;
import java.math.BigDecimal;
import java.util.Locale;
import java.util.regex.Pattern;

import com.example.android.bookstore.data.BookContract.BookEntry;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Intent.ACTION_NEW_OUTGOING_CALL;

public class EditActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_BOOK_LOADER = 0;

    private Uri mCurrentBookUri;
    private static final int PERMISSION_REQUEST_PHONE = 0;
    @BindView(R.id.edit_name)
    EditText mNameEditText;
    @BindView(R.id.edit_price)
    EditText mPriceEditText;
    @BindView(R.id.edit_quantity)
    EditText mQuantityEditText;
    @BindView(R.id.edit_supplier_name)
    EditText mSupplierNameEditText;
    @BindView(R.id.edit_supplier_phone)
    EditText mSupplierPhoneEditText;
    @BindView(R.id.incrementQuantity)
    Button mIncrementButton;
    @BindView(R.id.decrementQuantity)
    Button mDecrementButton;

    public String quantityString;
    public int quantity;
    private boolean mBookHasChanged = false;
    private boolean emptyText = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();
        mNameEditText = findViewById(R.id.edit_name);
        mPriceEditText = findViewById(R.id.edit_price);
        mQuantityEditText = findViewById(R.id.edit_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_supplier_name);
        mSupplierPhoneEditText = findViewById(R.id.edit_supplier_phone);
        mIncrementButton = findViewById(R.id.incrementQuantity);
        mDecrementButton = findViewById(R.id.decrementQuantity);

        if (mCurrentBookUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_book));
            mSupplierPhoneEditText.setEnabled(true);
            mPriceEditText.setEnabled(true);
            mQuantityEditText.setEnabled(true);
            mSupplierNameEditText.setEnabled(true);
            mNameEditText.setEnabled(true);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_book));
            mSupplierPhoneEditText.setEnabled(false);
            mPriceEditText.setEnabled(false);
            mQuantityEditText.setEnabled(false);
            mSupplierNameEditText.setEnabled(false);
            mNameEditText.setEnabled(false);
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneEditText.setOnTouchListener(mTouchListener);
        mIncrementButton.setOnTouchListener(mTouchListener);
        mDecrementButton.setOnTouchListener(mTouchListener);

        mPriceEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                if (string.isEmpty()) return;
                mPriceEditText.removeTextChangedListener(this);
                BigDecimal parsed = new BigDecimal(string).setScale(2, BigDecimal.ROUND_FLOOR).divide(new BigDecimal(100), BigDecimal.ROUND_FLOOR);
                String formatted = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(parsed);
                mPriceEditText.setText(formatted);
                mPriceEditText.setSelection(formatted.length());
                mPriceEditText.addTextChangedListener(this);
            }
        });

        mSupplierPhoneEditText.addTextChangedListener(new TextWatcher() {
            private boolean backspacingFlag = false;
            private boolean editedFlag = false;
            private int cursorComplement;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cursorComplement = s.length() - mSupplierPhoneEditText.getSelectionStart();
                if (count > after) {
                    backspacingFlag = count > after;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String string = s.toString();
                String phone = string.replaceAll("[^\\d]", "");

                if (!editedFlag) {

                    if (phone.length() >= 6 && !backspacingFlag) {
                        editedFlag = true;
                        String ans = "(" + phone.substring(0, 3) + ") " + phone.substring(3, 6) + "-" + phone.substring(6);
                        mSupplierPhoneEditText.setText(ans);
                        mSupplierPhoneEditText.setSelection(mSupplierPhoneEditText.getText().length() - cursorComplement);

                    } else if (phone.length() >= 3 && !backspacingFlag) {
                        editedFlag = true;
                        String ans = "(" + phone.substring(0, 3) + ") " + phone.substring(3);
                        mSupplierPhoneEditText.setText(ans);
                        mSupplierPhoneEditText.setSelection(mSupplierPhoneEditText.getText().length() - cursorComplement);
                    }
                } else {
                    editedFlag = false;
                }
            }
        });

        ButterKnife.bind(this);
    }

    @OnClick(R.id.callSupplier)
    void onCallClicked() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    PERMISSION_REQUEST_PHONE);

        } else {
            startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mSupplierPhoneEditText.getText().toString())));
        }

    }


    @OnClick(R.id.incrementQuantity)
    void onIncrementClicked() {
        if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
            quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
            quantity += 1;
            mQuantityEditText.setText("" + quantity);
        }
    }

    @OnClick(R.id.decrementQuantity)
    void onDecrementClicked() {
        if (!TextUtils.isEmpty(mQuantityEditText.getText().toString())) {
            quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
            quantity -= 1;
            if (quantity >= 0) {
                mQuantityEditText.setText("" + quantity);
            } else {
                quantity = 0;
            }
        }
    }


    private void saveBook() {

        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierPhoneEditText.getText().toString().trim();

        quantityString = mQuantityEditText.getText().toString().trim();


        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(supplierNameString) ||
                priceString.equals("") || quantityString.equals("") || TextUtils.isEmpty(supplierPhoneString)) {
            Toast.makeText(EditActivity.this, R.string.error_empty, Toast.LENGTH_LONG).show();
            return;
        }


        if (!TextUtils.isEmpty(quantityString)) {
            quantity = Integer.parseInt(quantityString);
        }

        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(BookEntry.COLUMN_PRODUCT_PRICE, priceString);
        values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, supplierPhoneString);

        if (mCurrentBookUri == null) {

            Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {

            int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        if (!validatePhone(supplierPhoneString)) {
            Toast.makeText(EditActivity.this, R.string.error_phone, Toast.LENGTH_LONG).show();
        }else {
            emptyTextCheck();
        }

    }
public void emptyTextCheck(){
    if (!emptyText) {
        finish();
    } else {
        emptyText = false;
        Toast.makeText(this, getText(R.string.editor_black_entry), Toast.LENGTH_LONG).show();
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        NavUtils.navigateUpFromSameTask(EditActivity.this);
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }
}
    public boolean validatePhone(String phoneNumber) {
        if(!Pattern.matches("[a-zA-Z]+", phoneNumber))
        {
            if(phoneNumber.length() < 14 || phoneNumber.length() > 14)
            {
                return false;
            }
            else
            {
                return android.util.Patterns.PHONE.matcher(phoneNumber).matches();
            }
        }
        else
        {
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveBook();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditActivity.this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            case R.id.edit:
                onEditClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBackPressed() {
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE,
                BookEntry.COLUMN_PRODUCT_QUANTITY,
                BookEntry.COLUMN_PRODUCT_PRICE};

        return new CursorLoader(this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME);
            int supplierPhoneColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE);

            String name = cursor.getString(nameColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhone = cursor.getString(supplierPhoneColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);

            mNameEditText.setText(name);
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneEditText.setText(supplierPhone);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(Integer.toString(quantity));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        onPause();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void onEditClick() {


        if (mSupplierPhoneEditText.isEnabled()) {
            mSupplierPhoneEditText.setEnabled(false);
            mPriceEditText.setEnabled(false);
            mQuantityEditText.setEnabled(false);
            mSupplierNameEditText.setEnabled(false);
            mNameEditText.setEnabled(false);
        } else {
            mSupplierPhoneEditText.setEnabled(true);
            mPriceEditText.setEnabled(true);
            mQuantityEditText.setEnabled(true);
            mSupplierNameEditText.setEnabled(true);
            mNameEditText.setEnabled(true);
        }

    }

    private void deleteBook() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        finish();
    }

}
