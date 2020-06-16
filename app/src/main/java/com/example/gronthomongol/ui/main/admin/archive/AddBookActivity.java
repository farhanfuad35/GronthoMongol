package com.example.gronthomongol.ui.main.admin.archive;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import com.example.gronthomongol.backend.models.Book;
import com.example.gronthomongol.R;

public class AddBookActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Book newBook = new Book();
    private EditText etBookName;
    private EditText etWriterName;
    private EditText etPrice;
    private NumberPicker npQuantity;
    private Spinner spLanguage;
    private ArrayAdapter<CharSequence> adapter;

    private String name;
    private String writer;
    private int price;
    private int quantity = 1;
    private String language;

    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_book);

        setTitle("Add New Book");
        androidx.appcompat.widget.Toolbar toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar_addBook);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        initializeGUIElements();

        npQuantity.setMinValue(1);
        npQuantity.setMaxValue(300);
        npQuantity.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                quantity = picker.getValue();
            }
        });
        spLanguage.setOnItemSelectedListener(AddBookActivity.this);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gotValues()){
                    newBook.setName(name);
                    newBook.setLanguage(language);
                    newBook.setQuantity(quantity);
                    newBook.setPrice(price);
                    newBook.setWriter(writer);

                    final Dialog dialog = new Dialog(AddBookActivity.this);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setCancelable(false);
                    dialog.setContentView(R.layout.dialog_adding_book);
                    dialog.show();

                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            newBook.saveBook(AddBookActivity.this, dialog, true);
                        }
                    });

                    thread.start();
                }

            }
        });
    }

    private void initializeGUIElements() {
        etBookName = findViewById(R.id.etAddBook_BookName);
        etWriterName = findViewById(R.id.etAddBook_WriterName);
        etPrice = findViewById(R.id.etAddBook_Price);
        btnSubmit = findViewById(R.id.btnAddBook_Submit);
        spLanguage = findViewById(R.id.spnAddBook_language);
        npQuantity = findViewById(R.id.npAddBook_NumberOfBooks);
    }

    private boolean gotValues(){
        name = etBookName.getText().toString().trim();
        if(name.isEmpty()) {
            etBookName.setError("Please Enter the Book Name");
            return false;
        }
        writer = etWriterName.getText().toString().trim();
        if(writer.isEmpty()){
            etWriterName.setError("Please Enter Writer's Name");
            return false;
        }
        String priceStr = etPrice.getText().toString();
        if(priceStr.isEmpty()){
            etPrice.setError("Please Enter the Price of the Book");
            return false;
        }
        else{
            try{
            price = Integer.parseInt(priceStr);
            }catch (NumberFormatException nfe){
                etPrice.setError("Please enter a valid price!");
                return false;
            }
        }
        return true;
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