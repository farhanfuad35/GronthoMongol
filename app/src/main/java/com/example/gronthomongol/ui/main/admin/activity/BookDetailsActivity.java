package com.example.gronthomongol.ui.main.admin.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.CONSTANTS;
import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.ui.main.admin.AdminMainActivity;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPicker;
import com.michaelmuenzer.android.scrollablennumberpicker.ScrollableNumberPickerListener;

public class BookDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {

    private Book selectedBook;
    private Book unsavedBook;

    private EditText nameEditText;
    private EditText authorEditText;
    private EditText priceEditText;
    private Spinner languageSpinner;
    private ScrollableNumberPicker countNumberPicker;

    private TextView errorTextView;

    private Button backButton;
    private Button deleteButton;
    private Button updateButton;

    private ArrayAdapter<CharSequence> adapter;

    private String name;
    private String writer;
    private String priceStr;
    private int price;
    private int quantity;
    private String language;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        selectedBook = (Book) getIntent().getSerializableExtra("selectedBook");
        Log.i("book_deletion", "SelectedBook oId: " + selectedBook.getObjectId());
        unsavedBook = selectedBook;

        findXmlElements();
        setUpSpinner();
        setUpNumberPicker();

        setValues();
        setUpListeners();
    }

    private void findXmlElements(){
        nameEditText = findViewById(R.id.nameEditTextBookDetails);
        authorEditText = findViewById(R.id.authorEditTextBookDetails);
        priceEditText = findViewById(R.id.priceEditTextBookDetails);
        languageSpinner = findViewById(R.id.languageSpinnerBookDetails);
        countNumberPicker = findViewById(R.id.countNumberPickerBookDetails);

        errorTextView = findViewById(R.id.errorTextViewBookDetails);

        backButton = findViewById(R.id.backButtonBookDetails);
        deleteButton = findViewById(R.id.deleteButtonBookDetails);
        updateButton = findViewById(R.id.updateButtonBookDetails);
    }

    private void setUpSpinner(){
        // Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, R.layout.language_spinner_color_layout);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.language_spinner_dropdown_layout);
        // Apply the adapter to the spinner
        languageSpinner.setAdapter(adapter);
        int position = adapter.getPosition(selectedBook.getLanguage());
        languageSpinner.setSelection(position);
    }

    private void getValues(){
        name = nameEditText.getText().toString().trim();
        writer = authorEditText.getText().toString().trim();
        priceStr = priceEditText.getText().toString().trim();
    }

    private void setValues(){
        nameEditText.setText(selectedBook.getName());
        authorEditText.setText(selectedBook.getWriter());
        priceEditText.setText(Integer.toString(selectedBook.getPrice()));
    }

    private void setUpNumberPicker(){
        countNumberPicker.setMinValue(0);
        countNumberPicker.setMaxValue(300);
        countNumberPicker.setValue(selectedBook.getQuantity());
//        countNumberPicker.setWrapSelectorWheel(true);
//        countNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//            @Override
//            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                quantity = picker.getValue();
//            }
//        });
        countNumberPicker.setListener(new ScrollableNumberPickerListener() {
            @Override
            public void onNumberPicked(int value) {
                quantity = value;
            }
        });
    }

    private void setUpListeners(){
        languageSpinner.setOnItemSelectedListener(BookDetailsActivity.this);
        backButton.setOnClickListener(this);
        deleteButton.setOnClickListener(this);
        updateButton.setOnClickListener(this);
    }

    private boolean isEmpty(EditText editText){
        if(editText.getText().toString().isEmpty()){
            editText.setError("This field cannot be empty!");
            return true;
        }
        return false;
    }

    private boolean isPriceOkay(String priceStr){
        if(priceStr.isEmpty()){
            priceEditText.setError("Please enter the price of the book");
            return false;
        }
        try {
            price = Integer.parseInt(priceStr);
            return true;
        } catch (NumberFormatException nfe) {
            priceEditText.setError("Please enter a valid price");
            return false;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        language = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View view) {
        if(view == updateButton){
            handleUpdate();
        } else if(view == deleteButton){
            handleDelete();
        } else if(view == backButton){
            handleBack();
        }
    }

    private void handleUpdate(){
        getValues();
        if(!isEmpty(nameEditText) && isPriceOkay(priceStr) && !isEmpty(authorEditText)){
            // TODO UPDATE THE BOOK
            unsavedBook.setName(name);
            unsavedBook.setWriter(writer);
            unsavedBook.setPrice(price);
            unsavedBook.setQuantity(quantity);
            unsavedBook.setLanguage(language);

            final Dialog dialog = new Dialog(BookDetailsActivity.this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.dialog_updating_book);
            dialog.show();

            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    unsavedBook.saveBook(BookDetailsActivity.this, dialog, false);
                }
            });

            thread.start();
        }
    }

    private void handleDelete(){
        String title = "Are you sure?";
        String message = "This operation is going to delete the book from the database";
        String positiveButton = "Yes";
        String negativeButton = "Cancel";
        Context context = BookDetailsActivity.this;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(((Activity) context));
        alertDialogBuilder.setTitle(title);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton(positiveButton,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                        //Toast.makeText(Splash_Screen.this,"You clicked yes button",Toast.LENGTH_LONG).show();
                        final Dialog dialog = new Dialog(BookDetailsActivity.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setCancelable(false);
                        dialog.setContentView(R.layout.dialog_deleting_book);
                        dialog.show();

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                selectedBook.deleteBook(BookDetailsActivity.this, dialog);
                            }
                        });

                        thread.start();
                        Log.i("book_deletion", "onClick: Sent the book for deletion");
                    }
                });
        alertDialogBuilder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void handleBack(){
        Intent intent = new Intent(BookDetailsActivity.this, AdminMainActivity.class);
        startActivity(intent);
        finish();
    }
}