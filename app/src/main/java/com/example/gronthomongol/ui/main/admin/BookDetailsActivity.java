package com.example.gronthomongol.ui.main.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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

import com.example.gronthomongol.R;
import com.example.gronthomongol.backend.models.Book;

public class BookDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private Book selectedBook;
    private Book unsavedBook;
    private EditText etBookName;
    private EditText etWriterName;
    private EditText etPrice;
    private NumberPicker npQuantity;
    private Spinner spLanguage;
    private ArrayAdapter<CharSequence> adapter;

    private String name;
    private String writer;
    private String priceStr;
    private int price;
    private int quantity;
    private String language;

    private Button btnDelete;
    private Button btnUpdate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);

        setTitle("Book Details");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_BookDetails);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        selectedBook = (Book) getIntent().getSerializableExtra("selectedBook");
        Log.i("book_deletion", "SelectedBook oId: " + selectedBook.getObjectId());
        unsavedBook = selectedBook;
        initializeGUIElements();
        initializeSpinner();
        setGUIElements();
        spLanguage.setOnItemSelectedListener(BookDetailsActivity.this);
        npQuantity.setMinValue(0);
        npQuantity.setMaxValue(300);
        npQuantity.setValue(selectedBook.getQuantity());
        npQuantity.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                quantity = picker.getValue();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getValues();
                if(!isEmpty(etBookName) && isPriceOkay(priceStr) && !isEmpty(etWriterName)){
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
        });

    }

    private void initializeGUIElements(){
        etBookName = findViewById(R.id.etBookDetails_BookName);
        etWriterName = findViewById(R.id.etBookDetails_WriterName);
        etPrice = findViewById(R.id.etBookDetails_Price);
        spLanguage = findViewById(R.id.spnRequest_language);
        npQuantity = findViewById(R.id.npBookDetails_NumberOfBooks);
        btnUpdate = findViewById(R.id.btnBookDetails_update);
        btnDelete = findViewById(R.id.btnBookDetails_delete);
    }

    private void getValues(){
        name = etBookName.getText().toString().trim();
        writer = etWriterName.getText().toString().trim();
        priceStr = etPrice.getText().toString().trim();
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
            etPrice.setError("Please enter the price of the book");
            return false;
        }
        try {
            price = Integer.parseInt(priceStr);
            return true;
        } catch (NumberFormatException nfe) {
            etPrice.setError("Please enter a valid price");
            return false;
        }
    }

    private void initializeSpinner(){
// Create an ArrayAdapter using the string array and a default spinner layout
        adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);
// Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// Apply the adapter to the spinner
        spLanguage.setAdapter(adapter);
        int position = adapter.getPosition(selectedBook.getLanguage());
        spLanguage.setSelection(position);
    }

    private void setGUIElements(){
        etBookName.setText(selectedBook.getName());
        etPrice.setText(Integer.toString(selectedBook.getPrice()));
        etWriterName.setText(selectedBook.getWriter());
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
}